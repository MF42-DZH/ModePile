package zeroxfc.nullpo.custom.modes.objects.seasons;

import mu.nu.nullpo.game.component.Field;
import mu.nu.nullpo.game.component.Piece;
import mu.nu.nullpo.game.play.GameEngine;

// Holds the state of the next, hold and field.
public class NextAndFieldState {
    public final int nextPosition;
    public final Field field;
    public final Piece holdPiece;

    public NextAndFieldState(GameEngine engine) {
        this.nextPosition = engine.nextPieceCount;
        this.field = new Field(engine.field);
        this.holdPiece = engine.holdPieceObject == null ? null : new Piece(engine.holdPieceObject);
    }
}
