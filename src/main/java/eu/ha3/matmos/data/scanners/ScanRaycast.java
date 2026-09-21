package eu.ha3.matmos.data.scanners;

import java.util.HashSet;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

import eu.ha3.matmos.Matmos;
import eu.ha3.matmos.util.BlockPos;
import eu.ha3.matmos.util.MAtUtil;
import eu.ha3.matmos.util.Vec3d;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockBed;
import net.minecraft.block.BlockLeaves;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class ScanRaycast extends Scan {

    private static final Random rnd = new Random();

    int startX, startY, startZ;
    Vec3d center;
    int xSize, ySize, zSize;
    int startNearness;
    int maxRange;

    int raysCast = 0;
    int raysToCast;
    int directScore;
    int indirectScore;
    int distanceSqSum;

    private int THRESHOLD_SCORE;
    private int THRESHOLD_INDIRECT_SCORE;

    private Vec3d[] rays;

    private Set<BlockPos> scanned;

    @Override
    void initScan(int x, int y, int z, int xsizeIn, int ysizeIn, int zsizeIn, int opspercallIn) {
        startX = x;
        startY = y;
        startZ = z;

        center = new Vec3d(startX + 0.5, startY + 0.5, startZ + 0.5);

        xSize = xsizeIn;
        ySize = ysizeIn;
        zSize = zsizeIn;

        raysToCast = opspercall * 20;
        raysCast = 0;
        distanceSqSum = 0;

        if (rays == null || rays.length != raysToCast) {
            rnd.setSeed(1);
            rays = new Vec3d[raysToCast];
            for (int i = 0; i < raysToCast; i++) {
                double vx = 0, vy = 0, vz = 0;
                // avoid normalizing a vector of 0 length (impossible), or tiny length
                // (numerically unstable)
                double squareDist;
                while ((squareDist = vx * vx + vy * vy + vz * vz) < 0.01 || squareDist > 1) {
                    vx = 2.0 * (rnd.nextDouble() - 0.5);
                    vy = 2.0 * (rnd.nextDouble() - 0.5);
                    vz = 2.0 * (rnd.nextDouble() - 0.5);
                }
                rays[i] = new Vec3d(vx, vy, vz).normalize();
            }
        }

        if (scanned == null) {
            scanned = new HashSet<BlockPos>(raysToCast + 1, 1);
        }

        finalProgress = 1;
        startNearness = 60;
        maxRange = 100;

        directScore = 0;
        indirectScore = 0;
        THRESHOLD_SCORE = 10000;
        THRESHOLD_INDIRECT_SCORE = 10;

        scanned.clear();
    }

    private Vec3d getRay(int index) {
        return rays[index];
    }

    private Optional<Vec3d> getNormalVector(MovingObjectPosition result) {
        if (result == null) {
            return Optional.empty();
        }

        switch (result.sideHit) {
        case 0: // BOTTOM
            return Optional.of(new Vec3d(0, -1, 0));
        case 1: // TOP
            return Optional.of(new Vec3d(0, 1, 0));
        case 2: // NORTH
            return Optional.of(new Vec3d(0, 0, 1));
        case 3: // SOUTH
            return Optional.of(new Vec3d(0, 0, -1));
        case 4: // WEST
            return Optional.of(new Vec3d(-1, 0, 0));
        case 5: // EAST
            return Optional.of(new Vec3d(1, 0, 0));
        default:
            return Optional.empty();
        }
    }

    @Override
    protected boolean doRoutine() {
        for (int ops = 0; ops < opspercall && raysCast < raysToCast; ops++) {
            castRay(getRay(raysCast));

            raysCast++;
        }

        if (raysCast >= raysToCast) {
            progress = 1;

            pipeline.setValue(".is_outdoors", directScore > THRESHOLD_SCORE ? 1 : 0);
            pipeline.setValue(".__score", directScore);
            pipeline.setValue(".__indirect_score", indirectScore);
            pipeline.setValue("._is_near_surface_own", indirectScore > THRESHOLD_INDIRECT_SCORE ? 1 : 0);
            pipeline.setValue(".spaciousness", distanceSqSum);
        }

        return true;
    }

    private int calculateWeight(int dx, int dy, int dz, int maxRange) {
        int distanceSquared = dx * dx + dy * dy + dz * dz;
        float distanceScale = 1f; // a block this^2 far away will have a weight of 1/2
        float weight = MathHelper.clamp_float(1f / (1 + (distanceSquared / distanceScale)), 0f, 1f);
        return (int) (weight * 1000f);
    }

    public static MovingObjectPosition rayTraceNonSolid(Vec3d start, Vec3d end) {
        World w = Minecraft.getMinecraft().theWorld;

        // Vec3d is immutable in 1.12.2 but Vec3 is not in 1.7.10
        MovingObjectPosition result = w.func_147447_a(start.clone(), end.clone(), true, false, true); // rayTraceBlocks

        Vec3d delta = end.subtract(start);
        double infNorm = Math.max(Math.abs(delta.xCoord), Math.max(Math.abs(delta.yCoord), Math.abs(delta.zCoord)));
        delta = delta.scale(0.01 / infNorm);

        while (result != null && result.typeOfHit == MovingObjectType.BLOCK
                && ScanAir.isTransparentToSound(
                        MAtUtil.getBlockAt(new BlockPos(result.blockX, result.blockY, result.blockZ)),
                        MAtUtil.getMetaAt(new BlockPos(result.hitVec), -1), w, new BlockPos(result.hitVec), true)) {
            result = w.func_147447_a(delta.add(result.hitVec), end, true, true, true); // rayTraceBlocks
        }
        return result;
    }

    private boolean scanNearRayHit(MovingObjectPosition result, int scanDistance, boolean direct) {
        World w = Minecraft.getMinecraft().theWorld;

        BlockPos hitBlock = new BlockPos(result.blockX, result.blockY, result.blockZ);

        distanceSqSum += hitBlock.distanceSq(center.xCoord, center.yCoord, center.zCoord);

        Block[] blockBuf = new Block[1];
        int[] metaBuf = new int[1];
        int[] pos = new int[3];

        boolean centerSolid = false;
        boolean foundSky = false;

        int airPenalty = 0;
        int solidPenalty = 55;

        for (int scanDir = 0; scanDir < 6; scanDir++) {
            int nearness = startNearness;
            for (int offset = 0; offset <= scanDistance; offset++) {
                if (offset == 0 && scanDir != 0) {
                    continue;
                }

                int scanAxis = scanDir >= 3 ? scanDir - 3 : scanDir;

                pos[0] = hitBlock.getX();
                pos[1] = hitBlock.getY();
                pos[2] = hitBlock.getZ();
                pos[scanAxis] += offset * (scanDir >= 3 ? -1 : 1);

                BlockPos blockPos = new BlockPos(pos[0], pos[1], pos[2]);

                if (!scanned.contains(blockPos)) {
                    scanned.add(blockPos);

                    int dx = startX - pos[0];
                    int dy = startY - pos[1];
                    int dz = startZ - pos[2];

                    if (direct) {
                        ((ScannerModule) pipeline).inputAndReturnBlockMeta(pos[0], pos[1], pos[2],
                                calculateWeight(dx, dy, dz, maxRange), blockBuf, metaBuf);
                    } else {
                        blockBuf[0] = MAtUtil.getBlockAt(new BlockPos(pos[0], pos[1], pos[2]));
                        metaBuf[0] = MAtUtil.getMetaAt(new BlockPos(pos[0], pos[1], pos[2]), -1);
                    }

                    Block block = blockBuf[0];

                    boolean solid = block.getCollisionBoundingBoxFromPool(w, blockPos.getX(), blockPos.getY(),
                            blockPos.getZ()) != null && !(block instanceof BlockLeaves);

                    if (solid && offset == 0 && scanDir == 0) {
                        centerSolid = true;
                    } else if (centerSolid && scanDir != 0 && offset == 1) {
                        nearness -= centerSolid ? solidPenalty : airPenalty;
                    }

                    nearness -= solid ? solidPenalty : airPenalty;

                    if (nearness > 0 && block instanceof BlockAir && MAtUtil.canSeeSky(blockPos)) {
                        int hitScore = nearness;
                        if (direct) {
                            directScore += hitScore;
                        } else {
                            indirectScore += 1;
                        }

                        foundSky = true;
                    }
                }
            }
        }
        return foundSky;
    }

    private void castRay(Vec3d dir) {
        Vec3d end = center.add(dir.scale(maxRange));
        MovingObjectPosition result = rayTraceNonSolid(center, end);

        if (result != null && result.typeOfHit == MovingObjectType.BLOCK) { // ray hit a solid block
            boolean foundSky = scanNearRayHit(result, 2, true);

            if (!foundSky) {
                Vec3d normal = getNormalVector(result).orElse(Vec3d.ZERO);

                if (!normal.equals(Vec3d.ZERO)) {
                    Vec3d otherSide = normal.scale(-1.1).add(result.hitVec);

                    MovingObjectPosition continuedResult = rayTraceNonSolid(otherSide, end);

                    if (continuedResult != null) {
                        scanNearRayHit(continuedResult, 1, false);
                    } else if (dir.yCoord > 0) {
                        indirectScore += 7;
                    }
                }

            }
        } else { // ray didn't hit anything
            distanceSqSum += maxRange * maxRange;

            if (dir.yCoord > 0) { // and it's because we hit the sky, probably
                Vec3d rayEnd = center.add(dir.scale(maxRange));
                BlockPos rayEndBlockPos = new BlockPos(MathHelper.floor_double(rayEnd.xCoord),
                        MathHelper.floor_double(rayEnd.yCoord), MathHelper.floor_double(rayEnd.zCoord));

                if (MAtUtil.canSeeSky(rayEndBlockPos)) {
                    directScore += startNearness * 13;
                }
            }
        }
    }

}
