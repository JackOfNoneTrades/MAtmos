package eu.ha3.matmos.data.modules.legacy;

import eu.ha3.matmos.core.expansion.ExpansionManager;
import eu.ha3.matmos.core.sheet.DataPackage;
import eu.ha3.matmos.data.modules.Module;
import eu.ha3.matmos.data.modules.ModuleProcessor;
import eu.ha3.matmos.util.MAtUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityMinecartEmpty;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class ModuleLegacy extends ModuleProcessor implements Module {
    private DataPackage data;
    public ModuleLegacy(DataPackage data) {
        super(data, "legacy");
        this.data = data;
    }

    @Override
    protected void doProcess() {
        Minecraft mc = Minecraft.getMinecraft();

        EntityPlayer player = getPlayer();

        Entity ride = player.ridingEntity;

        setValue("player_health_ceil", (int) Math.ceil(player.getHealth()));
        setValue("world_nether", player.dimension == -1);
        setValue("player_current_item_as_number", number(ExpansionManager.dealias(player.inventory.getCurrentItem(), data)));

        setValue("72000_minus_item_use_duration", 72000 - player.getItemInUseCount());
        setValue("riding_minecart", ride != null && ride.getClass() == EntityMinecartEmpty.class);
        setValue("riding_boat", ride != null && ride.getClass() == EntityBoat.class);
        setValue("armor_0_as_number", number(ExpansionManager.dealias(player.inventory.armorInventory[0], data)));
        setValue("armor_1_as_number", number(ExpansionManager.dealias(player.inventory.armorInventory[1], data)));
        setValue("armor_2_as_number", number(ExpansionManager.dealias(player.inventory.armorInventory[2], data)));
        setValue("armor_3_as_number", number(ExpansionManager.dealias(player.inventory.armorInventory[3], data)));
        setValue("gui_instanceof_container", mc.currentScreen != null && mc.currentScreen instanceof GuiContainer);
        setValue("riding_horse", ride != null && ride instanceof EntityHorse);
        setValue("seed_higher", (int) (mc.theWorld.getSeed() >> 32));
        setValue("seed_lower", (int) (mc.theWorld.getSeed() & 0xFFFFFFFF));
    }

    private int number(ItemStack item) {
        return item != null ? MAtUtil.legacyOf(item) : LEGACY_NO_ITEM;
    }
}
