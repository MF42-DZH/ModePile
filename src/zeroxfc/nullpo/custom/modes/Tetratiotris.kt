package zeroxfc.nullpo.custom.modes

import mu.nu.nullpo.game.component.Piece
import mu.nu.nullpo.game.event.EventReceiver
import mu.nu.nullpo.game.play.GameEngine
import zeroxfc.nullpo.custom.libs.ProfileProperties
import java.math.BigDecimal
import java.util.*

class Tetratiotris : MarathonModeBase() {
    /* POWERS */
    private val POWER_SINGLE: BigDecimal = BigDecimal(1.01)
    private val POWER_DOUBLE: BigDecimal = BigDecimal(1.02)
    private val POWER_TRIPLE: BigDecimal = BigDecimal(1.04)
    private val POWER_TETRIS: BigDecimal = BigDecimal(1.08)

    /**
     * Rankings' scores
     */
    lateinit var rankingScore: IntArray

    /**
     * Rankings' line counts
     */
    lateinit var rankingLines: IntArray

    /**
     * Rankings' times
     */
    lateinit var rankingTime: IntArray

    // PROFILE
    private var playerProperties: ProfileProperties? = null
    private var showPlayerStats = false
    private val headerColour = EventReceiver.COLOR_RED
    private var rankingRankPlayer = 0
    private lateinit var rankingScorePlayer: IntArray
    private lateinit var rankingTimePlayer: IntArray
    private lateinit var rankingLinesPlayer: IntArray
    private var PLAYER_NAME: String? = null

    /**
     * The good hard drop effect
     */
    private var pCoordList: ArrayList<IntArray>? = null
    private var cPiece: Piece? = null

    /** Last score before increase */
    private var scoreBefore: BigDecimal = BigDecimal(0)

    /** Mode name */
    override fun getName(): String {
        return "TETRATIOTRIS"
    }

    /*
     * Initialization
     */
    override fun playerInit(engine: GameEngine, playerID: Int) {
        owner = engine.owner
        receiver = engine.owner.receiver
        lastscore = 0
        scgettime = 0
        lastevent = EVENT_NONE
        lastb2b = false
        lastcombo = 0
        lastpiece = 0
        bgmlv = 0

        // TODO: pls fix
        rankingRank = -1
        rankingScore = IntArray(RANKING_MAX)
        rankingLines = IntArray(RANKING_MAX)
        rankingTime = IntArray(RANKING_MAX)

        if (playerProperties == null) {
            playerProperties = ProfileProperties(headerColour)
            showPlayerStats = false
        }

        rankingRankPlayer = -1
        rankingScorePlayer = IntArray(RANKING_MAX)
        rankingLinesPlayer = IntArray(RANKING_MAX)
        rankingTimePlayer = IntArray(RANKING_MAX)

        pCoordList = ArrayList()
        cPiece = null

        netPlayerInit(engine, playerID)

        if (owner.replayMode == false) {
            loadSetting(owner.modeConfig)
            loadRanking(owner.modeConfig, engine.ruleopt.strRuleName)
            version = CURRENT_VERSION
        } else {
            loadSetting(owner.replayProp)
            if (version == 0 && owner.replayProp.getProperty("tetratiotris.endless", false) == true) goaltype = 2

            // NET: Load name
            netPlayerName = engine.owner.replayProp.getProperty("$playerID.net.netPlayerName", "")
        }

        engine.owner.backgroundStatus.bg = startlevel
        engine.framecolor = GameEngine.FRAME_COLOR_PINK
    }

    // EXTENSIONS
    private fun lerp(v0: BigDecimal, v1: BigDecimal, t: BigDecimal) : BigDecimal {
        return t * v1 + (BigDecimal(1) - t) * v0
    }
}