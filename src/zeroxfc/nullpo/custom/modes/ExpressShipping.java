package zeroxfc.nullpo.custom.modes;

import mu.nu.nullpo.game.event.EventReceiver;
import mu.nu.nullpo.game.play.GameEngine;
import mu.nu.nullpo.game.play.GameManager;
import zeroxfc.nullpo.custom.libs.*;
import zeroxfc.nullpo.custom.modes.objects.expressshipping.*;

public class ExpressShipping extends PuzzleGameEngine {
	// This is literally Puzzle Express.

	private GameManager owner;
	private EventReceiver receiver;
	private WeightedRandomiser pieceRandomiser;
	private GamePiece[] conveyorBelt;

	@Override
	public String getName() {
		return "EXPRESS SHIPPING";
	}

	@Override
	public void playerInit(GameEngine engine, int playerID) {
		// FILL THIS
	}
}
