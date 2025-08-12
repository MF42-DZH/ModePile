package zeroxfc.nullpo.custom.libs.types;

import zeroxfc.nullpo.custom.libs.types.tuples.FloatPair;
import zeroxfc.nullpo.custom.libs.types.tuples.IntPair;

/** Represents a chunk of an image to draw. */
public class ImageChunk {
    private ObjectAlignment alignment;

    private IntPair anchorLocation;
    private IntPair sourceLocation;
    private IntPair sourceDimensions;
    private FloatPair scale;

    private IntPair drawLocation;

    public ImageChunk() {
        this(ObjectAlignment.TOP_LEFT, IntPair.of(0, 0), IntPair.of(0, 0), IntPair.of(1, 1), FloatPair.of(1f, 1f));
    }

    public ImageChunk(ObjectAlignment alignment, IntPair anchorLocation, IntPair sourceLocation, IntPair sourceDimensions, FloatPair scale) {
        this.alignment = alignment;

        this.anchorLocation = anchorLocation;
        this.sourceLocation = sourceLocation;
        this.sourceDimensions = sourceDimensions;
        this.scale = scale;

        calibrateDrawLocation();
    }

    private void calibrateDrawLocation() {
        IntPair ddim = getDrawDimensions();

        switch (alignment) {
            case TOP_MIDDLE:
                drawLocation = IntPair.of(anchorLocation.valL - (ddim.valL / 2), anchorLocation.valR);
                break;
            case TOP_RIGHT:
                drawLocation = IntPair.of(anchorLocation.valL - ddim.valL, anchorLocation.valR);
                break;
            case MIDDLE_LEFT:
                drawLocation = IntPair.of(anchorLocation.valL, anchorLocation.valR - (ddim.valR / 2));
                break;
            case MIDDLE_MIDDLE:
                drawLocation = IntPair.of(anchorLocation.valL - (ddim.valL / 2), anchorLocation.valR - (ddim.valR / 2));
                break;
            case MIDDLE_RIGHT:
                drawLocation = IntPair.of(anchorLocation.valL - ddim.valL, anchorLocation.valR - (ddim.valR / 2));
                break;
            case BOTTOM_LEFT:
                drawLocation = IntPair.of(anchorLocation.valL, anchorLocation.valR - ddim.valR);
                break;
            case BOTTOM_MIDDLE:
                drawLocation = IntPair.of(anchorLocation.valL - (ddim.valL / 2), anchorLocation.valR - ddim.valR);
                break;
            case BOTTOM_RIGHT:
                drawLocation = IntPair.of(anchorLocation.valL - ddim.valL, anchorLocation.valR - ddim.valR);
                break;
            default:
                drawLocation = IntPair.of(anchorLocation.valL, anchorLocation.valR);
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

    public IntPair getAnchorLocation() {
        return anchorLocation;
    }

    public void setAnchorLocation(IntPair anchorLocation) {
        this.anchorLocation = anchorLocation;
        calibrateDrawLocation();
    }

    public FloatPair getScale() {
        return scale;
    }

    public void setScale(FloatPair scale) {
        this.scale = scale;
        calibrateDrawLocation();
    }

    public IntPair getSourceLocation() {
        return sourceLocation;
    }

    private void setSourceLocation(IntPair sourceLocation) {
        this.sourceLocation = sourceLocation;
    }

    public IntPair getSourceDimensions() {
        return sourceDimensions;
    }

    public void setSourceDimensions(IntPair sourceDimensions) {
        this.sourceDimensions = sourceDimensions;
        calibrateDrawLocation();
    }

    public IntPair getDrawDimensions() {
        return IntPair.of((int) (sourceDimensions.valL * scale.valL), (int) (sourceDimensions.valR * scale.valR));
    }

    public IntPair getDrawLocation() {
        return drawLocation;
    }
}
