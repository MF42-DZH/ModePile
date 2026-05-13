package zeroxfc.nullpo.custom.libs;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.sound.sampled.Clip;
import mu.nu.nullpo.game.play.GameEngine;
import mu.nu.nullpo.gui.sdl.ResourceHolderSDL;
import mu.nu.nullpo.gui.sdl.SoundManagerSDL;
import mu.nu.nullpo.gui.slick.ResourceHolder;
import mu.nu.nullpo.gui.slick.SoundManager;
import mu.nu.nullpo.gui.swing.ResourceHolderSwing;
import mu.nu.nullpo.gui.swing.WaveEngine;
import org.apache.log4j.Logger;
import org.newdawn.slick.Sound;
import sdljava.mixer.MixChunk;

public class SoundLoader {
    private static final String CUSTOM_SKIN_DIRECTORY = "custom.skin.directory";

    // Add your own sound directories here when they are needed.
    private static final String SE_ZEROXFC = "/se/zeroxfc/";

    // Sound name enums for easier use. Use the static method of the enum classes to
    // load all sounds for a sound set.
    public static class Sounds {
        public interface IsSounds {
            String sfx();

            default void playOn(GameEngine engine) {
                engine.playSE(sfx());
            }
        }

        public enum Fireworks implements IsSounds {
            LAUNCH("fireworklaunch"),
            EXPLODE("fireworkexplode");

            // region Boilerplate
            private final String soundName;

            Fireworks(String soundName) {
                this.soundName = soundName;
            }

            @Override
            public String sfx() {
                return soundName;
            }

            @Override
            public String toString() {
                return soundName;
            }

            public static void loadAllSounds() {
                for (Fireworks sound : values()) importSoundZXF(sound.sfx());
            }
            // endregion Boilerplate
        }

        public enum Scanner implements IsSounds {
            SCANNED("linescanned"),
            MOVE("linescannermove");

            private final String soundName;

            // region Boilerplate
            Scanner(String soundName) {
                this.soundName = soundName;
            }

            @Override
            public String sfx() {
                return soundName;
            }

            @Override
            public String toString() {
                return soundName;
            }

            public static void loadAllSounds() {
                for (Scanner sound : values()) importSoundZXF(sound.sfx());
            }
            // endregion Boilerplate
        }

        public enum Minesweeper implements IsSounds {
            EXPLOSION1("explosion1"),
            EXPLOSION2("explosion2"),
            EXPLOSION3("explosion3"),
            EXPLOSION4("explosion4");

            private final String soundName;

            // region Boilerplate
            Minesweeper(String soundName) {
                this.soundName = soundName;
            }

            @Override
            public String sfx() {
                return soundName;
            }

            @Override
            public String toString() {
                return soundName;
            }

            public static void loadAllSounds() {
                for (Minesweeper sound : values()) importSoundZXF(sound.sfx());
            }
            // endregion Boilerplate
        }

        public enum Collapse implements IsSounds {
            BOMB_EXPLODE("bombexplode"),
            LANDING("landing"),
            RISE("rise"),
            BONUS("bonus"),
            BIG_CLEAR("bigclear"),
            NORMAL_CLEAR("normalclear"),
            NO_LANDING("nolanding"),
            NO_CLEAR("noclear"),
            BONUS_POP("bonuspop");


            private final String soundName;

            // region Boilerplate
            Collapse(String soundName) {
                this.soundName = soundName;
            }

            @Override
            public String sfx() {
                return soundName;
            }

            @Override
            public String toString() {
                return soundName;
            }

            public static void loadAllSounds() {
                for (Collapse sound : values()) importSoundZXF(sound.sfx());
            }
            // endregion Boilerplate
        }

        public enum Constantris implements IsSounds {
            HORN("horn"),
            TIME_INCREASE("timeincrease"),
            TIME_REDUCE("timereduce");

            private final String soundName;

            // region Boilerplate
            Constantris(String soundName) {
                this.soundName = soundName;
            }

