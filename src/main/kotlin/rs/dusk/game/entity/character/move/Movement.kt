package rs.dusk.game.entity.character.move

import get
import rs.dusk.client.ui.awaitInterfaces
import rs.dusk.core.action.action
import rs.dusk.core.map.Tile
import rs.dusk.core.path.PathFinder
import rs.dusk.core.path.PathFinder.Companion.getStrategy
import rs.dusk.engine.action.ActionType
import rs.dusk.engine.entity.Direction
import rs.dusk.engine.path.PathResult
import rs.dusk.engine.path.TargetStrategy
import rs.dusk.engine.path.TraversalStrategy
import rs.dusk.engine.path.find.BreadthFirstSearch
import rs.dusk.engine.task.TaskExecutor
import rs.dusk.engine.task.sync
import rs.dusk.game.entity.character.player.Player
import java.util.*

private val Any?.type : Any
	get() {
		TODO("Not yet implemented")
	}

private val Character?.action : Any
	get() {
		TODO("Not yet implemented")
	}

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 26, 2020
 */

typealias Steps = LinkedList<Direction>

data class Movement(
	var previousTile : Tile,
	var trailingTile : Tile = Tile.EMPTY,
	var delta : Tile = Tile.EMPTY,
	var walkStep : Direction = Direction.NONE,
	var runStep : Direction = Direction.NONE,
	val steps : LinkedList<Direction> = LinkedList(),
	val directions : Array<Array<Direction?>> = Array(BreadthFirstSearch.GRAPH_SIZE) {
		Array<Direction?>(
			BreadthFirstSearch.GRAPH_SIZE
		) { null }
	},
	val distances : Array<IntArray> = Array(BreadthFirstSearch.GRAPH_SIZE) { IntArray(BreadthFirstSearch.GRAPH_SIZE) },
	val calc : Queue<Tile> = LinkedList(),
	var frozen : Boolean = false,
	var running : Boolean = false
) {
	
	lateinit var traversal : TraversalStrategy
	
	fun clear() {
		steps.clear()
		reset()
	}
	
	fun reset() {
		delta = Tile.EMPTY
		walkStep = Direction.NONE
		runStep = Direction.NONE
	}
}

fun Player.walkTo(target : Character, strategy : TargetStrategy = getStrategy(target), action : (PathResult) -> Unit) =
	get<TaskExecutor>().sync {
		action(ActionType.Movement) {
			try {
				val player = this@walkTo
				val path : PathFinder = get()
				retry@ while (true) {
					if (strategy.reached(player.tile, size)) {
						action(PathResult.Success.Complete(tile))
						break
					} else {
						movement.clear()
						val result = path.find(player, strategy)
						if (result is PathResult.Failure) {
							action(result)
							break
						}
						
						// Await until reached the end of the path
						while (delay(0) && awaitInterfaces()) {
							if (movement.steps.isEmpty()) {
								break
							}
							if ((target as? Character)?.action?.type == ActionType.Movement) {
								continue@retry
							}
						}
						
						if (result is PathResult.Success.Partial) {
							action(result)
							break
						}
					}
				}
			} finally {
				movement.clear()
			}
		}
	}