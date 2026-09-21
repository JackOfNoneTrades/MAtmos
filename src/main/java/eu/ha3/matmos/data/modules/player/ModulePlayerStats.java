package eu.ha3.matmos.data.modules.player;

import eu.ha3.matmos.core.sheet.DataPackage;
import eu.ha3.matmos.data.modules.Module;
import eu.ha3.matmos.data.modules.ModuleProcessor;
import eu.ha3.matmos.util.MAtUtil;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.item.ItemStack;

/**
 * Module for processing the base player statistics.
 *
 */
public class ModulePlayerStats extends ModuleProcessor implements Module {
    public ModulePlayerStats(DataPackage data) {
        super(data, "ply_general");
    }

    @Override
    protected void doProcess() {
        EntityPlayerSP player = (EntityPlayerSP) getPlayer();

        setValue("in_water", player.isInWater());
        setValue("wet", player.isWet());
        setValue("on_ground", player.onGround);
        setValue("burning", player.isBurning());
        setValue("jumping", player.movementInput.jump);
        setValue("in_web", player.isInWeb);
        setValue("on_ladder", player.isOnLadder());

        setValue("blocking", player.isBlocking());
        setValue("sprinting", player.isSprinting());
        setValue("sneaking", player.isSneaking());
        setValue("airborne", player.isAirBorne);
        setValue("using_item", player.isUsingItem());
        setValue("riding", player.isRiding());
        setValue("creative", player.capabilities.isCreativeMode);
        setValue("flying", player.capabilities.isFlying);

        setValue("under_water", MAtUtil.isUnderwaterAnyGamemode());
    }
}
