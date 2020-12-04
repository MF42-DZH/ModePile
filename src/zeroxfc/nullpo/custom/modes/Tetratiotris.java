//package zeroxfc.nullpo.custom.modes;
//
//import mu.nu.nullpo.game.component.Piece;
//import mu.nu.nullpo.game.event.EventReceiver;
//import mu.nu.nullpo.game.play.GameEngine;
//import zeroxfc.nullpo.custom.libs.ProfileProperties;
//import java.math.BigDecimal;
//import java.util.*;
//
//public class Tetratiotris extends MarathonModeBase {
//    /**
//     * POWERS
//     */
//    private BigDecimal POWER_SINGLE = new BigDecimal( 1.01 );
//    private BigDecimal POWER_DOUBLE = new BigDecimal( 1.02 );
//    private BigDecimal POWER_TRIPLE = new BigDecimal( 1.04 );
//    private BigDecimal POWER_TETRIS = new BigDecimal( 1.08 );
//
//    /**
//     * Rankings' scores
//     */
//    private BigDecimal[] rankingScore;
//
//    /**
//     * Rankings' line counts
//     */
//    private int[] rankingLines;
//
//    /**
//     * Rankings' times
//     */
//    private int[] rankingTime;
//
//    // PROFILE
//    private ProfileProperties playerProperties = null;
//    private boolean showPlayerStats = false;
//    private static final int headerColour = EventReceiver.COLOR_RED;
//    private int rankingRankPlayer = -1;
//    private BigDecimal[] rankingScorePlayer;
//    private int[] rankingTimePlayer;
//    private int[] rankingLinesPlayer;
//    private String PLAYER_NAME = null;
//
//    /**
//     * The good hard drop effect
//     */
//    private ArrayList< int[] > pCoordList = null;
//    private Piece cPiece = null;
//
//    /**
//     * Last score before increase
//     */
//    private BigDecimal scoreBefore = new BigDecimal( 0 );
//
//    /**
//     * Mode name
//     */
//    @Override
//    public String getName() {
//        return "TETRATIOTRIS";
//    }
//
//    /*
//     * Initialization
//     */
//    @Override
//    public void playerInit( GameEngine engine, int playerID ) {
//        owner = engine.owner;
//        receiver = engine.owner.receiver;
//        lastscore = 0;
//        scgettime = 0;
//        lastevent = EVENT_NONE;
//        lastb2b = false;
//        lastcombo = 0;
//        lastpiece = 0;
//        bgmlv = 0;
//
//        rankingRank = -1;
//        rankingScore = new BigDecimal[ RANKING_MAX ];
//        for ( int i = 0; i < RANKING_MAX; ++i ) rankingScore[ i ] = new BigDecimal( 0 );
//        rankingLines = new int[ RANKING_MAX ];
//        rankingTime = new int[ RANKING_MAX ];
//
//        if ( playerProperties == null ) {
//            playerProperties = new ProfileProperties( headerColour );
//            showPlayerStats = false;
//        }
//
//        rankingRankPlayer = -1;
//        rankingScorePlayer = new BigDecimal[ RANKING_MAX ];
//        for ( int i = 0; i < RANKING_MAX; ++i ) rankingScorePlayer[ i ] = new BigDecimal( 0 );
//        rankingLinesPlayer = new int[ RANKING_MAX ];
//        rankingTimePlayer = new int[ RANKING_MAX ];
//
//        pCoordList = new ArrayList<>();
//        cPiece = null;
//
//        netPlayerInit( engine, playerID );
//
//        if ( owner.replayMode == false ) {
//            loadSetting( owner.modeConfig );
//            loadRanking( owner.modeConfig, engine.ruleopt.strRuleName );
//            version = CURRENT_VERSION;
//        } else {
//            loadSetting( owner.replayProp );
//            if ( version == 0 && owner.replayProp.getProperty( "tetratiotris.endless", false ) ) goaltype = 2;
//
//            // NET: Load name
//            netPlayerName = engine.owner.replayProp.getProperty( "$playerID.net.netPlayerName", "" );
//        }
//
//        engine.owner.backgroundStatus.bg = startlevel;
//        engine.framecolor = GameEngine.FRAME_COLOR_PINK;
//    }
//
//    // EXTENSIONS
//    private BigDecimal lerp( BigDecimal v0, BigDecimal v1, BigDecimal t ) {
//        return ( t.multiply( v1 ) ).add( ( ( new BigDecimal( 1 ) ).subtract( t ) ).multiply( v0 ) );
//    }
//}
