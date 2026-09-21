package eu.ha3.matmos.data.modules.player;

import eu.ha3.matmos.core.expansion.ExpansionManager;
import eu.ha3.matmos.core.sheet.DataPackage;
import eu.ha3.matmos.data.modules.Module;
import eu.ha3.matmos.data.modules.ModuleProcessor;
import net.minecraft.entity.player.EntityPlayer;

/**
 * Module for processing a player's armour slot. Only one.
 */
public class ModuleArmour extends ModuleProcessor implements Module {
    private DataPackage data;
    public ModuleArmour(DataPackage data) {
        super(data, "ply_armor");
        this.data = data;
    }

    @Override
    protected void doProcess() {
        EntityPlayer player = getPlayer();

        for (int i = 0; i < 4; i++) {
            ItemProcessorHelper.setValue(this, ExpansionManager.dealias(player.inventory.armorInventory[i], data), Integer.toString(i));
        }
    }
}
