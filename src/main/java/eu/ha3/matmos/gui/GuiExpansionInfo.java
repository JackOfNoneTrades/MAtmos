package eu.ha3.matmos.gui;

import eu.ha3.matmos.Matmos;
import eu.ha3.matmos.core.expansion.Expansion;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.resources.I18n;
import eu.ha3.mc.abstraction.util.ATextFormatting;

public class GuiExpansionInfo extends GuiScreen {
    private final GuiMatMenu parentScreen;

    private final Expansion expansion;

    private final String[] info;

    public GuiExpansionInfo(GuiMatMenu menu, Matmos mod, Expansion expansion) {
        parentScreen = menu;
        this.expansion = expansion;

        String info = expansion.hasMoreInfo() ? expansion.getInfo() : I18n.format("mat.expansion.noinfo");
        this.info = info.replace("\r", "").replace("�", "\u00A7").split("\n");
    }

    @Override
    public void drawScreen(int par1, int par2, float par3) {
        drawGradientRect(0, 0, width, height, 0xF0000000, 0x90000000);

        drawCenteredString(fontRendererObj, "About " + ATextFormatting.YELLOW + ATextFormatting.ITALIC
                + expansion.getFriendlyName() + ATextFormatting.RESET + "...", width / 2, 4, 0xffffff);

        int lc = 0;
        for (String line : info) {
            fontRendererObj.drawString(line, width / 2 - 200, 16 + 8 * lc, 0xFFFFFF);
            lc++;
        }

        super.drawScreen(par1, par2, par3);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void initGui() {
        int h = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight).getScaledHeight() - 22;

        buttonList.add(new GuiButton(200, 2, h, 70, 20, I18n.format("mat.options.close")));
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 200) {
            mc.displayGuiScreen(parentScreen);
        }
    }
}