            @Override
            public String sfx() {
                return soundName;
            }

            @Override
            public String toString() {
                return soundName;
            }

            public static void loadAllSounds() {
                for (Constantris sound : values()) importSoundZXF(sound.sfx());
            }
            // endregion Boilerplate
        }

        public enum Seasons implements IsSounds {
            STEAM("steam"),
            ICICLE("icicle"),
            GROUND_CRACKLE_1("groundcrackle1"),
            GROUND_CRACKLE_2("groundcrackle2"),
            GROUND_CRACKLE_3("groundcrackle3"),
            WIND_1("wind1"),
            WIND_2("wind2"),
            WIND_3("wind3"),
            ZERO_FREEZE("zerofreeze");

            private final String soundName;

            // region Boilerplate
            Seasons(String soundName) {
                this.soundName = soundName;
            }

            @Override
            public String sfx() {
                return soundName;
            }

            @Override
            public String toString() {
                return soundName;
            }

            public static void loadAllSounds() {
                for (Seasons sound : values()) importSoundZXF(sound.sfx());
            }
            // endregion Boilerplate

        }

        public enum Achievements implements IsSounds {
            ACHIEVEMENT("achievement");

            private final String soundName;

            Achievements(String soundName) {
                this.soundName = soundName;
            }

            @Override
            public String sfx() {
                return soundName;
            }

            @Override
            public String toString() {
                return soundName;
            }

            public static void loadAllSounds() {
                for (Achievements sound : values()) importSoundZXF(sound.sfx());
            }
        }

        private Sounds() {}
    }

    /**
     * Debug log
     */
    private static final Logger log = Logger.getLogger(SoundLoader.class);

    private SoundLoader() {}

    private static boolean extended = false;

    /** Extends the sound effect maps' capacities in the NullpoMino runtime. */
    @SuppressWarnings("unchecked")
    private static void extendClipMaps() {
        if (extended) return;

        final CustomResourceHolder.Runtime holderType = CustomResourceHolder.getCurrentNullpominoRuntime();

        try {
            switch (holderType) {
                case SLICK:
                    {
                        final Field cm = SoundManager.class.getDeclaredField("clipMap");
                        final Field mc = SoundManager.class.getDeclaredField("maxClips");

                        cm.setAccessible(true);
                        mc.setAccessible(true);

                        cm.set(
                            ResourceHolder.soundManager,
                            new HashMap<>(
                                (HashMap<String, org.newdawn.slick.Sound>) cm.get(ResourceHolder.soundManager)
                            )
                        );
                        mc.setInt(ResourceHolder.soundManager, Integer.MAX_VALUE);
                    }
                    break;
                case SWING:
                    {
                        final Field cm = WaveEngine.class.getDeclaredField("clipMap");
                        final Field mc = WaveEngine.class.getDeclaredField("maxClips");

                        cm.setAccessible(true);
                        mc.setAccessible(true);

                        cm.set(
                            ResourceHolderSwing.soundManager,
                            new HashMap<>(
                                (HashMap<String, javax.sound.sampled.Clip>) cm.get(ResourceHolderSwing.soundManager)
                            )
                        );
                        mc.setInt(ResourceHolderSwing.soundManager, Integer.MAX_VALUE);
                    }
                    break;
                case SDL:
                    {
                        final Field clm = SoundManagerSDL.class.getDeclaredField("clipMap");
                        final Field chm = SoundManagerSDL.class.getDeclaredField("channelMap");
                        final Field mc = SoundManagerSDL.class.getDeclaredField("maxClips");

                        clm.setAccessible(true);
                        chm.setAccessible(true);
                        mc.setAccessible(true);

                        clm.set(
                            ResourceHolderSDL.soundManager,
                            new HashMap<>(
                                (HashMap<String, sdljava.mixer.MixChunk>) clm.get(ResourceHolderSDL.soundManager)
                            )
                        );
                        chm.set(
                            ResourceHolderSDL.soundManager,
                            new HashMap<>(
                                (HashMap<String, Integer>) chm.get(ResourceHolderSDL.soundManager)
                            )
                        );
                        mc.setInt(ResourceHolderSDL.soundManager, Integer.MAX_VALUE);
                    }
                    break;
                case UNKNOWN:
                    break;
            }
        } catch (Exception e) {
            log.error("Failed to extend clipmaps:");
            log.error(e);
        } finally {
            log.info("Successful in extending clipmaps.");
            extended = true;
        }
    }

