package eu.ha3.matmos.core;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.gtnewhorizon.gtnhmixins.IEarlyMixinLoader;

import cpw.mods.fml.relauncher.FMLLaunchHandler;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import eu.ha3.matmos.core.preinit.SoundSystemReplacer;

@IFMLLoadingPlugin.MCVersion("1.7.10")
public class EarlyMixinLoader implements IEarlyMixinLoader, IFMLLoadingPlugin {
    public EarlyMixinLoader() {
        SoundSystemReplacer.run();
    }

    @Override
    public String getMixinConfig() { return "mixins.matmos.early.json"; }

    @Override
    public List<String> getMixins(Set<String> loadedCoreMods) {
        return FMLLaunchHandler.side().isClient() ? Collections.singletonList("MixinSoundManager")
                : Collections.<String>emptyList();
    }

    @Override
    public String[] getASMTransformerClass() { return null; }
    @Override
    public String getModContainerClass() { return null; }
    @Override
    public String getSetupClass() { return null; }
    @Override
    public void injectData(Map<String, Object> data) {}
    @Override
    public String getAccessTransformerClass() { return null; }
}
