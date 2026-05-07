package zeroxfc.nullpo.custom.libs;

import java.util.LinkedList;
import java.util.List;
import mu.nu.nullpo.game.component.Piece;
import mu.nu.nullpo.game.event.EventReceiver;
import mu.nu.nullpo.game.play.GameEngine;
import zeroxfc.nullpo.custom.libs.types.tuples.IntPair;

public final class HardDropTrail {
    private final List<IntPair> trailCoordList;
    private Piece currentPiece;

    public HardDropTrail() {
        this.trailCoordList = new LinkedList<>();
    }

    // Call in onFirst.
    public void inOnFirst() {
        trailCoordList.clear();
        currentPiece = null;
    }

    public void addPiece(GameEngine engine, int fall) {
        currentPiece = new Piece(engine.nowPieceObject);

        for (int i = 1; i <= fall; i++) {
            trailCoordList.add(
                IntPair.of(engine.nowPieceX, engine.nowPieceY - i)
            );
        }
    }

    public void draw(RendererExtension rendererExtension, EventReceiver receiver, GameEngine engine, int playerID) {
        final int baseX = receiver.getFieldDisplayPositionX(engine, playerID) + 4;
        final int baseY = receiver.getFieldDisplayPositionY(engine, playerID) + 52;

        if (!trailCoordList.isEmpty() && currentPiece != null) {
            for (final IntPair loc : trailCoordList) {
                final int cx = baseX + (16 * loc.valL);
                final int cy = baseY + (16 * loc.valR);

                rendererExtension.drawScaledPiece(receiver, engine, playerID, cx, cy, currentPiece, 1f, 1f, 0f);
            }
        }
    }
}
