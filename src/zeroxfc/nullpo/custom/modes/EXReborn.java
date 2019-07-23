package zeroxfc.nullpo.custom.modes;

import mu.nu.nullpo.game.component.Block;
import mu.nu.nullpo.game.component.Controller;
import mu.nu.nullpo.game.component.Piece;
import mu.nu.nullpo.game.component.RuleOptions;
import mu.nu.nullpo.game.component.WallkickResult;
import mu.nu.nullpo.game.play.GameEngine;
import mu.nu.nullpo.game.subsystem.mode.DummyMode;

public class EXReborn extends DummyMode {
	private static final int[] tableARE = {
        25, 25, 25, 25, 25, 20, 20, 20, 15, 15, 20, 15, 12, 10, 10, 8, 8, 8, 6, 6, 10
    };

	private static final int[] tableLineARE = {
        25, 25, 25, 25, 25, 20, 20, 20, 15, 15, 20, 15, 12, 10, 10, 8, 8, 8, 6, 6, 10
    };

	private static final int[] tableLineDelay = {
        35, 25, 25, 20, 20, 15, 15, 15, 15, 15, 12, 12, 12, 6, 6, 4, 4, 4, 4, 4, 4
    };

	private static final int[] tableGravity = {
        2, 4, 8, 16, 30, 60, 120, 360, 600, 1200, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1
    };

	private static final int[] tableLockDelay = {
        40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 33, 28, 24, 20, 18, 15, 12, 10, 8, 20
    };

	private static final int[] tableDAS = {
        12, 12, 12, 12, 12, 10, 10, 10, 10, 10, 8, 8, 8, 8, 8, 6, 6, 6, 5, 5, 5
    };
    
	private static final int gravityDenominator = 120;
    
	private static final int maxLevel = 2000;
	
	private static final int maxSection = 20;
	
	/** Mode name */
	@Override
	public String getName() {
		return "EX REBORN";
	}
	
	@Override
	public void playerInit(GameEngine engine, int playerID) {
		// TODO Auto-generated method stub
		engine.speed.denominator = gravityDenominator;
		engine.speed.gravity = 2;
	}
	
