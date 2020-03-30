package zeroxfc.nullpo.custom.libs.particles;

import mu.nu.nullpo.game.event.EventReceiver;
import org.apache.log4j.Logger;
import zeroxfc.nullpo.custom.libs.BufferedPrimitiveDrawingHook;

import java.util.ArrayList;

public abstract class ParticleEmitter {
	/** Debug logger */
	protected static final Logger log = Logger.getLogger(ParticleEmitter.class);

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