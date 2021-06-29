package world.gregs.voidps.engine.client.update.task.npc

import kotlinx.coroutines.Runnable
import world.gregs.voidps.engine.entity.character.MoveStop
import world.gregs.voidps.engine.entity.character.Moved
import world.gregs.voidps.engine.entity.character.move.running
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCMoveType
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.update.visual.npc.turn
import world.gregs.voidps.engine.map.Delta
import world.gregs.voidps.engine.map.collision.Collisions

/**
 * Changes the tile npcs are located on based on [Movement.delta] and [Movement.steps]
 */
class NPCMovementTask(
    private val npcs: NPCs,
    private val collisions: Collisions
) : Runnable {

    override fun run() {
        npcs.forEach { npc ->
            if (!npc.movement.frozen) {
                step(npc)
            }
            move(npc)
        }
    }

    /**
     * Sets up walk and run changes based on [Steps] queue.
     */
    fun step(npc: NPC) {
        val movement = npc.movement
        val steps = movement.steps
        movement.moving = steps.peek() != null
        if (movement.moving) {
            var step = steps.poll()
            if (!movement.traversal.blocked(npc.tile, step)) {
                movement.previousTile = npc.tile
                movement.walkStep = step
                movement.delta = step.delta
                npc.turn(step, false)
                npc.movementType = if (npc.crawling) NPCMoveType.Crawl else NPCMoveType.Walk
                if (npc.running) {
                    if (steps.peek() != null) {
                        val tile = npc.tile.add(step.delta)
                        step = steps.poll()
                        if (!movement.traversal.blocked(tile, step)) {
                            movement.previousTile = tile
                            movement.runStep = step
                            movement.delta = movement.delta.add(step.delta)
                            npc.turn(step, false)
                            npc.movementType = NPCMoveType.Run
                        }
                    } else {
                        npc.movementType = if (npc.crawling) NPCMoveType.Crawl else NPCMoveType.Walk
                    }
                }
            }
            if (steps.isEmpty()) {
                npc.events.emit(MoveStop)
            }
        }
    }

    /**
     * Moves the npc tile and emits Moved event
     */
    fun move(npc: NPC) {
        val movement = npc.movement
        movement.trailingTile = npc.tile
        if (movement.delta != Delta.EMPTY) {
            val from = npc.tile
            npc.tile = npc.tile.add(movement.delta)
            npcs.update(from, npc.tile, npc)
            collisions.move(npc, from, npc.tile)
            npc.events.emit(Moved(from, npc.tile))
        }
    }
}