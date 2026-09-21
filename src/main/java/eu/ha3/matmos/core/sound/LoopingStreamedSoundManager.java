package eu.ha3.matmos.core.sound;

import java.net.URL;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import eu.ha3.matmos.Matmos;
import eu.ha3.mc.haddon.supporting.SupportsTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ITickableSound;
import net.minecraft.client.audio.SoundCategory;
import net.minecraft.client.audio.SoundEventAccessorComposite;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.audio.SoundManager;
import net.minecraft.client.audio.SoundPoolEntry;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import paulscode.sound.SoundSystem;

/**
 * <p>
 * A sound manager designed to work well with looping streamed sounds.
 * 
 * <p>
 * Minecraft's sound manager behaves oddly with streamed sounds, and it's even
 * more apparent with ones that loop. It is possible for sounds that should be
 * playing to stop playing, and for sounds that should not be playing to keep
 * playing. (For a possible explanation, see below.)
 * 
 * <p>
 * This sound manager was created to work correctly with looping streamed
 * sounds. It's a modified version of Minecraft's SoundManager class.
 * 
 * <h1>What's the deal with Minecraft's sound manager?</h1>
 * 
 * <p>
 * Streamed sound playback has never been a significant feature of Minecraft's
 * engine: it's only used for playing (non-looping) music, and it's notorious
 * for being buggy. I have identified the following possible issues. Note that
 * they may not be entirely correct, I haven't been verified these theories
 * extensively.
 * 
 * <li>A polling method is used to check when sounds aren't playing. Since the
 * sound manager is on another thread, this can cause a desync to happen when
 * the game freezes. When this happens, the sound manager (correctly) reports
 * that the sounds aren't being played at the time... which causes Minecraft to
 * remove them from the list of playing sounds. Only, after the freeze ends, the
 * sounds may continue playing, and since Minecraft doesn't acknowledge their
 * existence anymore, they never get stopped.
 * 
 * <li>The opposite problem can also happen (sometimes sounds stop playing), but
 * I'm not sure why. This can be observed in vanilla when music stops playing
 * after the game freezes for a few seconds.
 * 
 * <li>A HashBiMap is used to store the inverse map of playing sounds. According
 * to CreativeMD's research, it doesn't work correctly -- sometimes a desync can
 * occur between the map and its inverse, which can cause all sorts of mayhem.
 *
 */

public class LoopingStreamedSoundManager implements SupportsTickEvents, SoundManagerListener {

    private Set<ResourceLocation> UNABLE_TO_PLAY = new HashSet<>();

    private final Map<String, StreamingSound> playingSounds = new ConcurrentHashMap<>();
    private final Map<StreamingSound, String> invPlayingSounds = new ConcurrentHashMap<>();

    public void onTick() {
        for (StreamingSound streamingSound : this.invPlayingSounds.keySet()) {
            ITickableSound itickablesound = streamingSound.asTickable();

            itickablesound.update();

            if (itickablesound.isDonePlaying()) {
                this.stopSound(streamingSound);
            } else {
                String s = this.invPlayingSounds.get(streamingSound);
                getSoundSystem().setVolume(s, this.getClampedVolume(streamingSound));
                getSoundSystem().setPitch(s, this.getClampedPitch(streamingSound));
                getSoundSystem().setPosition(s, itickablesound.getXPosF(), itickablesound.getYPosF(),
                        itickablesound.getZPosF());
            }
        }
    }

