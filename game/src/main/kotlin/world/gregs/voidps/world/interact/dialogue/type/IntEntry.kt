package world.gregs.voidps.world.interact.dialogue.type

import world.gregs.voidps.engine.client.ui.dialogue.DialogueContext
import world.gregs.voidps.network.encode.sendScript

private const val INTEGER_ENTRY_SCRIPT = 108

suspend fun DialogueContext.intEntry(text: String): Int {
    player.sendScript(INTEGER_ENTRY_SCRIPT, text)
    return await("int")
}