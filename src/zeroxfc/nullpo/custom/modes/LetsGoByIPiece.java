package zeroxfc.nullpo.custom.modes;

import zeroxfc.nullpo.custom.libs.Interpolation;
import zeroxfc.nullpo.custom.libs.MathHelper;

public class LetsGoByIPiece extends MarathonModeBase {
	private static final double FRICTION_COEFFICIENT = 0.6;

	// All masses in kilograms.
	private static final double LOCOMOTIVE_MASS = 80000d;
	private static final double PASSENGER_CARRIAGE_MASS = 50000d;
	private static final double PASSENGER_MASS = 70d;

	private static final int PASSENGER_CARRIAGE_COUNT = 11;
	private static final int PASSENGER_MAX_COUNT = 40 * PASSENGER_CARRIAGE_COUNT;

	private int passengers;
	private int brakeApplicationTime;
	private int throttleApplicationTime;
	private double passengerPercentage;
	private double velocity;
	private boolean controlsLocked;
	private GameType gameType;
	private GameDifficulty difficulty;
	private GearboxGear engineState;

	/**
	 * Gets the acceleration of the train relative to the gear state, application time and velocity.
	 * @param gear     Current train gear
	 * @param velocity Current train velocity
	 * @return Current acceleration.
	 */
	private double getEngineAcceleration(GearboxGear gear, double velocity) {
		double acceleration;
		int tat = MathHelper.clamp(throttleApplicationTime, 0, 300);
		int bat = MathHelper.clamp(brakeApplicationTime, 0, 30);

		switch (gear) {
			case THROTTLE_1:
				acceleration = Interpolation.lerp(1d, 0d, velocity / 25d);
				return Interpolation.lerp(0, acceleration, tat / 300d);
			case THROTTLE_2:
				acceleration = Interpolation.lerp(2d, 0d, velocity / 50d);
				return Interpolation.lerp(0, acceleration, tat / 300d);
			case THROTTLE_3:
				acceleration = Interpolation.lerp(4d, 0d, velocity / 75d);
				return Interpolation.lerp(0, acceleration, tat / 300d);
			case THROTTLE_4:
				acceleration = Interpolation.lerp(5d, 0d, velocity / 100d);
				return Interpolation.lerp(0, acceleration, tat / 300d);
			case THROTTLE_5:
				acceleration = Interpolation.lerp(7d, 0d, velocity / 125d);
				return Interpolation.lerp(0, acceleration, tat / 300d);
			case THROTTLE_6:
				acceleration = Interpolation.lerp(8d, 0d, velocity / 150d);
				return Interpolation.lerp(0, acceleration, tat / 300d);
			case BRAKE_1:
				return Interpolation.lerp(0d, -0.5d, bat / 30d);
			case BRAKE_2:
				return Interpolation.lerp(0d, -1.0d, bat / 30d);
			case BRAKE_3:
				return Interpolation.lerp(0d, -2.0d, bat / 30d);
			case BRAKE_4:
				return Interpolation.lerp(0d, -3.0d, bat / 30d);
			case BRAKE_5:
				return Interpolation.lerp(0d, -4.5d, bat / 30d);
			case BRAKE_6:
				return Interpolation.lerp(0d, -8.0d, bat / 30d);
			case BRAKE_EMERGENCY:
				return Interpolation.lerp(0d, -12.0d, bat / 30d);
			default:
				return 0d;
		}
	}

	/**
	 * Gets new velocity given old velocity and acceleration.
	 * @param oldVelocity  Old train velocity
	 * @param acceleration Current train acceleration
	 * @return New train velocity.
	 */
	private double calculateNewVelocity(double oldVelocity, double acceleration) {
		double vel = oldVelocity + ((1d / 60d) * acceleration);
		if (vel < 0) vel = 0;

		return vel;
	}

	/**
	 * The engine's various power states during travel.
	 */
	private enum GearboxGear {
		// Neutral engine state.
		NEUTRAL,