    // logic copied from SoundManager.playSound
    public void playSound(StreamingSound streamingSound) {
        if (isSoundManagerLoaded()) {
            if (streamingSound == null)
                return;

            ITickableSound p_sound = streamingSound.asTickable();

            SoundEventAccessorComposite soundeventaccessor = getSoundHandler().getSound(p_sound.getPositionedSoundLocation());
            ResourceLocation resourcelocation = p_sound.getPositionedSoundLocation();

            if (soundeventaccessor == null) {
                if (UNABLE_TO_PLAY.add(resourcelocation)) {
                    Matmos.LOGGER.warn("Unable to play unknown soundEvent: {}", (Object) resourcelocation);
                }
            } else {
                if (getSoundSystem().getMasterVolume() <= 0.0F) {
                    Matmos.DEBUGLOGGER.debug("Skipped playing soundEvent: {}, master volume was zero",
                            (Object) resourcelocation);
                } else {
                    SoundPoolEntry sound = soundeventaccessor.func_148720_g();

                    if (sound == SoundHandler.missing_sound) {
                        if (UNABLE_TO_PLAY.add(resourcelocation)) {
                            Matmos.LOGGER.warn("Unable to play empty soundEvent: {}", (Object) resourcelocation);
                        }
                    } else {
                        float f3 = p_sound.getVolume();
                        float f = 16.0F;

                        if (f3 > 1.0F) {
                            f *= f3;
                        }

                        SoundCategory soundcategory = soundeventaccessor.getSoundCategory();
                        float f1 = this.getClampedVolume(streamingSound);
                        float f2 = this.getClampedPitch(streamingSound);
                        ResourceLocation resourcelocation2 = sound.getSoundPoolEntryLocation();

                        if (f1 == 0.0F) {
                            Matmos.DEBUGLOGGER.debug("Skipped playing sound {}, volume was zero.",
                                    (Object) resourcelocation);
                        } else {
                            boolean flag = p_sound.canRepeat() && p_sound.getRepeatDelay() == 0;
                            String s = UUID.randomUUID().toString();
                            ResourceLocation resourcelocation1 = resourcelocation2;

                            if (sound.func_148648_d()) { // isStreamingSound
                                getSoundSystem().newStreamingSource(false, s, SoundManager.getURLForSoundResource(resourcelocation1),
                                        resourcelocation1.toString(), flag, p_sound.getXPosF(), p_sound.getYPosF(),
                                        p_sound.getZPosF(), p_sound.getAttenuationType().getTypeInt(), f);
                            } else {
                                getSoundSystem().newSource(false, s, SoundManager.getURLForSoundResource(resourcelocation1),
                                        resourcelocation1.toString(), flag, p_sound.getXPosF(), p_sound.getYPosF(),
                                        p_sound.getZPosF(), p_sound.getAttenuationType().getTypeInt(), f);
                            }

                            Matmos.DEBUGLOGGER.debug("Playing sound {} for event {} as channel {}", resourcelocation2,
                                    resourcelocation, s);
                            getSoundSystem().setPitch(s, f2);
                            getSoundSystem().setVolume(s, f1);
                            getSoundSystem().play(s);
                            this.playingSounds.put(s, streamingSound);
                            this.invPlayingSounds.put(streamingSound, s);
                        }
                    }
                }
            }
        }
    }

    public void stopSound(StreamingSound sound) {
        if (isSoundManagerLoaded()) {
            String s = this.invPlayingSounds.get(sound);

            if (s != null) {
                getSoundSystem().stop(s);

                playingSounds.remove(s);
                invPlayingSounds.remove(sound);
            }
        }
    }

    private SoundManager getSoundManager() {
        return getSoundHandler().sndManager;
    }

    private SoundHandler getSoundHandler() {
        return Minecraft.getMinecraft().getSoundHandler();
    }

    private SoundSystem getSoundSystem() {
        return getSoundManager().sndSystem;
    }

    private boolean isSoundManagerLoaded() {
        return getSoundManager().loaded;
    }

    private float getClampedPitch(StreamingSound sound) {
        return (float) MathHelper.clamp_double(sound.asTickable().getPitch(), 0.5F, 2.0F);
    }

    private float getClampedVolume(StreamingSound sound) {
        return (float) MathHelper
                .clamp_double(sound.asTickable().getVolume() * this.getCategoryVolume(sound.getCategory()), 0.0F, 1.0F);
    }

    private float getCategoryVolume(SoundCategory category) {
        return getSoundManager().getSoundCategoryVolume(category);
    }

    @Override
    public void onStopAllSounds() {

    }

    public void stopAllSounds() {
        for (StreamingSound sound : playingSounds.values()) {
            sound.interrupt();
            stopSound(sound);
        }
    }
    
    public void dispose() {
        stopAllSounds();
        playingSounds.clear();
        invPlayingSounds.clear();
    }

    @Override
    public void onPauseAllSounds(boolean pause) {
        for (String sound : playingSounds.keySet()) {
            if (pause) {
                getSoundSystem().pause(sound);
            } else {
                getSoundSystem().play(sound);
            }

        }
    }
}
