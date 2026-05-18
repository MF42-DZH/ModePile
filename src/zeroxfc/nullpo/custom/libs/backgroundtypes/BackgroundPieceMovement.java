package zeroxfc.nullpo.custom.libs.backgroundtypes;

import mu.nu.nullpo.game.component.Controller;
import mu.nu.nullpo.game.play.GameEngine;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import zeroxfc.nullpo.custom.libs.CustomResourceHolder;
import zeroxfc.nullpo.custom.libs.Interpolation;
import zeroxfc.nullpo.custom.libs.MathHelper;
import zeroxfc.nullpo.custom.libs.types.ImageChunk;
import zeroxfc.nullpo.custom.libs.types.ObjectAlignment;

public class BackgroundPieceMovement extends AnimatedBackgroundHook {
    private float zoomFactor;

    private int leftMove = 0;
    private int rightMove = 0;
    private int downMove = 0;
    private int downQueue = 0;

    private int[] imageDims = null;
    private final ImageChunk chunk = new ImageChunk();

    {
        ID = AnimatedBackgroundHook.ANIMATION_PIECE_MOVEMENT;
        setImageName("localBG");

        reset();
    }

    public BackgroundPieceMovement(int bgNumber, float zoomFactor) {
        customHolder = new CustomResourceHolder();
        customHolder.loadImage("res/graphics/back" + bgNumber + ".png", imageName);

        this.zoomFactor = zoomFactor;

        log.debug("Non-custom piece movement background (" + bgNumber + ") created.");
    }

    public BackgroundPieceMovement(String filePath, float zoomFactor) {
        customHolder = new CustomResourceHolder();
        customHolder.loadImage(filePath, imageName);

        this.zoomFactor = zoomFactor;

        log.debug("Custom piece movement background created (File Path: " + filePath  + ").");
    }

    @Override
    public void setBG(int bg) {
        customHolder.loadImage("res/graphics/back" + bg + ".png", imageName);
        imageDims = null;

        log.debug("Non-custom piece movement background modified (New BG: " + bg + ").");
    }

    @Override
    public void setBG(String filePath) {
        customHolder.loadImage(filePath, imageName);
        imageDims = null;

        log.debug("Custom piece movement background modified (New File Path: " + filePath  + ").");
    }

    @Override
    public void setBGFromHolder(CustomResourceHolder holder, String name) {
        customHolder.putImageAt(holder.getImageAt(name), imageName);
        imageDims = null;

        log.debug("Custom user movement background modified (New Image Reference: " + name + ").");
    }

    @Override
    public int getID() {
        return ID;
    }

    @Override
    public void update() {
        // Use the other update methods instead.
        throw new NotImplementedException();
    }

    @Override
    public void reset() {
        leftMove = 0;
        rightMove = 0;
        downMove = 0;
        downQueue = 0;
    }

    public void setZoomFactor(float zoomFactor) {
        this.zoomFactor = zoomFactor;
    }

    public void updateMove(GameEngine engine) {
        final int lateral = engine.getMoveDirection();

        if (lateral == -1 && engine.stat == GameEngine.STAT_MOVE) {
            leftMove = Math.min(engine.getDAS(), leftMove + 1);
            rightMove = Math.max(0, rightMove - 1);
        } else if (lateral == 1 && engine.stat == GameEngine.STAT_MOVE) {
            rightMove = Math.min(engine.getDAS(), rightMove + 1);
            leftMove = Math.max(0, leftMove - 1);
        } else {
            leftMove = Math.max(0, leftMove - 1);
            rightMove = Math.max(0, rightMove - 1);
        }
    }

    // Call in afterSoftDropFall and afterHardDropFall, and onLast with 0 height if engine state is not STAT_MOVE.
    public void updateDrop(GameEngine engine, int height) {
        if (engine.field == null) return;

        int gravity = 0;

        if (engine.stat == GameEngine.STAT_MOVE) {
            gravity = engine.speed.gravity;
            if (gravity < 0 || engine.speed.denominator <= 0) gravity = engine.field.getHeight();
            else gravity /= engine.speed.denominator;
        }

        gravity = Math.min(2, gravity);

        downQueue += height + gravity;
        final int half = downQueue >>> 1;

        downMove = Math.min(engine.field.getHeight() * 2, downMove + (downQueue - half));
        downQueue = Math.max(0, downQueue - Math.max(half, 1));

        if (engine.stat == GameEngine.STAT_MOVE && gravity <= 0 && height == 0 && !engine.ctrl.isPress(Controller.BUTTON_DOWN)) {
            downMove = Math.max(0, downMove - 1);
            return;
        }

        if (engine.stat != GameEngine.STAT_MOVE) {
            downMove = Math.max(0, downMove - 1);
        }
    }

    @Override
    public void draw(GameEngine engine, int playerID) {
        if (MathHelper.almostEqual(zoomFactor, 1.0, 0.0001)) {
            customHolder.drawImage(engine, imageName, 0, 0);
            return;
        }

        chunk.setAlignment(ObjectAlignment.TOP_LEFT);
        chunk.setAnchor(0, 0);
        chunk.setScale(zoomFactor, zoomFactor);

        if (imageDims == null) imageDims = customHolder.getImageDimensions(imageName);

        final int sw = (int) Math.ceil((float) imageDims[0] / zoomFactor);
        final int sh = (int) Math.ceil((float) imageDims[1] / zoomFactor);

        chunk.setSourceDimensions(sw, sh);

        final int leewayX = imageDims[0] - sw;
        final int leewayY = imageDims[1] - sh;

        final double leftRightBias = 0.5 - (0.5 * (leftMove / (double) engine.getDAS())) + (0.5 * (rightMove / (double) engine.getDAS()));
        final double downBias = engine.field != null ? downMove / ((double) engine.field.getHeight() * 2) : 0.0;

        chunk.setSourceLocation(
            (int) Interpolation.tanStep(0, leewayX, leftRightBias),
            (int) Interpolation.tanStep(0, leewayY, downBias)
        );

        customHolder.drawOffsetImage(engine, imageName, chunk, 255, 255, 255, 255);
    }
}
