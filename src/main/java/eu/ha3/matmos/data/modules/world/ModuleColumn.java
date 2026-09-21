package eu.ha3.matmos.data.modules.world;

import eu.ha3.matmos.core.sheet.DataPackage;
import eu.ha3.matmos.data.modules.Module;
import eu.ha3.matmos.data.modules.ModuleProcessor;
import eu.ha3.matmos.util.BlockPos;
import eu.ha3.matmos.util.MAtUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.world.World;

public class ModuleColumn extends ModuleProcessor implements Module {
    public ModuleColumn(DataPackage data) {
        super(data, "cb_column");

        EI("y-2", "Block under the feet");
        EI("y-1", "Block at the legs");
        EI("y0", "Block at the body (y)");
        EI("y1", "Block over the head");
        EI("topmost_block", "y coordinate of the top most solid block");
        EI("thickness_overhead", "Number of blocks over the player until topmost_block");
        EI("can_rain_reach", "Can rain reach y?");
    }

    @Override
    protected void doProcess() {
        World w = Minecraft.getMinecraft().theWorld;

        BlockPos pos = MAtUtil.getPlayerPos();
        BlockPos topMostBlock = new BlockPos(pos.getX(), w.getTopSolidOrLiquidBlock(pos.getX(), pos.getZ()),
                pos.getZ());

        setValue("y-1", MAtUtil.getNameAt(pos.down(), NO_BLOCK_OUT_OF_BOUNDS));
        setValue("y-2", MAtUtil.getNameAt(pos.down(2), NO_BLOCK_OUT_OF_BOUNDS));
        setValue("y0", MAtUtil.getNameAt(pos, NO_BLOCK_OUT_OF_BOUNDS));
        setValue("y1", MAtUtil.getNameAt(pos.up(), NO_BLOCK_OUT_OF_BOUNDS));

        setValue("topmost_block", topMostBlock.getY());
        setValue("thickness_overhead", topMostBlock.getY() - pos.getY());
        setValue("can_rain_reach",
                w.canBlockSeeTheSky(pos.getX(), pos.getY(), pos.getZ()) && !(topMostBlock.getY() > pos.getY()));
    }
}
