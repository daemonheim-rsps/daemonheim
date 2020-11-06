package rs.dusk.network.rs.codec.game.encode.message

import rs.dusk.core.network.model.message.Message

/**
 * Private message sent to another player
 * @param name The display name of the player the message was sent to
 * @param text The chat message text
 */
data class ChatPrivateToMessage(val name: String, val text: String) : Message