    /**
     * Private sound import method for use with soundpacks.<br />
     * <br />
     * Credit goes to <code>GlitchyPSI</code> for the base of this code snippet.
     *
     * @param soundName Name of sound in pack
     */
    private static void importSoundZXF(String soundName) {
        extendClipMaps();

        String skindir = null;
        final CustomResourceHolder.Runtime holderType = CustomResourceHolder.getCurrentNullpominoRuntime();

        switch (holderType) {
            case SLICK:
                skindir = mu.nu.nullpo.gui.slick.NullpoMinoSlick.propConfig.getProperty(CUSTOM_SKIN_DIRECTORY, "res");
                break;
            case SWING:
                skindir = mu.nu.nullpo.gui.swing.NullpoMinoSwing.propConfig.getProperty(CUSTOM_SKIN_DIRECTORY, "res");
                break;
            case SDL:
                skindir = mu.nu.nullpo.gui.sdl.NullpoMinoSDL.propConfig.getProperty(CUSTOM_SKIN_DIRECTORY, "res");
                break;
        }

        if (ResourceHolderSwing.soundManager != null) {
            ResourceHolderSwing.soundManager.load(soundName, skindir + SE_ZEROXFC + soundName + ".wav");
        } else if (ResourceHolder.soundManager != null) {
            ResourceHolder.soundManager.load(soundName, skindir + SE_ZEROXFC + soundName + ".wav");
        } else if (ResourceHolderSDL.soundManager != null) {
            ResourceHolderSDL.soundManager.load(soundName, skindir + SE_ZEROXFC + soundName + ".wav");
        }
    }

    /**
     * Public sound import method when not using a soundpack.
     *
     * @param filePath  Path to sound
     * @param soundName Name to store sound as
     */
    public static void importSound(String filePath, String soundName) {
        extendClipMaps();

        if (ResourceHolderSwing.soundManager != null) {
            ResourceHolderSwing.soundManager.load(soundName, filePath);
        } else if (ResourceHolder.soundManager != null) {
            ResourceHolder.soundManager.load(soundName, filePath);
        } else if (ResourceHolderSDL.soundManager != null) {
            ResourceHolderSDL.soundManager.load(soundName, filePath);
        }
    }

