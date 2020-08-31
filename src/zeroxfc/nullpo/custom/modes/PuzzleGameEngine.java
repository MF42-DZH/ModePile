package zeroxfc.nullpo.custom.modes;

import mu.nu.nullpo.game.component.BGMStatus;
import mu.nu.nullpo.game.component.Block;
import mu.nu.nullpo.game.component.Controller;
import mu.nu.nullpo.game.component.Field;
import mu.nu.nullpo.game.event.EventReceiver;
import mu.nu.nullpo.game.play.GameEngine;
import mu.nu.nullpo.game.play.GameManager;
import mu.nu.nullpo.game.subsystem.mode.DummyMode;
import mu.nu.nullpo.util.CustomProperties;

public abstract class PuzzleGameEngine extends DummyMode {
    // XXX: PLEASE OVERRIDE ALL OF THESE AND ADD MORE AS NECESSARY!

    public GameManager owner;
    public EventReceiver receiver;
    public int localState;
    public int rankingRank;

    @Override
    public void playerInit( GameEngine engine, int playerID ) {
        owner = engine.owner;
        receiver = engine.owner.receiver;
        rankingRank = -1;
        localState = 0;
    }

    @Override
    public boolean onReady( GameEngine engine, int playerID ) {
        // 横溜め
        if ( engine.ruleopt.dasInReady && engine.gameActive ) engine.padRepeat();
        else if ( engine.ruleopt.dasRedirectInDelay ) {
            engine.dasRedirect();
        }

        // Initialization
        if ( engine.statc[ 0 ] == 0 ) {
            // fieldInitialization
            engine.ruleopt.fieldWidth = 8;
            engine.ruleopt.fieldHeight = 8;
            engine.ruleopt.fieldHiddenHeight = 0;
            engine.displaysize = 1;

            engine.ruleopt.nextDisplay = 0;
            engine.ruleopt.holdEnable = false;

            engine.fieldWidth = engine.ruleopt.fieldWidth;
            engine.fieldHeight = engine.ruleopt.fieldHeight;
            engine.fieldHiddenHeight = engine.ruleopt.fieldHiddenHeight;
            engine.field = new Field( engine.fieldWidth, engine.fieldHeight, engine.fieldHiddenHeight, true );

            if ( !engine.readyDone ) {
                //  button input状態リセット
                engine.ctrl.reset();
                // ゲーム中 flagON
                engine.gameActive = true;
                engine.gameStarted = true;
                engine.isInGame = true;
            }

            rankingRank = -1;
        }

        // READY音
        if ( engine.statc[ 0 ] == engine.readyStart ) engine.playSE( "ready" );

        // GO音
        if ( engine.statc[ 0 ] == engine.goStart ) engine.playSE( "go" );

        // 開始
        if ( engine.statc[ 0 ] >= engine.goEnd ) {
            if ( !engine.readyDone ) engine.owner.bgmStatus.bgm = -1;
            if ( engine.owner.mode != null ) engine.owner.mode.startGame( engine, playerID );
            engine.owner.receiver.startGame( engine, playerID );
            engine.stat = GameEngine.STAT_CUSTOM;
            localState = 0;
            engine.timerActive = true;
            engine.resetStatc();
            if ( !engine.readyDone ) {
                engine.startTime = System.nanoTime();
                //startTime = System.nanoTime()/1000000L;
            }
            engine.readyDone = true;
            return true;
        }

        engine.statc[ 0 ]++;

        return true;
    }

