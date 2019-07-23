package zeroxfc.nullpo.custom.libs;

public class Interpolation {
	public static int lerp(int v0, int v1, double lerpVal) {
		return (int)((1.0 - lerpVal) * v0) + (int)(lerpVal * v1);
	}
	
	public static double lerp(double v0, double v1, double lerpVal) {
		return ((1.0 - lerpVal) * v0) + (lerpVal * v1);
	}
	
	public static long lerp(long v0, long v1, double lerpVal) {
		return (long)((1.0 - lerpVal) * v0) + (long)(lerpVal * v1);
	}
	
	public static float lerp(float v0, float v1, double lerpVal) {
		return (float)((1.0 - lerpVal) * v0) + (float)(lerpVal * v1);
	}
}
