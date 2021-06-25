package world.gregs.voidps.engine.client.ui.interact

import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.event.Event

/**
 * @author Jacob Rhiel <jacob.rhiel@gmail.com>
 * @created Jun 19, 2021
 */
data class InterfaceOnObject(
    val obj: GameObject,
    val interfaceId: Int,
    val name: String,
    val componentId: Int,
    val component: String,
    val item: Item,
    val itemIndex: Int,
    val container: String
) : Event