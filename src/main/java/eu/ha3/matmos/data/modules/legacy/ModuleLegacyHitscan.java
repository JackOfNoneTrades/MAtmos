package eu.ha3.matmos.data.modules.legacy;

import java.util.HashMap;
import java.util.Map;

import eu.ha3.matmos.core.sheet.DataPackage;
import eu.ha3.matmos.data.modules.Module;
import eu.ha3.matmos.data.modules.ModuleProcessor;
import eu.ha3.matmos.util.BlockPos;
import eu.ha3.matmos.util.MAtUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.util.MovingObjectPosition;

public class ModuleLegacyHitscan extends ModuleProcessor implements Module {
    private final Map<MovingObjectPosition.MovingObjectType, String> equiv = new HashMap<>();

    public ModuleLegacyHitscan(DataPackage data) {
        super(data, "legacy_hitscan");

        // The ordinal values was different back then, "0" was the block.
        equiv.put(MovingObjectPosition.MovingObjectType.MISS, "-1");
        equiv.put(MovingObjectPosition.MovingObjectType.ENTITY, "1");
        equiv.put(MovingObjectPosition.MovingObjectType.BLOCK, "0");
    }

    @Override
    protected void doProcess() {
        MovingObjectPosition mc = Minecraft.getMinecraft().objectMouseOver;

        if (mc == null || mc.typeOfHit == null) {
            setValue("mouse_over_something", false);
            setValue("mouse_over_what_remapped", -1);
            setValue("block_as_number", LEGACY_NO_BLOCK_IN_THIS_CONTEXT);
            setValue("meta_as_number", LEGACY_NO_BLOCK_IN_THIS_CONTEXT);

            return;
        }

        BlockPos hitPos = new BlockPos(mc.blockX, mc.blockY, mc.blockZ);

        setValue("mouse_over_something", mc.typeOfHit != MovingObjectPosition.MovingObjectType.MISS);
        setValue("mouse_over_what_remapped", equiv.get(mc.typeOfHit));
        setValue("block_as_number",
                mc.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK
                        ? MAtUtil.legacyOf(MAtUtil.getBlockAt(hitPos))
                        : LEGACY_NO_BLOCK_IN_THIS_CONTEXT);
        setValue("meta_as_number",
                mc.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK
                        ? MAtUtil.getMetaAt(hitPos, LEGACY_NO_BLOCK_OUT_OF_BOUNDS)
                        : LEGACY_NO_BLOCK_IN_THIS_CONTEXT);
    }
}
