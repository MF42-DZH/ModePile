package zeroxfc.nullpo.custom.libs.particles;

import zeroxfc.nullpo.custom.libs.DoubleVector;
import zeroxfc.nullpo.custom.libs.Interpolation;

import java.util.Random;

public class Fireworks extends ParticleEmitter {
	/** Gravity */
	private static final double GRAVITY = 2.4d / 30d;

	/** Default max velocity */
	public static final double DEF_MAX_VEL = 3.2;

	/** Default min lifetime */
	public static final int DEF_MIN_LIFE = 60;

	/** Default max lifetime */
	public static final int DEF_MAX_LIFE = 120;

	/**
	 * Default colour set.<br />
	 * In order: Gray, Red, Orange, Yellow, Green, Cyan, Blue, Purple<br />
	 * Parameters: Red, Green, Blue, Alpha, Variance
	 */
	public static final int[][] DEF_COLOURS = {
			new int[] { 240, 240, 240, 235, 20 },
			new int[] { 240,  30,   0, 235, 20 },
			new int[] { 240, 130,   0, 235, 20 },
			new int[] { 240, 240,   0, 235, 20 },
			new int[] {  30, 240,   0, 235, 20 },
			new int[] {   0, 240, 240, 235, 20 },
			new int[] {   0,  30, 240, 235, 20 },
			new int[] { 210,   0, 210, 235, 20 }
	};

	/** Randomiser */
	private Random randomiser;

	/**
	 * Parameterless constructor. Uses time as the random seed.
	 */
	public Fireworks() {
	    this(new Random());
	}

	/**
	 * Constructor that uses a fixed random seed.
	 * @param seed Random seed
	 */
	public Fireworks(long seed) {
	    randomiser = new Random(seed);
	}

	/**
	 * Constructor that uses a fixed random object.
	 * @param random Random instance
	 */
	public Fireworks(Random random) {
	    randomiser = random;
	}

	/**
	 * Add some number of fireworks.
	 * <code>params</code> are min start X location, max start X location, min start Y location, max start Y location,
	 * red, green, blue, alpha, max colour variance (all <code>int</code> type),
	 * max velocity (velocity is a <code>double</code>),
	 * min lifetime, max lifetime (both <code>int</code>) in that order.
	 *
	 * @param num    Number of particles / particle groups.
	 * @param params Parameters to pass onto the particles.
	 */
	@Override
	public void addNumber(int num, Object[] params) {
		int minX, maxX, minY, maxY, red, green, blue, alpha, variance, minLifeTime, maxLifeTime;
		double maxVelocity;

		try {
			minX = (int)params[0];
			maxX = (int)params[1];
			minY = (int)params[2];
			maxY = (int)params[3];

			red = (int)params[4];
			green = (int)params[5];
			blue = (int)params[6];
			alpha = (int)params[7];
			variance = (int)params[8];

			maxVelocity = (double)params[9];

			minLifeTime = (int)params[10];
			maxLifeTime = (int)params[11];

			int ured, ugreen, ublue, ualpha;
			ured = red + (int)(2 * randomiser.nextDouble() * variance - variance);
			ugreen = green + (int)(2 * randomiser.nextDouble() * variance - variance);
			ublue = blue + (int)(2 * randomiser.nextDouble() * variance - variance);
			ualpha = alpha + (int)(2 * randomiser.nextDouble() * variance - variance);

			for (int i = 0; i < num; ++i) {
				DoubleVector origin = new DoubleVector(Interpolation.lerp(minX, maxX, randomiser.nextDouble()),Interpolation.lerp(minY, maxY, randomiser.nextDouble()),false);
				for (int j = 0; j < randomiser.nextInt(121) + 120; ++j) {
					int s = 1 + randomiser.nextInt(3);
					DoubleVector v = new DoubleVector(2 * randomiser.nextDouble() * maxVelocity - maxVelocity, 2 * randomiser.nextDouble() * Math.PI, true);

					Particle particle = new Particle(
							Particle.ParticleShape.Rectangle,
							Interpolation.lerp(minLifeTime, maxLifeTime, randomiser.nextDouble()),
							origin,
							v,
							new DoubleVector(0, GRAVITY, false),
							s, s,
							ured, ugreen, ublue, ualpha,
							(int)(ured / 1.5), (int)(ugreen / 1.5), (int)(ublue / 1.5), 64
					);

					particles.add(particle);
				}
			}
		} catch (ClassCastException ce) {
			log.error("Fireworks.addNumber: Invalid argument in params.", ce);
		} catch (Exception e) {
			log.error("Fireworks.addNumber: Other exception occurred.", e);
		}
	}
}
