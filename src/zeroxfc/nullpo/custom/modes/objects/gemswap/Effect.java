package zeroxfc.nullpo.custom.modes.objects.gemswap;

import mu.nu.nullpo.game.event.EventReceiver;
import mu.nu.nullpo.game.play.GameEngine;
import zeroxfc.nullpo.custom.libs.ResourceHolderCustomAssetExtension;

public interface Effect {
    void update();

    boolean shouldNull();

    void draw(GameEngine engine, EventReceiver receiver, int playerID, int[] args, ResourceHolderCustomAssetExtension customHolder);
}
