package rs.dusk.engine.entity.character.player.social

import rs.dusk.engine.entity.character.player.Player

interface FriendsChat {

    /**
     * The name of the channel
     */
    val channelName: String?
    /**
     * The username of the owner of the channel
     */
    val owner: Name?
    /**
     * Owners relations to other players
     */
    val relations: Relations
    /**
     * The minimum rank required to join the channel
     */
    val joinRank: Ranks
    /**
     * The minimum rank required to talk in the channel
     */
    val talkRank: Ranks
    /**
     * The minimum rank required to kick others in the channel
     */
    val kickRank: Ranks
    /**
     * The minimum rank required to be able to receive loot-share
     */
    val lootRank: Ranks
    /**
     * Whether coin-share is enabled
     */
    val coinShare: Boolean
    /**
     * List of online members
     */
    val members: List<Player>
    /**
     * List of banned user names & ban expiration times
     */
    val bans: Map<Name, Long>

    /**
     * Checks if [player] is able to join the channel
     * And joins if they do
     * @param player The player attempting to join
     */
    fun join(player: Player)

    /**
     * Sends the channel details. Doesn't have any checks
     * Used for connecting/disconnecting between lobby
     * @param player The player to send the display too
     */
    fun display(player: Player)

    /**
     * Removes player from the channel
     * @param player The player leaving
     * @param kick Whether to give the message that they were kicked or that they have left.
     */
    fun leave(player: Player, kick: Boolean)

    /**
     * Messaging in the channel to all of it's members
     * @param player The player sending the message
     * @param message The message being sent
     */
    fun message(player: Player, message: String)

    /**
     * Quick Chat messaging in the channel to all of it's members
     * @param player The player sending the message
     * @param fileId The quick chat message type
     * @param data Additional message details being sent
     */
    fun quickMessage(player: Player, fileId: Int, data: ByteArray?)

    /**
     * Attempts to kick a member by name [target]
     * Checks rank & target rank
     * @param player The player kicking
     * @param target The name of the member to find and kick
     * @param ban Whether to ban the member from re-joining for an hour
     */
    fun kick(player: Player, target: Name, ban: Boolean)

    /**
     * Checks whether a player with username [name] is banned
     * from this channel. If their ban has elapsed it is removed.
     * @param name The name of the player to check
     * @return If the player is banned
     */
    fun banned(name: Name): Boolean

    /**
     * Checks if a player is a member of this channel
     * @param player The player to check
     * @return If the player is a member
     */
    fun isMember(player: Player): Boolean

    /**
     * Checks if a player is the owner (or an admin)
     * @param player The player to check
     * @return If the player is the owner
     */
    fun isOwner(player: Player): Boolean

    /**
     * Checks whether [player] meets the [rank] required (optional can check exclusively)
     * @param player The player who's rank to check
     * @param rank The rank required
     * @param inclusive Whether to check inclusively (default) or exclusively
     * @return Whether the player has the rank required
     */
    fun hasRank(player: Player, rank: Ranks, inclusive: Boolean = true): Boolean

    /**
     * Returns the rank of the player and also checks if they are an admin
     * Note: Recommended over [Name] [getRank] when possible
     * @param player The player who's rank to check
     * @return The rank of the player
     */
    fun getRank(player: Player): Ranks

    /**
     * Returns the rank of a player by name
     * @param name The username of the player who's rank to check
     * @return The rank of the player
     */
    fun getRank(name: Name): Ranks

    /**
     * Sets up/re-activates the channel
     * @param owner The owner of the channel
     * @param name The new name of the channel
     */
    fun setup(owner: Player, name: String)

    /**
     * Disables the channel, kicking all members
     * @param player The player attempting to disable the channel
     */
    fun disable(player: Player)

    /**
     * Checks and removes any bans which have expired
     * Should be called periodically
     */
    fun clean()

    /**
     * Updates a members rank, used for rank changes, adding & removing members
     * @param member The member to update
     * @param rank The new rank (optional)
     */
    fun updateMember(member: Player, rank: FriendsChat.Ranks = getRank(member))

    /**
     * Attempts to change a friends rank
     * @param player The player changing the rank
     * @param friend The player who's rank to change
     * @param rank The rank to set
     */
    fun promote(player: Player, friend: Name, rank: Int)

    /**
     * Removes rank from a member
     * @param player The player changing the rank
     * @param member The player who's rank to change
     */
    fun demote(player: Player, member: Name)

    /**
     * Sets the channel name so long as it does not exceed the maximum length or contain a filtered word
     * @param player The player making the change
     * @param name The newly proposed name
     * @return The new name (if set)
     */
    fun rename(player: Player, name: String): String?

    /**
     * Attempts to set the channels [joinRank]
     * @param player The player making the change
     * @param rank The rank to be set
     */
    fun setJoin(player: Player, rank: Ranks): Ranks?

    /**
     * Attempts to set the channels [talkRank]
     * @param player The player making the change
     * @param rank The rank to be set
     */
    fun setTalk(player: Player, rank: Ranks): Ranks?

    /**
     * Attempts to set the channels [kickRank]
     * @param player The player making the change
     * @param rank The rank to be set
     */
    fun setKick(player: Player, rank: Ranks): Ranks?

    /**
     * Attempts to set the channels [lootRank]
     * @param player The player making the change
     * @param rank The rank to be set
     */
    fun setLoot(player: Player, rank: Ranks): Ranks?

    /**
     * Attempts to set the channels [coinShare]
     * @param player The player making the change
     * @param value The value to be set
     */
    fun setShare(player: Player, value: Boolean): Boolean


    /**
     * Updates after a delay for settings refreshing
     */
    fun update()

    enum class Ranks(val value: Int, val string: String) {
        NO_ONE(-1, "No one"),//Un-ranked,
        ANYONE(-128, "Anyone"),
        FRIEND(0, "Any friends"),
        RECRUIT(1, "Recruit+"),
        CORPOREAL(2, "Corporeal+"),
        SERGEANT(3, "Sergeant+"),
        LIEUTENANT(4, "Lieutenant+"),
        CAPTAIN(5, "Captain+"),
        GENERAL(6, "General+"),
        OWNER(7, "Only me"),
        ADMIN(127, "");
    }

}