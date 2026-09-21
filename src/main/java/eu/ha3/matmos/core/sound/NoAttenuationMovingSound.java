package eu.ha3.matmos.core.sound;

import eu.ha3.matmos.Matmos;
import eu.ha3.matmos.core.SystemClock;
import eu.ha3.matmos.util.math.HelperFadeCalculator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.ITickableSound;
import net.minecraft.client.audio.MovingSound;
import net.minecraft.client.audio.SoundCategory;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

public class NoAttenuationMovingSound extends MovingSound implements StreamingSound {
    private boolean usesPause;
    private final HelperFadeCalculator helper = new HelperFadeCalculator(new SystemClock());
    private float desiredVolume;
    private float desiredPitch;
    private float volumeMod;
    private boolean underwater;

    private boolean notYetPlayed = true;

    protected NoAttenuationMovingSound(ResourceLocation myResource, float volume, float pitch, boolean isLooping,
            boolean usesPause, boolean underwater) {
        super(myResource);

        field_147666_i = ISound.AttenuationType.NONE; // attenuationType
        repeat = isLooping;
        field_147665_h = 0; // repeatDelay

        desiredVolume = volume;
        desiredPitch = pitch;
        this.volume = 0.00001f;
        this.field_147663_c = pitch; // pitch

        this.usesPause = usesPause;
        this.underwater = underwater;
    }

    public NoAttenuationMovingSound copy() {
        return new NoAttenuationMovingSound(getPositionedSoundLocation(), desiredVolume, desiredPitch, repeat, usesPause,
                underwater);
    }

    @Override
    public void update() {
        Entity e = Minecraft.getMinecraft().thePlayer;

        xPosF = (float) e.posX;
        yPosF = (float) e.posY;
        zPosF = (float) e.posZ;

        volume = helper.calculateFadeFactor() * desiredVolume * volumeMod;

        if (volume < 0.01f && usesPause) {
            field_147663_c = 0f;
        }

        if (volume > 0.01f && usesPause) {
            field_147663_c = desiredPitch;
        }

        if (helper.isDoneFadingOut() && repeat && !isDonePlaying()) {
            dispose();
        }
    }

    @Override
    public void play(float fadeIn) {
        setVolume(desiredVolume, fadeIn);
    }

    @Override
    public void stop(float fadeOut) {
        setVolume(0, fadeOut);
    }

    public void setVolume(float volume, float fadeTime) {
        helper.fadeTo(volume / desiredVolume, (long) (fadeTime * 1000));
    }

    @Override
    public void applyVolume(float volumeMod) {
        this.volumeMod = volumeMod;
    }

    public float getTargetVolume() {
        return helper.getTargetFade() * desiredVolume;
    }

    @Override
    public void dispose() {
        donePlaying = true;
    }

    @Override
    public void interrupt() {
        donePlaying = true;
    }

    /*
     * @Override public Sound getSound() { notYetPlayed = false; return
     * super.getSound(); }
     */

    public boolean popNotYetPlayed() {
        boolean wasNotYetPlayed = notYetPlayed;
        if (notYetPlayed) {
            notYetPlayed = false;
        }
        return wasNotYetPlayed;
    }

    @Override
    public ITickableSound asTickable() {
        return (ITickableSound) this;
    }

    @Override
    public SoundCategory getCategory() {
        return underwater ? SoundCategory.MASTER : SoundCategory.AMBIENT;
    }

}
