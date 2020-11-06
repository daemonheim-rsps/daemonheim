package rs.dusk.world.community.social.friend

import rs.dusk.engine.client.send
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.engine.entity.character.player.PlayerRegistered
import rs.dusk.engine.entity.character.player.chat.ChatFilterStatus
import rs.dusk.engine.entity.character.player.social.ContactsImpl
import rs.dusk.engine.event.then
import rs.dusk.network.rs.codec.game.encode.message.FriendListEnableMessage
import rs.dusk.utility.inject

/**
 * @author Jacob Rhiel <jacob.rhiel@gmail.com>
 * @created Oct 30, 2020
 */

PlayerRegistered then {

    val contacts: ContactsImpl by inject()

    contacts.setStatus(player, ChatFilterStatus.ON)//todo sql loading of status?

    player.send(FriendListEnableMessage())

}

fun Player.hasFriend(player: Player)
{



}