    @SuppressWarnings("unchecked")
    private static org.newdawn.slick.Sound getSlickSound(String name) {
        try {
            final Field cm = SoundManager.class.getDeclaredField("clipMap");
            cm.setAccessible(true);

            return ((HashMap<String, org.newdawn.slick.Sound>) cm.get(ResourceHolder.soundManager)).get(name);
        } catch (Exception e) {
            log.error("Unable to get Slick sound: " + name);
            log.error(e);

            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private static javax.sound.sampled.Clip getSwingSound(String name) {
        try {
            final Field cm = WaveEngine.class.getDeclaredField("clipMap");
            cm.setAccessible(true);

            return ((HashMap<String, javax.sound.sampled.Clip>) cm.get(ResourceHolderSwing.soundManager)).get(name);
        } catch (Exception e) {
            log.error("Unable to get Swing sound: " + name);
            log.error(e);

            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private static sdljava.mixer.MixChunk getSDLSound(String name) {
        try {
            final Field cm = SoundManagerSDL.class.getDeclaredField("clipMap");
            cm.setAccessible(true);

            return ((HashMap<String, sdljava.mixer.MixChunk>) cm.get(ResourceHolderSDL.soundManager)).get(name);
        } catch (Exception e) {
            log.error("Unable to get SDL sound: " + name);
            log.error(e);

            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private static int getSDLChannel(String name) {
        try {
            final Field cm = SoundManagerSDL.class.getDeclaredField("channelMap");
            cm.setAccessible(true);

            return ((HashMap<String, Integer>) cm.get(ResourceHolderSDL.soundManager)).get(name);
        } catch (Exception e) {
            log.error("Unable to get SDL sound: " + name);
            log.error(e);

            return -1;
        }
    }

    /**
     * Play a panned sound if possible.
     *
     * @param engine    Fallback game engine.
     * @param soundName Sound name in memory to play.
     * @param balance   Audio balance (-1.0 = left, 0 = neutral, 1.0 = right).
     */
    public static void playPannedSound(GameEngine engine, String soundName, float balance) {
        final float pan = MathHelper.clamp(balance, -1f, 1f);
        final CustomResourceHolder.Runtime holderType = CustomResourceHolder.getCurrentNullpominoRuntime();

        if (holderType == CustomResourceHolder.Runtime.SLICK) {
            final Sound slSound = getSlickSound(soundName);
            if (slSound != null) slSound.playAt(pan, 0f, 0f);
        } else {
            engine.playSE(soundName);
        }
    }

    /**
     * Gets all the internal names for all the loaded sounds.<br />
     * Useful for random sound selection or debugging.
     *
     * @return ArrayList containing the internal names for all sounds.
     */
    @SuppressWarnings("unchecked")
    public static List<String> getSoundNames() {
        final ArrayList<String> soundList = new ArrayList<>();
        final CustomResourceHolder.Runtime holderType = CustomResourceHolder.getCurrentNullpominoRuntime();

        /*
         * XXX: welp, i guess it's time to use reflection again... apologies in advance for this atrocity.
         */
        Field localField;
        switch (holderType) {
            case SLICK:
                Class<SoundManager> slickSound = SoundManager.class;
                try {
                    localField = slickSound.getDeclaredField("clipMap");
                    localField.setAccessible(true);

                    /*
                     * This should not return anything other than HashMap<String, Sound>,
                     * as verified in the source code (see SoundManager.java).
                     */
                    HashMap<String, Sound> slickSE = (HashMap<String, Sound>) localField.get(ResourceHolder.soundManager);

                    soundList.addAll(slickSE.keySet());
                    log.info("Sound name loading from Slick soundManager successful.");
                } catch (Exception e) {
                    log.error("Sound name loading from Slick soundManager failed.");
                }

                break;
            case SWING:
                Class<WaveEngine> swingSound = WaveEngine.class;
                try {
                    localField = swingSound.getDeclaredField("clipMap");
                    localField.setAccessible(true);

                    /*
                     * This should not return anything other than HashMap<String, Clip>,
                     * as verified in the source code (see WaveEngine.java).
                     */
                    HashMap<String, Clip> swingSE = (HashMap<String, Clip>) localField.get(ResourceHolderSwing.soundManager);

                    soundList.addAll(swingSE.keySet());
                    log.info("Sound name loading from Swing soundManager successful.");
                } catch (Exception e) {
                    log.error("Sound name loading from Swing soundManager failed.");
                }

                break;
            case SDL:
                Class<SoundManagerSDL> sdlSound = SoundManagerSDL.class;
                try {
                    localField = sdlSound.getDeclaredField("clipMap");
                    localField.setAccessible(true);

                    /*
                     * This should not return anything other than HashMap<String, MixChunk>,
                     * as verified in the source code (see SoundManagerSDL.java).
                     */
                    HashMap<String, MixChunk> sdlSE = (HashMap<String, MixChunk>) localField.get(ResourceHolderSDL.soundManager);

                    soundList.addAll(sdlSE.keySet());
                    log.info("Sound name loading from SDL soundManager successful.");
                } catch (Exception e) {
                    log.error("Sound name loading from SDL soundManager failed.");
                }

                break;
            default:
                return new ArrayList<>();
        }

        return soundList;
    }
}
