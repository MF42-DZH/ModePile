package zeroxfc.nullpo.custom.libs.particles;

import java.util.ArrayList;
import mu.nu.nullpo.game.event.EventReceiver;
import org.apache.log4j.Logger;
import zeroxfc.nullpo.custom.libs.BufferedPrimitiveDrawingHook;

public abstract class ParticleEmitterBase {
    /**
     * Default colour set shared by all emitters.<br />
     * In order: Gray, Red, Orange, Yellow, Green, Cyan, Blue, Purple<br />
     * Parameters: Red, Green, Blue, Alpha, Variance
     */
    public static final int[][] DEF_COLOURS = {
        new int[] { 240, 240, 240, 235, 20 },
        new int[] { 240, 30, 0, 235, 20 },
        new int[] { 240, 130, 0, 235, 20 },
        new int[] { 240, 240, 0, 235, 20 },
        new int[] { 30, 240, 0, 235, 20 },
        new int[] { 0, 240, 240, 235, 20 },
        new int[] { 0, 30, 240, 235, 20 },
        new int[] { 210, 0, 210, 235, 20 }
    };
    /**
     * Debug logger
     */
    protected static final Logger log = Logger.getLogger(ParticleEmitterBase.class);
    /**
     * Particle container
     */
    protected ArrayList<Particle> particles = new ArrayList<>();

    /**
     * Drawing buffer
     */
    protected BufferedPrimitiveDrawingHook drawingQueue = new BufferedPrimitiveDrawingHook();

    /**
     * Update method. Used to update all partcles.
     */
    public void update() {
        if (particles.size() <= 0) return;
        for (int i = particles.size() - 1; i >= 0; i--) {
            boolean res = particles.get(i).update();
            if (res) {
                particles.remove(i);
            }
        }
    }

    /**
     * Draw the particles to the current renderer.
     *
     * @param receiver Renderer to use
     */
    public void draw(EventReceiver receiver) {
        if (particles.size() <= 0) return;
        for (Particle p : particles) {
            if (p.position.getX() < 0 || p.position.getX() > 640) continue;
            if (p.position.getY() < 0 || p.position.getY() > 480) continue;

            p.draw(drawingQueue);
        }
        drawingQueue.renderAll(receiver);
    }

    /**
     * Add particles directly to the collection.
     *
     * @param particle Particle to add
     */
    public void addSpecific(Particle particle) {
        particles.add(particle);
    }

    /**
     * Add some number of particles or particle groups.
     * Varies upon child class.
     *
     * @param num    Number of particles / particle groups.
     * @param params Parameters to pass onto the particles.
     */
    public abstract void addNumber(int num, Object[] params);
}