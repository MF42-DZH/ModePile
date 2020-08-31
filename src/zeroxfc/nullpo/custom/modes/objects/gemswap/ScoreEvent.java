package zeroxfc.nullpo.custom.modes.objects.gemswap;

public class ScoreEvent {
    private int scoreValue;
    private int x;
    private int y;
    private int origin;
    private int colour;

    public ScoreEvent( int scoreValue, int x, int y, int origin, int colour ) {
        this.scoreValue = scoreValue;
        this.x = x;
        this.y = y;
        this.origin = origin;
        this.colour = colour;
    }

    public int getColour() {
        return colour;
    }

    public int getOrigin() {
        return origin;
    }

    public int getScoreValue() {
        return scoreValue;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

}
