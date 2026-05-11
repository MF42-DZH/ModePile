package zeroxfc.nullpo.custom.modes.objects.seasons;

import mu.nu.nullpo.game.play.GameEngine;
import zeroxfc.nullpo.custom.libs.GameTextUtilities;
import zeroxfc.nullpo.custom.libs.Interpolation;
import zeroxfc.nullpo.custom.libs.types.ObjectAlignment;

public class SweepingPassage {
    private final GameTextUtilities.TextBlock text;
    private final int fadeInTime;
    private final int sustainTime;
    private final int fadeOutTime;

    private int lifetime;

    public SweepingPassage(GameTextUtilities.TextBlock text, int fadeInTime, int sustainTime, int fadeOutTime) {
        this.text = text;
        this.fadeInTime = fadeInTime;
        this.sustainTime = sustainTime;
        this.fadeOutTime = fadeOutTime;
    }

    public boolean update() {
        return ++lifetime >= (fadeInTime + sustainTime + fadeOutTime);
    }

    public void draw(
        GameEngine engine,
        int anchorX,
        int anchorY
    ) {

        final int minX = lifetime < (fadeInTime + sustainTime)
            ? anchorX - (text.getWidth() / 2)
            : Interpolation.lerp(anchorX - (text.getWidth() / 2), anchorX + (text.getWidth() / 2), (lifetime - (fadeInTime + sustainTime)) / (double) fadeOutTime);
        final int maxX = Interpolation.lerp(anchorX - (text.getWidth() / 2), anchorX + (text.getWidth() / 2), lifetime / (double) fadeInTime);

        GameTextUtilities.drawAlignedBoundedTextBlock(
            engine,
            anchorX, anchorY,
            minX, anchorY - (text.getHeight() / 2),
                maxX, anchorY + (text.getHeight() / 2),
            false,
            text,
            ObjectAlignment.MIDDLE_MIDDLE
        );
    }
}
