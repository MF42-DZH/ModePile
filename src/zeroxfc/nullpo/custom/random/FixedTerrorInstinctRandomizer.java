package zeroxfc.nullpo.custom.random;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import mu.nu.nullpo.game.component.Piece;
import net.omegaboshi.nullpomino.game.subsystem.randomizer.Randomizer;

public class FixedTerrorInstinctRandomizer extends Randomizer {
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
    private int tiRandState;

    private short tiRandNext() {
        int result = (int) (0x41C64E6DL * tiRandState + 12345L);
        tiRandState = result;
        return (short) ((result >>> 10) & 0x7FFF);
    }

    private short tiRandInt(short upperBound) {
        return (short) (tiRandNext() % upperBound);
    }

    @Override
    public void reseed(long seed) {
        super.reseed(seed);
        tiRandState = (int) seed;
    }

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
            if (i > 1) history.add(Piece.PIECE_S);
            else history.add(Piece.PIECE_Z);
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
                piece = piecePool.get(tiRandInt((short) piecePool.size()));
                history.set(0, piece);
            } while (piece == Piece.PIECE_S || piece == Piece.PIECE_Z || piece == Piece.PIECE_O);
        } else {
            for (int rolls = 0; rolls < MAX_ROLLS; ++rolls) {
                bagPos = tiRandInt((short) piecePool.size());
                piece = piecePool.get(bagPos);

                if (!history.contains(piece)) break;

                insertDroughtedPieceIntoBagAt(bagPos);

                bagPos = tiRandInt((short) piecePool.size());
                piece = piecePool.get(bagPos);
            }
        }

        updateDroughtHistogram(piece);
        insertDroughtedPieceIntoBagAt(bagPos);

        history.set(3, history.get(2));
        history.set(2, history.get(1));
        history.set(1, history.get(0));
        history.set(0, piece);

        ++count;

        return piece;
    }

    private void insertDroughtedPieceIntoBagAt(int bagPos) {
        // Originally in the TI code, this line was omitted, causing a bug where the bag is not updated.
        int highScore = 0;
        int droughted = 0;

        for (int pid : PIECE_ORDER) {
            final Integer score = histogram.get(pid);
            if (score != null && highScore < score) {
                highScore = score;
                droughted = pid;
            }

            piecePool.set(bagPos, droughted);
        }
    }

    private void updateDroughtHistogram(int id) {
        for (int key : histogram.keySet()) {
            if (key == id) histogram.put(key, 0);
            else histogram.put(key, histogram.get(key) + 1);
        }
    }
}
