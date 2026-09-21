package eu.ha3.matmos.data.modules.player;

import eu.ha3.matmos.core.sheet.DataPackage;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class ModuleHeldEnchantment extends AbstractEnchantmentModule {
    public ModuleHeldEnchantment(DataPackage data) {
        super(data, "ench_current");
    }

    @Override
    protected ItemStack getItem(EntityPlayer player) {
        return player.getHeldItem();
    }
}
