package eu.ha3.matmos.gui;

import java.io.IOException;
import java.util.Arrays;

import eu.ha3.matmos.Matmos;
import eu.ha3.matmos.util.MAtUtil;
import eu.ha3.mc.gui.HGuiSliderControl;
import eu.ha3.mc.gui.HSliderListener;
import net.minecraft.client.audio.SoundCategory;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import eu.ha3.mc.abstraction.util.ATextFormatting;

public class GuiMore extends GuiScreen {
    private final int IDS_PER_PAGE = 5;

    private GuiScreen parentScreen;

    private Matmos mod;

    private int buttonId = -1;
    
    GuiButton overrideHintButton;

    public GuiMore(GuiScreen parent, Matmos matmos) {
        parentScreen = parent;
        mod = matmos;
    }

    @Override
    public void initGui() {
        final int _GAP = 2;
        final int _UNIT = 20;
        final int _WIDTH = 155 * 2;

        final int _MIX = _GAP + _UNIT;

        final int _LEFT = width / 2 - _WIDTH / 2;
        final int _RIGHT = width / 2 + _WIDTH / 2;

        final int _SEPARATOR = 10;
        final int _TURNOFFWIDTH = _WIDTH / 5;

        buttonList.add(new GuiButton(211, _LEFT + _MIX, _MIX * (0 + 1), _WIDTH - _MIX * 2, _UNIT,
                mod.getConfig().getBoolean("reversed.controls") ? "Menu: Hold Down Key to open"
                        : "Menu: Press Key to open"));

        buttonList.add(new GuiButton(212, _LEFT + _MIX, _MIX * (1 + 1), _WIDTH - _MIX * 2, _UNIT,
                formatOpt("mat.options.ambience.low", mod.getConfig().getBoolean("useroptions.altitudes.low"))));
        buttonList.add(new GuiButton(213, _LEFT + _MIX, _MIX * (2 + 1), _WIDTH - _MIX * 2, _UNIT,
                formatOpt("mat.options.ambience.high", mod.getConfig().getBoolean("useroptions.altitudes.high"))));
        
        overrideHintButton = new GuiButton(214, _RIGHT - _UNIT, _MIX * (2 + 1), _UNIT, _UNIT, ATextFormatting.GOLD + "?");
        buttonList.add(overrideHintButton);

        GuiBiomeSlider biomeSlider = new GuiBiomeSlider(mod, mod.getConfig().getInteger("useroptions.biome.override"));
        HGuiSliderControl biomeControl = new HGuiSliderControl(214, _LEFT, _MIX * (3 + 1), _WIDTH, _UNIT, "",
                biomeSlider.calculateSliderLocation(mod.getConfig().getInteger("useroptions.biome.override")));

        biomeControl.setListener(biomeSlider);
        biomeControl.setDisplayStringProvider(biomeSlider);
        biomeControl.updateDisplayString();
        buttonList.add(biomeControl);

        HGuiSliderControl ambienceVolume = new HGuiSliderControl(216, _LEFT, _MIX * (4 + 1), _WIDTH, _UNIT, "",
                mod.getConfig().getFloat("minecraftsound.ambient.volume"));
        ambienceVolume.setListener(new HSliderListener() {

            @Override
            public void sliderValueChanged(HGuiSliderControl slider, float value) {
                mc.gameSettings.setSoundLevel(SoundCategory.AMBIENT, value);
                mod.getConfig().setProperty("minecraftsound.ambient.volume", value);
                slider.updateDisplayString();
            }

            @Override
            public void sliderReleased(HGuiSliderControl hGuiSliderControl) {
                mod.saveConfig();
                mc.gameSettings.saveOptions();
            }
        });
        ambienceVolume.setDisplayStringProvider(() -> {
            return I18n.format("mat.options.volume.ambient",
                    (int) Math.floor(mod.getConfig().getFloat("minecraftsound.ambient.volume") * 100) + "%");
        });
        ambienceVolume.updateDisplayString();
        buttonList.add(ambienceVolume);
        buttonList.add(new GuiButton(215, _LEFT + _MIX, _MIX * (6 + 1), _WIDTH - _MIX * 2, _UNIT,
                formatOpt("mat.options.devmode", mod.getConfig().getInteger("debug.mode") == 1)));
        buttonList.add(new GuiButton(200, _LEFT + _MIX, _SEPARATOR + _MIX * (IDS_PER_PAGE + 4),
                _WIDTH - _MIX * 2 - _GAP - _TURNOFFWIDTH, _UNIT, I18n.format("mat.options.done")));
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 200) {
            mc.displayGuiScreen(parentScreen);
        } else if (button.id == 211) {
            mod.getConfig().setProperty("reversed.controls", !mod.getConfig().getBoolean("reversed.controls"));
            button.displayString = mod.getConfig().getBoolean("reversed.controls") ? "Menu: Hold Down Key to open"
                    : "Menu: Press Key to open";
            mod.saveConfig();
        } else if (button.id == 212) {
            toggleOption("useroptions.altitudes.low", "mat.options.ambience.low", button);
        } else if (button.id == 213) {
            toggleOption("useroptions.altitudes.high", "mat.options.ambience.high", button);
        } else if (button.id == 214) {
            MAtUtil.playSound("mob.villager.idle", 1f, 1f);
        } else if (button.id == 215) {
            mod.getConfig().setProperty("debug.mode", mod.getConfig().getInteger("debug.mode") == 0 ? 1 : 0);
            button.displayString = mod.getConfig().getInteger("debug.mode") == 1
                    ? ATextFormatting.GOLD + "Dev/Editor mode: ON"
                    : "Dev/Editor mode: OFF";
            mod.changedDebugMode();
            mod.saveConfig();
        }

    }

    private void toggleOption(String key, String name, GuiButton button) {
        mod.getConfig().setProperty(key, !mod.getConfig().getBoolean(key));
        mod.saveConfig();

        button.displayString = formatOpt(name, mod.getConfig().getBoolean(key));
    }

    private String formatOpt(String key, boolean value) {
        return I18n.format(key) + ": " + I18n.format("mat." + value);
    }

    @Override
    public void onGuiClosed() {
        mod.saveConfig();
    }

    @Override
    protected void mouseClicked(int par1, int par2, int par3) {
        if (buttonId < 0) {
            super.mouseClicked(par1, par2, par3);
        }
    }

    @Override
    public void drawScreen(int par1, int par2, float par3) {
        final int _GAP = 2;
        final int _UNIT = 20;
        final int _MIX = _GAP + _UNIT;
        final int _SEPARATOR = 10;

        if (!mod.isDebugMode()) {
            drawGradientRect(0, 0, width, height, 0xC0000000, 0x60000000);
            drawCenteredString(fontRendererObj, I18n.format("mat.title.advanced"), width / 2, 8, 0xffffff);
        } else {
            drawGradientRect(0, 0, width, height, 0xC0C06000, 0x60C06000);
            drawCenteredString(fontRendererObj, I18n.format("mat.title.advanced") + "(Dev mode)", width / 2, 8,
                    0xffffff);

            drawCenteredString(fontRendererObj, I18n.format("mat.title.devmode"), width / 2,
                    _SEPARATOR + _MIX * (IDS_PER_PAGE + 3) - 9, 0xffffff);
        }

        if (mod.isActivated()) {
            mod.util().prepareDrawString();
            mod.util().drawString(I18n.format("mat.resources.lag", mod.getLag().getMilliseconds()), 1f, 1f, 0, 0, '3',
                    0, 0, 0, 0, true);
        }

        super.drawScreen(par1, par2, par3);
        
        if(overrideHintButton.func_146115_a()) { // isMouseOver
            drawHoveringText(Arrays.asList(I18n.format("mat.options.hint.overrides").split("\\\\n")), par1, par2, fontRendererObj);
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

}
