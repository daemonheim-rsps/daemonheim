package rs.dusk.network.rs.codec.game.encode.message

import rs.dusk.core.network.model.message.Message

/**
 * @author Jacob Rhiel <jacob.rhiel@gmail.com>
 * @created Oct 30, 2020 
 */
data class FriendListUpdateMessage(
    val worldId: Int,
    val displayName: String,
    val previousName: String,
    val renamed: Boolean,
    val friendsChatRank: Int,
    val referred: Boolean
) : Message