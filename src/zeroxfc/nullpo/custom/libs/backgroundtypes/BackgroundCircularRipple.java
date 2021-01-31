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
package zeroxfc.nullpo.custom.libs.backgroundtypes;

import java.util.ArrayList;
import java.util.Collections;
import mu.nu.nullpo.game.play.GameEngine;
import zeroxfc.nullpo.custom.libs.ResourceHolderCustomAssetExtension;

public class BackgroundCircularRipple extends AnimatedBackgroundHook {
    private static final int DEF_FIELD_DIM = 8;
    private static final int DEF_GRID_WIDTH = 640 / DEF_FIELD_DIM;
    private static final int DEF_GRID_HEIGHT = 480 / DEF_FIELD_DIM;
    private static final int DEF_PULSE_CENTRE_X = 640 / 2;
    private static final int DEF_PULSE_CENTRE_Y = 480 / 2;
    private static final int DEF_WAVESPEED = 8;
    private static final int MAX_RADIUS = 960;
    private static final float DEF_WAVELENGTH = 80;
    private static final float BASE_SCALE = 1f;
    private static final float SCALE_VARIANCE = 1f;

    private ImageChunk[][] chunkGrid;
    private int pulseTimerMax, currentPulseTimer;
    private ArrayList<Integer> pulseRadii;
    private ArrayList<int[]> pulseCentres;
    private Float pulseBaseScale, pulseScaleVariance;
    private Float wavelength;
    private Integer centreX, centreY;
    private Integer waveSpeed;

    {
        ID = AnimatedBackgroundHook.ANIMATION_CIRCULAR_RIPPLE;
        pulseRadii = new ArrayList<>();
        pulseCentres = new ArrayList<>();
        setImageName("localBG");
    }

    public BackgroundCircularRipple(int bgNumber, Integer cellWidth, Integer cellHeight, Integer pulseCentreX, Integer pulseCentreY, Float wavelength, Integer waveSpeed, int pulseTimerFrames, Float pulseBaseScale, Float pulseScaleVariance) {
        if (bgNumber < 0 || bgNumber > 19) bgNumber = 0;

        customHolder = new ResourceHolderCustomAssetExtension();
        customHolder.loadImage("res/graphics/back" + bgNumber + ".png", imageName);

        setup(cellWidth, cellHeight, pulseCentreX, pulseCentreY, wavelength, waveSpeed, pulseTimerFrames, pulseBaseScale, pulseScaleVariance);

        log.debug("Non-custom circular ripple background (" + bgNumber + ") created.");
    }

    public BackgroundCircularRipple(String filePath, Integer cellWidth, Integer cellHeight, Integer pulseCentreX, Integer pulseCentreY, Float wavelength, Integer waveSpeed, int pulseTimerFrames, Float pulseBaseScale, Float pulseScaleVariance) {
        customHolder = new ResourceHolderCustomAssetExtension();
        customHolder.loadImage(filePath, imageName);

        setup(cellWidth, cellHeight, pulseCentreX, pulseCentreY, wavelength, waveSpeed, pulseTimerFrames, pulseBaseScale, pulseScaleVariance);

        log.debug("Custom circular ripple background created (File Path: " + filePath + ").");
    }

    public void modifyValues(Integer waveSpeed, Integer pulseTimerFrames, Integer pulseCentreX, Integer pulseCentreY, Float wavelength, Float pulseBaseScale, Float pulseScaleVariance) {
        if (pulseTimerFrames != null) pulseTimerMax = pulseTimerFrames;
        if (pulseBaseScale != null) this.pulseBaseScale = pulseBaseScale;
        if (pulseScaleVariance != null) this.pulseScaleVariance = pulseScaleVariance;
        if (wavelength != null) this.wavelength = wavelength;
        if (pulseCentreX != null) this.centreX = pulseCentreX;
        if (pulseCentreY != null) this.centreY = pulseCentreY;
        if (waveSpeed != null) this.waveSpeed = waveSpeed;

        if (currentPulseTimer > pulseTimerMax) currentPulseTimer = pulseTimerMax;
    }

