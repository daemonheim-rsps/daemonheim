package rs.dusk.engine.entity.character.player.social

data class Names(override val username: String) : Name {
    override var name = username
    override var previousName = ""
}