    @Override
    public boolean onGameOver( GameEngine engine, int playerID ) {
        if ( engine.lives <= 0 ) {
            // もう復活できないとき
            if ( engine.statc[ 0 ] == 0 ) {
                engine.gameEnded();
                engine.blockShowOutlineOnly = false;
                if ( owner.getPlayers() < 2 ) owner.bgmStatus.bgm = BGMStatus.BGM_NOTHING;

                if ( engine.field.isEmpty() ) {
                    engine.statc[ 0 ] = engine.field.getHeight() + 1;
                } else {
                    engine.resetFieldVisible();
                }
            }

            if ( engine.statc[ 0 ] < engine.field.getHeight() + 1 ) {
                for ( int i = 0; i < engine.field.getWidth(); i++ ) {
                    if ( engine.field.getBlockColor( i, engine.field.getHeight() - engine.statc[ 0 ] ) != Block.BLOCK_COLOR_NONE ) {
                        Block blk = engine.field.getBlock( i, engine.field.getHeight() - engine.statc[ 0 ] );

                        if ( blk != null ) {
                            if ( blk.color > Block.BLOCK_COLOR_NONE ) {
                                if ( !blk.getAttribute( Block.BLOCK_ATTRIBUTE_GARBAGE ) ) {
                                    blk.color = Block.BLOCK_COLOR_GRAY;
                                    blk.setAttribute( Block.BLOCK_ATTRIBUTE_GARBAGE, true );
                                }
                                blk.darkness = 0.3f;
                                blk.elapsedFrames = -1;
                            }
                        }
                    }
                }
                engine.statc[ 0 ]++;
            } else if ( engine.statc[ 0 ] == engine.field.getHeight() + 1 ) {
                engine.playSE( "gameover" );
                engine.statc[ 0 ]++;
            } else if ( engine.statc[ 0 ] < engine.field.getHeight() + 1 + 180 ) {
                if ( ( engine.statc[ 0 ] >= engine.field.getHeight() + 1 + 60 ) && ( engine.ctrl.isPush( Controller.BUTTON_A ) ) ) {
                    engine.statc[ 0 ] = engine.field.getHeight() + 1 + 180;
                }

                engine.statc[ 0 ]++;
            } else {
                if ( !owner.replayMode || owner.replayRerecord ) owner.saveReplay();

                for ( int i = 0; i < owner.getPlayers(); i++ ) {
                    if ( ( i == playerID ) || ( engine.gameoverAll ) ) {
                        if ( owner.engine[ i ].field != null ) {
                            owner.engine[ i ].field.reset();
                        }
                        owner.engine[ i ].resetStatc();
                        owner.engine[ i ].stat = GameEngine.STAT_RESULT;
                    }
                }
            }
        } else {
            // 復活できるとき
            if ( engine.statc[ 0 ] == 0 ) {
                engine.blockShowOutlineOnly = false;
                engine.playSE( "died" );

                engine.resetFieldVisible();

                for ( int i = ( engine.field.getHiddenHeight() * -1 ); i < engine.field.getHeight(); i++ ) {
                    for ( int j = 0; j < engine.field.getWidth(); j++ ) {
                        if ( engine.field.getBlockColor( j, i ) != Block.BLOCK_COLOR_NONE ) {
                            engine.field.setBlockColor( j, i, Block.BLOCK_COLOR_GRAY );
                        }
                    }
                }

                engine.statc[ 0 ] = 1;
            }

            if ( !engine.field.isEmpty() ) {
                engine.field.pushDown();
            } else if ( engine.statc[ 1 ] < engine.getARE() ) {
                engine.statc[ 1 ]++;
            } else {
                engine.lives--;
                engine.resetStatc();
                engine.stat = GameEngine.STAT_CUSTOM;
            }
        }
        return true;
    }

    /*
     * Called when saving replay
     */
    @Override
    public void saveReplay( GameEngine engine, int playerID, CustomProperties prop ) {
        saveSetting( prop );

        if ( ( !owner.replayMode ) ) {
            updateRanking( engine.statistics.score, engine.statistics.level + 1, 0 );

            if ( rankingRank != -1 ) {
                saveRanking( owner.modeConfig );
                receiver.saveModeConfig( owner.modeConfig );
            }
        }
    }

    @Override
    public boolean onCustom( GameEngine engine, int playerID ) {
        boolean updateTimer = false;
        // Override this.

        switch ( localState ) {
            case 0:

                break;

            default:
                break;
        }

        if ( updateTimer ) engine.statc[ 0 ]++;
        return true;
    }

    /**
     * Read rankings from property file
     *
     * @param prop Property file
     */
    protected void loadRanking( CustomProperties prop ) {
        // Override this.
    }

    /**
     * Save rankings to property file
     *
     * @param prop Property file
     */
    private void saveRanking( CustomProperties prop ) {
        // Override this.
    }

    private void updateRanking( int sc, int level, int type ) {
        rankingRank = checkRanking( sc, level, type );
        // Override this.
    }

    private int checkRanking( int sc, int level, int type ) {
        // Override this.

        return -1;
    }

    /**
     * Load settings from property file
     *
     * @param prop Property file
     */
    private void loadSetting( CustomProperties prop ) {
        // Override this.
    }

    /**
     * Save settings to property file
     *
     * @param prop Property file
     */
    private void saveSetting( CustomProperties prop ) {
        // Override this.
    }
}
