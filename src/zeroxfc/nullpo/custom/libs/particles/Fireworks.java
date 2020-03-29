package zeroxfc.nullpo.custom.libs.particles;

import zeroxfc.nullpo.custom.libs.DoubleVector;
import zeroxfc.nullpo.custom.libs.Interpolation;

import java.util.Random;

public class Fireworks extends ParticleEmitter {
	/** Gravity */
	private static final double GRAVITY = 4.9 / 60;

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
	 * <code>params</code> are min start X location, max start X location, min start Y location, max start Y location
	 * red, green, blue, alpha, max colour variance (all <code>int</code> type),
	 * max velocity (velocity is a <code>double</code>) in that order.
	 *
	 * @param num    Number of particles / particle groups.
	 * @param params Parameters to pass onto the particles.
	 */
	@Override
	public void addNumber(int num, Object[] params) {
		int minX, maxX, minY, maxY, red, green, blue, alpha, variance;
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

			int ured, ugreen, ublue, ualpha;
			ured = red + (int)(2 * randomiser.nextDouble() * variance - variance);
			ugreen = green + (int)(2 * randomiser.nextDouble() * variance - variance);
			ublue = blue + (int)(2 * randomiser.nextDouble() * variance - variance);
			ualpha = alpha + (int)(2 * randomiser.nextDouble() * variance - variance);

			for (int i = 0; i < num; ++i) {
				Particle particle = new Particle(
						Particle.ParticleShape.Rectangle,
						120 + (int)(2 * randomiser.nextDouble() * 30 - 30),
						new DoubleVector(Interpolation.lerp(minX, maxX, randomiser.nextDouble()),Interpolation.lerp(minY, maxY, randomiser.nextDouble()),false),
						new DoubleVector(2 * randomiser.nextDouble() - maxVelocity, 2 * randomiser.nextDouble() - maxVelocity,false),
						new DoubleVector(0, GRAVITY, false),
						2, 2,
						ured, ugreen, ublue, ualpha,
						(int)(ured / 1.5), (int)(ugreen / 1.5), (int)(ublue / 1.5), 0
				);

				particles.add(particle);
			}
		} catch (ClassCastException ce) {
			log.error("Fireworks.addNumber: Invalid argument in params.", ce);
		} catch (Exception e) {
			log.error("Fireworks.addNumber: Other exception occurred.", e);
		}
	}
}