		// Engine throttle levels. (SPEED UP.)
		/*
		 * 1: 25 km/h, Acc. Max = 1 km/h/s
		 * 2: 50 km/h, Acc. Max = 2 km/h/s
		 * 3: 75 km/h, Acc. Max = 4 km/h/s
		 * 4: 100 km/h, Acc. Max = 5 km/h/s
		 * 5: 125 km/h, Acc. Max = 7 km/h/s
		 * 6: 150 km/h, Acc. Max = 8 km/h/s
		 */
		THROTTLE_1, THROTTLE_2, THROTTLE_3, THROTTLE_4, THROTTLE_5, THROTTLE_6,

		// Braking state. E-Brake is only set via ATS (Standard Only).
		/*
		 * 1: Dec. Max = -0.5 km/h/s
		 * 2: Dec. Max = -1.0 km/h/s
		 * 3: Dec. Max = -2.0 km/h/s
		 * 4: Dec. Max = -3.0 km/h/s
		 * 5: Dec. Max = -4.5 km/h/s
		 * 6: Dec. Max = -8.0 km/h/s
		 * E: Dec. Max = -12 km/h/s
		 */
		BRAKE_1, BRAKE_2, BRAKE_3, BRAKE_4, BRAKE_5, BRAKE_6, BRAKE_EMERGENCY
	}

	/**
	 * Game types.
	 */
	private enum GameType {
		/*
		 * TYPES:
		 * > Standard: normal service, stop at each station.
		 * > Rapid: rapid service, skip some stations.
		 * > Express: express service, only stop at one middle station and the endpoint station.
		 * > MTD: speedrun mode, get to the end of the track with no regard to station timings or safety.
		 */

		STANDARD(0), RAPID(1), EXPRESS(2), MTD(3);

		private int value;

		/**
		 * Gets the underlying <code>int</code> value of the game type.
		 * @return <code>int</code> value of game type.
		 */
		public int getValue() {
			return value;
		}

		/**
		 * Gets the game type enum associated with a value.
		 * @return Game type enum with the selected value. Returns <code>null</code> if it doesn't exist.
		 */
		public GameType getEnumFromValue(int value) {
			for (GameType type : values()) {
				if (type.getValue() == value) return type;
			}
			return null;
		}

		GameType(int value) {
			this.value = value;
		}
	}

	/**
	 * Game difficulties.
	 */
	private enum GameDifficulty {
		/*
		 * DIFFICULTIES:
		 * > LENIENT: 7.5 metres, 15 second leeway for stops / passes. 100 starting life points. HALF PENALTIES.
		 * > FAIR: 5 metres, 10 second leeway for stops / passes. 75 starting life points.
		 * > HARSH: 2 metres, 5 second leeway for stops / passes. 50 starting life points. DOUBLE PENALTIES.
		 *
		 * Note: 100 life points is the max, and penalties vary between difficulties.
		 *
		 * Bonuses:
		 * > Piece-Spin-X (Does not apply to MTD gametype):
		 *     >   ZERO: NO LP BONUS.
		 *     > SINGLE: +1 LP
		 *     > DOUBLE: +2 LP
		 *     > TRIPLE: +5 LP
		 * > 0cm stop differential: +20 LP
		 * > 0s stop / pass differential: +5 LP
		 *
		 * Base Penalties (Does not apply to MTD gametype):
		 * > Stop Point Overshoot / Undershoot: -10 LP per metre out of lenience
		 * > ATS Triggered (STANDARD): -10 LP, E-Brake engaged until stop
		 * > Speed Limit Violation (Any): -2 LP per second that passes with speed over speed limit
		 * > Stopping / Passing Too Early / Too Late: -10 LP per second out of lenience
		 */

		LENIENT(0), FAIR(1), HARSH(2);

		private int value;

		/**
		 * Gets the underlying <code>int</code> value of the difficulty.
		 * @return <code>int</code> value of game type.
		 */
		public int getValue() {
			return value;
		}

		/**
		 * Gets the difficulty enum associated with a value.
		 * @return Difficulty enum with the selected value. Returns <code>null</code> if it doesn't exist.
		 */
		public GameDifficulty getEnumFromValue(int value) {
			for (GameDifficulty type : values()) {
				if (type.getValue() == value) return type;
			}
			return null;
		}

		GameDifficulty(int value) {
			this.value = value;
		}
	}
}
