package eu.ha3.matmos.gui;

import java.util.Set;

import cpw.mods.fml.client.IModGuiFactory;
import eu.ha3.matmos.ForgeMatmos;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

public class MAtmosGuiFactory implements IModGuiFactory {
    @Override
    public void initialize(Minecraft minecraftInstance) {}
    @Override
    public Class<? extends GuiScreen> mainConfigGuiClass() { return ConfigGui.class; }
    @Override
    public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() { return null; }
    @Override
    public RuntimeOptionGuiHandler getHandlerFor(RuntimeOptionCategoryElement element) { return null; }

    public static class ConfigGui extends GuiMore {
        public ConfigGui(GuiScreen parentScreen) {
            super(parentScreen, ForgeMatmos.MATMOS);
        }
    }
}
