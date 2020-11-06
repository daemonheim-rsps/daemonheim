package rs.dusk.network.rs.codec.game.encode.message

import rs.dusk.core.network.model.message.Message

/**
 * Updates the details of the friends chat
 * @param channel The friends chat channel to display
 */
data class FriendsChatUpdate(
    val owner: String?,
    val channelName: Long,
    val kickRank: Int,
    val memberCount: Int,
    val friends: List<Friend> = listOf()
) : Message
{

    class Friend(
        val username: String,
        val displayName: String,
        val worldId: Int,
        val rank: Int
    )

}