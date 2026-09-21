package eu.ha3.matmos.util.math;

import eu.ha3.matmos.util.BlockPos;

/**
 * @author dags_ <dags@dags.me>
 */
public class MAtMutableBlockPos extends BlockPos {
    private static final MAtMutableBlockPos INSTANCE = new MAtMutableBlockPos();

    private int x;
    private int y;
    private int z;

    private MAtMutableBlockPos() {
        super(0, 0, 0);
    }

    /**
     * Returns a block position wrapper set at the given coordinates.
     */
    public static BlockPos of(int x, int y, int z) {
        return INSTANCE.at(x, y, z);
    }

    private MAtMutableBlockPos at(int xPos, int yPos, int zPos) {
        x = xPos;
        y = yPos;
        z = zPos;
        return this;
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }

    @Override
    public int getZ() {
        return z;
    }
}
