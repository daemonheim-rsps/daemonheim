package rs.dusk.engine.entity.character.player.social

import rs.dusk.engine.entity.character.player.Player

interface FriendChannels {
    /**
     * List of all the friends chat channels; active & inactive
     */
    val channels: List<FriendsChat>

    /**
     * Starts the settings refresh executor
     */
    fun init()

    /**
     * Search for a channel by owner's name
     * @param name The owner's name
     * @return Whether the channel was found
     */
    fun find(name: Name): FriendsChat?

    /**
     * Disables the players channel if active
     * @param player The owner of the channel
     */
    fun disable(player: Player)

    /**
     * Attempts to join channel owned by a user with the name [friendsName]
     * @param player The player attempting to join
     * @param friendsName The name of the friend who's channel to search for
     */
    fun join(player: Player, friendsName: Name)

    /**
     * Creates or renames the players channel regardless of if they are in it or not
     * @param player The player attempting to name
     * @param name The proposed new channel name
     * @return The name (if set)
     */
    fun name(player: Player, name: String): String?

    /**
     * Removes rank of a member
     * @param player The player attempting to join
     * @param member The name of the member to demote
     */
    fun demote(player: Player, member: Name)

    /**
     * Promotes a member to a rank
     * @param player The player attempting to join
     * @param member The name of the member to promote
     * @param rank The rank to give
     */
    fun promote(player: Player, member: Name, rank: Int)

    /**
     * Attempts to rejoin channel
     * @param player The player attempting to rejoin
     */
    fun rejoin(player: Player)

    /**
     * Increments channel join counter
     * @param name The username of the player who attempted
     */
    fun increment(name: Name)

    /**
     * Checks if a player is temporarily blocked from joining channels
     * After attempting too many times
     * @param name The username of the player to check
     * @return Whether that username if blocked from joining
     */
    fun isBlocked(name: Name): Boolean

    /**
     * Requests if a player can open settings interface
     * @param player The player attempting to open the ui
     * @return can open settings interface
     */
    fun canSetup(player: Player): Boolean

    /**
     * Clears all channel join counters
     * Should be executed every few minutes
     */
    fun clearBlocks()

    /**
     * Get's the friends chat that [player] is owner of
     * Note: If admin then returns the current friends chat
     * @param player The player who's channel to get
     * @return The players friend chat if it exists
     */
    fun get(player: Player): FriendsChat?
}