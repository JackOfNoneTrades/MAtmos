package eu.ha3.matmos.core.sound;

import net.minecraft.client.audio.ISound;
import net.minecraft.util.ResourceLocation;

public class NoAttenuationSound extends PositionedSound {
    public NoAttenuationSound(ResourceLocation loc, float volume, float pitch, float x, float y, float z) {
        super(loc, volume, pitch, false, 0, ISound.AttenuationType.NONE, x, y, z);
    }
}
