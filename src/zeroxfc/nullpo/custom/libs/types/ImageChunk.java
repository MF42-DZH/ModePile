package zeroxfc.nullpo.custom.libs.types;

/** Represents a chunk of an image to draw. */
public class ImageChunk {
    private final int[] sourceLocation;
    private ObjectAlignment alignment;
    private int[] anchorLocation;
    private int[] drawLocation;
    private int[] sourceDimensions;
    private float[] scale;

    public ImageChunk() {
        this(ObjectAlignment.TOP_LEFT, new int[] { 0, 0 }, new int[] { 0, 0 }, new int[] { 1, 1 }, new float[] { 1, 1 });
    }

    public ImageChunk(ObjectAlignment alignment, int[] anchorLocation, int[] sourceLocation, int[] sourceDimensions, float[] scale) {
        this.alignment = alignment;
        this.anchorLocation = anchorLocation;
        this.sourceLocation = sourceLocation;
        this.sourceDimensions = sourceDimensions;
        this.scale = scale;

        calibrateDrawLocation();
    }

    private void calibrateDrawLocation() {
        int[] ddim = getDrawDimensions();

        switch (alignment) {
            case TOP_MIDDLE:
                drawLocation = new int[] { anchorLocation[0] - (ddim[0] / 2), anchorLocation[1] };
                break;
            case TOP_RIGHT:
                drawLocation = new int[] { anchorLocation[0] - ddim[0], anchorLocation[1] };
                break;
            case MIDDLE_LEFT:
                drawLocation = new int[] { anchorLocation[0], anchorLocation[1] - (ddim[1] / 2) };
                break;
            case MIDDLE_MIDDLE:
                drawLocation = new int[] { anchorLocation[0] - (ddim[0] / 2), anchorLocation[1] - (ddim[1] / 2) };
                break;
            case MIDDLE_RIGHT:
                drawLocation = new int[] { anchorLocation[0] - ddim[0], anchorLocation[1] - (ddim[1] / 2) };
                break;
            case BOTTOM_LEFT:
                drawLocation = new int[] { anchorLocation[0], anchorLocation[1] - ddim[1] };
                break;
            case BOTTOM_MIDDLE:
                drawLocation = new int[] { anchorLocation[0] - (ddim[0] / 2), anchorLocation[1] - ddim[1] };
                break;
            case BOTTOM_RIGHT:
                drawLocation = new int[] { anchorLocation[0] - ddim[0], anchorLocation[1] - ddim[1] };
                break;
            default:
                drawLocation = new int[] { anchorLocation[0], anchorLocation[1] };
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

    public int[] getAnchorLocation() {
        return anchorLocation;
    }

    public void setAnchorLocation(int[] anchorLocation) {
        this.anchorLocation = anchorLocation;
        calibrateDrawLocation();
    }

    public float[] getScale() {
        return scale;
    }

    public void setScale(float[] scale) {
        this.scale = scale;
        calibrateDrawLocation();
    }

    public int[] getSourceLocation() {
        return sourceLocation;
    }

    public int[] getSourceDimensions() {
        return sourceDimensions;
    }

    public void setSourceDimensions(int[] sourceDimensions) {
        this.sourceDimensions = sourceDimensions;
        calibrateDrawLocation();
    }

    public int[] getDrawDimensions() {
        return new int[] { (int) (sourceDimensions[0] * scale[0]), (int) (sourceDimensions[1] * scale[1]) };
    }

    public int[] getDrawLocation() {
        return drawLocation;
    }
}
