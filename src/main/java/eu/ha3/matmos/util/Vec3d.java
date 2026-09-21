package eu.ha3.matmos.util;

import net.minecraft.util.Vec3;

/***
 * Backport of Vec3d from 1.12.2.
 */

public class Vec3d extends Vec3 implements Cloneable {

    public static final Vec3d ZERO = new Vec3d(0, 0, 0);

    public Vec3d(double x, double y, double z) {
        super(x, y, z);
    }

    public Vec3d(Vec3 v) {
        this(v.xCoord, v.yCoord, v.zCoord);
    }

    public double length() {
        return super.lengthVector();
    }

    public Vec3d normalize() {
        double l = length();
        return new Vec3d(xCoord / l, yCoord / l, zCoord / l);
    }

    public Vec3d scale(double s) {
        return new Vec3d(xCoord * s, yCoord * s, zCoord * s);
    }

    public Vec3d add(double dx, double dy, double dz) {
        return new Vec3d(xCoord + dx, yCoord + dy, zCoord + dz);
    }

    public Vec3d add(Vec3 o) {
        return add(o.xCoord, o.yCoord, o.zCoord);
    }

    public Vec3d subtract(Vec3d o) {
        return add(-o.xCoord, -o.yCoord, -o.zCoord);
    }

    public Vec3d clone() {
        return new Vec3d(xCoord, yCoord, zCoord);
    }
}
