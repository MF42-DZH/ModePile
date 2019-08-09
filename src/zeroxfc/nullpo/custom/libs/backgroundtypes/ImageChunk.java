package zeroxfc.nullpo.custom.libs.backgroundtypes;

public class ImageChunk {
	private int anchorType;
	private int[] anchorLocation;
	private int[] drawLocation;
	private int[] sourceLocation;
	private int[] sourceDimensions;
	private float[] scale;

	public static final int ANCHOR_POINT_TL = 0, ANCHOR_POINT_TM = 1, ANCHOR_POINT_TR = 2, ANCHOR_POINT_ML = 3, ANCHOR_POINT_MM = 4, ANCHOR_POINT_MR = 5, ANCHOR_POINT_LL = 6, ANCHOR_POINT_LM = 7, ANCHOR_POINT_LR = 8;

	public ImageChunk() {
		this(0, new int[] { 0, 0 }, new int[] { 0, 0 }, new int[] { 1, 1 }, new float[] { 1, 1 });
	}

	public ImageChunk(int anchorType, int[] anchorLocation, int[] sourceLocation, int[] sourceDimensions, float[] scale) {
		this.anchorType = anchorType;
		this.anchorLocation = anchorLocation;
		this.sourceLocation = sourceLocation;
		this.sourceDimensions = sourceDimensions;
		this.scale = scale;

		calibrateDrawLocation();
	}

	private void calibrateDrawLocation() {
		int[] ddim = getDrawDimensions();

		switch (anchorType) {
			case ANCHOR_POINT_TM:
				drawLocation = new int[] { anchorLocation[0] - (ddim[0] / 2), anchorLocation[1] };
				break;
			case ANCHOR_POINT_TR:
				drawLocation = new int[] { anchorLocation[0] - ddim[0], anchorLocation[1] };
				break;
			case ANCHOR_POINT_ML:
				drawLocation = new int[] { anchorLocation[0], anchorLocation[1] - (ddim[1] / 2) };
				break;
			case ANCHOR_POINT_MM:
				drawLocation = new int[] { anchorLocation[0] - (ddim[0] / 2), anchorLocation[1] - (ddim[1] / 2) };
				break;
			case ANCHOR_POINT_MR:
				drawLocation = new int[] { anchorLocation[0] - ddim[0], anchorLocation[1]  - (ddim[1] / 2) };
				break;
			case ANCHOR_POINT_LL:
				drawLocation = new int[] { anchorLocation[0], anchorLocation[1] - ddim[1] };
				break;
			case ANCHOR_POINT_LM:
				drawLocation = new int[] { anchorLocation[0] - (ddim[0] / 2), anchorLocation[1] - ddim[1] };
				break;
			case ANCHOR_POINT_LR:
				drawLocation = new int[] { anchorLocation[0] - ddim[0], anchorLocation[1] - ddim[1] };
				break;
			default:
				drawLocation = new int[] { anchorLocation[0], anchorLocation[1] };
				break;
		}
	}

	public void setAnchorLocation(int[] anchorLocation) {
		this.anchorLocation = anchorLocation;
		calibrateDrawLocation();
	}

	public int[] getAnchorLocation() {
		return anchorLocation;
	}

	public void setSourceDimensions(int[] sourceDimensions) {
		this.sourceDimensions = sourceDimensions;
		calibrateDrawLocation();
	}

	public void setScale(float[] scale) {
		this.scale = scale;
		calibrateDrawLocation();
	}

	public float[] getScale() {
		return scale;
	}

	public int[] getSourceLocation() {
		return sourceLocation;
	}

	public int[] getSourceDimensions() {
		return sourceDimensions;
	}

	public int[] getDrawDimensions() {
		return new int[] { (int)(sourceDimensions[0] * scale[0]), (int)(sourceDimensions[1] * scale[1]) };
	}

	public int[] getDrawLocation() {
		return drawLocation;
	}
}
