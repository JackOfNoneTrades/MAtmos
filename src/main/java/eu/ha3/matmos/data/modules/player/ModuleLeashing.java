package eu.ha3.matmos.data.modules.player;

import java.util.List;

import eu.ha3.matmos.core.sheet.DataPackage;
import eu.ha3.matmos.data.modules.Module;
import eu.ha3.matmos.data.modules.ModuleProcessor;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;

/**
 * Processing module for the total entities a player has leashed and withing
 * range. (default 20 blocks)
 */
public class ModuleLeashing extends ModuleProcessor implements Module {
    private static final int RADIUS = 20;

    public ModuleLeashing(DataPackage data) {
        super(data, "ply_leash");
    }

    @Override
    protected void doProcess() {
        EntityPlayer player = getPlayer();

        List<EntityLiving> entities = player.getEntityWorld().getEntitiesWithinAABB(EntityLiving.class,
                AxisAlignedBB.getBoundingBox(player.posX - RADIUS, player.posY - RADIUS, player.posZ - RADIUS,
                        player.posX + RADIUS, player.posY + RADIUS, player.posZ + RADIUS));

        setValue("total", entities.stream().filter(entity -> {
            return entity.getLeashed() && entity.getLeashedToEntity() == player;
        }).count());
    }
}
