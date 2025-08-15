package zeroxfc.nullpo.custom.libs.types;

/** Represents a chunk of an image to draw. */
public class ImageChunk {
    private ObjectAlignment alignment;

    // New Members
    private int anchorX, anchorY;
    private int sourceX, sourceY;
    private int sourceDimensionsX, sourceDimensionsY;
    private float scaleX, scaleY;

    private int drawLocationX, drawLocationY;

    public ImageChunk() {
        this(
            ObjectAlignment.TOP_LEFT,
            0, 0,
            0, 0,
            1, 1,
            1f, 1f
        );
    }

    public ImageChunk(ObjectAlignment alignment, int anchorX, int anchorY, int sourceX, int sourceY, int sourceDimensionsX, int sourceDimensionsY, float scaleX, float scaleY) {
        this.alignment = alignment;

        this.anchorX = anchorX;
        this.anchorY = anchorY;
        this.sourceX = sourceX;
        this.sourceY = sourceY;
        this.sourceDimensionsX = sourceDimensionsX;
        this.sourceDimensionsY = sourceDimensionsY;
        this.scaleX = scaleX;
        this.scaleY = scaleY;

        calibrateDrawLocation();
    }

    private void calibrateDrawLocation() {
        final int ddimX = getDrawDimensionsX();
        final int ddimY = getDrawDimensionsY();

        switch (alignment) {
            case TOP_MIDDLE:
                drawLocationX = anchorX - (ddimX / 2);
                drawLocationY = anchorY;
                break;
            case TOP_RIGHT:
                drawLocationX = anchorX - ddimX;
                drawLocationY = anchorY;
                break;
            case MIDDLE_LEFT:
                drawLocationX = anchorX;
                drawLocationY = anchorY - (ddimY / 2);
                break;
            case MIDDLE_MIDDLE:
                drawLocationX = anchorX - (ddimX / 2);
                drawLocationY = anchorY - (ddimY / 2);
                break;
            case MIDDLE_RIGHT:
                drawLocationX = anchorX - ddimX;
                drawLocationY = anchorY - (ddimY / 2);
                break;
            case BOTTOM_LEFT:
                drawLocationX = anchorX;
                drawLocationY = anchorY - ddimY;
                break;
            case BOTTOM_MIDDLE:
                drawLocationX = anchorX - (ddimX / 2);
                drawLocationY = anchorY - ddimY;
                break;
            case BOTTOM_RIGHT:
                drawLocationX = anchorX - ddimX;
                drawLocationY = anchorY - ddimY;
                break;
            default:
                drawLocationX = anchorX;
                drawLocationY = anchorY;
                break;
        }
    }

    public ObjectAlignment getAlignment() {
        return alignment;
    }

    public void setAlignment(ObjectAlignment alignment) {
        this.alignment = alignment;

        calibrateDrawLocation();
    }

    public int getAnchorX() {
        return anchorX;
    }

    public int getAnchorY() {
        return anchorY;
    }

    public void setAnchor(int x, int y) {
        this.anchorX = x;
        this.anchorY = y;

        calibrateDrawLocation();
    }

    public float getScaleX() {
        return scaleX;
    }

    public float getScaleY() {
        return scaleY;
    }

    public void setScale(float scaleX, float scaleY) {
        this.scaleX = scaleX;
        this.scaleY = scaleY;

        calibrateDrawLocation();
    }

    public int getSourceX() {
        return sourceX;
    }

    public int getSourceY() {
        return sourceY;
    }

    private void setSourceLocation(int x, int y) {
        this.sourceX = x;
        this.sourceY = y;
    }

    public int getSourceDimensionsX() {
        return sourceDimensionsX;
    }

    public int getSourceDimensionsY() {
        return sourceDimensionsY;
    }

    public void setSourceDimensions(int sizeX, int sizeY) {
        this.sourceDimensionsX = sizeX;
        this.sourceDimensionsY = sizeY;

        calibrateDrawLocation();
    }

    public int getDrawDimensionsX() {
        return (int) (sourceDimensionsX * scaleX);
    }

    public int getDrawDimensionsY() {
        return (int) (sourceDimensionsY * scaleY);
    }

    public int getDrawLocationX() {
        return drawLocationX;
    }

    public int getDrawLocationY() {
        return drawLocationY;
    }
}
