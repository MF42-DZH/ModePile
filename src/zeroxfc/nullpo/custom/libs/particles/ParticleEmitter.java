package zeroxfc.nullpo.custom.libs.particles;

import mu.nu.nullpo.game.event.EventReceiver;

import java.util.ArrayList;

public abstract class ParticleEmitter {
	/** Particle container */
	ArrayList<Particle> particles = new ArrayList<>();

	/**
	 * Update method. Used to update all partcles.
	 */
	public void update() {
		for (int i = particles.size() - 1; i >= 0; i--) {
			boolean res = particles.get(i).update();
			if (res) {
				particles.remove(i);
			}
		}
	}

	/**
	 * Draw the particles to the current renderer.
	 * @param receiver Renderer to use
	 */
	void draw(EventReceiver receiver) {
		for (Particle p : particles) {
			p.draw(receiver);
		}
	}

	/**
	 * Add particles directly to the collection.
	 * @param particle Particle to add
	 */
	public void addSpecific(Particle particle) {
		particles.add(particle);
	}

	/**
	 * Add some number of particles or particle groups.
	 * Varies upon child class.
	 * @param num Number of particles / particle groups.
	 * @param params Parameters to pass onto the particles.
	 */
	abstract void addNumber(int num, Object[] params);
}
