package world.gregs.voidps.engine.client.ui.interact

import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Event

data class InterfaceOnNPC(
    val npc: NPC,
    val interfaceId: Int,
    val name: String,
    val componentId: Int,
    val component: String,
    val item: Item,
    val itemIndex: Int,
    val container: String
) : Event