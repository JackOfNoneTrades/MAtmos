package eu.ha3.matmos.data.modules.world;

import eu.ha3.matmos.core.sheet.DataPackage;
import eu.ha3.matmos.data.modules.Module;
import eu.ha3.matmos.data.modules.ModuleProcessor;
import eu.ha3.matmos.util.BlockPos;
import eu.ha3.matmos.util.MAtUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;

public class ModuleLighting extends ModuleProcessor implements Module {
    public ModuleLighting(DataPackage data) {
        super(data, "cb_light");
    }

    @Override
    protected void doProcess() {
        World w = Minecraft.getMinecraft().theWorld;

        BlockPos playerPos = MAtUtil.getPlayerPos();

        setValue("sky", w.getSavedLightValue(EnumSkyBlock.Sky, playerPos.getX(), playerPos.getY(), playerPos.getZ()));
        setValue("lamp",
                w.getSavedLightValue(EnumSkyBlock.Block, playerPos.getX(), playerPos.getY(), playerPos.getZ()));
        setValue("final", w.getBlockLightValue(playerPos.getX(), playerPos.getY(), playerPos.getZ()));
        setValue("see_sky", w.canBlockSeeTheSky(playerPos.getX(), playerPos.getY(), playerPos.getZ()));
    }
}
