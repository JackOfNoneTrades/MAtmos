package eu.ha3.matmos.gui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nullable;

import eu.ha3.matmos.Matmos;
import eu.ha3.mc.gui.HDisplayStringProvider;
import eu.ha3.mc.gui.HGuiSliderControl;
import eu.ha3.mc.gui.HSliderListener;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.StringUtils;
import net.minecraft.world.biome.BiomeGenBase;

public class GuiBiomeSlider implements HDisplayStringProvider, HSliderListener {
    final protected Matmos mod;

    final protected int maxBiomes = calculateMaxBiomes();
    protected int definedBiomeID;

    private List<Integer> validBiomes = new ArrayList<>();

    public GuiBiomeSlider(Matmos mod, int define) {
        this.mod = mod;
        definedBiomeID = define;

        computeBiomes();
    }

    private void computeBiomes() {
        for (BiomeGenBase i : BiomeGenBase.getBiomeGenArray()) {
            if (i != null) {
                validBiomes.add(i.biomeID);
            }
        }
    }

    @Override
    public void sliderValueChanged(HGuiSliderControl slider, float value) {
        int biomeID = (int) (Math.floor(value * validBiomes.size()) - 1);

        definedBiomeID = biomeID >= 0 && biomeID < validBiomes.size() ? validBiomes.get(biomeID) : biomeID;

        slider.updateDisplayString();
    }

    @Override
    public void sliderReleased(HGuiSliderControl hGuiSliderControl) {
        mod.getConfig().setProperty("useroptions.biome.override", definedBiomeID);
        mod.saveConfig();
    }

    @Override
    public String provideDisplayString() {
        String desc = getDescriptorString();

        if (desc == null) {
            return "";
        }

        return I18n.format("mat.biome.override", desc);
    }

    @Nullable
    protected String getDescriptorString() {
        if (definedBiomeID >= 0 && definedBiomeID <= maxBiomes) {
            BiomeGenBase biome = BiomeGenBase.getBiome(definedBiomeID);

            if (biome == null) {
                return I18n.format("mat.biome.undef", definedBiomeID);
            }

            if (StringUtils.isNullOrEmpty(biome.biomeName)) {
                return I18n.format("mat.biome.unamed", definedBiomeID);
            }

            return I18n.format("mat.biome.biome", biome.biomeName, definedBiomeID);
        }

        if (definedBiomeID == -1) {
            return I18n.format("mat.biome.disabled");
        }

        return null;
    }

    public float calculateSliderLocation(int biomeID) {

        if (biomeID == -1) {
            return 0;
        }

        if (validBiomes.contains(biomeID)) {
            return (validBiomes.indexOf(biomeID) + 1) / validBiomes.size();
        }

        return 1;
    }

    private int calculateMaxBiomes() {
        int max = 0;

        for (BiomeGenBase i : BiomeGenBase.getBiomeGenArray()) {
            if (i != null) {
                int id = i.biomeID;

                if (id > max) {
                    max = id;
                }
            }
        }

        return max;
    }

}
