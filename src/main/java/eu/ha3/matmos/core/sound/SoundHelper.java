package eu.ha3.matmos.core.sound;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import eu.ha3.matmos.Matmos;
import eu.ha3.matmos.core.StreamHandle;
import eu.ha3.matmos.core.expansion.Stable;
import eu.ha3.matmos.util.IDontKnowHowToCode;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

public class SoundHelper implements SoundCapabilities, Stable {

    private LoopingStreamedSoundManager soundManager;

    protected final Map<String, StreamHandle<NoAttenuationMovingSound>> streaming = new LinkedHashMap<>();

    private float volumeModulator;

    private boolean isInterrupt;

    private boolean isActivated;

    public SoundHelper(LoopingStreamedSoundManager soundManager) {
        this.soundManager = soundManager;
    }

    @Override
    public void playMono(String event, double xx, double yy, double zz, float volume, float pitch) {
        if (isInterrupt) {
            return;
        }

        playUnattenuatedSound(xx, yy, zz, event, volume * volumeModulator, pitch);
    }

    @Override
    public void playStereo(String event, float volume, float pitch) {
        if (isInterrupt) {
            return;
        }

        // Play the sound 2048 blocks above the player to keep support of mono sounds
        Entity e = Minecraft.getMinecraft().thePlayer;
        playUnattenuatedSound(e.posX, e.posY + 2048, e.posZ, event, volume * volumeModulator, pitch);
    }

    private void playUnattenuatedSound(double xx, double yy, double zz, String loc, float volume, float pitch) {
        NoAttenuationSound nas = new NoAttenuationSound(new ResourceLocation(loc), volume, pitch, (float) xx,
                (float) yy, (float) zz);

        Minecraft.getMinecraft().getSoundHandler().playSound(nas);

    }

    @Override
    public void registerStreaming(String customName, String path, float volume, float pitch, boolean isLooping,
            boolean usesPause, boolean underwater, SoundHelperRelay relay) {
        if (isInterrupt) {
            return;
        }

        String loc = path.replace(".ogg", "").replace('/', '.').replaceAll("[0-9]", "");
        NoAttenuationMovingSound nams = new NoAttenuationMovingSound(new ResourceLocation(loc), volume, pitch,
                isLooping, usesPause, underwater);

        streaming.put(customName, StreamHandle.of(nams, relay));
    }

    @Override
    public void playStreaming(String customName, float fadeIn) {
        if (isInterrupt) {
            return;
        }

        if (!streaming.containsKey(customName)) {
            IDontKnowHowToCode.warnOnce("Tried to play missing stream " + customName);
            return;
        }

        setVolume(customName, streaming.get(customName).getSound().getVolume(), fadeIn);
    }

    @Override
    public void stopStreaming(String customName, float fadeOut) {
        if (isInterrupt) {
            return;
        }

        if (!streaming.containsKey(customName)) {
            IDontKnowHowToCode.warnOnce("Tried to stop missing stream " + customName);
            return;
        }

        setVolume(customName, 0, fadeOut);
    }

    public void routine() {
        for (StreamHandle<NoAttenuationMovingSound> handle : streaming.values()) {
            float newVolume = handle.getLargestVolumeCommandThisTick();
            if (newVolume >= 0) {
                float fade = handle.getFadeOfLargestVolumeCommandThisTick();

                NoAttenuationMovingSound sound = handle.getSound();

                if (newVolume > 0 && (sound.getTargetVolume() != newVolume)) {
                    boolean reuse = false;
                    boolean previousIsDonePlaying = sound.isDonePlaying();

                    if (previousIsDonePlaying) {
                        sound = sound.copy();
                        handle.setSound(sound);
                    } else {
                        reuse = true;
                    }
                    sound.setVolume(newVolume, fade);
                    sound.applyVolume(volumeModulator);

                    boolean notYetPlayed = sound.popNotYetPlayed();

                    Matmos.DEBUGLOGGER.debug("playStreaming " + sound.getPositionedSoundLocation() + " (reuse=" + reuse
                            + ", notYetPlayed = " + notYetPlayed + ", donePlaying=" + previousIsDonePlaying + ")");

                    if (notYetPlayed) {
                        try {
                            soundManager.playSound(sound);
                        } catch (Exception e) {
                            Matmos.LOGGER.warn("There was an exception when trying to start stream "
                                    + sound.getPositionedSoundLocation() + ": " + e.getMessage());
                        }
                    }
                } else if (newVolume == 0 && sound.getTargetVolume() != newVolume) {
                    Matmos.DEBUGLOGGER.debug("stopStreaming " + sound.getPositionedSoundLocation());
                    sound.stop(fade);
                }
            }

            handle.resetLargestVolumeCommandThisTick();
        }
    }

    public void setVolume(String customName, float newVolume, float fade) {
        if (isInterrupt) {
            return;
        }

        if (!streaming.containsKey(customName)) {
            IDontKnowHowToCode.warnOnce("Tried to set volume of missing stream " + customName);
            return;
        }

        streaming.get(customName).setVolume(newVolume, fade);
    }

    @Override
    public void stop() {
        if (isInterrupt) {
            return;
        }

        for (StreamHandle<? extends StreamingSound> handle : streaming.values()) {
            handle.getSound().dispose();
        }
    }

    @Override
    public void applyVolume(float volumeMod) {
        volumeModulator = volumeMod;

        for (StreamHandle<? extends StreamingSound> handle : streaming.values()) {
            handle.getSound().applyVolume(volumeMod);
        }
    }

    @Override
    public void interrupt() {
        isInterrupt = true;
    }

    @Override
    public void cleanUp() {
        if (isInterrupt) {
            return;
        }

        for (StreamHandle<? extends StreamingSound> handle : streaming.values()) {
            handle.getSound().dispose();
        }
        streaming.clear();
    }

    @Override
    public boolean isActivated() {
        return isActivated;
    }

    @Override
    public void activate() {
        if (isActivated)
            return;

        isActivated = true;
    }

    @Override
    public void deactivate() {
        if (!isActivated)
            return;

        streaming.keySet().forEach(s -> {
            stopStreaming(s, 2);
        });
        isActivated = false;
    }

    @Override
    public void dispose() {
        cleanUp();
    }

}
