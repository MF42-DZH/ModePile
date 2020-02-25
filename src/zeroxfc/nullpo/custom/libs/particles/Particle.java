package zeroxfc.nullpo.custom.libs.particles;

import mu.nu.nullpo.game.event.EventReceiver;
import zeroxfc.nullpo.custom.libs.DoubleVector;

public class Particle {
	/** Default colour */
	private static final int[] DEFAULT_COLOUR = { 255, 255, 255, 255 };

	/** Particle shape */
	private ParticleShape shape;

	/** Lifetime */
	private final int particleMaxLifetime;

	/** Current life */
	private int particleLifetime;

	/** Position vector */
	private DoubleVector position;

	/** Velocity vector */
	private DoubleVector velocity;

	/** Acceleration vector */
	private DoubleVector acceleration;

	/*
	 * Colour variables.
	 * Please use <code>0 <= value <= 255</code>
	 */

	/** X size */
	private int sizeX;

	/** Y size */
	private int sizeY;

	/** Red colour component */
	private int red;

	/** Green colour component */
	private int green;

	/** Blue colour component */
	private int blue;

	/** Alpha component */
	private int alpha;

	/**
	 * Create an instance of a particle.
	 * @param shape The shape of the particle. Warning: SDL cannot draw circular particles.
	 * @param maxLifeTime The maximum frame lifetime of the particle.
	 * @param position Vector position of the particle.
	 * @param velocity Vector velocity of the particle.
	 * @param acceleration Vector acceleration of the particle.
	 * @param sizeX Horizontal size of the particle.
	 * @param sizeY Vertical size of the particle.
	 * @param red Red component of colour.
	 * @param green Green component of colour.
	 * @param blue Blue component of colour.
	 * @param alpha Alpha component of colour.
	 */
	public Particle(ParticleShape shape, int maxLifeTime, DoubleVector position, DoubleVector velocity, DoubleVector acceleration, int sizeX, int sizeY, int red, int green, int blue, int alpha) {
	    this.shape = shape;
	    particleMaxLifetime = maxLifeTime;
	    this.position = position;
	    this.velocity = velocity;
	    this.acceleration = acceleration;
	    this.sizeX = sizeX;
	    this.sizeY = sizeY;
	    this.red = red;
	    this.green = green;
	    this.blue = blue;
	    this.alpha = alpha;

	    particleLifetime = 0;
	}

	/**
	 * Draw the particle.
	 * @param receiver Renderer to draw with.
	 */
	public void draw(EventReceiver receiver) {
		switch (shape) {
			case Rectangle:
				break;
			case Circle:
				break;
			default:
				break;
		}
	}

	/**
	 * Particle Shapes
	 * Warning: you cannot use circular particles with SDL.
	 */
	public enum ParticleShape {
		Rectangle, Circle
	}
}
