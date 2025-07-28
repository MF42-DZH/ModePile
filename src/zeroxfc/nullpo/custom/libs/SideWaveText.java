package zeroxfc.nullpo.custom.libs;

import mu.nu.nullpo.game.event.EventReceiver;
import mu.nu.nullpo.game.play.GameEngine;
import zeroxfc.nullpo.custom.libs.types.ObjectAlignment;

public class SideWaveText {
    public static final int MaxLifeTime = 120;
    private static final DoubleVector VerticalVelocity = new DoubleVector(0, -1 * (4.0 / 5.0), false);
    private final String text;
    private final double offsetMax;
    private final double sinFrequency;
    private final boolean big;
    private final boolean lClear;
    private DoubleVector position;
    private double xOffset;
    private double sinPhase;
    private int lifeTime;

    /**
     * Creates a Super Collapse II-styled score popup that flies away.<br />
     * For now, drawing is manual.
     *
     * @param x           X-coordinate of centre (?)
     * @param y           Y-coordinate of centre (?)
     * @param frequency   Waving frequency
     * @param offsetWidth Max offset
     * @param text        Text to draw
     * @param big         Should it be double size?
     * @param largeclear  Should it flash and actually wave?
     */
    public SideWaveText(int x, int y, double frequency, double offsetWidth, String text, boolean big, boolean largeclear) {
        this.text = text;
        position = new DoubleVector(x, y, false);

        sinPhase = 0.0;
        sinFrequency = frequency;
        offsetMax = offsetWidth;
        this.big = big;
        lClear = largeclear;

        xOffset = 0;
        lifeTime = 0;
    }

    /**
     * Updates the instance to a new position.
     */
    public void update() {
        sinPhase += sinFrequency * ((Math.PI * 2) / 60);

        xOffset = offsetMax * (Math.sin(sinPhase) / 2.0);

        position = DoubleVector.add(position, VerticalVelocity);

        lifeTime++;
    }

    /**
     * Gets the current location of the text for manual drawing.
     *
     * @return A 2-long int[] in the format { x, y }
     */
    public int[] getLocation() {
        return new int[] { (int) (position.getX() + xOffset), (int) position.getY() };
    }

    /**
     * Automatic drawing of the text object.
     *
     * @param receiver Renderer to draw on
     * @param engine   Current GameEngine instance
     * @param playerID Current player ID
     * @param flag     true ? orange : yellow
     */
    public void drawCentral(EventReceiver receiver, GameEngine engine, int playerID, boolean flag) {
        int[] location = getLocation();
        final int x = location[0];
        final int y = location[1];

        int color = EventReceiver.COLOR_ORANGE;
        if (flag) color = EventReceiver.COLOR_YELLOW;

        float scale = 0;
        float baseScale = big ? 2 : 1;
        if (lifeTime < 24) {
            scale = baseScale;
        } else {
            scale = baseScale - (baseScale * ((float) (lifeTime - 24) / 96));
        }

        GameTextUtilities.drawDirectTextAlign(engine, x, y, ObjectAlignment.MIDDLE_MIDDLE, text, color, scale);
    }

    public int getLifeTime() {
        return lifeTime;
    }

    public boolean getBig() {
        return big;
    }

    public boolean getLargeClear() {
        return lClear;
    }

    public String getText() {
        return text;
    }
}
