package eu.ha3.matmos.util;

import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

/***
 * Backport of BlockPos to 1.7.10. The purpose of this is to ease backporting
 * without having to rewrite a lot of code.
 * 
 * @author makamys
 *
 */

public class BlockPos {
    public static BlockPos ORIGIN = new BlockPos(0, 0, 0);

    private int x, y, z;

    public BlockPos(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public BlockPos(Vec3 v) {
        this(v.xCoord, v.yCoord, v.zCoord);
    }

    public BlockPos(double x, double y, double z) {
        this((int) MathHelper.floor_double(x), (int) MathHelper.floor_double(y), (int) MathHelper.floor_double(z));
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public BlockPos down(int n) {
        return new BlockPos(x, y + n, z);
    }

    public BlockPos down() {
        return down(1);
    }

    public BlockPos up(int n) {
        return down(-n);
    }

    public BlockPos up() {
        return up(1);
    }

    public BlockPos add(BlockPos o) {
        return new BlockPos(x + o.x, y + o.y, z + o.z);
    }

    public double distanceSq(double ox, double oy, double oz) {
        double dx = ox - x;
        double dy = oy - y;
        double dz = oz - z;
        return dx * dx + dy * dy + dz * dz;
    }

    @Override
    public String toString() {
        return "{" + x + ", " + y + ", " + z + "}";
    }
}
