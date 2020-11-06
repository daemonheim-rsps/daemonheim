package rs.dusk.engine.entity.character.player.social

import rs.dusk.engine.entity.character.player.Player

interface Affiliate {

    /**
     * Adds a friend to a players friend list
     * @param player The player who's adding a friend
     * @param friend The friends name
     */
    fun addFriend(player: Player, friend: Name)

    /**
     * Adds a person to a players ignore list
     * @param player The player who's ignoring someone
     * @param ignore The person to ignores name
     */
    fun addIgnore(player: Player, ignore: Name, temporary: Boolean)

    /**
     * Removes a friend from a players friend list
     * @param player The player who's removing a friend
     * @param friend The friend to remove's name
     */
    fun removeFriend(player: Player, friend: Name)

    /**
     * Removes a person from a players ignore list
     * @param player The player who's removing ignore
     * @param ignore The person to remove from ignores name
     */
    fun removeIgnore(player: Player, ignore: Name)

}