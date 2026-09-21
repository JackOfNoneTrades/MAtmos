package eu.ha3.matmos.data.modules.world;

import eu.ha3.matmos.Matmos;
import eu.ha3.matmos.core.sheet.DataPackage;
import eu.ha3.matmos.data.modules.Module;
import eu.ha3.matmos.data.modules.ModuleProcessor;
import eu.ha3.matmos.util.BlockPos;
import eu.ha3.matmos.util.MAtUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.world.biome.BiomeGenBase;

public class ModuleBiome extends ModuleProcessor implements Module {
    private final Matmos mod;

    public ModuleBiome(DataPackage data, Matmos mod) {
        super(data, "w_biome");
        this.mod = mod;
    }

    @Override
    protected void doProcess() {
        int biomej = mod.getConfig().getInteger("useroptions.biome.override");

        if (biomej <= -1) {
            BiomeGenBase biome = calculateBiome();
            setValue("id", biome.biomeID);
            setValue("biome_name", biome.biomeName);
        } else {
            setValue("id", biomej);
            setValue("biome_name", "");
        }
    }

    private BiomeGenBase calculateBiome() {
        Minecraft mc = Minecraft.getMinecraft();
        BlockPos playerPos = MAtUtil.getPlayerPos();

        return mc.theWorld.getChunkFromBlockCoords(playerPos.getX(), playerPos.getZ()).getBiomeGenForWorldCoords(
                playerPos.getX() & 15, playerPos.getZ() & 15, mc.theWorld.getWorldChunkManager());
    }
}
