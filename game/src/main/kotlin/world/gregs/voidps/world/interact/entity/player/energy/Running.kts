package world.gregs.voidps.world.interact.entity.player.energy

import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.client.ui.event.InterfaceOpened
import world.gregs.voidps.engine.client.variable.*
import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.character.move.running
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.get
import world.gregs.voidps.engine.entity.set
import world.gregs.voidps.engine.entity.start
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.network.encode.message
import world.gregs.voidps.network.encode.sendRunEnergy

StringMapVariable(173, Variable.Type.VARP, true, mapOf(
    "walk" to 0,
    "run" to 1,
    "rest" to 3,
    "music" to 4
)).register("movement")

on<InterfaceOpened>({ name == "energy_orb" }) { player: Player ->
    player.sendRunEnergy(player.energyPercent())
}

on<Registered> { player: Player ->
    player.sendVar("movement")
    player.running = player.getVar("movement", "walk") == "run"
    player.start("energy")
}

on<InterfaceOption>({ name == "energy_orb" && option == "Turn Run mode on" }) { player: Player ->
    if (player.action.type == ActionType.Resting) {
        toggleRun(player, player["movement", "walk"])
        player["movement"] = if (player["movement", "walk"] == "walk") "run" else "walk"
        player.action.cancel(ActionType.Resting)
        return@on
    }
    toggleRun(player, player.getVar("movement", "walk"))
}

fun toggleRun(player: Player, type: String) {
    val energy = player.energyPercent()
    if (energy == 0) {
        player.message("You don't have enough energy left to run!", ChatType.GameFilter)
    }
    val walk = type == "walk" && energy > 0
    player.setVar("movement", if (walk) "run" else "walk")
    player.running = walk
}