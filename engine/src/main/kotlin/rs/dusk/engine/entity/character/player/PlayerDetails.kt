package rs.dusk.engine.entity.character.player

import rs.dusk.engine.entity.character.player.social.Name

/**
 * @author Jacob Rhiel <jacob.rhiel@gmail.com>
 * @created Oct 29, 2020
 */
data class PlayerDetails(
    val name: Name,
    var rights: Int = 0
)