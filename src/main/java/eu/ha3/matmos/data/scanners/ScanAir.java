package eu.ha3.matmos.data.scanners;

import eu.ha3.matmos.core.sheet.Sheet;
import eu.ha3.matmos.util.BlockPos;
import eu.ha3.matmos.util.ByteQueue;
import eu.ha3.matmos.util.IDontKnowHowToCode;
import eu.ha3.matmos.util.MAtUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.BlockGlass;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.BlockPane;
import net.minecraft.block.BlockStone;
import net.minecraft.client.Minecraft;
import net.minecraft.world.World;

/**
 * This scanner is used to determine whether the player should hear indoors or
 * outdoors ambiance.
 */
public class ScanAir extends Scan {

    private int scanDistance, scanSize;

    private int startX, startY, startZ;

    private int xx, yy, zz;

    private boolean[] visited;
    ByteQueue floodQueue;
    ByteQueue solidQueue;
    ByteQueue solidInfoQueue;
    ByteQueue finalQueue;

    int skylitXMin, skylitXMax, skylitYMin, skylitYMax, skylitZMin, skylitZMax;

    private int score;

    private Sheet raycastSheet;

    enum Stage {
        AIR1, SOLID, AIR2, FINISH
    };

    Stage stage = Stage.AIR1;

    // these are meant to be final, but for ease of development are non-final for
    // now(?)
    private int AIR_COST, SOLID_COST, PANE_COST;
    byte START_NEARNESS;
    private int SCORE_THRESHOLD = 200;

    public ScanAir(Object raycastSheetObj) {
        raycastSheet = (Sheet) raycastSheetObj;
    }

    @Override
    void initScan(int x, int y, int z, int xsizeIn, int ysizeIn, int zsizeIn, int opspercallIn) {
        startX = x;
        startY = y;
        startZ = z;

        xx = 0;
        yy = 0;
        zz = 0;

        score = 0;

        int newScanDistance = Math.min(Math.min((xsizeIn - 1) / 2, (ysizeIn - 1) / 2), (zsizeIn - 1) / 2);

        boolean reallocate = newScanDistance != scanDistance;
        scanDistance = newScanDistance;

        scanSize = scanDistance * 2 + 1;

        if (reallocate || visited == null) {
            visited = new boolean[scanSize * scanSize * scanSize];
        } else {
            for (int i = 0; i < visited.length; i++) {
                visited[i] = false;
            }
        }

        if (reallocate || floodQueue == null) {
            floodQueue = new ByteQueue(scanDistance * scanDistance * 6 * 4);
        } else {
            floodQueue.clear();
        }
        if (reallocate || solidQueue == null) {
            solidQueue = new ByteQueue(scanDistance * scanDistance * 6 * 4);
        } else {
            solidQueue.clear();
        }
        if (reallocate || solidInfoQueue == null) {
            solidInfoQueue = new ByteQueue(scanDistance * scanDistance * 6);
        } else {
            solidInfoQueue.clear();
        }
        if (reallocate || finalQueue == null) {
            finalQueue = new ByteQueue(scanDistance * scanDistance * 6 * 4);
        } else {
            finalQueue.clear();
        }

        floodQueue.push4((byte) scanDistance, (byte) scanDistance, (byte) scanDistance, START_NEARNESS);

        skylitXMin = skylitYMin = skylitZMin = Integer.MAX_VALUE;
        skylitXMax = skylitYMax = skylitZMax = Integer.MIN_VALUE;

        stage = Stage.AIR1;

        AIR_COST = 5;
        SOLID_COST = 30;
        PANE_COST = 10;
        START_NEARNESS = 80;

        finalProgress = 1; // TODO proper progress tracking. for now it just sets it to 1 when finished
    }