	// TODO: Modify wallkick - oh no.
	@Override
	public boolean onMove(GameEngine engine, int playerID) {
		// 横溜めInitialization
		int moveDirection = engine.getMoveDirection();

		if((engine.statc[0] > 0) || (engine.ruleopt.dasInMoveFirstFrame)) {
			if(engine.dasDirection != moveDirection) {
				engine.dasDirection = moveDirection;
				if(!(engine.dasDirection == 0 && engine.ruleopt.dasStoreChargeOnNeutral)){
					engine.dasCount = 0;
				}
			}
		}

		// 出現時の処理
		if(engine.statc[0] == 0) {
			if((engine.statc[1] == 0) && (engine.initialHoldFlag == false)) {
				// 通常出現
				engine.nowPieceObject = engine.getNextObjectCopy(engine.nextPieceCount);
				engine.nextPieceCount++;
				if(engine.nextPieceCount < 0) engine.nextPieceCount = 0;
				engine.holdDisable = false;
			} else {
				// ホールド出現
				if(engine.initialHoldFlag) {
					// 先行ホールド
					if(engine.holdPieceObject == null) {
						// 1回目
						engine.holdPieceObject = engine.getNextObjectCopy(engine.nextPieceCount);
						engine.holdPieceObject.applyOffsetArray(engine.ruleopt.pieceOffsetX[engine.holdPieceObject.id], engine.ruleopt.pieceOffsetY[engine.holdPieceObject.id]);
						engine.nextPieceCount++;
						if(engine.nextPieceCount < 0) engine.nextPieceCount = 0;

						if(engine.bone == true) engine.getNextObject(engine.nextPieceCount + engine.ruleopt.nextDisplay - 1).setAttribute(Block.BLOCK_ATTRIBUTE_BONE, true);

						engine.nowPieceObject = engine.getNextObjectCopy(engine.nextPieceCount);
						engine.nextPieceCount++;
						if(engine.nextPieceCount < 0) engine.nextPieceCount = 0;
					} else {
						// 2回目以降
						Piece pieceTemp = engine.holdPieceObject;
						engine.holdPieceObject = engine.getNextObjectCopy(engine.nextPieceCount);
						engine.holdPieceObject.applyOffsetArray(engine.ruleopt.pieceOffsetX[engine.holdPieceObject.id], engine.ruleopt.pieceOffsetY[engine.holdPieceObject.id]);
						engine.nowPieceObject = pieceTemp;
						engine.nextPieceCount++;
						if(engine.nextPieceCount < 0) engine.nextPieceCount = 0;
					}
				} else {
					// 通常ホールド
					if(engine.holdPieceObject == null) {
						// 1回目
						engine.nowPieceObject.big = false;
						engine.holdPieceObject = engine.nowPieceObject;
						engine.nowPieceObject = engine.getNextObjectCopy(engine.nextPieceCount);
						engine.nextPieceCount++;
						if(engine.nextPieceCount < 0) engine.nextPieceCount = 0;
					} else {
						// 2回目以降
						engine.nowPieceObject.big = false;
						Piece pieceTemp = engine.holdPieceObject;
						engine.holdPieceObject = engine.nowPieceObject;
						engine.nowPieceObject = pieceTemp;
					}
				}

				// Directionを戻す
				if((engine.ruleopt.holdResetDirection) && (engine.ruleopt.pieceDefaultDirection[engine.holdPieceObject.id] < Piece.DIRECTION_COUNT)) {
					engine.holdPieceObject.direction = engine.ruleopt.pieceDefaultDirection[engine.holdPieceObject.id];
					engine.holdPieceObject.updateConnectData();
				}

				// 使用した count+1
				engine.holdUsedCount++;
				engine.statistics.totalHoldUsed++;

				// ホールド無効化
				engine.initialHoldFlag = false;
				engine.holdDisable = true;
			}
			engine.playSE("piece" + engine.getNextObject(engine.nextPieceCount).id);

			if(engine.nowPieceObject.offsetApplied == false)
				engine.nowPieceObject.applyOffsetArray(engine.ruleopt.pieceOffsetX[engine.nowPieceObject.id], engine.ruleopt.pieceOffsetY[engine.nowPieceObject.id]);

			engine.nowPieceObject.big = engine.big;

			// 出現位置 (横）
			engine.nowPieceX = engine.getSpawnPosX(engine.field, engine.nowPieceObject);

			// 出現位置 (縦）
			engine.nowPieceY = engine.getSpawnPosY(engine.nowPieceObject);

			engine.nowPieceBottomY = engine.nowPieceObject.getBottom(engine.nowPieceX, engine.nowPieceY, engine.field);
			engine.nowPieceColorOverride = -1;

			if(engine.itemRollRollEnable) engine.nowPieceColorOverride = Block.BLOCK_COLOR_GRAY;

			// 先行rotation
			if(engine.versionMajor < 7.5f) engine.initialRotate(); //XXX: Weird active time IRS
			//if( (getARE() != 0) && ((getARELine() != 0) || (version < 6.3f)) ) initialRotate();

			if((engine.speed.gravity > engine.speed.denominator) && (engine.speed.denominator > 0))
				engine.gcount = engine.speed.gravity % engine.speed.denominator;
			else
				engine.gcount = 0;

			engine.lockDelayNow = 0;
			engine.dasSpeedCount = engine.getDASDelay();
			engine.dasRepeat = false;
			engine.dasInstant = false;
			engine.extendedMoveCount = 0;
			engine.extendedRotateCount = 0;
			engine.softdropFall = 0;
			engine.harddropFall = 0;
			engine.manualLock = false;
			engine.nowPieceMoveCount = 0;
			engine.nowPieceRotateCount = 0;
			engine.nowPieceRotateFailCount = 0;
			engine.nowWallkickCount = 0;
			engine.nowUpwardWallkickCount = 0;
			engine.lineClearing = 0;
			engine.lastmove = GameEngine.LASTMOVE_NONE;
			engine.kickused = false;
			engine.tspin = false;
			engine.tspinmini = false;
			engine.tspinez = false;

			engine.getNextObject(engine.nextPieceCount + engine.ruleopt.nextDisplay - 1).setAttribute(Block.BLOCK_ATTRIBUTE_BONE, engine.bone);

			if(engine.ending == 0) engine.timerActive = true;

			if((engine.ai != null) && (!engine.owner.replayMode || engine.owner.replayRerecord)) engine.ai.newPiece(engine, playerID);
		}

		engine.checkDropContinuousUse();

		boolean softdropUsed = false; // この frame にSoft dropを使ったらtrue
		int softdropFallNow = 0; // この frame のSoft dropで落下した段count

		boolean updown = false; // Up下同時押し flag
		if(engine.ctrl.isPress(engine.getUp()) && engine.ctrl.isPress(engine.getDown())) updown = true;

		if(!engine.dasInstant) {

			// ホールド
			if(engine.ctrl.isPush(Controller.BUTTON_D) || engine.initialHoldFlag) {
				if(engine.isHoldOK()) {
					engine.statc[0] = 0;
					engine.statc[1] = 1;
					if(!engine.initialHoldFlag) engine.playSE("hold");
					engine.initialHoldContinuousUse = true;
					engine.initialHoldFlag = false;
					engine.holdDisable = true;
					engine.initialRotate(); //Hold swap triggered IRS
					engine.statMove();
					return true;
				} else if((engine.statc[0] > 0) && (!engine.initialHoldFlag)) {
					engine.playSE("holdfail");
				}
			}

			// rotation
			boolean onGroundBeforeRotate = engine.nowPieceObject.checkCollision(engine.nowPieceX, engine.nowPieceY + 1, engine.field);
			int move = 0;
			boolean rotated = false;

			if(engine.initialRotateDirection != 0) {
				move = engine.initialRotateDirection;
				engine.initialRotateLastDirection = engine.initialRotateDirection;
				engine.initialRotateContinuousUse = true;
				engine.playSE("initialrotate");
			} else if((engine.statc[0] > 0) || (engine.ruleopt.moveFirstFrame == true)) {
				if((engine.itemRollRollEnable) && (engine.replayTimer % engine.itemRollRollInterval == 0)) move = 1;	// Roll Roll

				//  button input
				if(engine.ctrl.isPush(Controller.BUTTON_A) || engine.ctrl.isPush(Controller.BUTTON_C)) move = -1;
				else if(engine.ctrl.isPush(Controller.BUTTON_B)) move = 1;
				else if(engine.ctrl.isPush(Controller.BUTTON_E)) move = 2;

				if(move != 0) {
					engine.initialRotateLastDirection = move;
					engine.initialRotateContinuousUse = true;
				}
			}

			if((engine.ruleopt.rotateButtonAllowDouble == false) && (move == 2)) move = -1;
			if((engine.ruleopt.rotateButtonAllowReverse == false) && (move == 1)) move = -1;
			if(engine.isRotateButtonDefaultRight() && (move != 2)) move = move * -1;

			if(move != 0) {
				// Direction after rotationを決める
				int rt = engine.getRotateDirection(move);

				// rotationできるか判定
				if(!engine.ruleopt.strRuleName.contains("T-EX") && engine.nowPieceObject.checkCollision(engine.nowPieceX, engine.nowPieceY, rt, engine.field) == false)
				{
					// Wallkickなしでrotationできるとき
					rotated = true;
					engine.kickused = false;
					engine.nowPieceObject.direction = rt;
					engine.nowPieceObject.updateConnectData();
				} else if( (engine.ruleopt.rotateWallkick == true) &&
						   (engine.wallkick != null) &&
						   ((engine.initialRotateDirection == 0) || (engine.ruleopt.rotateInitialWallkick == true)) &&
						   ((engine.ruleopt.lockresetLimitOver != RuleOptions.LOCKRESET_LIMIT_OVER_NOWALLKICK) || (engine.isRotateCountExceed() == false)) )
				{
					// Wallkickを試みる
					boolean allowUpward = (engine.ruleopt.rotateMaxUpwardWallkick < 0) || (engine.nowUpwardWallkickCount < engine.ruleopt.rotateMaxUpwardWallkick);
					WallkickResult kick = engine.wallkick.executeWallkick(engine.nowPieceX, engine.nowPieceY, move, engine.nowPieceObject.direction, rt,
										  allowUpward, engine.nowPieceObject, engine.field, engine.ctrl);

					if(kick != null) {
						rotated = true;
						engine.kickused = true;
						engine.nowWallkickCount++;
						if(kick.isUpward()) engine.nowUpwardWallkickCount++;
						engine.nowPieceObject.direction = kick.direction;
						engine.nowPieceObject.updateConnectData();
						engine.nowPieceX += kick.offsetX;
						engine.nowPieceY += kick.offsetY;

						if(engine.ruleopt.lockresetWallkick && !engine.isRotateCountExceed()) {
							engine.lockDelayNow = 0;
							engine.nowPieceObject.setDarkness(0f);
						}
					}
				}

				// Domino Quick Turn
				if(!rotated && engine.dominoQuickTurn && (engine.nowPieceObject.id == Piece.PIECE_I2) && (engine.nowPieceRotateFailCount >= 1)) {
					rt = engine.getRotateDirection(2);
					rotated = true;
					engine.nowPieceObject.direction = rt;
					engine.nowPieceObject.updateConnectData();
					engine.nowPieceRotateFailCount = 0;

					if(engine.nowPieceObject.checkCollision(engine.nowPieceX, engine.nowPieceY, rt, engine.field) == true) {
						engine.nowPieceY--;
					} else if(onGroundBeforeRotate) {
						engine.nowPieceY++;
					}
				}

				if(rotated == true) {
					// rotation成功
					engine.nowPieceBottomY = engine.nowPieceObject.getBottom(engine.nowPieceX, engine.nowPieceY, engine.field);

					if((engine.ruleopt.lockresetRotate == true) && (engine.isRotateCountExceed() == false)) {
						engine.lockDelayNow = 0;
						engine.nowPieceObject.setDarkness(0f);
					}

					if(onGroundBeforeRotate) {
						engine.extendedRotateCount++;
						engine.lastmove = GameEngine.LASTMOVE_ROTATE_GROUND;
					} else {
						engine.lastmove = GameEngine.LASTMOVE_ROTATE_AIR;
					}

					if(engine.initialRotateDirection == 0) {
						engine.playSE("rotate");
					}

					engine.nowPieceRotateCount++;
					if((engine.ending == 0) || (engine.staffrollEnableStatistics)) engine.statistics.totalPieceRotate++;
				} else {
					// rotation失敗
					engine.playSE("rotfail");
					engine.nowPieceRotateFailCount++;
				}
			}
			engine.initialRotateDirection = 0;

			// game over check
			if((engine.statc[0] == 0) && (engine.nowPieceObject.checkCollision(engine.nowPieceX, engine.nowPieceY, engine.field) == true)) {
				// Blockの出現位置を上にずらすことができる場合はそうする
				for(int i = 0; i < engine.ruleopt.pieceEnterMaxDistanceY; i++) {
					if(engine.nowPieceObject.big) engine.nowPieceY -= 2;
					else engine.nowPieceY--;

					if(engine.nowPieceObject.checkCollision(engine.nowPieceX, engine.nowPieceY, engine.field) == false) {
						engine.nowPieceBottomY = engine.nowPieceObject.getBottom(engine.nowPieceX, engine.nowPieceY, engine.field);
						break;
					}
				}

				// 死亡
				if(engine.nowPieceObject.checkCollision(engine.nowPieceX, engine.nowPieceY, engine.field) == true) {
					engine.nowPieceObject.placeToField(engine.nowPieceX, engine.nowPieceY, engine.field);
					engine.nowPieceObject = null;
					engine.stat = GameEngine.STAT_GAMEOVER;
					if((engine.ending == 2) && (engine.staffrollNoDeath)) engine.stat = GameEngine.STAT_NOTHING;
					engine.resetStatc();
					return true;
				}
			}

		}

		int move = 0;
		boolean sidemoveflag = false;	// この frame に横移動したらtrue

		if((engine.statc[0] > 0) || (engine.ruleopt.moveFirstFrame == true)) {
			// 横移動
			boolean onGroundBeforeMove = engine.nowPieceObject.checkCollision(engine.nowPieceX, engine.nowPieceY + 1, engine.field);

			move = moveDirection;

			if (engine.statc[0] == 0 && engine.delayCancel) {
				if (engine.delayCancelMoveLeft) move = -1;
				if (engine.delayCancelMoveRight) move = 1;
				engine.dasCount = 0;
				// delayCancel = false;
				engine.delayCancelMoveLeft = false;
				engine.delayCancelMoveRight = false;
			} else if (engine.statc[0] == 1 && engine.delayCancel && (engine.dasCount < engine.getDAS())) {
				move = 0;
				engine.delayCancel = false;
			}

			if(move != 0) sidemoveflag = true;

			if(engine.big && engine.bigmove) move *= 2;

			if((move != 0) && (engine.dasCount == 0)) engine.shiftLock = 0;

			if( (move != 0) && ((engine.dasCount == 0) || (engine.dasCount >= engine.getDAS())) ) {
				engine.shiftLock &= engine.ctrl.getButtonBit();

				if(engine.shiftLock == 0) {
					if( (engine.dasSpeedCount >= engine.getDASDelay()) || (engine.dasCount == 0) ) {
						if(engine.dasCount > 0) engine.dasSpeedCount = 1;

						if(engine.nowPieceObject.checkCollision(engine.nowPieceX + move, engine.nowPieceY, engine.field) == false) {
							engine.nowPieceX += move;

							if((engine.getDASDelay() == 0) && (engine.dasCount > 0) && (engine.nowPieceObject.checkCollision(engine.nowPieceX + move, engine.nowPieceY, engine.field) == false)) {
								if(!engine.dasInstant) engine.playSE("move");
								engine.dasRepeat = true;
								engine.dasInstant = true;
							}

							//log.debug("Successful movement: move="+move);

							if((engine.ruleopt.lockresetMove == true) && (engine.isMoveCountExceed() == false)) {
								engine.lockDelayNow = 0;
								engine.nowPieceObject.setDarkness(0f);
							}

							engine.nowPieceMoveCount++;
							if((engine.ending == 0) || (engine.staffrollEnableStatistics)) engine.statistics.totalPieceMove++;
							engine.nowPieceBottomY = engine.nowPieceObject.getBottom(engine.nowPieceX, engine.nowPieceY, engine.field);

							if(onGroundBeforeMove) {
								engine.extendedMoveCount++;
								engine.lastmove = GameEngine.LASTMOVE_SLIDE_GROUND;
							} else {
								engine.lastmove = GameEngine.LASTMOVE_SLIDE_AIR;
							}

							if(!engine.dasInstant) engine.playSE("move");

						} else if (engine.ruleopt.dasChargeOnBlockedMove) {
							engine.dasCount = engine.getDAS();
							engine.dasSpeedCount = engine.getDASDelay();
						}
					} else {
						engine.dasSpeedCount++;
					}
				}
			}

			// Hard drop
			if( (engine.ctrl.isPress(engine.getUp()) == true) &&
				(engine.harddropContinuousUse == false) &&
				(engine.ruleopt.harddropEnable == true) &&
				((engine.isDiagonalMoveEnabled() == true) || (sidemoveflag == false)) &&
				((engine.ruleopt.moveUpAndDown == true) || (updown == false)) &&
				(engine.nowPieceY < engine.nowPieceBottomY) )
			{
				engine.harddropFall += engine.nowPieceBottomY - engine.nowPieceY;

				if(engine.nowPieceY != engine.nowPieceBottomY) {
					engine.nowPieceY = engine.nowPieceBottomY;
					engine.playSE("harddrop");
				}

				if(engine.owner.mode != null) engine.owner.mode.afterHardDropFall(engine, playerID, engine.harddropFall);
				engine.owner.receiver.afterHardDropFall(engine, playerID, engine.harddropFall);

				engine.lastmove = GameEngine.LASTMOVE_FALL_SELF;
				if(engine.ruleopt.lockresetFall == true) {
					engine.lockDelayNow = 0;
					engine.nowPieceObject.setDarkness(0f);
					engine.extendedMoveCount = 0;
					engine.extendedRotateCount = 0;
				}
			}

			if(!engine.ruleopt.softdropGravitySpeedLimit || (engine.ruleopt.softdropSpeed < 1.0f)) {
				// Old Soft Drop codes
				if( (engine.ctrl.isPress(engine.getDown()) == true) &&
					(engine.softdropContinuousUse == false) &&
					(engine.ruleopt.softdropEnable == true) &&
					((engine.isDiagonalMoveEnabled() == true) || (sidemoveflag == false)) &&
					((engine.ruleopt.moveUpAndDown == true) || (updown == false)) )
				{
					if((engine.ruleopt.softdropMultiplyNativeSpeed == true) || (engine.speed.denominator <= 0))
						engine.gcount += (int)(engine.speed.gravity * engine.ruleopt.softdropSpeed);
					else
						engine.gcount += (int)(engine.speed.denominator * engine.ruleopt.softdropSpeed);

					softdropUsed = true;
				}
			} else {
				// New Soft Drop codes
				if( engine.ctrl.isPress(engine.getDown()) && !engine.softdropContinuousUse &&
						engine.ruleopt.softdropEnable && (engine.isDiagonalMoveEnabled() || !sidemoveflag) &&
					(engine.ruleopt.moveUpAndDown || !updown) &&
					(engine.ruleopt.softdropMultiplyNativeSpeed || (engine.speed.gravity < (int)(engine.speed.denominator * engine.ruleopt.softdropSpeed))) )
				{
					if((engine.ruleopt.softdropMultiplyNativeSpeed == true) || (engine.speed.denominator <= 0)) {
						// gcount += (int)(speed.gravity * ruleopt.softdropSpeed);
						engine.gcount = (int)(engine.speed.gravity * engine.ruleopt.softdropSpeed);
					} else {
						// gcount += (int)(speed.denominator * ruleopt.softdropSpeed);
						engine.gcount = (int)(engine.speed.denominator * engine.ruleopt.softdropSpeed);
					}

					softdropUsed = true;
				} else {
					// 落下
					// This prevents soft drop from adding to the gravity speed.
					engine.gcount += engine.speed.gravity;
				}
			}

			if((engine.ending == 0) || (engine.staffrollEnableStatistics)) engine.statistics.totalPieceActiveTime++;
		}

		if(!engine.ruleopt.softdropGravitySpeedLimit || (engine.ruleopt.softdropSpeed < 1.0f))
			engine.gcount += engine.speed.gravity;	// Part of Old Soft Drop

		while((engine.gcount >= engine.speed.denominator) || (engine.speed.gravity < 0)) {
			if(engine.nowPieceObject.checkCollision(engine.nowPieceX, engine.nowPieceY + 1, engine.field) == false) {
				if(engine.speed.gravity >= 0) engine.gcount -= engine.speed.denominator;
				engine.nowPieceY++;

				if(engine.ruleopt.lockresetFall == true) {
					engine.lockDelayNow = 0;
					engine.nowPieceObject.setDarkness(0f);
				}

				if((engine.lastmove != GameEngine.LASTMOVE_ROTATE_GROUND) && (engine.lastmove != GameEngine.LASTMOVE_SLIDE_GROUND) && (engine.lastmove != GameEngine.LASTMOVE_FALL_SELF)) {
					engine.extendedMoveCount = 0;
					engine.extendedRotateCount = 0;
				}

				if(softdropUsed == true) {
					engine.lastmove = GameEngine.LASTMOVE_FALL_SELF;
					engine.softdropFall++;
					softdropFallNow++;
					engine.playSE("softdrop");
				} else {
					engine.lastmove = GameEngine.LASTMOVE_FALL_AUTO;
				}
			} else {
				break;
			}
		}

		if(softdropFallNow > 0) {
			if(engine.owner.mode != null) engine.owner.mode.afterSoftDropFall(engine, playerID, softdropFallNow);
			engine.owner.receiver.afterSoftDropFall(engine, playerID, softdropFallNow);
		}

		// 接地と固定
		if( (engine.nowPieceObject.checkCollision(engine.nowPieceX, engine.nowPieceY + 1, engine.field) == true) &&
			((engine.statc[0] > 0) || (engine.ruleopt.moveFirstFrame == true)) )
		{
			if((engine.lockDelayNow == 0) && (engine.getLockDelay() > 0))
				engine.playSE("step");

			if(engine.lockDelayNow < engine.getLockDelay())
				engine.lockDelayNow++;

			if((engine.getLockDelay() >= 99) && (engine.lockDelayNow > 98))
				engine.lockDelayNow = 98;

			if(engine.lockDelayNow < engine.getLockDelay()) {
				if(engine.lockDelayNow >= engine.getLockDelay() - 1)
					engine.nowPieceObject.setDarkness(0.5f);
				else
					engine.nowPieceObject.setDarkness((engine.lockDelayNow * 7 / engine.getLockDelay()) * 0.05f);
			}

			if(engine.getLockDelay() != 0)
				engine.gcount = engine.speed.gravity;

			// trueになると即固定
			boolean instantlock = false;

			// Hard drop固定
			if( (engine.ctrl.isPress(engine.getUp()) == true) &&
				(engine.harddropContinuousUse == false) &&
				(engine.ruleopt.harddropEnable == true) &&
				((engine.isDiagonalMoveEnabled() == true) || (sidemoveflag == false)) &&
				((engine.ruleopt.moveUpAndDown == true) || (updown == false)) &&
				(engine.ruleopt.harddropLock == true) )
			{
				engine.harddropContinuousUse = true;
				engine.manualLock = true;
				instantlock = true;
			}

			// Soft drop固定
			if( (engine.ctrl.isPress(engine.getDown()) == true) &&
				(engine.softdropContinuousUse == false) &&
				(engine.ruleopt.softdropEnable == true) &&
				((engine.isDiagonalMoveEnabled() == true) || (sidemoveflag == false)) &&
				((engine.ruleopt.moveUpAndDown == true) || (updown == false)) &&
				(engine.ruleopt.softdropLock == true) )
			{
				engine.softdropContinuousUse = true;
				engine.manualLock = true;
				instantlock = true;
			}

			// 接地状態でソフドドロップ固定
			if( (engine.ctrl.isPush(engine.getDown()) == true) &&
				(engine.ruleopt.softdropEnable == true) &&
				((engine.isDiagonalMoveEnabled() == true) || (sidemoveflag == false)) &&
				((engine.ruleopt.moveUpAndDown == true) || (updown == false)) &&
				(engine.ruleopt.softdropSurfaceLock == true) )
			{
				engine.softdropContinuousUse = true;
				engine.manualLock = true;
				instantlock = true;
			}

			if((engine.manualLock == true) && (engine.ruleopt.shiftLockEnable)) {
				// bit 1 and 2 are button_up and button_down currently
				engine.shiftLock = engine.ctrl.getButtonBit() & 3;
			}

			// 移動＆rotationcount制限超過
			if( (engine.ruleopt.lockresetLimitOver == RuleOptions.LOCKRESET_LIMIT_OVER_INSTANT) && (engine.isMoveCountExceed() || engine.isRotateCountExceed()) ) {
				instantlock = true;
			}

			// 接地即固定
			if( (engine.getLockDelay() == 0) && ((engine.gcount >= engine.speed.denominator) || (engine.speed.gravity < 0)) ) {
				instantlock = true;
			}

			// 固定
			if( ((engine.lockDelayNow >= engine.getLockDelay()) && (engine.getLockDelay() > 0)) || (instantlock == true) ) {
				if(engine.ruleopt.lockflash > 0) engine.nowPieceObject.setDarkness(-0.8f);

				/*if((lastmove == LASTMOVE_ROTATE_GROUND) && (tspinEnable == true)) {

					tspinmini = false;

					// T-Spin Mini判定

					if(!useAllSpinBonus) {
						if(spinCheckType == SPINTYPE_4POINT) {
							if(tspinminiType == TSPINMINI_TYPE_ROTATECHECK) {
								if(nowPieceObject.checkCollision(nowPieceX, nowPieceY, getRotateDirection(-1), field) &&
								   nowPieceObject.checkCollision(nowPieceX, nowPieceY, getRotateDirection( 1), field))
									tspinmini = true;
							} else if(tspinminiType == TSPINMINI_TYPE_WALLKICKFLAG) {
								tspinmini = kickused;
							}
						} else if(spinCheckType == SPINTYPE_IMMOBILE) {
							Field copyField = new Field(field);
							nowPieceObject.placeToField(nowPieceX, nowPieceY, copyField);
							if((copyField.checkLineNoFlag() == 1) && (kickused == true)) tspinmini = true;
						}
					}
				}*/

				// T-Spin判定
				if((engine.lastmove == GameEngine.LASTMOVE_ROTATE_GROUND) && (engine.tspinEnable == true)) {
					if(engine.useAllSpinBonus)
						engine.setAllSpin(engine.nowPieceX, engine.nowPieceY, engine.nowPieceObject, engine.field);
					else
						engine.setTSpin(engine.nowPieceX, engine.nowPieceY, engine.nowPieceObject, engine.field);
				}

				engine.nowPieceObject.setAttribute(Block.BLOCK_ATTRIBUTE_SELFPLACED, true);

				boolean partialLockOut = engine.nowPieceObject.isPartialLockOut(engine.nowPieceX, engine.nowPieceY, engine.field);
				boolean put = engine.nowPieceObject.placeToField(engine.nowPieceX, engine.nowPieceY, engine.field);

				engine.playSE("lock");

				engine.holdDisable = false;

				if((engine.ending == 0) || (engine.staffrollEnableStatistics)) engine.statistics.totalPieceLocked++;

				if (engine.clearMode == GameEngine.CLEAR_LINE)
					engine.lineClearing = engine.field.checkLineNoFlag();
				else if (engine.clearMode == GameEngine.CLEAR_COLOR)
					engine.lineClearing = engine.field.checkColor(engine.colorClearSize, false, engine.garbageColorClear, engine.gemSameColor, engine.ignoreHidden);
				else if (engine.clearMode == GameEngine.CLEAR_LINE_COLOR)
					engine.lineClearing = engine.field.checkLineColor(engine.colorClearSize, false, engine.lineColorDiagonals, engine.gemSameColor);
				else if (engine.clearMode == GameEngine.CLEAR_GEM_COLOR)
					engine.lineClearing = engine.field.gemColorCheck(engine.colorClearSize, false,engine. garbageColorClear, engine.ignoreHidden);
				engine.chain = 0;
				engine.lineGravityTotalLines = 0;

				if(engine.lineClearing == 0) {
					engine.combo = 0;

					if(engine.tspin) {
						engine.playSE("tspin0");

						if((engine.ending == 0) || (engine.staffrollEnableStatistics)) {
							if(engine.tspinmini) engine.statistics.totalTSpinZeroMini++;
							else engine.statistics.totalTSpinZero++;
						}
					}

					if(engine.owner.mode != null) engine.owner.mode.calcScore(engine, playerID, engine.lineClearing);
					engine.owner.receiver.calcScore(engine, playerID, engine.lineClearing);
				}

				if(engine.owner.mode != null) engine.owner.mode.pieceLocked(engine, playerID, engine.lineClearing);
				engine.owner.receiver.pieceLocked(engine, playerID, engine.lineClearing);

				engine.dasRepeat = false;
				engine.dasInstant = false;

				// Next 処理を決める(Mode 側でステータスを弄っている場合は何もしない)
				if((engine.stat == GameEngine.STAT_MOVE) || (engine.versionMajor <= 6.3f)) {
					engine.resetStatc();

					if((engine.ending == 1) && (engine.versionMajor >= 6.6f) && (engine.versionMinorOld >= 0.1f)) {
						// Ending
						engine.stat = GameEngine.STAT_ENDINGSTART;
					} else if( (!put && engine.ruleopt.fieldLockoutDeath) || (partialLockOut && engine.ruleopt.fieldPartialLockoutDeath) ) {
						// 画面外に置いて死亡
						engine.stat = GameEngine.STAT_GAMEOVER;
						if((engine.ending == 2) && (engine.staffrollNoDeath)) engine.stat = GameEngine.STAT_NOTHING;
					} else if ((engine.lineGravityType == GameEngine.LINE_GRAVITY_CASCADE || engine.lineGravityType == GameEngine.LINE_GRAVITY_CASCADE_SLOW)
							&& !engine.connectBlocks) {
						engine.stat = GameEngine.STAT_LINECLEAR;
						engine.statc[0] = engine.getLineDelay();
						engine.statLineClear();
					} else if( (engine.lineClearing > 0) && ((engine.ruleopt.lockflash <= 0) || (!engine.ruleopt.lockflashBeforeLineClear)) ) {
						// Line clear
						engine.stat = GameEngine.STAT_LINECLEAR;
						engine.statLineClear();
					} else if( ((engine.getARE() > 0) || (engine.lagARE) || (engine.ruleopt.lockflashBeforeLineClear)) &&
							    (engine.ruleopt.lockflash > 0) && (engine.ruleopt.lockflashOnlyFrame) )
					{
						// AREあり (光あり）
						engine.stat = GameEngine.STAT_LOCKFLASH;
					} else if((engine.getARE() > 0) || (engine.lagARE)) {
						// AREあり (光なし）
						engine.statc[1] = engine.getARE();
						engine.stat = GameEngine.STAT_ARE;
					} else if(engine.interruptItemNumber != GameEngine.INTERRUPTITEM_NONE) {
						// 中断効果のあるアイテム処理
						engine.nowPieceObject = null;
						engine.interruptItemPreviousStat = GameEngine.STAT_MOVE;
						engine.stat = GameEngine.STAT_INTERRUPTITEM;
					} else {
						// AREなし
						engine.stat = GameEngine.STAT_MOVE;
						if(engine.ruleopt.moveFirstFrame == false) engine.statMove();
					}
				}
				return true;
			}
		}

		// 横溜め
		if((engine.statc[0] > 0) || (engine.ruleopt.dasInMoveFirstFrame)) {
			if( (moveDirection != 0) && (moveDirection == engine.dasDirection) && ((engine.dasCount < engine.getDAS()) || (engine.getDAS() <= 0)) ) {
				engine.dasCount++;
			}
		}

		engine.statc[0]++;
		return true;
	}
	
	/**
	 * Gets the level string.
	 * @param engine Current GameEngine.
	 * @return String that gives the current level.
	 */
	private String getLevelName(GameEngine engine) {
		int lvRaw = engine.statistics.level;
		int lv100 = lvRaw / 100;
		int lvUnit = lvRaw % 100;
		String str = "";
		
		if (lv100 < 10) {
			str += "H" + lv100 + "-" + lvUnit;
		} else if (lv100 < 20) {
			str += "M" + (lv100 - 10) + "-" + lvUnit;
		} else {
			str += "WTF";
		}
		
		return str;
	}
}
