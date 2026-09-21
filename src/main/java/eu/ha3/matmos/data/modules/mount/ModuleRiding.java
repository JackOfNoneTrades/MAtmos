package eu.ha3.matmos.data.modules.mount;

import eu.ha3.matmos.core.sheet.DataPackage;
import eu.ha3.matmos.data.modules.Module;
import eu.ha3.matmos.data.modules.ModuleProcessor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.player.EntityPlayer;

public class ModuleRiding extends ModuleProcessor implements Module {
    public ModuleRiding(DataPackage data) {
        super(data, "ride_general");
    }

    @Override
    protected void doProcess() {
        Entity ride = getPlayer().ridingEntity;

        setValue("minecart", ride instanceof EntityMinecart);
        setValue("boat", ride instanceof EntityBoat);
        setValue("pig", ride instanceof EntityPig);
        setValue("horse", ride instanceof EntityHorse);
        setValue("player", ride instanceof EntityPlayer);

        setValue("burning", ride != null && ride.isBurning());
        setValue("entity_id", ride == null ? NO_ENTITY : EntityList.getEntityString(ride));
    }
}