    /**
     * It works by finding the nearest skylit block to the player. The algorithm's
     * phases in order are: AIR1: It performs a breadth-first flood fill starting
     * from the player, restricted to air blocks within the region of blocks at a
     * maximum taxicab distance of 15 (or whatever the parameter is). SOLID: It
     * expands one block outwards (still staying inside the region), into solid
     * blocks. AIR2: Then it continues expanding while restricted to air blocks.
     * 
     * We keep track of the distance of each block we step onto, subtracting 1 for
     * air blocks and 4(?) for solid blocks. When we reach a skylit block, we add
     * its distance to the score. The algorithm terminates if the score reaches a
     * certain value, or if we have fully scanned the area.
     * 
     * As a result of this algorithm, we obtain an evaluation score of how outdoors
     * the player is. The player hears no (outdoors) ambiance, indoors ambiance, or
     * outdoors ambiance as the score increases.
     * 
     * The reason this algorithm is used is because a traditional breadth first
     * search algorithm would need to pass over the same block multiple times to
     * give a correct result. This algorithm avoids that.
     */
    @Override
    protected boolean doRoutine() {
        int ops = 0;

        int scanSize = scanDistance * 2 + 1;

        World w = Minecraft.getMinecraft().theWorld;

        byte[] p = new byte[4];
        while (ops < opspercall && progress < finalProgress) {
            switch (stage) {
            case AIR1:
            case AIR2:
                if (floodQueue.pop4(p)) {
                    if (!(p[0] < 0 || p[0] >= scanSize || p[1] < 0 || p[1] >= scanSize || p[2] < 0
                            || p[2] >= scanSize)) {
                        int wx = startX - scanDistance + p[0];
                        int wy = startY - scanDistance + p[1];
                        int wz = startZ - scanDistance + p[2];
                        if (p[3] > 0 && MAtUtil.isWithinBounds(new BlockPos(0, wy, 0))
                                && !getVisited(p[0], p[1], p[2])) {

                            Block[] blockBuf = new Block[1];
                            int[] metaBuf = new int[1];

                            ((ScannerModule) pipeline).inputAndReturnBlockMeta(wx, wy, wz, blockBuf, metaBuf);
                            ops++;

                            Block block = blockBuf[0];
                            int meta = metaBuf[0];

                            BlockPos pos = new BlockPos(wx, wy, wz);

                            byte newN = (byte) (p[3] - AIR_COST);

                            if (isTransparentToSound(block, meta, w, pos, false)) {
                                if (w.canBlockSeeTheSky(pos.getX(), pos.getY(), pos.getZ())
                                        && block instanceof BlockAir) {
                                    score += newN;

                                    skylitXMin = Math.min(skylitXMin, wx);
                                    skylitXMax = Math.max(skylitXMax, wx);
                                    skylitYMin = Math.min(skylitYMin, wy);
                                    skylitYMax = Math.max(skylitYMax, wy);
                                    skylitZMin = Math.min(skylitZMin, wz);
                                    skylitZMax = Math.max(skylitZMax, wz);

                                    if (score > SCORE_THRESHOLD && !isTooHigh()) {
                                        stage = Stage.FINISH;
                                    }
                                    // stage = Stage.FINISH;

                                    // finalQueue.push4(p[0], p[1], p[2], p[3]);
                                }

                                // enqueue neighbors
                                floodQueue.push4((byte) (p[0] - 1), p[1], p[2], newN);
                                floodQueue.push4((byte) (p[0] + 1), p[1], p[2], newN);
                                floodQueue.push4(p[0], (byte) (p[1] - 1), p[2], newN);
                                floodQueue.push4(p[0], (byte) (p[1] + 1), p[2], newN);
                                floodQueue.push4(p[0], p[1], (byte) (p[2] - 1), newN);
                                floodQueue.push4(p[0], p[1], (byte) (p[2] + 1), newN);
                            } else {
                                (stage == Stage.AIR1 ? solidQueue : finalQueue).push4(p[0], p[1], p[2], p[3]);
                                solidInfoQueue.push(isThinBlock(block) ? (byte) 1 : (byte) 0);
                            }

                            setVisited(p[0], p[1], p[2], true);
                        }
                    }
                } else {
                    if (solidQueue.length() == 0) {
                        stage = Stage.FINISH;
                    } else {
                        stage = Stage.SOLID;
                    }
                }
                break;
            case SOLID:
                if (solidQueue.pop4(p)) {
                    boolean isThin = solidInfoQueue.pop() == 1;
                    int wy = startY - scanDistance + p[1];
                    if (MAtUtil.isWithinBounds(new BlockPos(0, wy, 0))) {

                        // enqueue neighbors
                        byte newN = (byte) (p[3] - (isThin ? PANE_COST : SOLID_COST));

                        floodQueue.push4((byte) (p[0] - 1), p[1], p[2], newN);
                        floodQueue.push4((byte) (p[0] + 1), p[1], p[2], newN);
                        floodQueue.push4(p[0], (byte) (p[1] - 1), p[2], newN);
                        floodQueue.push4(p[0], (byte) (p[1] + 1), p[2], newN);
                        floodQueue.push4(p[0], p[1], (byte) (p[2] - 1), newN);
                        floodQueue.push4(p[0], p[1], (byte) (p[2] + 1), newN);

                        setVisited(p[0], p[1], p[2], true);
                    }
                } else {
                    stage = Stage.AIR2;
                }
                break;
            case FINISH:
                // We could scan the rest of the region here (expanding from finalQueue), with
                // the ultimate
                // goal of outsourcing scanning it to us in scan_large's stead, avoiding
                // scanning it twice.
                // But I'm not sure if it's worth the trouble for the optimization gain.

                progress = 1;

                pipeline.setValue(".__score", score);
                int width = skylitXMax - skylitXMin;
                int height = skylitYMax - skylitYMin;
                int depth = skylitZMax - skylitZMin;

                pipeline.setValue(".__width", width);
                pipeline.setValue(".__height", height);
                pipeline.setValue(".__depth", depth);

                boolean tooHigh = isTooHigh();

                boolean ownJudgement = (score > SCORE_THRESHOLD && !tooHigh);
                pipeline.setValue("._is_near_surface_own", ownJudgement ? 1 : 0);
                boolean raycastJudgement = Integer.parseInt(raycastSheet.get("._is_near_surface_own")) > 0;
                pipeline.setValue(".is_near_surface", (ownJudgement | raycastJudgement) ? 1 : 0);
                break;
            }
        }

        return true;
    }

