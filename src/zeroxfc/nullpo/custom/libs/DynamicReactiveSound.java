/*
 * This library class was created by 0xFC963F18DC21 / Shots243
 * It is part of an extension library for the game NullpoMino (copyright 2010)
 *
 * Herewith shall the term "Library Creator" be given to 0xFC963F18DC21.
 * Herewith shall the term "Game Creator" be given to the original creator of NullpoMino.
 *
 * THIS LIBRARY AND MODE PACK WAS NOT MADE IN ASSOCIATION WITH THE GAME CREATOR.
 *
 * Repository: https://github.com/Shots243/ModePile
 *
 * When using this library in a mode / library pack of your own, the following
 * conditions must be satisfied:
 *     - This license must remain visible at the top of the document, unmodified.
 *     - You are allowed to use this library for any modding purpose.
 *         - If this is the case, the Library Creator must be credited somewhere.
 *             - Source comments only are fine, but in a README is recommended.
 *     - Modification of this library is allowed, but only in the condition that a
 *       pull request is made to merge the changes to the repository.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package zeroxfc.nullpo.custom.libs;

import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import mu.nu.nullpo.gui.sdl.NullpoMinoSDL;
import mu.nu.nullpo.gui.slick.NullpoMinoSlick;
import mu.nu.nullpo.gui.swing.ResourceHolderSwing;
import org.apache.log4j.Logger;
import org.newdawn.slick.openal.Audio;
import org.newdawn.slick.openal.AudioLoader;

@Deprecated
public class DynamicReactiveSound {
    /**
     * Sound types
     */
    public static final int WAVE_SINUSOIDAL = 0,
        WAVE_SQUARE = 1,
        WAVE_TRIANGULAR = 2,
        WAVE_SAWTOOTH = 3;
    /**
     * Debug Logger
     */
    private static final Logger log = Logger.getLogger(DynamicReactiveSound.class);
    /**
     * Sound data
     */
    private static final int SAMPLE_RATE = 32000,
        LENGTH = 48000,
        BIT_DEPTH = 8,
        CHANNELS = 1;

    /**
     * Which sound interface to hook onto.
     */
    private final CustomResourceHolder.Runtime SOUNDMANAAGER_TYPE;

    /**
     * Sound volume
     */
    private float volume;

    {
        // Tells class which sound manager to hook onto to check volume.
        SOUNDMANAAGER_TYPE = CustomResourceHolder.getCurrentNullpominoRuntime();
    }

    /**
     * Constructor gets the volume of the
     */
    public DynamicReactiveSound() {
        if (SOUNDMANAAGER_TYPE == CustomResourceHolder.Runtime.SLICK) {
            volume = NullpoMinoSlick.appGameContainer.getSoundVolume();
        } else if (SOUNDMANAAGER_TYPE == CustomResourceHolder.Runtime.SWING) {  // Slick / Swing use 0f-1f
            volume = (float) ResourceHolderSwing.soundManager.getVolume();
        } else if (SOUNDMANAAGER_TYPE == CustomResourceHolder.Runtime.SDL) {
            // SDL uses 0-128 for volume.
            volume = NullpoMinoSDL.propConfig.getProperty("option.sevolume", 128);
        }
    }

    /**
     * Generates and plays a sound clip on the fly. Uses unsigned 8-bit PCM.
     *
     * @param type             Wave type (use internally defined types)
     * @param pitch            Starting pitch of sound, Hz
     * @param endPitch         End pitch of sound, Hz (use null to keep pitch constant)
     * @param duration         Length in samples (max 12000, min 100, sampleRate = 8 kHz)
     * @param fadeTime         Length of volume fade
     * @param volumeMultiplier Multiplier of volume relative to given volume
     */
    public void playSound(int type, double pitch, Double endPitch, int duration, Integer fadeTime, float volumeMultiplier) {
        //region INITIALISATION
        if (pitch <= 0) {
            log.warn("Invalid pitch: " + pitch);
            return;
        }

        if (endPitch != null) {
            if (endPitch <= 0) {
                endPitch = pitch;
            }
        } else {
            endPitch = pitch;
        }

        if (fadeTime == null) {
            fadeTime = 0;
        }

        duration = Math.max(100, duration);
        duration = Math.min(duration, LENGTH);

        // Used volume
        float usedVolume = volume * volumeMultiplier;
        if (usedVolume < 0) usedVolume = 0;
        if (SOUNDMANAAGER_TYPE == CustomResourceHolder.Runtime.SDL) {
            if (usedVolume > 128) usedVolume = 128;
            usedVolume = usedVolume / 128f;
        } else {
            if (usedVolume > 1) usedVolume = 1;
        }

        //endregion INITIALISATION

        //region Wave Data Creation
        // Create wave function
        Byte[] data = new Byte[LENGTH];
        final double TWO_PI = 2 * Math.PI;

        int period = (int) (SAMPLE_RATE / pitch);
        int endPeriod = (int) (SAMPLE_RATE / endPitch);

        switch (type) {
            case WAVE_SINUSOIDAL:
                for (int i = 0; i < data.length; i++) {
                    if (i < duration) {
                        double usedPeriod = period;
                        if (period != endPeriod)
                            usedPeriod = Interpolation.lerp(period, endPeriod, (double) i / duration);
                        double dVol = usedVolume;

                        if (i >= duration - fadeTime - 1) {
                            dVol = Interpolation.lerp(usedVolume, 0, (i - (duration - fadeTime)) / (double) fadeTime);
                        }

                        double x = TWO_PI * (i / usedPeriod);
                        byte value = (byte) (Math.round(127d * Math.sin(x) * dVol));

                        data[i] = value;
                    } else {
                        data[i] = 0;
                    }
                }
                break;
            case WAVE_SQUARE:
            case WAVE_TRIANGULAR:
            case WAVE_SAWTOOTH:
                log.debug("Not yet.");
                return;
            default:
                log.warn("Invalid type: " + type);
                return;
        }

        log.debug(Arrays.toString(data));
        //endregion Wave Data Creation

        // Input stream to feed into

        // region HEADER
        ArrayList<Byte> rawData = new ArrayList<>(Arrays.asList(new Byte[] { 0x52, 0x49, 0x46, 0x46 }));  // "RIFF"

        byte[] temp = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(44 + LENGTH).array();
        Byte[] tempObject = new Byte[temp.length];
        for (int i = 0; i < temp.length; i++) {
            tempObject[i] = temp[i];
        }
        rawData.addAll(Arrays.asList(tempObject));  // File length

        rawData.addAll(Arrays.asList(new Byte[] { 0x57, 0x41, 0x56, 0x45, 0x66, 0x6d, 0x74, 0x20 }));  // "WAVEfmt "

        temp = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(16).array();
        tempObject = new Byte[temp.length];
        for (int i = 0; i < temp.length; i++) {
            tempObject[i] = temp[i];
        }
        rawData.addAll(Arrays.asList(tempObject));  // Subchunk Size

        temp = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(1).array();
        tempObject = new Byte[2];
        for (int i = 0; i < tempObject.length; i++) {
            tempObject[i] = temp[i];
        }
        rawData.addAll(Arrays.asList(tempObject));  // PCM

        temp = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(CHANNELS).array();
        tempObject = new Byte[2];
        for (int i = 0; i < tempObject.length; i++) {
            tempObject[i] = temp[i];
        }
        rawData.addAll(Arrays.asList(tempObject));  // Channels

        temp = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(SAMPLE_RATE).array();
        tempObject = new Byte[temp.length];
        for (int i = 0; i < temp.length; i++) {
            tempObject[i] = temp[i];
        }
        rawData.addAll(Arrays.asList(tempObject));  // Sample Rate

        temp = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt((BIT_DEPTH * CHANNELS * SAMPLE_RATE) / 8).array();
        tempObject = new Byte[temp.length];
        for (int i = 0; i < temp.length; i++) {
            tempObject[i] = temp[i];
        }
        rawData.addAll(Arrays.asList(tempObject));  // Byte Rate

        temp = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(BIT_DEPTH * CHANNELS / 8).array();
        tempObject = new Byte[2];
        for (int i = 0; i < tempObject.length; i++) {
            tempObject[i] = temp[i];
        }
        rawData.addAll(Arrays.asList(tempObject));  // BlockAlign

        temp = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(BIT_DEPTH).array();
        tempObject = new Byte[2];
        for (int i = 0; i < tempObject.length; i++) {
            tempObject[i] = temp[i];
        }
        rawData.addAll(Arrays.asList(tempObject));  // Bit Depth

        rawData.addAll(Arrays.asList(new Byte[] { 0x64, 0x61, 0x74, 0x61 }));  // "data"

        temp = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(LENGTH).array();
        tempObject = new Byte[4];
        for (int i = 0; i < tempObject.length; i++) {
            tempObject[i] = temp[i];
        }
        rawData.addAll(Arrays.asList(tempObject));  // Data length

        // endregion HEADER

        // log.debug(Arrays.toString(rData));
        // log.debug(Arrays.toString(nData));

        rawData.addAll(Arrays.asList(data));

        Object[] finalArray = rawData.toArray();
        byte[] bytes = new byte[finalArray.length];

        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (Byte) finalArray[i];
        }

        ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
        AudioInputStream AS = new AudioInputStream(stream,
            new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, SAMPLE_RATE, BIT_DEPTH, CHANNELS, (BIT_DEPTH / 8), SAMPLE_RATE, false),
            LENGTH);

        if (SOUNDMANAAGER_TYPE == CustomResourceHolder.Runtime.SLICK) {
            try {
                Audio sound = AudioLoader.getAudio("WAV", AS);
                sound.playAsSoundEffect(1f, usedVolume, false);

                stream.close();
                AS.close();
            } catch (Exception e) {
                log.error("FAILED TO PLAY SOUND:\n", e);
            }
        } else if (SOUNDMANAAGER_TYPE == CustomResourceHolder.Runtime.SWING) {  // Slick / Swing use 0f-1f
            log.warn("NO SWING SUPPORT YET");
        } else if (SOUNDMANAAGER_TYPE == CustomResourceHolder.Runtime.SDL) {
            log.warn("NO SDL SUPPORT YET");
        }
    }
}
