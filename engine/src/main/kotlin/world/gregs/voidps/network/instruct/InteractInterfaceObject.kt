package world.gregs.voidps.network.instruct

import world.gregs.voidps.network.Instruction

/**
 * @author Jacob Rhiel <jacob.rhiel@gmail.com>
 * @created Jun 19, 2021
 */
data class InteractInterfaceObject(
    val objectId: Int,
    val worldX: Int,
    val worldY: Int,
    val interfaceId: Int,
    val componentId: Int,
    val itemId: Int,
    val itemSlot: Int
) : Instruction