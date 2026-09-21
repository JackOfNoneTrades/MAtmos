package eu.ha3.matmos.data.modules.world;

import eu.ha3.matmos.core.sheet.DataPackage;
import eu.ha3.matmos.data.modules.Module;
import eu.ha3.matmos.data.modules.ModuleProcessor;
import eu.ha3.matmos.util.BlockPos;
import eu.ha3.matmos.util.MAtUtil;

public class ModulePosition extends ModuleProcessor implements Module {
    public ModulePosition(DataPackage data) {
        super(data, "cb_pos");
    }

    @Override
    protected void doProcess() {
        BlockPos pos = MAtUtil.getPlayerPos();

        setValue("x", pos.getX());
        setValue("y", pos.getY());
        setValue("z", pos.getZ());
    }
}
