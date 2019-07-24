package zeroxfc.nullpo.custom.modes;

import zeroxfc.nullpo.custom.libs.*;
import zeroxfc.nullpo.custom.modes.objects.expressshipping.*;

public class ExpressShipping extends PuzzleGameEngine {
	// This is literally Puzzle Express.

	private WeightedRandomiser pieceRandomiser;
	private GamePiece[] conveyorBelt;

	@Override
	public String getName() {
		return "EXPRESS SHIPPING";
	}
}
