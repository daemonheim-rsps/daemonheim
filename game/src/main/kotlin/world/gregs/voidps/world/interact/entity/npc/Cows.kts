package world.gregs.voidps.world.interact.entity.npc

import world.gregs.voidps.engine.client.ui.interact.InterfaceOnNPC
import world.gregs.voidps.engine.delay
import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.visual.forceChat
import world.gregs.voidps.engine.entity.character.update.visual.setAnimation
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.network.encode.message
import kotlin.random.Random

on<Registered>({ it.def.name == "cow" }) { npc: NPC ->
    delay(npc, ticks = Random.nextInt(50, 200), loop = true) {
        npc.movement.clear()
        npc.forceChat = "Moo"
        npc.setAnimation("cow_eat_grass")
    }
}

on<InterfaceOnNPC>({ npc.def.name == "cow" }) { player: Player ->
    player.message("The cow doesn't want that.")
}