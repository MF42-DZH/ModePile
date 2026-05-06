package zeroxfc.nullpo.custom.modes.objects.fluorescence;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import mu.nu.nullpo.game.component.Block;
import mu.nu.nullpo.game.component.Piece;

public class FluorescenceRandomizer {
    private static final int BAGS = 4;

    private int colourA;
    private int colourB;

    private final Random random;
    private final List<PieceShape> bag;

    public FluorescenceRandomizer(Random random, int colourA, int colourB) {
        this.random = random;
        this.bag = new ArrayList<>(PieceShape.values().length * BAGS);

        this.colourA = colourA;
        this.colourB = colourB;
    }

    public void setColours(int a, int b) {
        colourA = a;
        colourB = b;
    }

    private void fillBag() {
        for (final PieceShape shape : PieceShape.values()) {
            for (int i = 0; i < BAGS; ++i) {
                bag.add(shape);
            }
        }
    }

    public void initPiece(Piece piece) {
        if (piece.id != Piece.PIECE_O) throw new IllegalArgumentException("Not an O-piece!");

        // By default, the O is described in clockwise order, from the top left.
        if (bag.isEmpty()) fillBag();

        final PieceShape shape = bag.remove(random.nextInt(bag.size()));

        final int usedA = random.nextBoolean() ? colourA : colourB;
        final int usedB = usedA == colourA ? colourB : colourA;

        switch (shape) {
            case BLANK:
                piece.block[0].color = usedA;
                piece.block[1].color = usedA;
                piece.block[2].color = usedA;
                piece.block[3].color = usedA;
                break;
            case ONE_THREE:
                piece.block[0].color = usedA;
                piece.block[1].color = usedA;
                piece.block[2].color = usedA;
                piece.block[3].color = usedB;
                break;
            case DUO:
                piece.block[0].color = usedA;
                piece.block[1].color = usedB;
                piece.block[2].color = usedB;
                piece.block[3].color = usedA;
                break;
            case CROSS:
                piece.block[0].color = usedA;
                piece.block[1].color = usedB;
                piece.block[2].color = usedA;
                piece.block[3].color = usedB;
                break;
        }

        for (final Block block : piece.block) block.bonusValue = 10;
        piece.direction = random.nextInt(Piece.DIRECTION_COUNT);
    }

    private enum PieceShape {
        BLANK, ONE_THREE, DUO, CROSS
    }
}