    public void resetPulseScaleValues() {
        if (pulseBaseScale != null) pulseBaseScale = null;
        if (pulseScaleVariance != null) pulseScaleVariance = null;
    }

    private void setup(Integer cellWidth, Integer cellHeight, Integer pulseCentreX, Integer pulseCentreY, Float wavelength, Integer waveSpeed, int pulseFrames, Float pulseBaseScale, Float pulseScaleVariance) {
        pulseTimerMax = pulseFrames;
        currentPulseTimer = pulseTimerMax;

        if (pulseBaseScale == null || pulseScaleVariance == null || pulseCentreX == null || pulseCentreY == null || wavelength == null || waveSpeed == null || cellWidth == null || cellHeight == null) {
            chunkGrid = new ImageChunk[DEF_GRID_HEIGHT][DEF_GRID_WIDTH];
            for (int y = 0; y < DEF_GRID_HEIGHT; y++) {
                for (int x = 0; x < DEF_GRID_WIDTH; x++) {
                    chunkGrid[y][x] = new ImageChunk(ImageChunk.ANCHOR_POINT_MM, new int[] { (DEF_FIELD_DIM * x) + (DEF_FIELD_DIM / 2), (DEF_FIELD_DIM * y) + (DEF_FIELD_DIM / 2) }, new int[] { (DEF_FIELD_DIM * x), (DEF_FIELD_DIM * y) }, new int[] { DEF_FIELD_DIM, DEF_FIELD_DIM }, new float[] { BASE_SCALE, BASE_SCALE });
                }
            }
        } else {
            if (wavelength <= 0) wavelength = DEF_WAVELENGTH;
            if (waveSpeed <= 0) waveSpeed = DEF_WAVESPEED;

            this.pulseBaseScale = pulseBaseScale;
            this.pulseScaleVariance = pulseScaleVariance;
            this.wavelength = wavelength;
            this.centreX = pulseCentreX;
            this.centreY = pulseCentreY;
            this.waveSpeed = waveSpeed;

            int w;
            if (640 % cellWidth != 0) w = 8;
            else w = 640 / cellWidth;

            int h;
            if (480 % cellHeight != 0) h = 8;
            else h = 480 / cellHeight;

            chunkGrid = new ImageChunk[h][w];
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    chunkGrid[y][x] = new ImageChunk(ImageChunk.ANCHOR_POINT_MM, new int[] { (cellWidth * x) + (cellWidth / 2), (cellHeight * y) + (cellHeight / 2) }, new int[] { (cellWidth * x), (cellHeight * y) }, new int[] { cellWidth, cellHeight }, new float[] { pulseBaseScale, pulseBaseScale });
                }
            }
        }

    }

    @Override
    public void update() {
        if (pulseTimerMax >= 0) currentPulseTimer++;
        if (currentPulseTimer >= pulseTimerMax && pulseTimerMax >= 0) {
            currentPulseTimer = 0;
            pulseRadii.add(0);
            pulseCentres.add(new int[] { (centreX == null) ? DEF_PULSE_CENTRE_X : centreX, (centreY == null) ? DEF_PULSE_CENTRE_Y : centreY });
        }

        int ws;
        if (waveSpeed == null) ws = DEF_WAVESPEED;
        else ws = waveSpeed;

        float baseScale = (pulseBaseScale == null) ? BASE_SCALE : pulseBaseScale;
        float scaleVariance = (pulseScaleVariance == null) ? SCALE_VARIANCE : pulseScaleVariance;
        float wl = (wavelength == null) ? DEF_WAVELENGTH : wavelength;

        if (pulseRadii.size() > 0) {
            for (int i = 0; i < pulseRadii.size(); i++) {
                pulseRadii.set(i, pulseRadii.get(i) + ws);
                int cx = pulseCentres.get(i)[0];
                int cy = pulseCentres.get(i)[1];

                int cr = pulseRadii.get(i);
                for (ImageChunk[] imageChunks : chunkGrid) {
                    for (ImageChunk imageChunk : imageChunks) {
                        int[] anch = imageChunk.getAnchorLocation();
                        int cellAnchorX = anch[0];
                        int cellAnchorY = anch[1];

                        double distanceX = Math.abs(cellAnchorX - cx);
                        double distanceY = Math.abs(cellAnchorY - cy);
                        double dTotal = Math.sqrt((distanceX * distanceX) + (distanceY * distanceY));
                        if (almostEqual(dTotal, cr, wl) && dTotal >= 0) {
                            double usedDistance = dTotal - cr;
                            double sinVal = Math.sin(Math.PI * (usedDistance / wl));
                            double newScale = imageChunk.getScale()[0] + (sinVal * scaleVariance);
                            if (newScale < 1d) newScale = 1d;

                            imageChunk.setScale(new float[] { (float) newScale, (float) newScale });
                        } else if (pulseRadii.size() <= 1) {
                            imageChunk.setScale(new float[] { baseScale, baseScale });
                        }
                    }
                }
            }
        }

        // pulseRadii.removeIf(integer -> (integer > MAX_RADIUS));
        for (int i = pulseRadii.size() - 1; i >= 0; i--) {
            if (pulseRadii.get(i) > MAX_RADIUS) {
                pulseRadii.remove(i);
                pulseCentres.remove(i);
            }
        }
    }

    public void manualRipple(int x, int y) {
        pulseRadii.add(0);
        pulseCentres.add(new int[] { x, y });
    }

    @Override
    public void reset() {
        pulseRadii = new ArrayList<>();
        pulseCentres = new ArrayList<>();
        currentPulseTimer = pulseTimerMax;
        update();
    }

    @Override
    public void draw(GameEngine engine, int playerID) {
        ArrayList<ImageChunk> priorityList = new ArrayList<>();
        for (ImageChunk[] imageChunks : chunkGrid) {
            Collections.addAll(priorityList, imageChunks);
        }
        priorityList.sort((c1, c2) -> Float.compare(c1.getScale()[0], c2.getScale()[0]));

        float baseScale = (pulseBaseScale == null) ? BASE_SCALE : pulseBaseScale;
        if (almostEqual(baseScale, 1, 0.005)) {
            customHolder.drawImage(engine, imageName, 0, 0);
            priorityList.removeIf(imageChunk -> almostEqual(imageChunk.getScale()[0], 1, 0.005));
        }
        for (ImageChunk i : priorityList) {
            int[] pos = i.getDrawLocation();
            int[] ddim = i.getDrawDimensions();
            int[] sloc = i.getSourceLocation();
            int[] sdim = i.getSourceDimensions();
            customHolder.drawImage(engine, imageName, pos[0], pos[1], ddim[0], ddim[1], sloc[0], sloc[1], sdim[0], sdim[1], 255, 255, 255, 255, 0);
        }
    }

    @Override
    public void setBG(int bg) {
        customHolder.loadImage("res/graphics/back" + bg + ".png", imageName);
        log.debug("Non-custom circular ripple background modified (New BG: " + bg + ").");
    }

    @Override
    public void setBG(String filePath) {
        customHolder.loadImage(filePath, imageName);
        log.debug("Custom circular ripple background modified (New File Path: " + filePath + ").");
    }

    /**
     * Allows the hot-swapping of pre-loaded BGs from a storage instance of a <code>ResourceHolderCustomAssetExtension</code>.
     *
     * @param holder Storage instance
     * @param name   Image name
     */
    @Override
    public void setBGFromHolder(ResourceHolderCustomAssetExtension holder, String name) {
        customHolder.putImageAt(holder.getImageAt(name), imageName);
        log.debug("Custom circular ripple background modified (New Image Reference: " + name + ").");
    }

    /**
     * This last one is important. In the case that any of the child types are used, it allows identification.
     * The identification can be used to allow casting during operations.
     *
     * @return Identification number of child class.
     */
    @Override
    public int getID() {
        return ID;
    }
}
