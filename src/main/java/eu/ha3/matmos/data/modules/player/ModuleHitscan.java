package eu.ha3.matmos.data.modules.player;

import java.util.HashMap;
import java.util.Map;

import eu.ha3.matmos.core.sheet.DataPackage;
import eu.ha3.matmos.data.modules.Module;
import eu.ha3.matmos.data.modules.ModuleProcessor;
import eu.ha3.matmos.util.BlockPos;
import eu.ha3.matmos.util.MAtUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityList;
import net.minecraft.util.MovingObjectPosition;

/**
 * Processes values for the entity/block that a player focuses over with the
 * cursor.
 */
public class ModuleHitscan extends ModuleProcessor implements Module {
    private final Map<MovingObjectPosition.MovingObjectType, String> equiv = new HashMap<>();

    public ModuleHitscan(DataPackage data) {
        super(data, "ply_hitscan");
        equiv.put(MovingObjectPosition.MovingObjectType.MISS, "");
        equiv.put(MovingObjectPosition.MovingObjectType.ENTITY, "entity");
        equiv.put(MovingObjectPosition.MovingObjectType.BLOCK, "block");
    }

    @Override
    protected void doProcess() {
        Minecraft mc = Minecraft.getMinecraft();

        if (mc.objectMouseOver == null || mc.objectMouseOver.typeOfHit == null) {
            setValue("mouse_over_something", false);
            setValue("mouse_over_what", "");
            setValue("block", NO_BLOCK_IN_THIS_CONTEXT);
            setValue("meta", NO_META);
            setValue("powermeta", NO_POWERMETA);
            setValue("entity_id", NO_ENTITY);

            return;
        }

        BlockPos mouseOverPos = new BlockPos(mc.objectMouseOver.blockX, mc.objectMouseOver.blockY,
                mc.objectMouseOver.blockZ);

        setValue("mouse_over_something", mc.objectMouseOver.typeOfHit != MovingObjectPosition.MovingObjectType.MISS);
        setValue("mouse_over_what", equiv.get(mc.objectMouseOver.typeOfHit));
        setValue("block",
                mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK
                        ? MAtUtil.getNameAt(mouseOverPos, NO_BLOCK_OUT_OF_BOUNDS)
                        : NO_BLOCK_IN_THIS_CONTEXT);
        setValue("meta",
                mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK
                        ? MAtUtil.getMetaAsStringAt(mouseOverPos, NO_BLOCK_OUT_OF_BOUNDS)
                        : NO_BLOCK_IN_THIS_CONTEXT);
        setValue("powermeta",
                mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK
                        ? MAtUtil.getPowerMetaAt(mouseOverPos, NO_BLOCK_OUT_OF_BOUNDS)
                        : NO_BLOCK_IN_THIS_CONTEXT);
        setValue("entity_id",
                mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY
                        ? EntityList.getEntityString(mc.objectMouseOver.entityHit)
                        : NO_ENTITY);
    }
}
