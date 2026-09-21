package eu.ha3.matmos.gui;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import eu.ha3.matmos.Matmos;
import eu.ha3.matmos.data.modules.ModuleProcessor;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;

public class GuiModules extends GuiScreen {
    private final Matmos mod;
    private GuiScreen parentScreen;

    private int buttonId;
    private List<String> val;

    public GuiModules(GuiScreen par1GuiScreen, Matmos mod) {
        this.mod = mod;
        buttonId = -1;
        parentScreen = par1GuiScreen;

        val = mod.getVisualDebugger().obtainSheetNamesCopy();
        Iterator<String> iter = val.iterator();
        while (iter.hasNext()) {
            if (iter.next().endsWith(ModuleProcessor.DELTA_SUFFIX)) {
                iter.remove();
            }
        }
    }

    @Override
    public void initGui() {
        final int _GAP = 2;
        final int _UNIT = 20;
        final int _WIDTH = 155 * 2;

        final int _MIX = _GAP + _UNIT;

        final int _LEFT = width / 2 - _WIDTH / 2;

        final int _SEPARATOR = 10;
        final int _TURNOFFWIDTH = _WIDTH / 5;

        buttonList.add(new GuiButton(201, _LEFT + _MIX + _WIDTH - _MIX * 2 - _GAP - _TURNOFFWIDTH + _GAP,
                _SEPARATOR + _MIX * (5 + 4), _TURNOFFWIDTH, _UNIT, I18n.format("mat.options.discard")));
        buttonList.add(new GuiButton(202, _LEFT + _MIX + _WIDTH - _MIX * 2 + _GAP, _SEPARATOR + _MIX * (5 + 4),
                _TURNOFFWIDTH, _UNIT, I18n.format("mat.options.deltas")));

        for (int id = 0; id < val.size(); id++) {
            int flid = id / 18;
            buttonList.add(new GuiButton(id, _LEFT + flid * _WIDTH / 3, _SEPARATOR + _MIX / 2 * (id % 18), _WIDTH / 3,
                    _UNIT / 2, val.get(id)));
        }

        buttonList.add(new GuiButton(200, _LEFT + _MIX, _SEPARATOR + _MIX * (5 + 4),
                _WIDTH - _MIX * 2 - _GAP - _TURNOFFWIDTH, _UNIT, I18n.format("mat.options.done")));
    }

    @Override
    protected void actionPerformed(GuiButton par1GuiButton) {
        if (par1GuiButton.id == 200) {
            mc.displayGuiScreen(parentScreen);
        } else if (par1GuiButton.id == 201) {
            mod.getVisualDebugger().noDebug();
        } else if (par1GuiButton.id == 202) {
            mod.getVisualDebugger().toggleDeltas();
        } else if (par1GuiButton.id < val.size()) {
            mod.getVisualDebugger().debugModeScan(val.get(par1GuiButton.id));
        }
    }

    private void aboutToClose() {
        mod.saveConfig();
    }

    @Override
    public void onGuiClosed() {
        aboutToClose();
    }

    @Override
    protected void mouseClicked(int par1, int par2, int par3) {
        if (buttonId < 0) {
            super.mouseClicked(par1, par2, par3);
        }
    }

    @Override
    public void drawScreen(int par1, int par2, float par3) {
        drawGradientRect(0, 0, width, height, 0xC0C06000, 0x60C06000);
        drawCenteredString(fontRendererObj, I18n.format("mat.title.devmode.display"), width / 2, 1, 0xffffff);

        mod.getVisualDebugger().onFrame(-1f);

        super.drawScreen(par1, par2, par3);

    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
