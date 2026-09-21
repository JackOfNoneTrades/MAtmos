package eu.ha3.matmos.data.scanners;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

import eu.ha3.matmos.Matmos;
import eu.ha3.matmos.core.sheet.DataPackage;
import eu.ha3.matmos.data.modules.AbstractThingCountModule;
import eu.ha3.matmos.data.modules.BlockCountModule;
import eu.ha3.matmos.data.modules.ExternalStringCountModule;
import eu.ha3.matmos.data.modules.Module;
import eu.ha3.matmos.data.modules.PassOnceModule;
import eu.ha3.matmos.data.modules.ThousandStringCountModule;
import eu.ha3.matmos.data.modules.VirtualCountModule;
import eu.ha3.matmos.data.modules.VirtualModuleProcessor;
import eu.ha3.matmos.util.BlockPos;
import eu.ha3.matmos.util.IDontKnowHowToCode;
import eu.ha3.matmos.util.MAtUtil;
import eu.ha3.matmos.util.math.MAtMutableBlockPos;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;

public class ScannerModule implements PassOnceModule, ScanOperations, Progress {
    public static final String THOUSAND_SUFFIX = "_p1k";
    public static final String WEIGHTED_SUFFIX = "_w";
    public static final String ABOVE_SUFFIX = "_above";
    public static final String BELOW_SUFFIX = "_below";

    private static final int WORLD_LOADING_DURATION = 100;

    private final String passOnceName;
    private final Set<Submodule> requiredSubmodules;
    private final int movement;
    private final int passivePulse;
    private final int pulse;

    private final int xS;
    private final int yS;
    private final int zS;
    private final int blocksPerCall;

    private final AbstractThingCountModule base;
    private final BlockCountModule weighted;
    private final AbstractThingCountModule thousand;
    private final BlockCountModule above;
    private final BlockCountModule below;

    private final Set<String> subModules = new HashSet<>();

    private int ticksSinceBoot;
    private boolean firstScan;
    private boolean workInProgress;

    private int lastScanTime = -1;

    private int dimension = Integer.MIN_VALUE;
    private int xx = Integer.MIN_VALUE;
    private int yy = Integer.MIN_VALUE;
    private int zz = Integer.MIN_VALUE;

    private final Scan scanner;

    public static enum Submodule {
        BASE, THOUSAND, WEIGHTED, ABOVE, BELOW
    }

    private Module initSubmodule(Submodule sm, String baseName, DataPackage data) {
        boolean useExternalStringCountModule = false;

        if (sm != Submodule.BASE && !requiredSubmodules.contains(sm)) {
            return null;
        }
        Module result = null;
        String submoduleName = baseName;

        switch (sm) {
        case BASE:
            if (useExternalStringCountModule) {
                result = new ExternalStringCountModule(data, baseName, true);
            } else {
                result = new BlockCountModule(data, baseName, true,
                        (VirtualCountModule<Pair<Block, Integer>>) thousand);
            }
            break;
        case THOUSAND:
            submoduleName = baseName + THOUSAND_SUFFIX;
            if (useExternalStringCountModule) {
                result = new ThousandStringCountModule(data, submoduleName, true);
            } else {
                result = new VirtualCountModule<Pair<Block, Integer>>(data, submoduleName, true);
            }

            break;
        case WEIGHTED:
            submoduleName = baseName + WEIGHTED_SUFFIX;
            result = new BlockCountModule(data, submoduleName, true, null);
            break;
        case ABOVE:
            submoduleName = baseName + ABOVE_SUFFIX;
            result = new BlockCountModule(data, submoduleName, true, null);
            break;
        case BELOW:
            submoduleName = baseName + BELOW_SUFFIX;
            result = new BlockCountModule(data, submoduleName, true, null);
            break;
        }

        if (result != null) {
            subModules.add(submoduleName);
            data.getSheet(submoduleName).setDefaultValue("0");
        }

        return result;
    }

