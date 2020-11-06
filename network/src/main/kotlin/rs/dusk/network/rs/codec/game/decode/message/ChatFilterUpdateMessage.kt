package rs.dusk.network.rs.codec.game.decode.message

import rs.dusk.core.network.model.message.Message

/**
 * Player has changed their online status while in the lobby
 * @param publicFlag Unknown
 * @param privateFlag The players online status
 * @param friendsFlag Unknown
 */
data class ChatFilterUpdateMessage(val publicFlag: Int, val privateFlag: Int, val friendsFlag: Int) : Message