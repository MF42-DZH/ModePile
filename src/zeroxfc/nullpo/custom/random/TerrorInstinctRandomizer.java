package zeroxfc.nullpo.custom.random;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import mu.nu.nullpo.game.component.Piece;
import net.omegaboshi.nullpomino.game.subsystem.randomizer.Randomizer;

public class TerrorInstinctRandomizer extends Randomizer {
    private static final int MAX_ROLLS = 6;
    private static final int[] PIECE_ORDER = {
        Piece.PIECE_I,
        Piece.PIECE_Z,
        Piece.PIECE_S,
        Piece.PIECE_J,
        Piece.PIECE_L,
        Piece.PIECE_O,
        Piece.PIECE_T,
        Piece.PIECE_I3,
        Piece.PIECE_L3,
        Piece.PIECE_I2,
        Piece.PIECE_I1,
    };

    private ArrayList<Integer> piecePool;
    private HashMap<Integer, Integer> histogram;
    private ArrayList<Integer> history;

    private int count;

    @Override
    public void init() {
        piecePool = new ArrayList<>();
        histogram = new HashMap<>();
        history = new ArrayList<>();

        count = 0;

        // For consistency's sake, we'll add the same pieces in order.
        for (int i : PIECE_ORDER) {
            if (Arrays.stream(pieces).anyMatch(p -> p == i)) {
                for (int j = 0; j < 5; j++) {
                    piecePool.add(i);
                }

                histogram.put(i, 4);
            }
        }

        for (int i = 0; i < 4; i++) {
            history.add(i > 1 ? Piece.PIECE_S : Piece.PIECE_Z);
        }
    }

    /**
     * This is overridden as changing piece count screws with the pool size.<br />
     * Therefore this calso calls <code>init()</code>.
     *
     * @param pieceEnable Array of enabled pieces.
     */
    @Override
    public void setPieceEnable(boolean[] pieceEnable) {
        super.setPieceEnable(pieceEnable);
        init();
    }

    /**
     * Based on <a href="https://tetrisconcept.net/threads/randomizer-theory.512/page-12#post-65418">this post</a>.
     * This implementation also fixes the droughted piece bug by adding a missing variable initialisation.
     */
    @Override
    public int next() {
        int bagPos = 0;
        int piece = 0;

        if (count == 0 && !isPieceSZOOnly()) {
            do {
                piece = piecePool.get(r.nextInt(piecePool.size()));
                history.set(0, piece);
            } while (piece == Piece.PIECE_S || piece == Piece.PIECE_Z || piece == Piece.PIECE_O);
        } else {
            for (int rolls = 0; rolls < MAX_ROLLS; ++rolls) {
                bagPos = r.nextInt(piecePool.size());
                piece = piecePool.get(bagPos);

                if (!history.contains(piece)) break;

                insertDroughtedPieceIntoBagAt(bagPos);

                bagPos = r.nextInt(piecePool.size());
                piece = piecePool.get(bagPos);
            }
        }

        updateDroughtHistogram(piece);
        insertDroughtedPieceIntoBagAt(bagPos);
        pushHistory(piece);

        ++count;

        return piece;
    }

    private void pushHistory(int id) {
        history.set(3, history.get(2));
        history.set(2, history.get(1));
        history.set(1, history.get(0));
        history.set(0, id);
    }

    private void insertDroughtedPieceIntoBagAt(int bagPos) {
        // Originally in the TI code there is a bug where the pool failed to be updated correctly in the
        // rare situation where you exhaust all the rerolls, then having the final roll give you the most droughted piece.
        // Instead of inserting the newly most droughted piece after the histogram update, it would instead place an instance
        // of the piece you've been given into the pool.
        // This update code used to be inside the same function as the piece rolling function, with the highScore variable
        // being initialised only once at the start, which was what caused the bug, as the variable was not re-initialised
        // to zero after the piece rolling loop, causing the previously used highScore from the last loop-around to stay
        // when the find-droughted-then-put-in-pool loop is run.
        int highScore = 0;
        int droughted = 0;

        for (int pid : PIECE_ORDER) {
            final Integer score = histogram.get(pid);
            if (score != null && highScore < score) {
                highScore = score;
                droughted = pid;
            }
        }

        piecePool.set(bagPos, droughted);
    }

    private void updateDroughtHistogram(int id) {
        for (Map.Entry<Integer, Integer> entry : histogram.entrySet()) {
            if (entry.getKey() == id) histogram.put(entry.getKey(), 0);
            else entry.setValue(entry.getValue() + 1);
        }
    }
}
