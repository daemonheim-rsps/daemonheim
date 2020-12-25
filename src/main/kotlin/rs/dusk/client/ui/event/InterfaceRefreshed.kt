package rs.dusk.client.ui.event

import rs.dusk.core.event.character.PlayerEvent
import rs.dusk.engine.event.EventCompanion
import rs.dusk.game.entity.character.player.Player

data class InterfaceRefreshed(override val player: Player, val id: Int, val name: String) : PlayerEvent() {
    companion object : EventCompanion<InterfaceRefreshed>
}
