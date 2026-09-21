package eu.ha3.matmos.game.user;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.lwjgl.opengl.GL11;

import eu.ha3.matmos.Matmos;
import eu.ha3.matmos.core.sheet.Sheet;
import eu.ha3.matmos.data.modules.ModuleRegistry;
import eu.ha3.matmos.data.scanners.Progress;
import eu.ha3.matmos.data.scanners.ScannerModule;
import eu.ha3.matmos.util.math.Numbers;
import eu.ha3.mc.haddon.supporting.SupportsFrameEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.ScaledResolution;
import eu.ha3.mc.abstraction.util.ATextFormatting;

public class VisualDebugger implements SupportsFrameEvents {
    private final Matmos mod;
    private final ModuleRegistry dataGatherer;

    private DebugMode mode = DebugMode.NONE;
    private VisualExpansionDebugging ed;
    private String scanDebug;
    private boolean deltas = false;

    public VisualDebugger(Matmos mod, ModuleRegistry dataGatherer) {
        this.mod = mod;
        this.dataGatherer = dataGatherer;
    }

    public List<String> obtainSheetNamesCopy() {
        List<String> sheetNames = new ArrayList<>(dataGatherer.getData().getSheetNames());
        Collections.sort(sheetNames);
        return sheetNames;
    }

    public void debugModeExpansion(VisualExpansionDebugging ed) {
        this.ed = ed;
        mode = DebugMode.EXPANSION;
    }

    public void debugModeScan(String name) {
        scanDebug = name;
        mode = DebugMode.SCAN;
    }

    public void noDebug() {
        mode = DebugMode.NONE;
    }

    @Override
    public void onFrame(float semi) {
        if (mod.isDebugMode()) {
            mod.util().prepareDrawString();
            mod.util().drawString(ATextFormatting.GRAY.toString() + mod.getLag().getMilliseconds() + "ms", 1f, 1f, 0,
                    0, '3', 0, 0, 0, 0, true);
        }

        if (mode == DebugMode.NONE) {
            return;
        }

        if (mode == DebugMode.EXPANSION && semi >= 0f && mod.util().getCurrentScreen() != null
                && !(mod.util().getCurrentScreen() instanceof GuiChat)) {
            return;
        }

        switch (mode) {
        case SCAN:
            debugScan();
            break;
        case EXPANSION:
            ed.onFrame(semi);
            break;
        default:
            break;
        }

    }

    private void debugScan() {
        debugScanWithSheet(dataGatherer.getData().getSheet(scanDebug), false);
        if (deltas && dataGatherer.getData().getSheetNames().contains(scanDebug + "_delta")) {
            debugScanWithSheet(dataGatherer.getData().getSheet(scanDebug + "_delta"), true);
        }
    }

    private void debugScanWithSheet(final Sheet sheet, boolean isDeltaPass) {
        Minecraft mc = Minecraft.getMinecraft();
        int fac = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight).getScaleFactor();
        float scale = 2f / fac;
        GL11.glPushMatrix();
        GL11.glScalef(scale, scale, 1.0F);

        final int ALL = 50;

        List<String> sort = new ArrayList<>(sheet.keySet());

        if (scanDebug.startsWith("scan_")) {
            try {
                Collections.sort(sort, (String o1, String o2) -> {
                    boolean o1IsVar = o1.startsWith(".");
                    boolean o2IsVar = o2.startsWith(".");
                    if (o1IsVar && o2IsVar) {
                        return o1.compareTo(o2);
                    } else if (o1IsVar) { // variables are on top
                        return 1;
                    } else if (o2IsVar) {
                        return -1;
                    }

                    Long l1 = Numbers.toLong(sheet.get(o1));
                    Long l2 = Numbers.toLong(sheet.get(o2));

                    if (l1 == null && l2 == null) {
                        return o1.compareTo(o2);
                    } else if (l1 == null) {
                        return -1;
                    } else if (l2 == null) {
                        return 1;
                    }

                    if (l1 > l2) {
                        return 1;
                    } else if (l1 < l2) {
                        return -1;
                    } else {
                        return o1.compareTo(o2);
                    }
                });
                Collections.reverse(sort);
            } catch (Exception e) {
                e.printStackTrace(System.out);
            }
        }

