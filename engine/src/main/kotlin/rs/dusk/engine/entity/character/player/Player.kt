package rs.dusk.engine.entity.character.player

import rs.dusk.engine.action.Action
import rs.dusk.engine.client.ui.InterfaceOptions
import rs.dusk.engine.client.ui.Interfaces
import rs.dusk.engine.client.ui.dialogue.Dialogues
import rs.dusk.engine.entity.Size
import rs.dusk.engine.entity.character.Character
import rs.dusk.engine.entity.character.CharacterEffects
import rs.dusk.engine.entity.character.CharacterValues
import rs.dusk.engine.entity.character.contain.Container
import rs.dusk.engine.entity.character.move.Movement
import rs.dusk.engine.entity.character.player.delay.Delays
import rs.dusk.engine.entity.character.player.req.Requests
import rs.dusk.engine.entity.character.player.skill.Experience
import rs.dusk.engine.entity.character.player.skill.Levels
import rs.dusk.engine.entity.character.player.social.*
import rs.dusk.engine.entity.character.update.LocalChange
import rs.dusk.engine.entity.character.update.Visuals
import rs.dusk.engine.entity.character.update.visual.player.appearance
import rs.dusk.engine.map.Tile
import rs.dusk.engine.path.TargetStrategy
import rs.dusk.utility.get

/**
 * A player controlled by client or bot
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 28, 2020
 */
class Player(
    @Transient override var index: Int = -1,
    override var id: Int = -1,
    override var tile: Tile = Tile.EMPTY,
    val names: Name = Names(""),
    @Transient val details: PlayerDetails = PlayerDetails(names),
    @Transient override var size: Size = Size.TILE,
    @Transient val viewport: Viewport = Viewport(),
    @Transient override val visuals: Visuals = Visuals(),
    @Transient override val movement: Movement = Movement(tile),
    @Transient override val action: Action = Action(),
    val containers: MutableMap<Int, Container> = mutableMapOf(),
    val variables: MutableMap<Int, Any> = mutableMapOf(),
    @Transient override val values: CharacterValues = CharacterValues(),
    @Transient val delays: Delays = Delays(),
    @Transient val dialogues: Dialogues = Dialogues(),
    val experience: Experience = Experience(),
    val levels: Levels = Levels(experience),
    val relations: Relations = Relationships(),
    var channel: FriendsChat? = null,
    var friendsChat: Name? = null
) : Character {

    override val effects = CharacterEffects(this)

    @Transient
    val requests: Requests = Requests(this)

    @Transient
    val options = PlayerOptions(this, get())

    @Transient
    val gameFrame = PlayerGameFrame()

    @Transient
    lateinit var interfaces: Interfaces

    @Transient
    lateinit var interfaceOptions: InterfaceOptions

    @Transient
    override lateinit var interactTarget: TargetStrategy

    @Transient
    lateinit var followTarget: TargetStrategy

    @Transient
    override var change: LocalChange? = null

    @Transient
    var changeValue: Int = -1

    val administrator: Boolean
        get() = details.rights >= 2

    fun start() {
        options.set(2, "Follow")
        options.set(4, "Trade with")
        options.set(7, "Req Assist")
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Player
        return index == other.index
    }

    override fun hashCode(): Int {
        return index
    }

    override fun toString(): String {
        return "Player(${appearance.displayName}, index=$index, tile=$tile)"
    }
}