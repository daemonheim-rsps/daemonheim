package rs.dusk.world.command.debug

import rs.dusk.engine.entity.character.player.social.FriendsChatChannels
import rs.dusk.engine.entity.character.player.social.Names
import rs.dusk.engine.event.then
import rs.dusk.engine.event.where
import rs.dusk.utility.get
import rs.dusk.world.command.Command


/**
 * @author Jacob Rhiel <jacob.rhiel@gmail.com>
 * @created Oct 28, 2020
 */
Command where { prefix == "displayFriendList" } then {

    val friendsChatChannels: FriendsChatChannels = get()

    friendsChatChannels.join(player, Names("Chk"))

}