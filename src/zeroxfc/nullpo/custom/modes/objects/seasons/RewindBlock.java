package zeroxfc.nullpo.custom.modes.objects.seasons;

import mu.nu.nullpo.game.component.Block;
import mu.nu.nullpo.game.event.EventReceiver;
import zeroxfc.nullpo.custom.libs.DoubleVector;
import zeroxfc.nullpo.custom.libs.RendererExtension;
import zeroxfc.nullpo.custom.libs.types.ObjectAlignment;

public class RewindBlock {
    private final int maxLife;
    private int life;

    private final DoubleVector position;
    private final DoubleVector velocity;
    private final DoubleVector acceleration;

    private final Block block;

    public RewindBlock(int maxLife, int startLife, DoubleVector position, DoubleVector velocity, DoubleVector acceleration, Block block) {
        this.maxLife = maxLife;
        this.life = startLife;

        this.position = position;
        this.velocity = velocity;
        this.acceleration = acceleration;
        this.block = new Block(block);
    }

    public boolean update() {
        if (++life <= 0) return life >= maxLife;

        velocity.add(acceleration);
        position.add(velocity);

        return life >= maxLife;
    }

    public void draw(RendererExtension extension, EventReceiver receiver) {
        if (life <= 0) return;

        extension.drawAlignedScaledBlock(
            receiver,
            (int) position.getX(), (int) position.getY(),
            ObjectAlignment.MIDDLE_MIDDLE,
            block.getDrawColor(),
            block.skin,
            block.getAttribute(Block.BLOCK_ATTRIBUTE_BONE),
            block.darkness,
            block.alpha,
            1f,
            block.attribute
        );
    }
}
