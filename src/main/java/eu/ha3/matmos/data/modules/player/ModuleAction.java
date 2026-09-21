package eu.ha3.matmos.data.modules.player;

import eu.ha3.matmos.core.sheet.DataPackage;
import eu.ha3.matmos.data.modules.Module;
import eu.ha3.matmos.data.modules.ModuleProcessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;

/**
 * Processing module for discovering player actions. Swing, Fall, Item_Use, etc.
 */
public class ModuleAction extends ModuleProcessor implements Module {
    public ModuleAction(DataPackage data) {
        super(data, "ply_action");
    }

    @Override
    protected void doProcess() {
        EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;

        setValue("swing_progress16", (int) Math.floor(player.swingProgress * 16));
        setValue("swinging", player.isSwingInProgress);
        setValue("fall_distance1k", (int) (player.fallDistance * 1000));
        setValue("item_use_duration", player.getItemInUseCount());
    }
}