    private boolean isTooHigh() {
        int width = skylitXMax - skylitXMin;
        int height = skylitYMax - skylitYMin;
        int depth = skylitZMax - skylitZMin;

        return width * depth < height * height * 0.5f;
    }

    /**
     * Returns true if the block doesn't impede the flow of sound (i.e. it is the
     * same as air with regards to sound propagation).
     */
    public static boolean isTransparentToSound(Block block, int meta, World world, BlockPos pos, boolean hitIfLiquid) {
        try {
            boolean result = !block.canCollideCheck(meta, hitIfLiquid) || ((!hitIfLiquid || !(block instanceof BlockLiquid))
                    && (block.getCollisionBoundingBoxFromPool(world, pos.getX(), pos.getY(), pos.getZ()) == null
                            || block instanceof BlockLeaves));
            return result;
        } catch(Exception e) {
            IDontKnowHowToCode.warnOnce("Exception in isTransparentToSound, checking block of class " + block.getClass().getName() + ": " + e.getMessage());
            return false;
        }
    }

    private boolean isThinBlock(Block block) {
        return block instanceof BlockDoor || block instanceof BlockGlass || block instanceof BlockPane;
    }

    private boolean getVisited(int x, int y, int z) {
        return visited[x * scanSize * scanSize + y * scanSize + z];
    }

    private boolean setVisited(int x, int y, int z, boolean newValue) {
        visited[x * scanSize * scanSize + y * scanSize + z] = newValue;
        return true;
    }
}
