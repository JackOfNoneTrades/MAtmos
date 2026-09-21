package eu.ha3.matmos.core.sound;

import net.minecraft.client.audio.ITickableSound;
import net.minecraft.client.audio.SoundCategory;

public interface StreamingSound {
    /**
     * Fading unit is in seconds.
     *
     * @param fadeIn
     */
    void play(float fadeIn);

    /**
     * Fading unit is in seconds.
     *
     * @param fadeOut
     */
    void stop(float fadeOut);

    /**
     * Instantly applies a volume modulation to this stream, upon the initially set
     * volume.
     *
     * @param volumeMod
     */
    void applyVolume(float volumeMod);

    /**
     * Dispose of this StreamingSound. The StreamingSound should never be able to be
     * used again.
     */
    void dispose();

    void interrupt();

    ITickableSound asTickable();

    SoundCategory getCategory();
}
