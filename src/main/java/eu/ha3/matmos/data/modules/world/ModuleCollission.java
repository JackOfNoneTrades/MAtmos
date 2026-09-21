package eu.ha3.matmos.data.modules.world;

import org.apache.commons.lang3.tuple.Pair;

import eu.ha3.matmos.core.sheet.DataPackage;
import eu.ha3.matmos.data.modules.BlockCountModule;
import eu.ha3.matmos.util.BlockPos;
import eu.ha3.matmos.util.MAtUtil;

public class ModuleCollission extends BlockCountModule {
    private final BlockPos[] blocks = { new BlockPos(0, -2, 0), // bottom
            new BlockPos(0, -1, 0), new BlockPos(0, 0, 0), // body
            new BlockPos(0, 1, 0), // column
            new BlockPos(-1, -1, 0), new BlockPos(1, -1, 0), // x -- 0
            new BlockPos(0, -1, -1), new BlockPos(0, -1, 1), // z -- 0
            new BlockPos(-1, 0, 0), new BlockPos(1, 0, 0), // x -- 1
            new BlockPos(0, 0, -1), new BlockPos(0, 0, 1), // z -- 1
    };

    public ModuleCollission(DataPackage dataIn) {
        super(dataIn, "block_contact", true);
        dataIn.getSheet(getName()).setDefaultValue("0");
    }

    @Override
    public void count() {
        for (BlockPos triplet : blocks) {
            BlockPos center = triplet.add(MAtUtil.getPlayerPos());
            increment(Pair.of(MAtUtil.getBlockAt(center), MAtUtil.getMetaAt(center, -1)));
        }
    }
}
