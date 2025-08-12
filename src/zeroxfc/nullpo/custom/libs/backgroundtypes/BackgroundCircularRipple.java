package zeroxfc.nullpo.custom.libs.backgroundtypes;

import java.util.ArrayList;
import java.util.Collections;
import mu.nu.nullpo.game.play.GameEngine;
import zeroxfc.nullpo.custom.libs.CustomResourceHolder;
import zeroxfc.nullpo.custom.libs.types.ObjectAlignment;
import zeroxfc.nullpo.custom.libs.types.ImageChunk;
import zeroxfc.nullpo.custom.libs.types.tuples.FloatPair;
import zeroxfc.nullpo.custom.libs.types.tuples.IntPair;

public class BackgroundCircularRipple extends AnimatedBackgroundHook {
    public static final int DEF_FIELD_DIM = 8;
    public static final int DEF_GRID_WIDTH = 640 / DEF_FIELD_DIM;
    public static final int DEF_GRID_HEIGHT = 480 / DEF_FIELD_DIM;
    public static final int DEF_PULSE_CENTRE_X = 640 / 2;
    public static final int DEF_PULSE_CENTRE_Y = 480 / 2;
    public static final int DEF_WAVESPEED = 8;
    private static final int MAX_RADIUS = 960;
    public static final float DEF_WAVELENGTH = 80;
    public static final float DEF_BASE_SCALE = 1f;
    public static final float DEF_SCALE_VARIANCE = 1f;

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

        customHolder = new CustomResourceHolder();
        customHolder.loadImage("res/graphics/back" + bgNumber + ".png", imageName);

        setup(cellWidth, cellHeight, pulseCentreX, pulseCentreY, wavelength, waveSpeed, pulseTimerFrames, pulseBaseScale, pulseScaleVariance);

        log.debug("Non-custom circular ripple background (" + bgNumber + ") created.");
    }

    public BackgroundCircularRipple(String filePath, Integer cellWidth, Integer cellHeight, Integer pulseCentreX, Integer pulseCentreY, Float wavelength, Integer waveSpeed, int pulseTimerFrames, Float pulseBaseScale, Float pulseScaleVariance) {
        customHolder = new CustomResourceHolder();
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

        this.pulseScaleVariance = pulseScaleVariance;
        this.centreX = pulseCentreX;
        this.centreY = pulseCentreY;

        if (pulseBaseScale == null || wavelength == null || waveSpeed == null || cellWidth == null || cellHeight == null) {
            chunkGrid = new ImageChunk[DEF_GRID_HEIGHT][DEF_GRID_WIDTH];
            for (int y = 0; y < DEF_GRID_HEIGHT; y++) {
                for (int x = 0; x < DEF_GRID_WIDTH; x++) {
                    chunkGrid[y][x] = new ImageChunk(ObjectAlignment.MIDDLE_MIDDLE, IntPair.of((DEF_FIELD_DIM * x) + (DEF_FIELD_DIM / 2), (DEF_FIELD_DIM * y) + (DEF_FIELD_DIM / 2)), IntPair.of((DEF_FIELD_DIM * x), (DEF_FIELD_DIM * y)), IntPair.of(DEF_FIELD_DIM, DEF_FIELD_DIM), FloatPair.of(DEF_BASE_SCALE, DEF_BASE_SCALE));
                }
            }
        } else {
            if (wavelength <= 0) wavelength = DEF_WAVELENGTH;
            if (waveSpeed <= 0) waveSpeed = DEF_WAVESPEED;

            this.wavelength = wavelength;
            this.pulseBaseScale = pulseBaseScale;
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
                    chunkGrid[y][x] = new ImageChunk(ObjectAlignment.MIDDLE_MIDDLE, IntPair.of((cellWidth * x) + (cellWidth / 2), (cellHeight * y) + (cellHeight / 2)), IntPair.of((cellWidth * x), (cellHeight * y)), IntPair.of(cellWidth, cellHeight), FloatPair.of(pulseBaseScale, pulseBaseScale));
                }
            }
        }

    }

    @Override
    public void update() {
        if (pulseTimerMax > 0) currentPulseTimer++;
        if (currentPulseTimer >= pulseTimerMax && pulseTimerMax > 0) {
            currentPulseTimer = 0;
            pulseRadii.add(0);
            pulseCentres.add(new int[] { (centreX == null) ? DEF_PULSE_CENTRE_X : centreX, (centreY == null) ? DEF_PULSE_CENTRE_Y : centreY });
        }

        int ws;
        if (waveSpeed == null) ws = DEF_WAVESPEED;
        else ws = waveSpeed;

        float baseScale = (pulseBaseScale == null) ? DEF_BASE_SCALE : pulseBaseScale;
        float scaleVariance = (pulseScaleVariance == null) ? DEF_SCALE_VARIANCE : pulseScaleVariance;
        float wl = (wavelength == null) ? DEF_WAVELENGTH : wavelength;

        if (!pulseRadii.isEmpty()) {
            for (int i = 0; i < pulseRadii.size(); i++) {
                pulseRadii.set(i, pulseRadii.get(i) + ws);
                int cx = pulseCentres.get(i)[0];
                int cy = pulseCentres.get(i)[1];

                int cr = pulseRadii.get(i);
                for (ImageChunk[] imageChunks : chunkGrid) {
                    for (ImageChunk imageChunk : imageChunks) {
                        final IntPair anch = imageChunk.getAnchorLocation();
                        int cellAnchorX = anch.valL;
                        int cellAnchorY = anch.valR;

                        double distanceX = Math.abs(cellAnchorX - cx);
                        double distanceY = Math.abs(cellAnchorY - cy);
                        double dTotal = Math.sqrt((distanceX * distanceX) + (distanceY * distanceY));
                        if (almostEqual(dTotal, cr, wl) && dTotal >= 0) {
                            double usedDistance = dTotal - cr;
                            double sinVal = Math.sin(Math.PI * (usedDistance / wl));
                            double newScale = imageChunk.getScale().valL + (sinVal * scaleVariance);
                            if (newScale < 1d) newScale = 1d;

                            imageChunk.setScale(FloatPair.of((float) newScale, (float) newScale));
                        } else if (pulseRadii.size() <= 1) {
                            imageChunk.setScale(FloatPair.of(baseScale, baseScale));
                        }
                    }
                }
            }
        } else {
            for (ImageChunk[] imageChunks : chunkGrid) {
                for (ImageChunk chunk : imageChunks) {
                    chunk.setScale(FloatPair.of(1f, 1f));
                }
            }
        }

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
        priorityList.sort((c1, c2) -> Float.compare(c1.getScale().valL, c2.getScale().valL));

        float baseScale = (pulseBaseScale == null) ? DEF_BASE_SCALE : pulseBaseScale;
        if (almostEqual(baseScale, 1, 0.005)) {
            customHolder.drawImage(engine, imageName, 0, 0);
            priorityList.removeIf(imageChunk -> almostEqual(imageChunk.getScale().valL, 1, 0.005));
        }
        for (ImageChunk chunk : priorityList) {
            customHolder.drawOffsetImage(engine, imageName, chunk, 255, 255, 255, 255);
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
     * Allows the hot-swapping of pre-loaded BGs from a storage instance of a <code>CustomResourceHolder</code>.
     *
     * @param holder Storage instance
     * @param name   Image name
     */
    @Override
    public void setBGFromHolder(CustomResourceHolder holder, String name) {
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
