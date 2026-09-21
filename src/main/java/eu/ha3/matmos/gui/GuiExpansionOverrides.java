package eu.ha3.matmos.gui;

import java.io.File;

import eu.ha3.matmos.ConfigManager;
import eu.ha3.matmos.Matmos;
import eu.ha3.matmos.core.expansion.Expansion;
import eu.ha3.matmos.core.sound.Simulacrum;
import eu.ha3.matmos.util.MAtUtil;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.resources.I18n;

public class GuiExpansionOverrides extends GuiScreen {
    
    private final GuiScreen parentScreen;
    private final Matmos mod;
    private final Expansion expansion;
    private GuiExpansionOverrideList list;
    
    public GuiExpansionOverrides(GuiScreen parentScreen, Matmos mod, Expansion expansion) {
        this.parentScreen = parentScreen;
        this.mod = mod;
        this.expansion = expansion;
    }
    
    @Override
    public void initGui() {
        super.initGui();
        
        list = new GuiExpansionOverrideList(this.mc, this, expansion);
        
        int h = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight).getScaledHeight() - 22;
        buttonList.add(new GuiButton(200, 2, h, 70, 20, I18n.format("gui.done")));
        buttonList.add(new GuiButton(201, 2 + 72, h, 170, 20, I18n.format(I18n.format("mat.options.openuserconfig"))));
    }
    
    @Override
    protected void actionPerformed(GuiButton button) {
        if(button.id == 200) {
            mc.displayGuiScreen(parentScreen);
        } else if(button.id == 201) {
            MAtUtil.openFolder(new File(ConfigManager.getConfigFolder(), Simulacrum.USERCONFIG_FOLDER));
        }
    }
    
    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        
        list.mouseClicked(mouseX, mouseY, mouseButton);
    }
    
    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        super.keyTyped(typedChar, keyCode);
        
        list.keyTyped(typedChar, keyCode);
    }
    
    @Override
    public void updateScreen() {
        super.updateScreen();
        
        list.updateScreen();
    }
    
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        list.drawScreen(mouseX, mouseY, partialTicks);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
    
    @Override
    public void onGuiClosed() {
        list.onGuiClosed();
        expansion.saveConfig();
        expansion.refreshKnowledge();
    }
}
