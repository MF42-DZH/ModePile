package zeroxfc.nullpo.custom.libs.mixins;

import mu.nu.nullpo.game.component.Block;
import mu.nu.nullpo.game.component.Controller;
import mu.nu.nullpo.game.play.GameEngine;

public interface HasCustomLineClear {
    // Call engine in an onLineClear override in a gamemode.
    default boolean inOnLineClear(GameEngine engine, int playerID) {
        inInputRepeatCheck(engine);

        inLineClearing(engine, playerID);

        inLineClearAnimation(engine);

        inLineDelayCancelCheck(engine);

        if (inAfterLineDelay(engine, playerID)) return true;

        engine.statc[0]++;
        return true;
    }

    default void inInputRepeatCheck(GameEngine engine) {
        engine.checkDropContinuousUse();

        // 横溜め
        if(engine.ruleopt.dasInLineClear) engine.padRepeat();
        else if(engine.ruleopt.dasRedirectInDelay) { engine.dasRedirect(); }
    }

    default void inLineClearing(GameEngine engine, int playerID) {
        // 最初の frame
        if(engine.statc[0] == 0) {
            final int li = flagLinesForClearing(engine);

            processLineClearStatistics(engine, li);

            processB2B(engine, li);

            processCombo(engine, li);

            engine.lineGravityTotalLines += engine.lineClearing;

            processTotalLineStatistics(engine, li);

            if(engine.field.getHowManyGemClears() > 0) engine.playSE("gem");

            callCalcScore(engine, playerID, li);

            callAndDrawBrokenBlocks(engine, playerID, li);

            eraseFlaggedBlocks(engine, li);
        }
    }

    default int flagLinesForClearing(GameEngine engine) {
        // Line clear flagを設定
        if (engine.clearMode == GameEngine.CLEAR_LINE)
            engine.lineClearing = engine.field.checkLine();
            // Set color clear flags
        else if (engine.clearMode == GameEngine.CLEAR_COLOR)
            engine.lineClearing = engine.field.checkColor(engine.colorClearSize, true, engine.garbageColorClear, engine.gemSameColor, engine.ignoreHidden);
            // Set line color clear flags
        else if (engine.clearMode == GameEngine.CLEAR_LINE_COLOR)
            engine.lineClearing = engine.field.checkLineColor(engine.colorClearSize, true, engine.lineColorDiagonals, engine.gemSameColor);
        else if (engine.clearMode == GameEngine.CLEAR_GEM_COLOR)
            engine.lineClearing = engine.field.gemColorCheck(engine.colorClearSize, true, engine.garbageColorClear, engine.ignoreHidden);

        // Linescountを決める
        int li = engine.lineClearing;
        if(engine.big && engine.bighalf)
            li >>= 1;
        return li;
    }

    default void processLineClearStatistics(GameEngine engine, int li) {
        if(engine.tspin) {
            engine.playSE("tspin" + Math.min(3, li));

            if((engine.ending == 0) || (engine.staffrollEnableStatistics)) {
                if((li == 1) && (engine.tspinmini))  engine.statistics.totalTSpinSingleMini++;
                if((li == 1) && (!engine.tspinmini)) engine.statistics.totalTSpinSingle++;
                if((li == 2) && (engine.tspinmini))  engine.statistics.totalTSpinDoubleMini++;
                if((li == 2) && (!engine.tspinmini)) engine.statistics.totalTSpinDouble++;
                if(li >= 3) engine.statistics.totalTSpinTriple++;
            }
        } else {
            if (engine.clearMode == GameEngine.CLEAR_LINE)
                engine.playSE("erase" + Math.min(4, li));

            if((engine.ending == 0) || (engine.staffrollEnableStatistics)) {
                if(li == 1) engine.statistics.totalSingle++;
                if(li == 2) engine.statistics.totalDouble++;
                if(li == 3) engine.statistics.totalTriple++;
                if(li >= 4) engine.statistics.totalFour++;
            }
        }
    }

    default void processB2B(GameEngine engine, int li) {
        // B2B bonus
        if(engine.b2bEnable) {
            if((engine.tspin) || (li >= 4)) {
                engine.b2bcount++;

                if(engine.b2bcount == 1) {
                    engine.playSE("b2b_start");
                } else {
                    engine.b2b = true;
                    engine.playSE("b2b_continue");

                    if((engine.ending == 0) || (engine.staffrollEnableStatistics)) {
                        if(li >= 4) engine.statistics.totalB2BFour++;
                        else engine.statistics.totalB2BTSpin++;
                    }
                }
            } else if(engine.b2bcount != 0) {
                engine.b2b = false;
                engine.b2bcount = 0;
                engine.playSE("b2b_end");
            }
        }
    }

