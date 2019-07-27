package zeroxfc.nullpo.custom.modes.objects.gemswap;

import mu.nu.nullpo.game.play.GameEngine;
import zeroxfc.nullpo.custom.libs.ResourceHolderCustomAssetExtension;
import mu.nu.nullpo.game.event.EventReceiver;

public class ScorePopup implements Effect {
    private static final int MAX_TIME_FULL_SIZE = 90;
    private static final int MAX_LIFETIME = 120;
    private static final int TIME_SIZE_STABLE = 60;
    private static final double THREEpiOVERtwo = Math.PI * (3.0 / 2.0);
    private static final double piOVERtwo = Math.PI * (1.0 / 2.0);

    private String value;
    private int lifeTime;
    private float baseDim;
    private float baseSize;
    private float size;
    private int[] location;
    private int[] offsets;
    private int colour;

    public ScorePopup(int score, int[] position, int colour, float baseSize) {
        value = String.valueOf(score);

        location = position;
        offsets = new int[2];
        lifeTime = 0;
        size = 0f;
        this.colour = colour;
        this.baseSize = baseSize;
        baseDim = baseSize * 16;
    }

    @Override
    public void update() {
        lifeTime++;

        if (lifeTime <= TIME_SIZE_STABLE) {
            size = baseSize * (float)( Math.abs( Math.sin( THREEpiOVERtwo * ( (double)lifeTime / TIME_SIZE_STABLE ) ) ) );

            float baseLength = baseDim * value.length();
            int offsetX = (int)(((baseLength * size)) / 2);
            int offsetY = (int)(((baseDim * size)) / 2);

            offsets = new int[] { offsetX, offsetY };

            if (lifeTime % 2 == 0) location[1] -= 1;
        } else if (lifeTime <= MAX_TIME_FULL_SIZE) {
            offsets = new int[] { (int)((baseDim * value.length() * baseSize) / 2), (int)((baseSize * baseDim) / 2) };
            size = baseSize;
        } else {
            size = baseSize * (float)( Math.abs( Math.cos( piOVERtwo * ( (double)(lifeTime - MAX_TIME_FULL_SIZE) / (MAX_LIFETIME - MAX_TIME_FULL_SIZE) ) ) ) );

            float baseLength = baseDim * value.length();
            int offsetX = (int)(((baseLength * size)) / 2);
            int offsetY = (int)(((baseDim * size)) / 2);

            offsets = new int[] { offsetX, offsetY };
        }
    }

    @Override
    public boolean shouldNull() {
        return lifeTime >= MAX_LIFETIME;
    }

    @Override
    public void draw(GameEngine engine, EventReceiver receiver, int playerID, int[] args, ResourceHolderCustomAssetExtension customHolder) {
        receiver.drawDirectFont(engine, playerID, location[0] - offsets[0], location[1] - offsets[1], value, colour, size);
    }

}
