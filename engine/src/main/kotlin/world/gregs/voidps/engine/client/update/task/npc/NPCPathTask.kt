package world.gregs.voidps.engine.client.update.task.npc

import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.path.PathFinder
import world.gregs.voidps.engine.tick.task.EntityTask

/**
 * Calculates paths for npcs that want to move
 */
class NPCPathTask(override val entities: NPCs, val finder: PathFinder) : EntityTask<NPC>() {

    override fun predicate(entity: NPC): Boolean {
        return entity.movement.strategy != null
    }

    override fun runAsync(npc: NPC) {
        val strategy = npc.movement.strategy!!
        npc.movement.result = finder.find(npc, strategy)
        npc.movement.length = npc.movement.steps.size
        npc.movement.strategy = null
    }

}