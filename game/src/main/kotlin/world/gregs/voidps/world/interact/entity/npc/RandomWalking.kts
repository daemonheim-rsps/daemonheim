package world.gregs.voidps.world.interact.entity.npc

import world.gregs.voidps.engine.action.ActionFinished
import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.action.action
import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.character.move.walkTo
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.getOrNull
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.area.Area
import kotlin.random.Random

on<Registered> { npc: NPC ->
    val walkMask = npc.def.walkMask.toInt()
    if (walkMask and 0x1 != 0 && walkMask and 0x2 != 0) {
        npc.events.on<NPC, ActionFinished>({ type != ActionType.Movement && it.levels.get(Skill.Constitution) > 0 }) {
            randomWalk(npc)
        }
        randomWalk(npc)
    }
}

fun randomWalk(npc: NPC) {
    npc.action(ActionType.Movement) {
        while (isActive) {
            val spawn: Tile = npc.getOrNull("spawn_tile") ?: break
            val radius = npc.def.getOrNull("wander_radius") as? Int
            val area: Area? = npc.getOrNull("area")
            val tile = npc.tile.add(Random.nextInt(-5, 5), Random.nextInt(-5, 5))
            if ((radius != null && tile.within(spawn, radius)) || (area != null && area.contains(tile))) {
                npc.walkTo(tile, cancelAction = false)
            }
            delay(Random.nextInt(0, 20))
        }
    }
}