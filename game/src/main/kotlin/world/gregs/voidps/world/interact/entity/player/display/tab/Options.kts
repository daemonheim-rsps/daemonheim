package world.gregs.voidps.world.interact.entity.player.display.tab

import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.client.ui.hasScreenOpen
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.network.encode.message

on<InterfaceOption>({ name == "options" && component == "graphics" && option == "Graphics Settings" }) { player: Player ->
    if (player.hasScreenOpen()) {
        player.message("Please close the interface you have open before setting your graphics options.")
        return@on
    }
    player.open("graphics_options")
}

on<InterfaceOption>({ name == "options" && component == "audio" && option == "Audio Settings" }) { player: Player ->
    if (player.hasScreenOpen()) {
        player.message("Please close the interface you have open before setting your audio options.")
        return@on
    }
    player.open("audio_options")
}