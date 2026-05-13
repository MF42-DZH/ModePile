package zeroxfc.nullpo.custom.modes.objects.seasons;

import mu.nu.nullpo.game.event.EventReceiver;
import zeroxfc.nullpo.custom.libs.GameTextUtilities;
import zeroxfc.nullpo.custom.libs.PlayerAchievements;
import zeroxfc.nullpo.custom.libs.ProfileProperties;
import zeroxfc.nullpo.custom.libs.PropertyCodec;

public class SeasonsAchievements extends PlayerAchievements {
    public final Achievement<Boolean> reachedMar;
    public final Achievement<Boolean> reachedApr;
    public final Achievement<Boolean> reachedMay;
    public final Achievement<Boolean> reachedJun;
    public final Achievement<Boolean> reachedJul;
    public final Achievement<Boolean> reachedAug;
    public final Achievement<Boolean> reachedSep;
    public final Achievement<Boolean> reachedOct;
    public final Achievement<Boolean> reachedNov;
    public final Achievement<Boolean> reachedDec;
    public final Achievement<Boolean> reachedJan;

    public final Achievement<Boolean> reachedRollStart;
    public final Achievement<Boolean> reachedRollSum;
    public final Achievement<Boolean> reachedRollAut;
    public final Achievement<Boolean> reachedRollWin;
    public final Achievement<Boolean> reachedRollEnd;

    public final Achievement<Integer> gotGreenGrade;
    public final Achievement<Integer> gotYellowGrade;
    public final Achievement<Integer> gotOrangeGrade;
    public final Achievement<Integer> gotCyanGrade;
    public final Achievement<Integer> gotMasterGrade;
    public final Achievement<Integer> gotSeasonsGrandMaster;

    public final Achievement<Boolean> secretGrade;
    public final Achievement<Boolean> oSpin;
    public final Achievement<Boolean> pentris;
    public final Achievement<Boolean> monthSpeedrun;
    public final Achievement<Boolean> perklessCompletion;
    public final Achievement<Boolean> nice;
    public final Achievement<Integer> aQuickEnd;