    /**
     * Movement: Requires the player to move to another block to trigger a new scan.
     * If movement is zero, no scan until the player moves. If movement is negative,
     * always scan even if the player hasn't moved. Passive pulse: if this many
     * pulses have elapsed since the last scan and the player has moved, the scan
     * will run even if the movement condition hasn't been satisfied.
     */

    private ScannerModule(Class<? extends Scan> scannerClass, Object scannerArgument, boolean hasScannerArgument,
            DataPackage data, String passOnceName, String baseName, List<Submodule> requiredSubmodules, int movement,
            int passivePulse, int pulse, int xS, int yS, int zS, int blocksPerCall) {
        this.passOnceName = passOnceName;
        this.requiredSubmodules = new HashSet<Submodule>(requiredSubmodules);
        this.movement = movement;
        this.passivePulse = passivePulse;
        this.pulse = pulse;

        this.xS = xS;
        this.yS = yS;
        this.zS = zS;
        this.blocksPerCall = blocksPerCall;

        thousand = (AbstractThingCountModule) initSubmodule(Submodule.THOUSAND, baseName, data);
        weighted = (BlockCountModule) initSubmodule(Submodule.WEIGHTED, baseName, data);
        base = (AbstractThingCountModule) initSubmodule(Submodule.BASE, baseName, data);
        above = (BlockCountModule) initSubmodule(Submodule.ABOVE, baseName, data);
        below = (BlockCountModule) initSubmodule(Submodule.BELOW, baseName, data);

        Scan theScanner = null;

        try {
            if (hasScannerArgument) {
                theScanner = (Scan) scannerClass.getConstructor(Object.class).newInstance(scannerArgument);
            } else {
                theScanner = (Scan) scannerClass.newInstance();
            }
        } catch (IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException
                | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        scanner = theScanner;
        scanner.setPipeline(this);

        ticksSinceBoot = 0;
        firstScan = true;
    }

    /***
     * Constructor used to pass an argument to the to-be-instantiated scanner object
     */
    public ScannerModule(Class<? extends Scan> scannerClass, Object scannerArgument, DataPackage data,
            String passOnceName, String baseName, List<Submodule> requiredSubmodules, int movement, int passivePulse,
            int pulse, int xS, int yS, int zS, int blocksPerCall) {
        this(scannerClass, scannerArgument, true, data, passOnceName, baseName, requiredSubmodules, movement,
                passivePulse, pulse, xS, yS, zS, blocksPerCall);
    }

    /*** Normal constructor */
    public ScannerModule(Class<? extends Scan> scannerClass, DataPackage data, String passOnceName, String baseName,
            List<Submodule> requiredSubmodules, int movement, int passivePulse, int pulse, int xS, int yS, int zS,
            int blocksPerCall) {
        this(scannerClass, null, false, data, passOnceName, baseName, requiredSubmodules, movement, passivePulse, pulse,
                xS, yS, zS, blocksPerCall);
    }

    @Override
    public String getName() {
        return passOnceName;
    }

    @Override
    public Set<String> getSubModules() {
        return subModules;
    }

    @Override
    public void process() {
        if (tryToReboot()) {
            Matmos.DEVLOGGER.info("Detected large movement or teleportation. Rebooted module " + getName());
            return;
        }

        if (ticksSinceBoot < WORLD_LOADING_DURATION) {
            ticksSinceBoot = ticksSinceBoot + 1;
            return;
        }

        tryToBoot();

        if (workInProgress) {
            scanner.routine();
        }
        ticksSinceBoot = ticksSinceBoot + 1;
    }

    private boolean tryToReboot() {
        int x = MAtUtil.getPlayerX();
        int y = MAtUtil.clampToBounds(MAtUtil.getPlayerY());
        int z = MAtUtil.getPlayerZ();

        if (Minecraft.getMinecraft().thePlayer.dimension != dimension) {
            reboot();
            return true;
        }

        int max = Math.max(Math.abs(xx - x), Math.abs(yy - y));
        max = Math.max(max, Math.abs(zz - z));

        if (max > 128) {
            reboot();
            return true;
        }

        return false;
    }

    private void reboot() {
        scanner.stopScan();
        workInProgress = false;

        ticksSinceBoot = 0;
        firstScan = true;

        dimension = Minecraft.getMinecraft().thePlayer.dimension;
        xx = MAtUtil.getPlayerX();
        yy = MAtUtil.clampToBounds(MAtUtil.getPlayerY());
        zz = MAtUtil.getPlayerZ();
    }

    private void tryToBoot() {
        if (workInProgress) {
            return;
        }

        if (ticksSinceBoot % pulse == 0) {
            boolean go = false;

            if (firstScan) {
                firstScan = false;

                go = true;
            } else if (movement >= 0) {
                int x = MAtUtil.getPlayerX();
                int y = MAtUtil.clampToBounds(MAtUtil.getPlayerY());
                int z = MAtUtil.getPlayerZ();

                int max = Math.max(Math.abs(xx - x), Math.abs(yy - y));
                max = Math.max(max, Math.abs(zz - z));

                go = max > movement || (passivePulse >= 0 && max > 0 && lastScanTime != -1
                        && (ticksSinceBoot - lastScanTime) / pulse > passivePulse);
            } else {
                go = true;
            }

            if (go) {
                workInProgress = true;
                lastScanTime = ticksSinceBoot;

                xx = MAtUtil.getPlayerX();
                yy = MAtUtil.clampToBounds(MAtUtil.getPlayerY());
                zz = MAtUtil.getPlayerZ();

                scanner.startScan(xx, yy, zz, xS, yS, zS, blocksPerCall);
            }
        }
    }

    @Override
    public void input(int x, int y, int z) {
        inputAndReturnBlockMeta(x, y, z, 1, null, null);
    }

    @Override
    public void input(int x, int y, int z, int weight) {
        inputAndReturnBlockMeta(x, y, z, weight, null, null);
    }

    public void inputAndReturnBlockMeta(int x, int y, int z, Block[] blockOut, int[] metaOut) {
        inputAndReturnBlockMeta(x, y, z, 1, blockOut, metaOut);
    }

    /** Not sure if this optimization is necessary */
    public void inputAndReturnBlockMeta(int x, int y, int z, int weight, Block[] blockOut, int[] metaOut) {
        Block block = MAtUtil.getBlockAt(new BlockPos(x, y, z));
        int meta = MAtUtil.getMetaAt(new BlockPos(x, y, z), -1);

        if (base instanceof BlockCountModule) {
            base.increment(Pair.of(block, meta));
        } else if (base instanceof ExternalStringCountModule) {
            String name = MAtUtil.nameOf(block);
            base.increment(name);
            base.increment(MAtUtil.asPowerMeta(block, meta));
            if (thousand != null) {
                thousand.increment(name);
            }
        }

        if (weighted != null) {
            weighted.increment(Pair.of(block, meta), weight);
        } else if (weight != 1) {
            IDontKnowHowToCode.warnOnce("Module " + getName()
                    + " doesn't have a weighted counter, but the scanner tried to input a block with a weight.");
        }

        // yy is the block the player player's head is in
        if (above != null && y >= yy) {
            above.increment(Pair.of(block, meta));
        } else if (below != null && y < yy) {
            below.increment(Pair.of(block, meta));
        }

        if (blockOut != null) {
            blockOut[0] = block;
        }
        if (metaOut != null) {
            metaOut[0] = meta;
        }
    }

    @Override
    public void begin() {
    }

    @Override
    public void finish() {
        base.apply();
        if (thousand != null) {
            thousand.apply();
        }
        if (weighted != null) {
            weighted.apply();
        }
        if (above != null) {
            above.apply();
        }
        if (below != null) {
            below.apply();
        }
        workInProgress = false;
    }

    @Override
    public int getProgress_Current() {
        return scanner.getProgress_Current();
    }

    @Override
    public int getProgress_Total() {
        return scanner.getProgress_Total();
    }

    @Override
    public void setValue(String key, int value) {
        if (!key.startsWith(".")) {
            Matmos.LOGGER
                    .error("Illegal scanner sheet key name: \"" + key + "\". Key must start with a '.' character.");
        } else {
            base.setValue(key, value);
        }
    }
}
