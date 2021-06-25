package world.gregs.voidps.engine.client.ui.interact

import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Event

/**
 * @author Jacob Rhiel <jacob.rhiel@gmail.com>
 * @created Jun 20, 2021
 */
data class InterfaceOnItem(
    val fromItem: Item,
    val toItem: Item,
    val fromSlot: Int,
    val toSlot: Int,
    val fromInterfaceId: Int,
    val fromComponentId: Int,
    val toInterfaceId: Int,
    val toComponentId: Int,
    val fromContainer: String,
    val toContainer: String
) : Event