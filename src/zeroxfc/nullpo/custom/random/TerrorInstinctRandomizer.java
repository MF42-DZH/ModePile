package zeroxfc.nullpo.custom.random;

import java.util.ArrayList;
import mu.nu.nullpo.game.component.Piece;
import net.omegaboshi.nullpomino.game.subsystem.randomizer.Randomizer;

public class TerrorInstinctRandomizer extends Randomizer {
    private static final int MAX_ROLLS = 6;

    private ArrayList<Integer> piecePool;
    private ArrayList<Integer> history;

    private int count;

    @Override
    public void init() {
        piecePool = new ArrayList<>();
        history = new ArrayList<>();

        count = 0;

        for (int i : pieces) {
            for (int j = 0; j < 5; j++) {
                piecePool.add(i);
            }
        }

        for (int i = 0; i < 4; i++) {
            if (i % 2 == 0) history.add(Piece.PIECE_S);
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
        int piece = 0;
        for (int i = 0; i < Piece.PIECE_COUNT; i++) {
            if (pieceEnable[i]) piece++;
        }
        pieces = new int[piece];
        piece = 0;
        for (int i = 0; i < Piece.PIECE_COUNT; i++) {
            if (pieceEnable[i]) {
                pieces[piece] = i;
                piece++;
            }
        }

        init();
    }

    @Override
    public int next() {
        int idx, id, rolls = 0;

        do {
            idx = r.nextInt(piecePool.size());
            id = piecePool.get(idx);

            rolls++;
        } while ((history.contains(id) && rolls < MAX_ROLLS) || (count == 0 && id == Piece.PIECE_O));

        count++;
        appendHistory(id, idx);

        return id;
    }

    private void appendHistory(int id, int idx) {
        int temp = history.remove(0);
        history.add(id);
        piecePool.remove(idx);

        if (count > 4) {
            piecePool.add(temp);
        }
    }
}
