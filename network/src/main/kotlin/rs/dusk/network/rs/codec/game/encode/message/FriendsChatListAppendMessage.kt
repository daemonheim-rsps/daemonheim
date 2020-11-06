package rs.dusk.network.rs.codec.game.encode.message

import rs.dusk.core.network.model.message.Message

/**
 * Updates the rank of a member in the friends chat list
 * @param member The player who's rank to update
 * @param rank Their new friend chat rank
 */
data class FriendsChatListAppendMessage(val worldId: Int,
                                        val username: String,
                                        val displayName: String,
                                        val rank: Int
) : Message