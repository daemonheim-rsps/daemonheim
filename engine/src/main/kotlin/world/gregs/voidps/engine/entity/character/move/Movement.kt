package world.gregs.voidps.engine.entity.character.move

import kotlinx.coroutines.suspendCancellableCoroutine
import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.action.action
import world.gregs.voidps.engine.delay
import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.visual.watch
import world.gregs.voidps.engine.entity.get
import world.gregs.voidps.engine.entity.set
import world.gregs.voidps.engine.map.Delta
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.nav.Edge
import world.gregs.voidps.engine.path.PathFinder.Companion.getStrategy
import world.gregs.voidps.engine.path.PathResult
import world.gregs.voidps.engine.path.algorithm.AvoidAlgorithm
import world.gregs.voidps.engine.path.strat.TileTargetStrategy
import world.gregs.voidps.engine.path.traverse.TileTraversalStrategy
import world.gregs.voidps.utility.get
import java.util.*
import kotlin.coroutines.resume

data class Movement(
    var previousTile: Tile = Tile.EMPTY,
    var trailingTile: Tile = Tile.EMPTY,
    var delta: Delta = Delta.EMPTY,
    var walkStep: Direction = Direction.NONE,
    var runStep: Direction = Direction.NONE,
    val steps: LinkedList<Direction> = LinkedList<Direction>(),
    val waypoints: LinkedList<Edge> = LinkedList(),
    var frozen: Boolean = false
) {

    var moving = false
    var strategy: TileTargetStrategy? = null
    var action: (() -> Unit)? = null
    var result: PathResult? = null
    var length: Int = 0

    lateinit var traversal: TileTraversalStrategy

    fun set(strategy: TileTargetStrategy, action: (() -> Unit)? = null) {
        clear()
        result = null
        this.strategy = strategy
        this.action = action
    }

    fun clear() {
        waypoints.clear()
        steps.clear()
        reset()
    }

    fun reset() {
        delta = Delta.EMPTY
        walkStep = Direction.NONE
        runStep = Direction.NONE
    }
}

var Character.running: Boolean
    get() = get("running", false)
    set(value) = set("running", value)

suspend fun Character.freeze(block: suspend () -> Unit) {
    movement.frozen = true
    block.invoke()
    movement.frozen = false
}

fun Player.cantReach(target: Any): Boolean = cantReach(getStrategy(target))

fun Player.cantReach(strategy: TileTargetStrategy): Boolean {
    return movement.result is PathResult.Failure || (movement.result is PathResult.Partial && !strategy.reached(tile, size))
}

fun Character.walkTo(target: Any, watch: Character? = null, cancelAction: Boolean = true, action: (() -> Unit)? = null) {
    walkTo(getStrategy(target), watch, cancelAction, action)
}

fun Character.walkTo(strategy: TileTargetStrategy, watch: Character? = null, cancelAction: Boolean = true, action: (() -> Unit)? = null) {
    delay(this) {
        if (cancelAction) {
            this@walkTo.action.cancelAndJoin()
        }
        watch(watch)
        if (this is Player) {
            dialogues.clear()
        }
        movement.set(strategy, action)
    }
}

fun Character.avoid(target: Character) {
    val strategy = getStrategy(target)
    val pathfinder: AvoidAlgorithm = get()
    action(ActionType.Movement) {
        try {
            movement.set(strategy)
            watch(target)
            val result = pathfinder.find(tile, size, movement, strategy, movement.traversal)
            if (result is PathResult.Success) {
                suspendCancellableCoroutine<Unit> {
                    movement.action = {
                        it.resume(Unit)
                    }
                }
            }
            delay(4)
        } finally {
            watch(null)
        }
    }
}