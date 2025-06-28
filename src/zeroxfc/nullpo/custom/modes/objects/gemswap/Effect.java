package zeroxfc.nullpo.custom.modes.objects.gemswap;

import mu.nu.nullpo.game.event.EventReceiver;
import mu.nu.nullpo.game.play.GameEngine;
import zeroxfc.nullpo.custom.libs.CustomResourceHolder;

public interface Effect {
    void update();

    boolean shouldNull();

    void draw(GameEngine engine, EventReceiver receiver, int playerID, int[] args, CustomResourceHolder customHolder);
}
