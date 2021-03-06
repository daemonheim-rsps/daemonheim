package world.gregs.voidps.engine.entity.gfx

import world.gregs.voidps.engine.entity.Entity
import world.gregs.voidps.engine.entity.Values
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.visual.Graphic
import world.gregs.voidps.engine.event.Events
import world.gregs.voidps.engine.map.Tile

data class AreaGraphic(override var tile: Tile, val graphic: Graphic, val owner: String? = null) : Entity {
    override val id = -1

    fun visible(player: Player) = owner == null || owner == player.name
    override val events: Events = Events(this)
    override val values: Values = Values()
}