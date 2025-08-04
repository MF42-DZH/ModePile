package zeroxfc.nullpo.custom.modes.objects.seasons;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Random;
import mu.nu.nullpo.game.component.Block;
import mu.nu.nullpo.game.event.EventReceiver;
import zeroxfc.nullpo.custom.libs.DoubleVector;
import zeroxfc.nullpo.custom.libs.Interpolation;
import zeroxfc.nullpo.custom.libs.MathHelper;
import zeroxfc.nullpo.custom.libs.RendererExtension;
import zeroxfc.nullpo.custom.libs.types.ObjectAlignment;

public class BlockVortex {
    private static final int MAX_PROGRESS = 90;

    private final Deque<BlockInstance> instances = new LinkedList<>();

    public void add(Random rand, int colour, int blockSkin) {
        final BlockInstance bi = new BlockInstance(colour, blockSkin);
        final DoubleVector origin = new DoubleVector(40, 2 * Math.PI * rand.nextDouble(), true);
        final DoubleVector end = new DoubleVector(Math.sqrt(400d * 400d + 320d * 320d), origin.getDirection(), true);
        final DoubleVector control = new DoubleVector(
            80 + rand.nextDouble() * 240,
            origin.getDirection() + (Math.PI * rand.nextDouble() * 0.5),
            true
        );

        bi.maxProgress = MAX_PROGRESS + (rand.nextInt(61) - 30);

        bi.startX = (int) origin.getX() + 320;
        bi.startY = (int) origin.getY() + 240;

        bi.endX = (int) end.getX() + 320;
        bi.endY = (int) end.getY() + 240;

        bi.controlX = (int) control.getX() + 320;
        bi.controlY = (int) control.getY() + 240;

        instances.add(bi);
    }

    public void update() {
        instances.removeIf(bi -> (bi.progress++) > MAX_PROGRESS);
    }

    public void draw(RendererExtension rendererExtension, EventReceiver receiver) {
        for (BlockInstance bi : instances) {
            bi.draw(rendererExtension, receiver);
        }
    }

    private static class BlockInstance {
        public final int colour;
        public final int blockSkin;

        public int progress;
        public int maxProgress;

        public int startX, startY;
        public int endX, endY;
        public int controlX, controlY;

        public BlockInstance(int colour, int blockSkin) {
            this.colour = colour;
            this.blockSkin = blockSkin;

            progress = 0;
        }

        public void draw(RendererExtension ext, EventReceiver receiver) {
            final double lerpVal = progress / (double) maxProgress;

            final int x1 = Interpolation.lerp(startX, controlX, lerpVal);
            final int x2 = Interpolation.lerp(controlX, endX, lerpVal);
            final int anchorX = (int) Interpolation.sineStep(x1, x2, lerpVal);

            final int y1 = Interpolation.lerp(startY, controlY, lerpVal);
            final int y2 = Interpolation.lerp(controlY, endY, lerpVal);
            final int anchorY = (int) Interpolation.sineStep(y1, y2, lerpVal);

            ext.drawAlignedScaledBlock(
                receiver,
                anchorX, anchorY,
                ObjectAlignment.MIDDLE_MIDDLE,
                colour, blockSkin,
                false,
                MathHelper.clamp(Interpolation.lerp(0.625f, 0.125f, lerpVal), 0.0f, 0.5f),
                1f,
                Interpolation.lerp(1f, 4f, lerpVal),
                Block.BLOCK_ATTRIBUTE_VISIBLE
            );
        }
    }
}
