package eu.ha3.matmos.core.expansion;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import eu.ha3.easy.TimeStatistic;
import eu.ha3.matmos.Matmos;
import eu.ha3.matmos.core.Evaluated;
import eu.ha3.matmos.core.Knowledge;
import eu.ha3.matmos.core.ProviderCollection;
import eu.ha3.matmos.core.ReferenceTime;
import eu.ha3.matmos.core.Simulated;
import eu.ha3.matmos.core.SystemClock;
import eu.ha3.matmos.core.event.EventInterface;
import eu.ha3.matmos.core.expansion.agents.LoadingAgent;
import eu.ha3.matmos.core.expansion.agents.RawJsonLoadingAgent;
import eu.ha3.matmos.core.sheet.DataPackage;
import eu.ha3.matmos.core.sound.LoopingStreamedSoundManager;
import eu.ha3.matmos.core.sound.SoundHelperRelay;
import eu.ha3.matmos.data.IDataCollector;
import eu.ha3.matmos.data.modules.ModuleRegistry;
import eu.ha3.mc.haddon.supporting.SupportsBlockChangeEvents;
import eu.ha3.mc.haddon.supporting.event.BlockChangeEvent;
import eu.ha3.util.property.simple.ConfigProperty;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.FolderResourcePack;
import net.minecraft.util.ResourceLocation;

public class Expansion implements VolumeUpdatable, Stable, Simulated, Evaluated, SupportsBlockChangeEvents {
    private static ReferenceTime TIME = new SystemClock();

    private final ExpansionIdentity identity;
    private final DataPackage data;
    private final IDataCollector collector;

    private final SoundHelperRelay capabilities;

    private final VolumeContainer masterVolume;

    private final ConfigProperty myConfiguration = new ConfigProperty();

    private float volume;
    private boolean isSuccessfullyBuilt;
    private boolean isActive;
    private boolean reliesOnLegacyModules;

    private Knowledge knowledge; // Knowledge is not final

    private LoadingAgent agent;
    private LoadingAgent jsonAgent;

    private Exception loadException;

