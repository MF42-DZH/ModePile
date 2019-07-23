package zeroxfc.nullpo.custom.libs;

public class SideWaveText {
	public static final int MaxLifeTime = 120;
	private static final DoubleVector VerticalVelocity = new DoubleVector(0, -1 * (4.0/5.0), false);
	
	private DoubleVector position;
	private String text;
	private double xOffset;
	private double offsetMax;
	private double sinFrequency;
	private double sinPhase;
	private int lifeTime;
	private boolean big;
	private boolean lClear;
	
	public SideWaveText(int x, int y, double frequency, double offsetWidth, String text, boolean big, boolean largeclear) {
		this.text = text;
		position = new DoubleVector(x, y, false);
		
		sinPhase = 0.0;
		sinFrequency = frequency;
		offsetMax = offsetWidth;
		this.big = big;
		lClear = largeclear;
		
		xOffset = 0;
		lifeTime = 0;
	}
	
	public void update() {
		sinPhase += sinFrequency * ((Math.PI * 2) / 60);
		
		xOffset = offsetMax * Math.sin(sinPhase);
		
		position = DoubleVector.add(position, VerticalVelocity);
		
		lifeTime++;
	}
	
	public int[] getLocation() {
		return new int[] { (int)(position.getX() + xOffset), (int)position.getY() };
	}
	
	public int getLifeTime() {
		return lifeTime;
	}
	
	public boolean getBig() {
		return big;
	}
	
	public boolean getLargeClear() {
		return lClear;
	}
	
	public String getText() {
		return text;
	}
}
