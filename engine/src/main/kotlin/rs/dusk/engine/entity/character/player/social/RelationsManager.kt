package rs.dusk.engine.entity.character.player.social

import rs.dusk.engine.entity.character.player.Player

interface RelationsManager {
    val relations: Map<Name, Relations>

    fun create(name: Name): Relations

    fun get(name: Name): Relations?

    fun get(player: Player): Relations? {
        return get(player.names)
    }
}