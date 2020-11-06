package rs.dusk.engine.entity.character.player.social

/**
 * NameList
 * List of character names
 */
interface NameList {
    /**
     * Adds a collection of names to the list
     */
    fun addName(name: Name)

    /**
     * Returns a name collection based on the players display name
     */
    fun getName(name: String): Name?

    /**
     * Returns a name collection based on the players username
     */
    fun getUserName(username: String): Name?
}