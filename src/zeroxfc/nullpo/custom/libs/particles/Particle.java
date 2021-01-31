package zeroxfc.nullpo.custom.libs.particles;

import zeroxfc.nullpo.custom.libs.BufferedPrimitiveDrawingHook;
import zeroxfc.nullpo.custom.libs.DoubleVector;
import zeroxfc.nullpo.custom.libs.Interpolation;

public class Particle {
    /**
     * Default colour
     */
    private static final int DEFAULT_COLOUR = 255;
    /**
     * Lifetime
     */
    private final int particleMaxLifetime;
    /**
     * Particle shape
     */
    private final ParticleShape shape;
    /**
     * X size
     */
    private final int sizeX;
    /**
     * Y size
     */
    private final int sizeY;
    /**
     * Red colour component
     */
    private final int red;
    /**
     * Green colour component
     */
    private final int green;
    /**
     * Blue colour component
     */
    private final int blue;
    /**
     * Alpha component
     */
    private final int alpha;

    /*
     * Colour variables.
     * Please use <code>0 <= value <= 255</code>
     */
    /**
     * Red colour component at end
     */
    private final int redEnd;
    /**
     * Green colour component at end
     */
    private final int greenEnd;
    /**
     * Blue colour component at end
     */
    private final int blueEnd;
    /**
     * Alpha component at end
     */
    private final int alphaEnd;
    /**
     * Position vector
     */
    public DoubleVector position;
    /**
     * Velocity vector
     */
    public DoubleVector velocity;
    /**
     * Acceleration vector
     */
    public DoubleVector acceleration;
    /**
     * Used colours
     */
    int ur = 0, ug = 0, ub = 0, ua = 0;
    /**
     * Current life
     */
    private int particleLifetime;

    /**
     * Create an instance of a particle.
     *
     * @param shape        The shape of the particle. Warning: SDL cannot draw circular particles.
     * @param maxLifeTime  The maximum frame lifetime of the particle.
     * @param position     Vector position of the particle.
     * @param velocity     Vector velocity of the particle.
     * @param acceleration Vector acceleration of the particle.
     * @param sizeX        Horizontal size of the particle.
     * @param sizeY        Vertical size of the particle.
     * @param red          Red component of colour.
     * @param green        Green component of colour.
     * @param blue         Blue component of colour.
     * @param alpha        Alpha component of colour.
     * @param redEnd       Red component of colour at particle death.
     * @param greenEnd     Green component of colour at particle death.
     * @param blueEnd      Blue component of colour at particle death.
     * @param alphaEnd     Alpha component of colour at particle death.
     */
    public Particle(ParticleShape shape, int maxLifeTime, DoubleVector position, DoubleVector velocity,
                    DoubleVector acceleration, int sizeX, int sizeY, int red, int green, int blue, int alpha,
                    int redEnd, int greenEnd, int blueEnd, int alphaEnd) {
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
        this.redEnd = redEnd;
        this.greenEnd = greenEnd;
        this.blueEnd = blueEnd;
        this.alphaEnd = alphaEnd;

        particleLifetime = 0;
    }

    /**
     * Initialise a default colour particle.
     *
     * @param shape        The shape of the particle. Warning: SDL cannot draw circular particles.
     * @param maxLifeTime  The maximum frame lifetime of the particle.
     * @param position     Vector position of the particle.
     * @param velocity     Vector velocity of the particle.
     * @param acceleration Vector acceleration of the particle.
     * @param sizeX        Horizontal size of the particle.
     * @param sizeY        Vertical size of the particle.
     */
    public Particle(ParticleShape shape, int maxLifeTime, DoubleVector position, DoubleVector velocity,
                    DoubleVector acceleration, int sizeX, int sizeY) {
        this(shape, maxLifeTime, position, velocity, acceleration, sizeX, sizeY,
            DEFAULT_COLOUR, DEFAULT_COLOUR, DEFAULT_COLOUR, DEFAULT_COLOUR,
            DEFAULT_COLOUR, DEFAULT_COLOUR, DEFAULT_COLOUR, DEFAULT_COLOUR);
    }

    /**
     * Initialise a stationary particle.
     *
     * @param shape       The shape of the particle. Warning: SDL cannot draw circular particles.
     * @param maxLifeTime The maximum frame lifetime of the particle.
     * @param sizeX       Horizontal size of the particle.
     * @param sizeY       Vertical size of the particle.
     * @param red         Red component of colour.
     * @param green       Green component of colour.
     * @param blue        Blue component of colour.
     * @param alpha       Alpha component of colour.
     * @param redEnd      Red component of colour at particle death.
     * @param greenEnd    Green component of colour at particle death.
     * @param blueEnd     Blue component of colour at particle death.
     * @param alphaEnd    Alpha component of colour at particle death.
     */
    public Particle(ParticleShape shape, int maxLifeTime, int sizeX, int sizeY,
                    int red, int green, int blue, int alpha,
                    int redEnd, int greenEnd, int blueEnd, int alphaEnd) {
        this(shape, maxLifeTime, DoubleVector.zero(), DoubleVector.zero(), DoubleVector.zero(), sizeX, sizeY,
            red, green, blue, alpha, redEnd, greenEnd, blueEnd, alphaEnd);
    }

    /**
     * Initialise a default colour, stationary particle.
     *
     * @param shape       The shape of the particle. Warning: SDL cannot draw circular particles.
     * @param maxLifeTime The maximum frame lifetime of the particle.
     * @param sizeX       Horizontal size of the particle.
     * @param sizeY       Vertical size of the particle.
     */
    public Particle(ParticleShape shape, int maxLifeTime, int sizeX, int sizeY) {
        this(shape, maxLifeTime, DoubleVector.zero(), DoubleVector.zero(), DoubleVector.zero(), sizeX, sizeY);
    }

    /**
     * Draw the particle.
     *
     * @param buffer Drawing buffer to use
     */
    public void draw(BufferedPrimitiveDrawingHook buffer) {
        if (particleLifetime > particleMaxLifetime) return;

        switch (shape) {
            case Rectangle:
                buffer.drawRectangle((int) position.getX() - (sizeX / 2), (int) position.getY() - (sizeY / 2), sizeX, sizeY, ur, ug, ub, ua, true);
                break;
            case Circle:
                buffer.drawOval((int) position.getX() - (sizeX / 2), (int) position.getY() - (sizeY / 2), sizeX, sizeY, ur, ug, ub, ua, true);
                break;
            default:
                break;
        }
    }

    /**
     * Update's the particle's position, colour and lifetime.
     *
     * @return <code>true</code> if the particle needs to be destroyed, else <code>false</code>.
     */
    public boolean update() {
        velocity = DoubleVector.add(velocity, acceleration);
        position = DoubleVector.add(position, velocity);

        ur = Interpolation.lerp(red, redEnd, (double) particleLifetime / particleMaxLifetime);
        ug = Interpolation.lerp(green, greenEnd, (double) particleLifetime / particleMaxLifetime);
        ub = Interpolation.lerp(blue, blueEnd, (double) particleLifetime / particleMaxLifetime);
        ua = Interpolation.lerp(alpha, alphaEnd, (double) particleLifetime / particleMaxLifetime);

        return ++particleLifetime > particleMaxLifetime;
    }

    /**
     * Particle Shapes
     * Warning: you cannot use circular particles with SDL.
     */
    public enum ParticleShape {
        Rectangle, Circle
    }
}
