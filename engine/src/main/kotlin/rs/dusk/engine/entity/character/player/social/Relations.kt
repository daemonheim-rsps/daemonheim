package rs.dusk.engine.entity.character.player.social

/**
 * Relations
 * Handles friends & ignored lists
 */
interface Relations {
    /**
     * Adds name to friends list
     * Note: If already friend then rank just overrides
     * @param name The name of the friend
     * @param rank The (optional) friends chat rank
     */
    fun addFriend(name: Name, rank: FriendsChat.Ranks = FriendsChat.Ranks.FRIEND)

    /**
     * Checks if name is a friend
     * @param name The users name to check
     * @return If is on friends list
     */
    fun isFriend(name: Name): Boolean

    /**
     * Removes a name from the friends list
     * @param name The users name to remove
     */
    fun removeFriend(name: Name)

    /**
     * Returns a friends rank
     * @param name The name of the friend
     * @return The players rank if they are a friend
     */
    fun getFriend(name: Name): FriendsChat.Ranks?

    /**
     * Returns the complete list of friends names
     * @return The friends list
     */
    fun getFriends(): List<Name>

    /**
     * The number of friends on the list
     * @return total friends count
     */
    fun friendCount(): Int

    /**
     * Adds name to ignore list
     * @param name The name of the player
     */
    fun addIgnore(name: Name, temp: Boolean = false)

    /**
     * Checks if name is ignored
     * @param name The users name to check
     * @return If is on ignores list
     */
    fun isIgnored(name: Name): Boolean

    /**
     * Removes a name from the ignore list
     * @param name The users name to remove
     */
    fun removeIgnore(name: Name)

    /**
     * Returns the complete list of ignored names
     * @return The ignored list
     */
    fun getIgnores(): List<Name>

    /**
     * The number of ignores on the list
     * @return total ignored count
     */
    fun ignoreCount(): Int

    /**
     * Removes any temporary ignores
     */
    fun removeIgnores()
}