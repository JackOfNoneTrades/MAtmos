package eu.ha3.matmos.core.sound;

import java.io.File;
import java.util.Locale;
import java.util.Map;

import eu.ha3.easy.TimeStatistic;
import eu.ha3.matmos.ConfigManager;
import eu.ha3.matmos.Matmos;
import eu.ha3.matmos.ResourcePackDealer;
import eu.ha3.matmos.core.expansion.Expansion;
import eu.ha3.matmos.core.expansion.ExpansionManager;
import eu.ha3.matmos.core.expansion.SoundpackIdentity;
import eu.ha3.matmos.core.expansion.VolumeUpdatable;
import eu.ha3.matmos.data.modules.ModuleRegistry;
import eu.ha3.matmos.game.user.VisualDebugger;
import eu.ha3.mc.haddon.supporting.SupportsFrameEvents;
import eu.ha3.mc.haddon.supporting.SupportsTickEvents;
import net.minecraft.client.Minecraft;

public class Simulacrum implements SupportsTickEvents, SupportsFrameEvents {
    private ExpansionManager expansionManager;
    private ModuleRegistry dataGatherer;
    private VisualDebugger visualDebugger;
    private boolean isBrutallyInterrupted;

    private boolean hasResourcePacks;
    private boolean hasDisabledResourcePacks;
    private boolean hasRainMuteableExpansions;
    
    public static final String USERCONFIG_FOLDER = "expansions_userconfig";

    public Simulacrum(Matmos mod) {
        expansionManager = new ExpansionManager(new File(ConfigManager.getConfigFolder(), USERCONFIG_FOLDER),
                ConfigManager.getConfigFolder(), mod.getSoundManager());
        expansionManager.setVolumeAndUpdate(mod.getConfig().getFloat("globalvolume.scale"));

        TimeStatistic stat = new TimeStatistic(Locale.ENGLISH);

        dataGatherer = new ModuleRegistry(mod);
        dataGatherer.load();
        visualDebugger = new VisualDebugger(mod, dataGatherer);
        expansionManager.setData(dataGatherer.getData());
        expansionManager.setCollector(dataGatherer);
        expansionManager.loadExpansions();

        hasResourcePacks = true;
        if (expansionManager.getExpansions().size() == 0) {
            ResourcePackDealer dealer = new ResourcePackDealer();

            if (dealer.findResourcePacks().count() == 0) {
                hasResourcePacks = false;
                hasDisabledResourcePacks = dealer.findDisabledResourcePacks().size() > 0;
            }
        } else {
            for (SoundpackIdentity id : expansionManager.getSoundpackIdentities()) {
                mod.addUpdateNotifierJob(id);
            }
        }

        expansionManager.synchronize();

        hasRainMuteableExpansions = expansionManager.getExpansions().values().stream().anyMatch(e -> e.isRainMuteable());
        
        Matmos.LOGGER.info("Expansions loaded (" + stat.getSecondsAsString(1) + "s).");
    }

    public void interruptBrutally() {
        isBrutallyInterrupted = true;
    }

    public void dispose() {
        if (!isBrutallyInterrupted) {
            expansionManager.dispose();
        }
    }

    @Override
    public void onFrame(float semi) {
        expansionManager.onFrame(semi);
        visualDebugger.onFrame(semi);
    }

    @Override
    public void onTick() {
        dataGatherer.process();
        expansionManager.onTick();
    }

    public boolean hasResourcePacks() {
        return hasResourcePacks;
    }

    public boolean hasDisabledResourcePacks() {
        return hasDisabledResourcePacks;
    }
    
    public boolean hasRainMuteableExpansions() {
        return hasRainMuteableExpansions;
    }

    public Map<String, Expansion> getExpansions() {
        return expansionManager.getExpansions();
    }

    public void synchronize() {
        expansionManager.synchronize();
    }

    public void saveConfig() {
        expansionManager.saveConfig();
    }

    public VisualDebugger getVisualDebugger() {
        return visualDebugger;
    }

    public VolumeUpdatable getGlobalVolumeControl() {
        return expansionManager;
    }

}