    public SeasonsAchievements(ProfileProperties playerProperties) {
        super(SeasonsSettings.PROP_ROOT, playerProperties);

        // region Progression
        reachedMar = registerSimple(
            "THE SPROUTS APPEAR",
            "mar",
            EventReceiver.COLOR_GREEN,
            GameTextUtilities.TextBlock.of(
                GameTextUtilities.Text.custom("REACH MARCH AND SEE THE", EventReceiver.COLOR_WHITE, 0.625f),
                GameTextUtilities.Text.newLine(),
                GameTextUtilities.Text.custom("APPEARANCE OF NEW SPROUTS", EventReceiver.COLOR_WHITE, 0.625f)
            ),
            false
        );

        reachedApr = registerSimple(
            "THE BLOOMS RISE",
            "apr",
            EventReceiver.COLOR_GREEN,
            GameTextUtilities.TextBlock.of(
                GameTextUtilities.Text.custom("REACH APRIL AND SEE THE", EventReceiver.COLOR_WHITE, 0.625f),
                GameTextUtilities.Text.newLine(),
                GameTextUtilities.Text.custom("THE VIBRANT BLOOMS", EventReceiver.COLOR_WHITE, 0.625f)
                ),
            false
        );

        reachedMay = registerSimple(
            "DRY HEAT",
            "may",
            EventReceiver.COLOR_YELLOW,
            GameTextUtilities.TextBlock.of(
                GameTextUtilities.Text.custom("REACH MAY AND FEEL THE", EventReceiver.COLOR_WHITE, 0.625f),
                GameTextUtilities.Text.newLine(),
                GameTextUtilities.Text.custom("RISE IN AIR TEMPERATURE", EventReceiver.COLOR_WHITE, 0.625f)
            ),
            false
        );

        reachedJun = registerSimple(
            "HALLUCINATION",
            "jun",
            EventReceiver.COLOR_YELLOW,
            GameTextUtilities.TextBlock.of(
                GameTextUtilities.Text.custom("REACH JUNE AND START", EventReceiver.COLOR_WHITE, 0.625f),
                GameTextUtilities.Text.newLine(),
                GameTextUtilities.Text.custom("SEEING THINGS THAT", EventReceiver.COLOR_WHITE, 0.625f),
                GameTextUtilities.Text.newLine(),
                GameTextUtilities.Text.custom("ARE NOT QUITE REAL", EventReceiver.COLOR_WHITE, 0.625f)
            ),
            false
        );

        reachedJul = registerSimple(
            "OUT OF THE FRYING PAN",
            "jul",
            EventReceiver.COLOR_YELLOW,
            GameTextUtilities.TextBlock.of(
                GameTextUtilities.Text.custom("REACH JULY AND BEGIN", EventReceiver.COLOR_WHITE, 0.625f),
                GameTextUtilities.Text.newLine(),
                GameTextUtilities.Text.custom("TO ESCAPE FROM THE FIRES", EventReceiver.COLOR_WHITE, 0.625f)
            ),
            false
        );

        reachedAug = registerSimple(
            "THE WIND IS CALLING",
            "aug",
            EventReceiver.COLOR_ORANGE,
            GameTextUtilities.TextBlock.of(
                GameTextUtilities.Text.custom("REACH AUGUST AND FEEL THE", EventReceiver.COLOR_WHITE, 0.625f),
                GameTextUtilities.Text.newLine(),
                GameTextUtilities.Text.custom("COOL BREEZE KNOCK THE LEAVES", EventReceiver.COLOR_WHITE, 0.625f),
                GameTextUtilities.Text.newLine(),
                GameTextUtilities.Text.custom("OFF THE TREES", EventReceiver.COLOR_WHITE, 0.625f)
            ),
            false
        );

        reachedSep = registerSimple(
            "MALIGNANT AIRFLOW",
            "sep",
            EventReceiver.COLOR_ORANGE,
            GameTextUtilities.TextBlock.of(
                GameTextUtilities.Text.custom("REACH SEPTEMBER AND SEE THE", EventReceiver.COLOR_WHITE, 0.625f),
                GameTextUtilities.Text.newLine(),
                GameTextUtilities.Text.custom("STRONG WINDS KNOCK EVERTHING", EventReceiver.COLOR_WHITE, 0.625f),
                GameTextUtilities.Text.newLine(),
                GameTextUtilities.Text.custom("OFF-COURSE", EventReceiver.COLOR_WHITE, 0.625f)
            ),
            false
        );

        reachedOct = registerSimple(
            "SPOOKY MONTH",
            "oct",
            EventReceiver.COLOR_ORANGE,
            GameTextUtilities.TextBlock.of(
                GameTextUtilities.Text.custom("REACH OCTOBER AND- HEY! WHO", EventReceiver.COLOR_WHITE, 0.625f),
                GameTextUtilities.Text.newLine(),
                GameTextUtilities.Text.custom("TURNED ALL THE LIGHTS OFF?!", EventReceiver.COLOR_WHITE, 0.625f)
            ),
            false
        );

        reachedNov = registerSimple(
            "VISIBILITY CRISIS",
            "nov",
            EventReceiver.COLOR_CYAN,
            GameTextUtilities.TextBlock.of(
                GameTextUtilities.Text.custom("REACH NOVEMBER AND WITNESS", EventReceiver.COLOR_WHITE, 0.625f),
                GameTextUtilities.Text.newLine(),
                GameTextUtilities.Text.custom("THE FOG AND SNOW OBSCURING", EventReceiver.COLOR_WHITE, 0.625f),
                GameTextUtilities.Text.newLine(),
                GameTextUtilities.Text.custom("ALL FROM VIEW", EventReceiver.COLOR_WHITE, 0.625f)
            ),
            false
        );

        reachedDec = registerSimple(
            "GET YOUR SHOVELS",
            "dec",
            EventReceiver.COLOR_CYAN,
            GameTextUtilities.TextBlock.of(
                GameTextUtilities.Text.custom("REACH DECEMBER, WITH THE HOPES", EventReceiver.COLOR_WHITE, 0.625f),
                GameTextUtilities.Text.newLine(),
                GameTextUtilities.Text.custom("THAT YOU'VE REMEMBERED TO BRING", EventReceiver.COLOR_WHITE, 0.625f),
                GameTextUtilities.Text.newLine(),
                GameTextUtilities.Text.custom("YOUR SHOVELS", EventReceiver.COLOR_WHITE, 0.625f)
            ),
            false
        );

        reachedJan = registerSimple(
            "FROZEN BURIAL",
            "jan",
            EventReceiver.COLOR_CYAN,
            GameTextUtilities.TextBlock.of(
                GameTextUtilities.Text.custom("REACH JANUARY AND HOPE TO AVOID", EventReceiver.COLOR_WHITE, 0.625f),
                GameTextUtilities.Text.newLine(),
                GameTextUtilities.Text.custom("THE ICE RAINING DOWN FROM THE", EventReceiver.COLOR_WHITE, 0.625f),
                GameTextUtilities.Text.newLine(),
                GameTextUtilities.Text.custom("HIGH HEAVENS", EventReceiver.COLOR_WHITE, 0.625f)
            ),
            false
        );

        reachedRollStart = registerSimple(
            "THE END...?",
            "notTheEnd",
            EventReceiver.COLOR_GREEN,
            GameTextUtilities.TextBlock.of(
                GameTextUtilities.Text.custom("OR IS IT REALLY...?", EventReceiver.COLOR_WHITE, 0.625f)
            ),
            false
        );

        reachedRollSum = registerSimple(
            "INFERNAL CATASTROPHE",
            "rollIsOnFire",
            EventReceiver.COLOR_YELLOW,
            GameTextUtilities.TextBlock.of(
                GameTextUtilities.Text.custom("GO FORTH, RUN FROM THE BLAZE!", EventReceiver.COLOR_WHITE, 0.625f)
            ),
            false
        );

        reachedRollAut = registerSimple(
            "EXCESS ECTOPLASM",
            "rollIsHaunted",
            EventReceiver.COLOR_ORANGE,
            GameTextUtilities.TextBlock.of(
                GameTextUtilities.Text.custom("PROTECT YOURSELF FROM THE SPIRITS!", EventReceiver.COLOR_WHITE, 0.625f)
            ),
            false
        );

        reachedRollWin = registerSimple(
            "STILLNESS IN THE COLD",
            "rollIsStolen",
            EventReceiver.COLOR_CYAN,
            GameTextUtilities.TextBlock.of(
                GameTextUtilities.Text.custom("THIS IS IT. IT'S NOW OR NEVER", EventReceiver.COLOR_WHITE, 0.625f),
                GameTextUtilities.Text.newLine(),
                GameTextUtilities.Text.custom("ESCAPE THIS COLD, AND FINISH", EventReceiver.COLOR_WHITE, 0.625f),
                GameTextUtilities.Text.newLine(),
                GameTextUtilities.Text.custom("YOUR JOURNEY!", EventReceiver.COLOR_WHITE, 0.625f)
            ),
            false
        );

        reachedRollEnd = registerSimple(
            "TRIUMPH",
            "youAreWinner",
            EventReceiver.COLOR_PINK,
            GameTextUtilities.TextBlock.of(
                GameTextUtilities.Text.custom("CONGRATULATIONS! YOU HAVE", EventReceiver.COLOR_WHITE, 0.625f),
                GameTextUtilities.Text.newLine(),
                GameTextUtilities.Text.custom("COMPLETED ", EventReceiver.COLOR_WHITE, 0.625f),
                GameTextUtilities.Text.custom("SEASONS", EventReceiver.COLOR_YELLOW, 0.625f),
                GameTextUtilities.Text.custom(" MODE!", EventReceiver.COLOR_WHITE, 0.625f)
            ),
            false
        );

        gotGreenGrade = this.<Integer>registerAchievement(
            GameTextUtilities.TextBlock.of(
                GameTextUtilities.Text.custom("A SIMPLE LIFE", EventReceiver.COLOR_GREEN, 1f)
            ),
            GameTextUtilities.TextBlock.of(
                GameTextUtilities.Text.custom("REACH A GREEN TITLE", EventReceiver.COLOR_WHITE, 0.625f)
            ),
            false,
            "greenGrade",
            PropertyCodec.IntegerCodec.INSTANCE,
            0,
            100,
            (v, t) -> v >= t,
            v -> v / 100.0
        );

        gotYellowGrade = this.<Integer>registerAchievement(
            GameTextUtilities.TextBlock.of(
                GameTextUtilities.Text.custom("BATTLE-SCARRED", EventReceiver.COLOR_YELLOW, 1f)
            ),
            GameTextUtilities.TextBlock.of(
                GameTextUtilities.Text.custom("REACH A YELLOW TITLE", EventReceiver.COLOR_WHITE, 0.625f)
            ),
            false,
            "yellowGrade",
            PropertyCodec.IntegerCodec.INSTANCE,
            0,
            2900,
            (v, t) -> v >= t,
            v -> v / 2900.0
        );

        gotOrangeGrade = this.<Integer>registerAchievement(
            GameTextUtilities.TextBlock.of(
                GameTextUtilities.Text.custom("LEARNED CONTROL", EventReceiver.COLOR_ORANGE, 1f)
            ),
            GameTextUtilities.TextBlock.of(
                GameTextUtilities.Text.custom("REACH AN ORANGE TITLE", EventReceiver.COLOR_WHITE, 0.625f)
            ),
            false,
            "orangeGrade",
            PropertyCodec.IntegerCodec.INSTANCE,
            0,
            5700,
            (v, t) -> v >= t,
            v -> v / 5700.0
        );

        gotCyanGrade = this.<Integer>registerAchievement(
            GameTextUtilities.TextBlock.of(
                GameTextUtilities.Text.custom("RULER OF THE ELEMENTS", EventReceiver.COLOR_CYAN, 1f)
            ),
            GameTextUtilities.TextBlock.of(
                GameTextUtilities.Text.custom("REACH A CYAN TITLE", EventReceiver.COLOR_WHITE, 0.625f)
            ),
            false,
            "cyanGrade",
            PropertyCodec.IntegerCodec.INSTANCE,
            0,
            8500,
            (v, t) -> v >= t,
            v -> v / 8500.0
        );

        gotMasterGrade = this.<Integer>registerAchievement(
            GameTextUtilities.TextBlock.of(
                GameTextUtilities.Text.custom("MASTER OF TIME", EventReceiver.COLOR_PINK, 1f)
            ),
            GameTextUtilities.TextBlock.of(
                GameTextUtilities.Text.custom("REACH THE MASTER TITLE", EventReceiver.COLOR_WHITE, 0.625f)
            ),
            false,
            "masterGrade",
            PropertyCodec.IntegerCodec.INSTANCE,
            0,
            11300,
            (v, t) -> v >= t,
            v -> v / 11300.0
        );

        gotSeasonsGrandMaster = this.<Integer>registerAchievement(
            GameTextUtilities.TextBlock.of(
                GameTextUtilities.Text.custom("GRAND MASTER OF REALITY", EventReceiver.COLOR_YELLOW, 1f)
            ),
            GameTextUtilities.TextBlock.of(
                GameTextUtilities.Text.custom("REACH THE ", EventReceiver.COLOR_WHITE, 0.625f),
                GameTextUtilities.Text.custom("SEASONS", EventReceiver.COLOR_YELLOW, 0.625f),
                GameTextUtilities.Text.newLine(),
                GameTextUtilities.Text.custom("GRAND MASTER ", EventReceiver.COLOR_ORANGE, 0.625f),
                GameTextUtilities.Text.custom("TITLE", EventReceiver.COLOR_WHITE, 0.625f)
            ),
            false,
            "seasonsGrandMasterGrade",
            PropertyCodec.IntegerCodec.INSTANCE,
            0,
            12000,
            (v, t) -> v >= t,
            v -> v / 12000.0
        );
        // endregion

        // region Bonus
        secretGrade = registerSimple(
            "GREATER THAN OBSCURE",
            "secretGrade",
            EventReceiver.COLOR_PINK,
            GameTextUtilities.TextBlock.of(
                GameTextUtilities.Text.custom("IT'S A SECRET TO EVERYONE...", EventReceiver.COLOR_WHITE, 0.625f),
                GameTextUtilities.Text.newLine(),
                GameTextUtilities.Text.custom("WHAT DO YOU MEAN EVERYONE", EventReceiver.COLOR_WHITE, 0.625f),
                GameTextUtilities.Text.newLine(),
                GameTextUtilities.Text.custom("KNOWS WHAT THIS SECRET IS?!", EventReceiver.COLOR_WHITE, 0.625f)
            ),
            true
        );

        oSpin = registerSimple(
            "A WHAT SPIN?",
            "oSpin",
            EventReceiver.COLOR_PINK,
            GameTextUtilities.TextBlock.of(
                GameTextUtilities.Text.custom("WHAT DO YOU MEAN YOU WANT ME", EventReceiver.COLOR_WHITE, 0.625f),
                GameTextUtilities.Text.newLine(),
                GameTextUtilities.Text.custom("TO SPIN THAT? YOU CAN'T SPIN", EventReceiver.COLOR_WHITE, 0.625f),
                GameTextUtilities.Text.newLine(),
                GameTextUtilities.Text.custom("THAT PIECE, IT'S IMPOSSIBLE!", EventReceiver.COLOR_WHITE, 0.625f)
            ),
            true
        );

        pentris = registerSimple(
            "FIVE OR MORE",
            "pentris",
            EventReceiver.COLOR_PINK,
            GameTextUtilities.TextBlock.of(
                GameTextUtilities.Text.custom("BUT WE DON'T HAVE ANYTHING", EventReceiver.COLOR_WHITE, 0.625f),
                GameTextUtilities.Text.newLine(),
                GameTextUtilities.Text.custom("THAT CAN CLEAR THAT MUCH?", EventReceiver.COLOR_WHITE, 0.625f)
            ),
            true
        );

        monthSpeedrun = registerSimple(
            "SIXTY SECOND SHOWDOWN",
            "monthSpeedrun",
            EventReceiver.COLOR_PINK,
            GameTextUtilities.TextBlock.of(
                GameTextUtilities.Text.custom("28/30/31 IN 60.", EventReceiver.COLOR_WHITE, 0.625f),
                GameTextUtilities.Text.newLine(),
                GameTextUtilities.Text.custom("DO YOU THINK YOU CAN DO THAT?", EventReceiver.COLOR_WHITE, 0.625f)
            ),
            true
        );

        perklessCompletion = registerSimple(
            "HELPLESS",
            "perklessCompletion",
            EventReceiver.COLOR_PINK,
            GameTextUtilities.TextBlock.of(
                GameTextUtilities.Text.custom("I DON'T NEED YOUR HELP", EventReceiver.COLOR_WHITE, 0.625f),
                GameTextUtilities.Text.newLine(),
                GameTextUtilities.Text.custom("TO SURVIVE THIS GAUNTLET!", EventReceiver.COLOR_WHITE, 0.625f)
            ),
            true
        );

        nice = registerSimple(
            "NICE :)",
            "nice",
            EventReceiver.COLOR_GREEN,
            GameTextUtilities.TextBlock.of(
                GameTextUtilities.Text.custom("YOU FOUND CHEESE! EVERYONE", EventReceiver.COLOR_WHITE, 0.625f),
                GameTextUtilities.Text.newLine(),
                GameTextUtilities.Text.custom("LOVES CHEESE, RIGHT? IT'S", EventReceiver.COLOR_WHITE, 0.625f),
                GameTextUtilities.Text.newLine(),
                GameTextUtilities.Text.custom("A LITTLE COLD THOUGH, SORRY.", EventReceiver.COLOR_WHITE, 0.625f)
            ),
            true
        );

        aQuickEnd = this.<Integer>registerAchievement(
            GameTextUtilities.TextBlock.of(
                GameTextUtilities.Text.custom("A QUICK END", EventReceiver.COLOR_GREEN, 1f)
            ),
            GameTextUtilities.TextBlock.of(
                GameTextUtilities.Text.custom("25 MINUTES. TAKE IT OR LEAVE IT.", EventReceiver.COLOR_WHITE, 0.625f)
            ),
            true,
            "aQuickEnd",
            PropertyCodec.IntegerCodec.INSTANCE,
            (25 * 60 * 60) + 1,
            (25 * 60 * 60),
            (v, t) -> v <= t,
            v -> (v <= 25 * 60 * 60) ? 1.0 : 0.0
        );
        // endregion
    }

    private Achievement<Boolean> registerSimple(String name, String path, int color, GameTextUtilities.TextBlock desc, boolean nonCounting) {
        return registerAchievement(
            GameTextUtilities.TextBlock.of(GameTextUtilities.Text.custom(name, color, 1f)),
            desc,
            nonCounting,
            path,
            PropertyCodec.BooleanCodec.INSTANCE,
            false,
            true,
            Boolean::equals,
            null
        );
    }
}