    default void processCombo(GameEngine engine, int li) {
        // Combo
        if((engine.comboType != GameEngine.COMBO_TYPE_DISABLE) && (engine.chain == 0)) {
            if( (engine.comboType == GameEngine.COMBO_TYPE_NORMAL) || ((engine.comboType == GameEngine.COMBO_TYPE_DOUBLE) && (li >= 2)) )
                engine.combo++;

            if(engine.combo >= 2) {
                int cmbse = engine.combo - 1;
                if(cmbse > 20) cmbse = 20;
                engine.playSE("combo" + cmbse);
            }

            if((engine.ending == 0) || (engine.staffrollEnableStatistics)) {
                if(engine.combo > engine.statistics.maxCombo) engine.statistics.maxCombo = engine.combo;
            }
        }
    }

    default void processTotalLineStatistics(GameEngine engine, int li) {
        if((engine.ending == 0) || (engine.staffrollEnableStatistics)) engine.statistics.lines += li;
    }

    default void callCalcScore(GameEngine engine, int playerID, int li) {
        // Calculate score
        if(engine.owner.mode != null) engine.owner.mode.calcScore(engine, playerID, li);
        engine.owner.receiver.calcScore(engine, playerID, li);
    }

    default void callAndDrawBrokenBlocks(GameEngine engine, int playerID, int li) {
        // Blockを消す演出を出す (まだ実際には消えていない）
        if (engine.clearMode == GameEngine.CLEAR_LINE) {
            for(int i = 0; i < engine.field.getHeight(); i++) {
                if(engine.field.getLineFlag(i)) {
                    for(int j = 0; j < engine.field.getWidth(); j++) {
                        Block blk = engine.field.getBlock(j, i);

                        if(blk != null) {
                            if(engine.owner.mode != null) engine.owner.mode.blockBreak(engine, playerID, j, i, blk);
                            engine.owner.receiver.blockBreak(engine, playerID, j, i, blk);
                        }
                    }
                }
            }
        } else if (engine.clearMode == GameEngine.CLEAR_LINE_COLOR || engine.clearMode == GameEngine.CLEAR_COLOR || engine.clearMode == GameEngine.CLEAR_GEM_COLOR)
            for(int i = 0; i < engine.field.getHeight(); i++) {
                for(int j = 0; j < engine.field.getWidth(); j++) {
                    Block blk = engine.field.getBlock(j, i);
                    if (blk == null)
                        continue;
                    if(blk.getAttribute(Block.BLOCK_ATTRIBUTE_ERASE)) {
                        if(engine.owner.mode != null) engine.owner.mode.blockBreak(engine, playerID, j, i, blk);
                        if (engine.displaysize == 1)
                        {
                            engine.owner.receiver.blockBreak(engine, playerID, 2*j, 2*i, blk);
                            engine.owner.receiver.blockBreak(engine, playerID, 2*j+1, 2*i, blk);
                            engine.owner.receiver.blockBreak(engine, playerID, 2*j, 2*i+1, blk);
                            engine.owner.receiver.blockBreak(engine, playerID, 2*j+1, 2*i+1, blk);
                        }
                        else
                            engine.owner.receiver.blockBreak(engine, playerID, j, i, blk);
                    }
                }
            }
    }

    default void eraseFlaggedBlocks(GameEngine engine, int li) {
        // Blockを消す
        if (engine.clearMode == GameEngine.CLEAR_LINE)
            engine.field.clearLine();
        else if (engine.clearMode == GameEngine.CLEAR_COLOR)
            engine.field.clearColor(engine.colorClearSize, engine.garbageColorClear, engine.gemSameColor, engine.ignoreHidden);
        else if (engine.clearMode == GameEngine.CLEAR_LINE_COLOR)
            engine.field.clearLineColor(engine.colorClearSize, engine.lineColorDiagonals, engine.gemSameColor);
        else if (engine.clearMode == GameEngine.CLEAR_GEM_COLOR)
            engine.lineClearing = engine.field.gemClearColor(engine.colorClearSize, engine.garbageColorClear, engine.ignoreHidden);
    }

    default void inLineClearAnimation(GameEngine engine) {
        // Linesを1段落とす
        if((engine.lineGravityType == GameEngine.LINE_GRAVITY_NATIVE) &&
            (engine.getLineDelay() >= (engine.lineClearing - 1)) && (engine.statc[0] >= engine.getLineDelay() - (engine.lineClearing - 1)) && (engine.ruleopt.lineFallAnim))
        {
            engine.field.downFloatingBlocksSingleLine();
        }
    }

