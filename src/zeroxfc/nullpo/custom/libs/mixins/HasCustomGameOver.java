package zeroxfc.nullpo.custom.libs.mixins;

import mu.nu.nullpo.game.component.BGMStatus;
import mu.nu.nullpo.game.component.Block;
import mu.nu.nullpo.game.component.Controller;
import mu.nu.nullpo.game.play.GameEngine;

// Game over override helper. Mix this into a game mode class.
public interface HasCustomGameOver {
    // Call this in onGameOver. Override and use inCustomAllLivesLost instead if you want to have a custom pre-results
    // screen or something.
    default boolean inGameOver(GameEngine engine, int playerID) {
        if (engine.lives <= 0) {
            inAllLivesLost(engine, playerID, 60, 180);
        } else {
            inLifeLostAnimation(engine);
        }

        return true;
    }

    default void inAllLivesLost(GameEngine engine, int playerID, int delayBeforeAllowSkip, int gameOverScreenTime) {
        // もう復活できないとき
        if (engine.statc[0] == 0) {
            endGameAndResetFieldVisibility(engine);
        }

        if (engine.statc[0] < engine.field.getHeight() + 1) {
            greyOutNonGarbageBlocks(engine);
        } else if (engine.statc[0] == engine.field.getHeight() + 1) {
            playGameOverSound(engine);
        } else if (engine.statc[0] < engine.field.getHeight() + 1 + gameOverScreenTime) {
            processSkipInput(engine, delayBeforeAllowSkip, gameOverScreenTime);
        } else {
            saveReplayAndShowResults(engine, playerID);
        }
    }

    // Override this with something useful if you want to use inCustomAllLivesLost.
    default boolean shouldAdvanceGameOver(GameEngine engine, int playerID) {
        return true;
    }

    default void inCustomAllLivesLost(GameEngine engine, int playerID) {
        // もう復活できないとき
        if (engine.statc[0] == 0) {
            endGameAndResetFieldVisibility(engine);
        }

        if (engine.statc[0] < engine.field.getHeight() + 1) {
            greyOutNonGarbageBlocks(engine);
        } else if (engine.statc[0] == engine.field.getHeight() + 1) {
            playGameOverSound(engine);
        } else if (!shouldAdvanceGameOver(engine, playerID)) {
            processPostGameOver(engine);
        } else {
            saveReplayAndShowResults(engine, playerID);
        }
    }

    default void endGameAndResetFieldVisibility(GameEngine engine) {
        engine.gameEnded();
        engine.blockShowOutlineOnly = false;
        if (engine.owner.getPlayers() < 2) engine.owner.bgmStatus.bgm = BGMStatus.BGM_NOTHING;

        if (engine.field.isEmpty()) {
            engine.statc[0] = engine.field.getHeight() + 1;
        } else {
            engine.resetFieldVisible();
        }
    }

    default void greyOutNonGarbageBlocks(GameEngine engine) {
        for (int i = 0; i < engine.field.getWidth(); i++) {
            if (engine.field.getBlockColor(i, engine.field.getHeight() - engine.statc[0]) != Block.BLOCK_COLOR_NONE) {
                Block blk = engine.field.getBlock(i, engine.field.getHeight() - engine.statc[0]);

                if (blk != null) {
                    if (!blk.getAttribute(Block.BLOCK_ATTRIBUTE_GARBAGE)) {
                        blk.color = Block.BLOCK_COLOR_GRAY;
                        blk.setAttribute(Block.BLOCK_ATTRIBUTE_GARBAGE, true);
                    }
                    blk.darkness = 0.3f;
                    blk.elapsedFrames = -1;
                }
            }
        }
        engine.statc[0]++;
    }

    default void playGameOverSound(GameEngine engine) {
        engine.playSE("gameover");
        engine.statc[0]++;
    }

    default void processSkipInput(GameEngine engine, int delayBeforeAllowSkip, int gameOverScreenTime) {
        if ((engine.statc[0] >= engine.field.getHeight() + 1 + delayBeforeAllowSkip) && (engine.ctrl.isPush(Controller.BUTTON_A))) {
            engine.statc[0] = engine.field.getHeight() + 1 + gameOverScreenTime;
        }

        engine.statc[0]++;
    }

    // Override this with something useful if using inCustomAllLivesLost
    default void processPostGameOver(GameEngine engine) {
        engine.statc[0]++;
    }

    default void saveReplayAndShowResults(GameEngine engine, int playerID) {
        if (!engine.owner.replayMode || engine.owner.replayRerecord) engine.owner.saveReplay();

        for (int i = 0; i < engine.owner.getPlayers(); i++) {
            if ((i == playerID) || (engine.gameoverAll)) {
                if (engine.owner.engine[i].field != null) {
                    engine.owner.engine[i].field.reset();
                }
                engine.owner.engine[i].resetStatc();
                engine.owner.engine[i].stat = GameEngine.STAT_RESULT;
            }
        }
    }

    default void inLifeLostAnimation(GameEngine engine) {
        // 復活できるとき
        if (engine.statc[0] == 0) {
            greyOutWholeField(engine);
        }

        if (!engine.field.isEmpty()) {
            engine.field.pushDown();
        } else if (engine.statc[1] < engine.getARE()) {
            engine.statc[1]++;
        } else {
            removeLifeAndGiveControl(engine);
        }
    }

    default void greyOutWholeField(GameEngine engine) {
        engine.blockShowOutlineOnly = false;
        engine.playSE("died");

        engine.resetFieldVisible();

        for (int i = (engine.field.getHiddenHeight() * -1); i < engine.field.getHeight(); i++) {
            for (int j = 0; j < engine.field.getWidth(); j++) {
                if (engine.field.getBlockColor(j, i) != Block.BLOCK_COLOR_NONE) {
                    engine.field.setBlockColor(j, i, Block.BLOCK_COLOR_GRAY);
                }
            }
        }

        engine.statc[0] = 1;
    }

    default void removeLifeAndGiveControl(GameEngine engine) {
        engine.lives--;
        engine.resetStatc();
        engine.stat = GameEngine.STAT_MOVE;
    }
}
