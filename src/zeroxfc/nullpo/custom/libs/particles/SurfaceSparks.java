package zeroxfc.nullpo.custom.libs.particles;

import java.util.Arrays;
import java.util.Random;
import mu.nu.nullpo.game.event.EventReceiver;
import mu.nu.nullpo.game.play.GameEngine;
import zeroxfc.nullpo.custom.libs.CustomResourceHolder;
import zeroxfc.nullpo.custom.libs.DoubleVector;
import zeroxfc.nullpo.custom.libs.Interpolation;

public class SurfaceSparks extends ParticleEmitterBase<SurfaceSparks.Parameters> implements BlockBasedEmitter {
    private final Random randomiser;

    public SurfaceSparks(CustomResourceHolder customGraphics) {
        this(customGraphics, new Random());
    }

    public SurfaceSparks(CustomResourceHolder customGraphics, long seed) {
        super(customGraphics);
        this.randomiser = new Random(seed);
    }

    public SurfaceSparks(CustomResourceHolder customGraphics, Random randomiser) {
        super(customGraphics);
        this.randomiser = randomiser;
    }

    public static class Parameters {
        public final int minX;
        public final int maxX;
        public final int startY;
        public final int direction;

        public Parameters(int minX, int maxX, int startY, int direction) {
            assert direction == 1 || direction == -1;

            this.minX = minX;
            this.maxX = maxX;
            this.startY = startY;
            this.direction = direction;
        }
    }

    // Helper for using engine data directly.
    public void addNumber(GameEngine engine, EventReceiver receiver, int playerID, int num) {
        int baseX = (16 * engine.nowPieceX) + 4 + receiver.getFieldDisplayPositionX(engine, playerID);
        int baseY = (16 * engine.nowPieceY) + 52 + receiver.getFieldDisplayPositionY(engine, playerID);

        if (engine.nowPieceObject != null &&
            engine.getMoveDirection() != 0 &&
            (engine.dasCount == 0 || engine.dasCount >= engine.getDAS())) {
            if (engine.nowPieceObject.checkCollision(engine.nowPieceX, engine.nowPieceY + 1, engine.field) &&
                !engine.nowPieceObject.checkCollision(engine.nowPieceX + engine.getMoveDirection(), engine.nowPieceY, engine.field)) {

                for (int i = 0; i < engine.nowPieceObject.getMaxBlock(); ++i) {
                    if (checkLowestBlock(engine.nowPieceObject, i)) {
                        int realX = engine.nowPieceX + engine.nowPieceObject.dataX[engine.nowPieceObject.direction][i];
                        int realY = engine.nowPieceY + engine.nowPieceObject.dataY[engine.nowPieceObject.direction][i];

                        if (engine.nowPieceObject.big) {
                            if (!engine.field.getBlockEmptyF(realX, realY + 2)) {
                                addNumber(num, new SurfaceSparks.Parameters(
                                    baseX + 16 * engine.nowPieceObject.dataX[engine.nowPieceObject.direction][i],
                                    baseX + 16 * (engine.nowPieceObject.dataX[engine.nowPieceObject.direction][i] + 1),
                                    baseY + 16 * (engine.nowPieceObject.dataY[engine.nowPieceObject.direction][i] + 2),
                                    engine.getMoveDirection() * -1)
                                );
                            }

                            if (!engine.field.getBlockEmptyF(realX + 1, realY + 2)) {
                                addNumber(num, new SurfaceSparks.Parameters(
                                    baseX + 16 * (engine.nowPieceObject.dataX[engine.nowPieceObject.direction][i] + 1),
                                    baseX + 16 * (engine.nowPieceObject.dataX[engine.nowPieceObject.direction][i] + 2),
                                    baseY + 16 * (engine.nowPieceObject.dataY[engine.nowPieceObject.direction][i] + 2),
                                    engine.getMoveDirection() * -1)
                                );
                            }
                        } else {
                            if (!engine.field.getBlockEmptyF(realX, realY + 1)) {
                                addNumber(num, new SurfaceSparks.Parameters(
                                    baseX + 16 * engine.nowPieceObject.dataX[engine.nowPieceObject.direction][i],
                                    baseX + 16 * (engine.nowPieceObject.dataX[engine.nowPieceObject.direction][i] + 1),
                                    baseY + 16 * (engine.nowPieceObject.dataY[engine.nowPieceObject.direction][i] + 1),
                                    engine.getMoveDirection() * -1)
                                );
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void addNumber(int num, Parameters params) {
        int ured, ugreen, ublue;

        for (int i = 0; i < num; ++i) {
            ured = randomiser.nextInt(16) + 240;
            ugreen = randomiser.nextInt(33) + 112;
            ublue = randomiser.nextInt(32);

            final DoubleVector p = new DoubleVector(
                Interpolation.lerp(params.minX, params.maxX, randomiser.nextDouble()),
                Interpolation.lerp(params.startY - 1.5, params.startY + 1.5, randomiser.nextDouble()),
                false
            );

            final DoubleVector v = new DoubleVector(
                Interpolation.lerp(-0.6 * params.direction, 6 * params.direction, randomiser.nextDouble()),
                Interpolation.lerp(-3, 0.25, randomiser.nextDouble()),
                false
            );

            final DoubleVector a = new DoubleVector(v.getX(), v.getY(), false);
            a.mul(-1d / 90d);
            a.setY(9.80665 / 60d);

            final Particle particle = new Particle(
                Particle.ParticleShape.Rectangle,
                Interpolation.lerp(12, 20, randomiser.nextDouble()),
                p,
                v,
                a,
                randomiser.nextInt(3) + 2, 2,
                ured, ugreen, ublue, 255,
                ured, ugreen, ublue, 64
            );

            particles.add(particle);
        }
    }
}
