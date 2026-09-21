package eu.ha3.matmos.data.modules.mount;

import eu.ha3.matmos.core.sheet.DataPackage;
import eu.ha3.matmos.data.modules.Module;
import eu.ha3.matmos.data.modules.ModuleProcessor;
import net.minecraft.entity.Entity;

public class ModuleRidingMotion extends ModuleProcessor implements Module {
    public ModuleRidingMotion(DataPackage data) {
        super(data, "ride_motion");
    }

    @Override
    protected void doProcess() {
        Entity ride = getPlayer().ridingEntity;

        if (ride != null) {
            int mxx = (int) Math.round(ride.motionX * 1000);
            int mzz = (int) Math.round(ride.motionZ * 1000);

            setValue("x_1k", mxx);
            setValue("y_1k", (int) Math.round(ride.motionY * 1000));
            setValue("z_1k", mzz);
            setValue("sqrt_xx_zz", (int) Math.floor(Math.sqrt(mxx * mxx + mzz * mzz)));
        } else {
            setValue("x_1k", 0);
            setValue("y_1k", 0);
            setValue("z_1k", 0);
            setValue("sqrt_xx_zz", 0);
        }
    }
}
