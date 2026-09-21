package eu.ha3.matmos.data.modules.legacy;

import eu.ha3.matmos.core.sheet.DataPackage;
import eu.ha3.matmos.data.modules.Module;
import eu.ha3.matmos.data.modules.ModuleProcessor;
import eu.ha3.matmos.util.BlockPos;
import eu.ha3.matmos.util.MAtUtil;

public class ModuleLegacyColumn extends ModuleProcessor implements Module {
    public ModuleLegacyColumn(DataPackage data) {
        super(data, "legacy_column");
    }

    @Override
    protected void doProcess() {
        BlockPos pos = MAtUtil.getPlayerPos();

        setValue("y-1_as_number", MAtUtil.legacyOf(MAtUtil.getBlockAt(pos.down())));
        setValue("y-2_as_number", MAtUtil.legacyOf(MAtUtil.getBlockAt(pos.down(2))));
        setValue("y0_as_number", MAtUtil.legacyOf(MAtUtil.getBlockAt(pos)));
        setValue("y1_as_number", MAtUtil.legacyOf(MAtUtil.getBlockAt(pos.up())));
    }
}