    default void inLineDelayCancelCheck(GameEngine engine) {
        // Line delay cancel check
        engine.delayCancelMoveLeft = engine.ctrl.isPush(Controller.BUTTON_LEFT);
        engine.delayCancelMoveRight = engine.ctrl.isPush(Controller.BUTTON_RIGHT);

        boolean moveCancel = engine.ruleopt.lineCancelMove && (engine.ctrl.isPush(engine.getUp()) ||
            engine.ctrl.isPush(engine.getDown()) || engine.delayCancelMoveLeft || engine.delayCancelMoveRight);
        boolean rotateCancel = engine.ruleopt.lineCancelRotate && (engine.ctrl.isPush(Controller.BUTTON_A) ||
            engine.ctrl.isPush(Controller.BUTTON_B) || engine.ctrl.isPush(Controller.BUTTON_C) ||
            engine.ctrl.isPush(Controller.BUTTON_E));
        boolean holdCancel = engine.ruleopt.lineCancelHold && engine.ctrl.isPush(Controller.BUTTON_D);

        engine.delayCancel = moveCancel || rotateCancel || holdCancel;

        if( (engine.statc[0] < engine.getLineDelay()) && engine.delayCancel ) {
            engine.statc[0] = engine.getLineDelay();
        }
    }

    default boolean inAfterLineDelay(GameEngine engine, int playerID) {
        // Next ステータス
        if (isLineDelayFinished(engine)) {
            if (processCascade(engine)) return true;
            afterLineClearFinishes(engine, playerID);

            return true;
        }
        
        return false;
    }

    default boolean isLineDelayFinished(GameEngine engine) {
        return engine.statc[0] >= engine.getLineDelay();
    }

    default boolean processCascade(GameEngine engine) {
        // Cascade
        if((engine.lineGravityType == GameEngine.LINE_GRAVITY_CASCADE || engine.lineGravityType == GameEngine.LINE_GRAVITY_CASCADE_SLOW)) {
            if (engine.statc[6] < engine.getCascadeDelay()) {
                engine.statc[6]++;
                return true;
            } else if(engine.field.doCascadeGravity(engine.lineGravityType)) {
                engine.statc[6] = 0;
                return true;
            } else if (engine.statc[6] < engine.getCascadeClearDelay()) {
                engine.statc[6]++;
                return true;
            } else if(((engine.clearMode == GameEngine.CLEAR_LINE) && engine.field.checkLineNoFlag() > 0) ||
                ((engine.clearMode == GameEngine.CLEAR_COLOR) && engine.field.checkColor(engine.colorClearSize, false, engine.garbageColorClear, engine.gemSameColor, engine.ignoreHidden) > 0) ||
                ((engine.clearMode == GameEngine.CLEAR_LINE_COLOR) && engine.field.checkLineColor(engine.colorClearSize, false, engine.lineColorDiagonals, engine.gemSameColor) > 0) ||
                ((engine.clearMode == GameEngine.CLEAR_GEM_COLOR) && engine.field.gemColorCheck(engine.colorClearSize, false, engine.garbageColorClear, engine.ignoreHidden) > 0)) {
                engine.tspin = false;
                engine.tspinmini = false;
                engine.chain++;
                if(engine.chain > engine.statistics.maxChain) engine.statistics.maxChain = engine.chain;
                engine.statc[0] = 0;
                engine.statc[6] = 0;
                return true;
            }
        }
        return false;
    }

    default void afterLineClearFinishes(GameEngine engine, int playerID) {
        boolean skip = false;
        if(engine.owner.mode != null) skip = engine.owner.mode.lineClearEnd(engine, playerID);
        engine.owner.receiver.lineClearEnd(engine, playerID);

        if(!skip) {
            if(engine.lineGravityType == GameEngine.LINE_GRAVITY_NATIVE) engine.field.downFloatingBlocks();
            engine.playSE("linefall");

            engine.field.lineColorsCleared = null;

            if((engine.stat == GameEngine.STAT_LINECLEAR) || (engine.versionMajor <= 6.3f)) {
                engine.resetStatc();
                if(engine.ending == 1) {
                    // Ending
                    engine.stat = GameEngine.STAT_ENDINGSTART;
                } else if((engine.getARELine() > 0) || (engine.lagARE)) {
                    // AREあり
                    engine.statc[0] = 0;
                    engine.statc[1] = engine.getARELine();
                    engine.statc[2] = 1;
                    engine.stat = GameEngine.STAT_ARE;
                } else if(engine.interruptItemNumber != GameEngine.INTERRUPTITEM_NONE) {
                    // 中断効果のあるアイテム処理
                    engine.nowPieceObject = null;
                    engine.interruptItemPreviousStat = GameEngine.STAT_MOVE;
                    engine.stat = GameEngine.STAT_INTERRUPTITEM;
                } else {
                    // AREなし
                    engine.nowPieceObject = null;
                    if(engine.versionMajor < 7.5f) engine.initialRotate(); //XXX: Weird IRS thing on lines cleared but no ARE
                    engine.stat = GameEngine.STAT_MOVE;
                }
            }
        }
    }
}
