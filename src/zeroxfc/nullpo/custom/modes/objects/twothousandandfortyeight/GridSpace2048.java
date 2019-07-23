package zeroxfc.nullpo.custom.modes.objects.twothousandandfortyeight;

public class GridSpace2048 {
	private int value;
	private boolean merged;

	public GridSpace2048(int value) {
		this.value = value;
		this.merged = false;
	}

	public GridSpace2048(int value, boolean merged) {
		this.value = value;
		this.merged = merged;
	}

	public GridSpace2048() {
		this(0);
	}

	public boolean isEqual(GridSpace2048 gridSpace) {
	    return value == gridSpace.getValue();
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	public boolean getMerged() {
		return merged;
	}

	public void setMerged(boolean merged) {
		this.merged = merged;
	}

	public GridSpace2048 clone() {
		return new GridSpace2048(value, merged);
	}
}
