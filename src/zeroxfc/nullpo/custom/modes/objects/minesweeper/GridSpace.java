package zeroxfc.nullpo.custom.modes.objects.minesweeper;

public class GridSpace {
    public int surroundingMines;
    public boolean isMine;
    public boolean uncovered;
    public boolean flagged;
    public boolean question;

    public GridSpace( boolean isMine ) {
        uncovered = false;
        this.isMine = isMine;
        surroundingMines = 0;
        flagged = false;
        question = false;
    }
}
