package world.gregs.voidps.engine.entity.item

import world.gregs.voidps.engine.event.Event

data class FloorItemOption(val floorItem: FloorItem, val option: String?, val partial: Boolean) : Event