package rs.dusk.engine.entity.character.player.social

interface Name {
    /**
     * The players login name
     */
    val username: String

    /**
     * The players display name
     */
    var name: String

    /**
     * The players previous display name
     */
    var previousName: String
}