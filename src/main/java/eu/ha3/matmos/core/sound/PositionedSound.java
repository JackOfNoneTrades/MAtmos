package eu.ha3.matmos.core.sound;

import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.util.ResourceLocation;

/**
 * Same as PositionedSoundRecord, just exposes the private constructor
 * 
 * @author makamys
 *
 */

public class PositionedSound extends PositionedSoundRecord {
    public PositionedSound(ResourceLocation loc, float volume, float pitch, boolean repeat, int repeatDelay,
            ISound.AttenuationType attenuationType, float x, float y, float z) {
        super(loc, volume, pitch, x, y, z);
        this.repeat = repeat;
        this.field_147665_h = repeatDelay; // repeatDelay
        this.field_147666_i = attenuationType; // attenuationType
    }
}
