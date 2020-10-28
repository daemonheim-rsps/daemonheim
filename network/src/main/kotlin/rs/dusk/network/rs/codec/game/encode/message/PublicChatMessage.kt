package rs.dusk.network.rs.codec.game.encode.message

import rs.dusk.core.network.model.message.Message

/**
 * @author Jacob Rhiel <jacob.rhiel@gmail.com>
 * @created Oct 27, 2020
 */
data class PublicChatMessage(
    val index: Int,
    val effects: Int,
    val rights: Int,
    val message: String
) : Message