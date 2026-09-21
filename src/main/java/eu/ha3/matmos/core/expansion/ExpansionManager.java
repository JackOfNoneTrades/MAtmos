package eu.ha3.matmos.core.expansion;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import eu.ha3.matmos.ConfigManager;
import eu.ha3.matmos.Matmos;
import eu.ha3.matmos.ResourcePackDealer;
import eu.ha3.matmos.core.expansion.agents.JsonLoadingAgent;
import eu.ha3.matmos.core.expansion.agents.LegacyXMLLoadingAgent;
import eu.ha3.matmos.core.sheet.DataPackage;
import eu.ha3.matmos.core.sheet.SheetDataPackage;
import eu.ha3.matmos.core.sound.LoopingStreamedSoundManager;
import eu.ha3.matmos.data.IDataCollector;
import eu.ha3.matmos.data.modules.BlockCountModule;
import eu.ha3.matmos.dealias.IDDealiaser;
import eu.ha3.matmos.util.MAtUtil;
import eu.ha3.mc.haddon.supporting.SupportsFrameEvents;
import eu.ha3.mc.haddon.supporting.SupportsTickEvents;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class ExpansionManager implements VolumeUpdatable, SupportsTickEvents, SupportsFrameEvents {
    private final LoopingStreamedSoundManager soundManager;
    private final File userconfigFolder;

    private final ResourcePackDealer dealer = new ResourcePackDealer();
    private final List<SoundpackIdentity> soundpackIdentities = new ArrayList<SoundpackIdentity>();
    private final Map<String, Expansion> expansions = new HashMap<>();
    private static IDDealiaser dealiasMap;

    private DataPackage data;

    private float volume = 1;

    List<Integer> dimensionList;
    boolean dimensionListIsWhitelist;
    private Optional<Integer> lastDimension = Optional.empty();

    private IDataCollector collector;

    public ExpansionManager(File userconfigFolder, File aliasFolder, LoopingStreamedSoundManager soundManager) {
        this.userconfigFolder = userconfigFolder;
        this.soundManager = soundManager;

        if (!this.userconfigFolder.exists()) {
            this.userconfigFolder.mkdirs();
        }

        dealiasMap = new IDDealiaser(aliasFolder);

        if (dimensionList == null) {
            buildDimensionList();
        }
    }

    private void buildDimensionList() {
        dimensionList = new ArrayList<Integer>();
        String dimensionListString = ConfigManager.getConfig().getString("dimensions.list");

        if (!dimensionListString.isEmpty()) {
            Arrays.stream(dimensionListString.split(",")).forEach(o -> {
                try {
                    dimensionList.add(Integer.parseInt(o));
                } catch (NumberFormatException e) {
                    Matmos.LOGGER.warn("Ignoring invalid dimension number: " + o);
                }
            });
        }

        dimensionListIsWhitelist = ConfigManager.getConfig().getString("dimensions.listtype").contentEquals("white");
    }

    public void loadExpansions() {
        dispose();

        dealer.findResourcePacks().forEach(this::readSoundPack);
    }

    private void readSoundPack(IResourcePack pack) {
        try (InputStream matPackStream = dealer.openMatPackPointerFile(pack);
                InputStream expansionsStream = dealer.openExpansionsPointerFile(pack)) {
            String matPackJSONString = IOUtils.toString(matPackStream, "UTF-8");
            String expansionsJSONString = IOUtils.toString(expansionsStream, "UTF-8");

            if (readMatPackFile(new JsonParser().parse(matPackJSONString).getAsJsonObject())) {
                readExpansionsFile(new JsonParser().parse(expansionsJSONString).getAsJsonObject(), pack);
            }
        } catch (Exception e) {
            Matmos.LOGGER.warn(pack + " " + "has failed with an error: " + e.getMessage());
        }
    }

    /*** Returns false if reading the pack should be cancelled */
    private boolean readMatPackFile(JsonObject matPackRoot) {
        soundpackIdentities.add(new SoundpackIdentity(matPackRoot));

        return true;
    }

    private void readExpansionsFile(JsonObject expansionsRoot, IResourcePack pack) {
        JsonArray expansions = expansionsRoot.get("expansions").getAsJsonArray();

        for (JsonElement element : expansions) {
            JsonObject o = element.getAsJsonObject();
            String uniqueName = MAtUtil.sanitizeUniqueName(o.get("uniquename").getAsString());
            String friendlyName = o.get("friendlyname").getAsString();
            float volumeModifier = o.has("volumemodifier") ? o.get("volumemodifier").getAsFloat() : 1;
            String pointer = o.get("pointer").getAsString();
            ResourceLocation location = new ResourceLocation("matmos", pointer);
            if (pack.resourceExists(location)) {
                addExpansion(new ExpansionIdentity(uniqueName, friendlyName, pack, location, volumeModifier));
            } else {
                Matmos.LOGGER.warn("An expansion pointer doesn't exist: " + pointer);
            }
        }
    }

    private void addExpansion(ExpansionIdentity identity) {
        Expansion expansion = new Expansion(identity, data, collector, soundManager, this,
                new File(userconfigFolder, identity.getUniqueName() + ".cfg"));
        expansions.put(identity.getUniqueName(), expansion);

        if (identity.getLocation().getResourcePath().endsWith(".xml")) {
            File folder = new File(userconfigFolder, "DO NOT EDIT UNLESS COPIED/");
            if (!folder.exists()) {
                folder.mkdirs();
            }

            String filename = identity.getUniqueName() + ".json";
            if (filename.startsWith("legacy__")) {
                filename = filename.substring("legacy__".length());
            }
            expansion.setLoadingAgent(new LegacyXMLLoadingAgent(new File(folder, filename)));
        } else {
            expansion.setLoadingAgent(new JsonLoadingAgent());
        }

        expansion.updateVolume();
    }

    public Map<String, Expansion> getExpansions() {
        return expansions;
    }

    public List<SoundpackIdentity> getSoundpackIdentities() {
        return soundpackIdentities;
    }

    public static ItemStack dealias(ItemStack is, DataPackage data) {
        if(is == null) return null;
        return new ItemStack(Item.getItemById(dealiasToID(is, data)), is.stackSize, 0);
    }
    
    public static int dealiasToID(ItemStack is, DataPackage data) {
        if(is == null) return -1;
        return dealiasIDIfNotReferenced(Item.getIdFromItem(is.getItem()), data, true);
    }
    
    public static Block dealias(Block b, DataPackage data) {
        if(b == null) return null;
        return Block.getBlockById(dealiasToID(b, data));
    }
    
    public static int dealiasToID(Block b, DataPackage data) {
        if(b == null) return -1;
        return dealiasIDIfNotReferenced(Block.getIdFromBlock(b), data, false);
    }
    
    private static int dealiasIDIfNotReferenced(int id, DataPackage data, boolean isItem) {
        if(data != null && data instanceof SheetDataPackage && ((SheetDataPackage)data).isIDReferenced(id, isItem)) {
            return id;
        } else {
            return dealiasMap.dealiasID(id, isItem);
        }
    }

    private void synchronizeStable(Expansion expansion) {
        if (expansion == null) {
            return;
        }

        if (expansion.isActivated()) {
            if (expansion.getVolume() <= 0) {
                expansion.deactivate();
            }
        } else if (expansion.getVolume() > 0) {
            expansion.activate();
        }
    }

    public void synchronize() {
        expansions.values().forEach(this::synchronizeStable);
    }

    @Override
    public void onFrame(float f) {
        expansions.values().forEach(Expansion::simulate);
    }

    @Override
    public void onTick() {
        if (!lastDimension.isPresent() || MAtUtil.getPlayer().dimension != lastDimension.get()) {
            expansions.values().forEach(e -> e.setOverrideOff(!isDimensionAllowed(MAtUtil.getPlayer().dimension)));
            lastDimension = Optional.of(MAtUtil.getPlayer().dimension);
        }

        expansions.values().forEach(Expansion::evaluate);
    }

    private boolean isDimensionAllowed(int dimension) {
        boolean inList = dimensionList.contains(dimension);
        return dimensionListIsWhitelist ? inList : !inList;
    }

    public void setData(DataPackage data) {
        this.data = data;
    }

    public void setCollector(IDataCollector collector) {
        this.collector = collector;
    }

    @Override
    public float getVolume() {
        return volume;
    }

    @Override
    public void setVolumeAndUpdate(float volume) {
        this.volume = volume;
        updateVolume();
    }

    @Override
    public void updateVolume() {
        expansions.values().forEach(Expansion::updateVolume);
    }

    public void interrupt() {
        expansions.values().forEach(Expansion::interrupt);
    }

    public void dispose() {
        expansions.values().forEach(Expansion::dispose);
    }

    public void saveConfig() {
        expansions.values().forEach(Expansion::saveConfig);
    }
}