        int total = 0;
        for (String index : sort) {
            if (!index.contains("^")) {
                Long l = Numbers.toLong(sheet.get(index));
                if (l != null) {
                    total = total + (int) (long) l;
                }
            }
        }

        FontRenderer fontRenderer = mc.fontRenderer;

        int lineNumber = 0;

        if (scanDebug.startsWith("scan_large")) {
            Progress progressObject = dataGatherer.getLargeScanProgress();
            float progress = (float) progressObject.getProgress_Current() / progressObject.getProgress_Total();

            fontRenderer.drawStringWithShadow("Scan [" + mc.theWorld.getHeight() + "]: "
                    + StringUtils.repeat("|", (int) (100 * progress)) + " (" + (int) (progress * 100) + "%)", 20,
                    2 + 9 * lineNumber, 0xFFFFCC);
        }

        lineNumber = lineNumber + 1;

        int leftAlign = 2 + (isDeltaPass ? 300 : 0);

        for (String index : sort) {
            if (lineNumber <= 100 && !index.contains("^")) {
                if (scanDebug.startsWith("scan_") || scanDebug.equals("block_contact")) {
                    if (index.startsWith(".")) {
                        fontRenderer
                                .drawStringWithShadow(
                                        ATextFormatting.AQUA + index + ": " + ATextFormatting.YELLOW
                                                + sheet.get(index) + ATextFormatting.RESET,
                                        leftAlign, 2 + 9 * lineNumber, 0xFFFFFF);
                        lineNumber += 1;
                    } else {
                        Long count = Numbers.toLong(sheet.get(index));
                        if (count != null) {
                            if (count > 0) {
                                float scalar = (float) count / total;
                                String percentage = !scanDebug.endsWith(ScannerModule.THOUSAND_SUFFIX)
                                        ? Float.toString(Math.round(scalar * 1000f) / 10f)
                                        : Integer.toString(Math.round(scalar * 100f));

                                if (percentage.equals("0.0")) {
                                    percentage = "0";
                                }

                                int fill = Math.round(scalar * ALL * 2 /* * 2 */);
                                int superFill = 0;

                                if (fill > ALL * 2) {
                                    fill = ALL * 2;
                                }

                                if (fill > ALL) {
                                    superFill = fill - ALL;
                                }

                                String bars = "";
                                if (superFill > 0) {
                                    bars += ATextFormatting.YELLOW + StringUtils.repeat("|", superFill);
                                }

                                bars += ATextFormatting.RESET + StringUtils.repeat("|", fill - superFill * 2);

                                if (index.startsWith("minecraft:")) {
                                    index = index.substring(10);
                                }

                                fontRenderer.drawStringWithShadow(
                                        bars + (fill == ALL * 2
                                                ? ATextFormatting.YELLOW + "++" + ATextFormatting.RESET
                                                : "") + " (" + count + ", " + percentage + "%) " + index,
                                        leftAlign, 2 + 9 * lineNumber, 0xFFFFFF);
                                lineNumber = lineNumber + 1;
                            }
                        }
                    }
                } else if (scanDebug.startsWith("detect_")) {
                    String val = sheet.get(index);
                    if (!val.equals("0") && !val.equals(Integer.toString(Integer.MAX_VALUE))) {
                        fontRenderer.drawStringWithShadow(String.format("%s (%s): %s", index, index, val), leftAlign,
                                2 + 9 * lineNumber, 0xFFFFFF);
                        lineNumber = lineNumber + 1;
                    }
                } else {
                    String val = sheet.get(index);
                    int color = 0xFFFFFF;

                    if (val.equals("0")) {
                        color = 0xFF0000;
                    } else if (val.equals("1")) {
                        color = 0x0099FF;
                    }

                    fontRenderer.drawStringWithShadow(index + ": " + val, leftAlign, 2 + 9 * lineNumber, color);
                    lineNumber = lineNumber + 1;
                }
            }
        }
        GL11.glPopMatrix();
    }

    public void toggleDeltas() {
        deltas = !deltas;
    }

    private enum DebugMode {
        NONE, SCAN, EXPANSION;
    }
}