    public Expansion(ExpansionIdentity identity, DataPackage data, IDataCollector collector,
            LoopingStreamedSoundManager soundManager, VolumeContainer masterVolume, File configurationSource) {
        this.identity = identity;
        this.masterVolume = masterVolume;
        this.data = data;
        this.collector = collector;
        this.capabilities = new SoundHelperRelay(soundManager);

        newKnowledge();

        myConfiguration.setProperty("volume", 1);
        myConfiguration.commit();
        try {
            myConfiguration.setSource(configurationSource.getCanonicalPath());
            myConfiguration.load();
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        Matmos.addBlockChangeListener(this);

        setVolumeAndUpdate(myConfiguration.getFloat("volume"));
    }

    public void setLoadingAgent(LoadingAgent agent) {
        this.agent = agent;
    }

    public void refreshKnowledge() {
        boolean reactivate = isActivated();
        deactivate();

        newKnowledge();
        isSuccessfullyBuilt = false;

        if (reactivate) {
            activate();
        }
    }

    public void pushDebugJsonAndRefreshKnowledge(String jsonString) {
        jsonAgent = new RawJsonLoadingAgent(jsonString);
        refreshKnowledge();
    }

    private void newKnowledge() {
        knowledge = new Knowledge(capabilities, TIME);

        knowledge.setData(data);
        knowledge.addKnowledge(Knowledge.getBuiltins(knowledge.obtainProviders()));
        knowledge.setConditionValueOverrides(collectConditionValueOverrides());
    }
    
    private Map<String, String> collectConditionValueOverrides() {
        String OVERRIDE_CONDITION_PREFIX = "override.condition.";
        Map<String, String> overrides = new HashMap<>();
        myConfiguration.getAllProperties().forEach((k, v) -> {
            if(k.startsWith(OVERRIDE_CONDITION_PREFIX)) {
                overrides.put(k.substring(OVERRIDE_CONDITION_PREFIX.length()), v);
            }
        });
        return overrides;
    }
    
    public Map<String, String> getConditionValueOverrides(){
        return knowledge.getConditionValueOverrides();
    }

    private void buildKnowledge() {
        if (agent == null) {
            return;
        }

        newKnowledge();

        loadException = null;

        if (jsonAgent == null) {
            loadException = agent.load(identity, knowledge);
        } else {
            loadException = jsonAgent.load(identity, knowledge);
            jsonAgent = null;
        }
        isSuccessfullyBuilt = loadException == null;

        if (!isSuccessfullyBuilt) {
            newKnowledge();
        }

        knowledge.cacheSounds(identity);
        knowledge.compile();
    }

    public void playSample() {
        if (!isActivated()) {
            return;
        }

        EventInterface event = knowledge.obtainProviders().getEvent().get("__SAMPLE");
        if (event != null) {
            event.playSound(1f, 1f);
        }
    }

    public String getName() {
        return identity.getUniqueName();
    }

    public String getFriendlyName() {
        return identity.getFriendlyName();
    }

    public Exception getLoadException() {
        return loadException;
    }

    public void saveConfig() {
        myConfiguration.setProperty("volume", volume);
        myConfiguration.getAllProperties().entrySet().removeIf(e -> e.getKey().startsWith("override.condition."));
        
        knowledge.getConditionValueOverrides().forEach((k, v) -> {
            myConfiguration.setProperty("override.condition." + k, v);
        });
        if (myConfiguration.commit()) {
            myConfiguration.save();
        }
    }

    public boolean reliesOnLegacyModules() {
        return reliesOnLegacyModules;
    }

    @Override
    public void simulate() {
        if (!isActive) {
            return;
        }

        try {
            knowledge.simulate();
        } catch (Exception e) {
            e.printStackTrace();
            deactivate();
        }
    }

    @Override
    public void evaluate() {
        if (!isActive) {
            return;
        }

        try {
            knowledge.evaluate();
        } catch (Exception e) {
            e.printStackTrace();
            deactivate();
        }
    }

    @Override
    public boolean isActivated() {
        return isActive;
    }

    @Override
    public void activate() {
        if (isActive) {
            return;
        }

        if (getVolume() <= 0) {
            return;
        }

        if (!isSuccessfullyBuilt && agent != null) {
            Matmos.DEVLOGGER.info("Building expansion " + getName() + "...");
            TimeStatistic stat = new TimeStatistic(Locale.ENGLISH);
            buildKnowledge();
            if (isSuccessfullyBuilt) {
                Matmos.DEVLOGGER.info("Expansion " + getName() + " built (" + stat.getSecondsAsString(3) + "s).");
            } else {
                Matmos.LOGGER
                        .warn("Expansion " + getName() + " failed to build!!! (" + stat.getSecondsAsString(3) + "s).");
            }
        }

        if (collector != null) {
            Set<String> requiredModules = knowledge.calculateRequiredModules();
            collector.addModuleStack(identity.getUniqueName(), requiredModules);

            Matmos.DEVLOGGER.info("Expansion " + identity.getUniqueName() + " requires " + requiredModules.size()
                    + " found modules: " + Arrays.toString(requiredModules.toArray()));

            String[] legacyModules = requiredModules.stream()
                    .filter(module -> module.startsWith(ModuleRegistry.LEGACY_PREFIX)).sorted().toArray(String[]::new);

            if (legacyModules.length > 0) {
                Matmos.DEVLOGGER.warn("Expansion " + identity.getUniqueName() + " uses LEGACY modules: "
                        + Arrays.toString(legacyModules));
                reliesOnLegacyModules = true;
            }
        }

        capabilities.activate();

        isActive = true;

    }

    @Override
    public void deactivate() {
        if (!isActive) {
            return;
        }

        if (collector != null) {
            collector.removeModuleStack(identity.getUniqueName());
        }

        capabilities.deactivate();

        isActive = false;
    }

    @Override
    public void dispose() {
        deactivate();
        capabilities.cleanUp();
        newKnowledge();
        setLoadingAgent(null);
        Matmos.removeBlockChangeListener(this);
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
        capabilities.applyVolume(masterVolume.getVolume() * getVolume() * identity.getVolumeModifier());
    }

    public void setOverrideOff(boolean overrideOff) {
        knowledge.setOverrideOff(overrideOff);
    }

    /**
     * Interrupt this expansion brutally, without calling cleanup calls.
     */
    @Override
    public void interrupt() {
        capabilities.interrupt();
    }

    /**
     * Obtain the providers of the knowledge, for debugging purposes.
     */
    public ProviderCollection obtainProvidersForDebugging() {
        return knowledge.obtainProviders();
    }

    public ExpansionDebugUnit obtainDebugUnit() {
        try {
            if (identity.getPack() instanceof FolderResourcePack) {
                FolderResourcePack frp = (FolderResourcePack) identity.getPack();
                String folderName = frp.getPackName();
                // XXX: getPackName might not be specified to return the folder name?

                final File folder = new File(Minecraft.getMinecraft().mcDataDir, "resourcepacks/" + folderName);

                if (folder.exists() && folder.isDirectory()) {
                    System.out.println(identity.getLocation().getResourcePath());
                    final File file = new File(folder, "assets/matmos/" + identity.getLocation().getResourcePath());

                    return new FolderExpansionDebugUnit() {
                        @Override
                        public Knowledge getKnowledge() {
                            return knowledge;
                        }

                        @Override
                        public DataPackage getData() {
                            return data;
                        }

                        @Override
                        public File getExpansionFile() {
                            return file;
                        }

                        @Override
                        public File getExpansionFolder() {
                            return folder;
                        }
                    };
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new JsonExpansionDebugUnit() {

            @Override
            public Knowledge getKnowledge() {
                return knowledge;
            }

            @Override
            public DataPackage getData() {
                return data;
            }

            @Override
            public String getJsonString() {
                try (Scanner sc = new Scanner(identity.getPack().getInputStream(identity.getLocation()))) {
                    // XXX does not handle XML
                    return sc.useDelimiter("\\Z").next();
                } catch (IOException e) {
                    e.printStackTrace();
                    System.err.println("Jason unavailable.");
                    return "{}";
                }
            }
        };
    }

    public boolean hasMoreInfo() {
        return identity.getPack().resourceExists(new ResourceLocation("matmos", "info.txt"));
    }

    public String getInfo() {
        try (Scanner sc = new Scanner(identity.getPack().getInputStream(new ResourceLocation("matmos", "info.txt")))) {
            return sc.useDelimiter("\\Z").next();
        } catch (Exception e) {
            e.printStackTrace();
            return "Error while fetching info.txt";
        }
    }
    
    public boolean isRainMuteable() {
        return knowledge.hasOverrideRainCondition();
    }

    @Override
    public void onBlockChanged(BlockChangeEvent event) {
        knowledge.onBlockChanged(event);
    }
}
