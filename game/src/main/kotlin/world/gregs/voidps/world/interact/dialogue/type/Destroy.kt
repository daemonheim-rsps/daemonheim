package world.gregs.voidps.world.interact.dialogue.type

import world.gregs.voidps.engine.client.ui.dialogue.DialogueContext
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.client.ui.sendItem
import world.gregs.voidps.engine.client.ui.sendText
import world.gregs.voidps.engine.entity.definition.ItemDefinitions
import world.gregs.voidps.utility.get

private const val DESTROY_INTERFACE_NAME = "confirm_destroy"

suspend fun DialogueContext.destroy(text: String, item: String): Boolean {
    val itemDecoder: ItemDefinitions = get()
    if (player.open(DESTROY_INTERFACE_NAME)) {
        player.interfaces.sendText(DESTROY_INTERFACE_NAME, "line1", text.trimIndent().replace("\n", "<br>"))
        val def = itemDecoder.get(item)
        player.interfaces.sendText(DESTROY_INTERFACE_NAME, "item_name", def.name)
        player.interfaces.sendItem(DESTROY_INTERFACE_NAME, "item_slot", def.id, 1)
        return await("destroy")
    }
    return false
}