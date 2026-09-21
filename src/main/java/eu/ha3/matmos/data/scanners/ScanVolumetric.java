package eu.ha3.matmos.data.scanners;

import net.minecraft.client.Minecraft;

/* x-placeholder */

public class ScanVolumetric extends Scan {
    private int xstart;
    private int ystart;
    private int zstart;

    private int xsize;
    private int ysize;
    private int zsize;

    //

    private int xx;
    private int yy;
    private int zz;

    protected void initScan(int x, int y, int z, int xsizeIn, int ysizeIn, int zsizeIn, int opspercallIn) // throws
                                                                                                          // MAtScannerTooLargeException
    {
        int worldHeight = Minecraft.getMinecraft().theWorld.getHeight();

        if (ysizeIn > worldHeight) {
            ysizeIn = worldHeight;
        }

        this.xsize = xsizeIn;
        this.ysize = ysizeIn;
        this.zsize = zsizeIn;

        y = y - this.ysize / 2;

        if (y < 0) {
            y = 0;
        } else if (y > worldHeight - this.ysize) {
            y = worldHeight - this.ysize;
        }

        this.xstart = x - this.xsize / 2;
        this.ystart = y; // ((y - ysize / 2)) already done before
        this.zstart = z - this.zsize / 2;

        this.finalProgress = this.xsize * this.ysize * this.zsize;

        this.xx = 0;
        this.yy = 0;
        this.zz = 0;
    }

    protected boolean doRoutine() {
        long ops = 0;
        while (ops < this.opspercall && this.progress < this.finalProgress) {
            this.pipeline.input(this.xstart + this.xx, this.ystart + this.yy, this.zstart + this.zz);

            this.xx = (this.xx + 1) % this.xsize;
            if (this.xx == 0) {
                this.zz = (this.zz + 1) % this.zsize;
                if (this.zz == 0) {
                    this.yy = this.yy + 1;
                    if (this.yy >= this.ysize && this.progress != this.finalProgress - 1) {
                        System.err.println("LOGIC ERROR");
                    }
                }
            }

            ops++;
            this.progress++;

        }
        return true;
    }

}
