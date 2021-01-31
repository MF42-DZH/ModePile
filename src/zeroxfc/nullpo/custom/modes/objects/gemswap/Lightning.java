package zeroxfc.nullpo.custom.modes.objects.gemswap;

import java.util.Random;
import mu.nu.nullpo.game.event.EventReceiver;
import mu.nu.nullpo.game.play.GameEngine;
import zeroxfc.nullpo.custom.libs.Interpolation;
import zeroxfc.nullpo.custom.libs.ResourceHolderCustomAssetExtension;

public class Lightning implements Effect {
    public static final int MAX_LIFETIME = 120;

    private final int[] locationStart;
    private final int[] locationEnd;
    private final Random localRandom;
    private int lifeTime;

    public Lightning(int[] startPoint, int[] endPoint, long seed) {
        locationStart = startPoint;
        locationEnd = endPoint;
        lifeTime = 0;

        localRandom = new Random(seed);
    }

    @Override
    public void update() {
        lifeTime++;
    }

    @Override
    public boolean shouldNull() {
        return lifeTime >= MAX_LIFETIME;
    }

    @Override
    public void draw(GameEngine engine, EventReceiver receiver, int playerID, int[] args, ResourceHolderCustomAssetExtension customHolder) {
        if (customHolder != null) {
            for (int i = 0; i <= args[0]; i++) {
                int[] drawLocation = new int[] {
                    Interpolation.lerp(locationStart[0], locationEnd[0], (double) i / args[0]),
                    Interpolation.lerp(locationStart[1], locationEnd[1], (double) i / args[0])
                };

                customHolder.drawImage(engine, "particle", drawLocation[0] + (localRandom.nextInt(9) - 4), drawLocation[1] + (localRandom.nextInt(9) - 4), 0, 0, 2, 2, 160, 255, 255, 255, 1.0f);
                customHolder.drawImage(engine, "particle", drawLocation[0] + (localRandom.nextInt(9) - 4), drawLocation[1] + (localRandom.nextInt(9) - 4), 0, 0, 2, 2, 200, 255, 255, 255, 1.0f);
                customHolder.drawImage(engine, "particle", drawLocation[0] + (localRandom.nextInt(9) - 4), drawLocation[1] + (localRandom.nextInt(9) - 4), 0, 0, 2, 2, 240, 255, 255, 255, 1.0f);
                customHolder.drawImage(engine, "particle", drawLocation[0] + (localRandom.nextInt(9) - 4), drawLocation[1] + (localRandom.nextInt(9) - 4), 0, 0, 2, 2, 255, 255, 255, 255, 1.0f);
            }
        } else {
            return;
        }
    }

}