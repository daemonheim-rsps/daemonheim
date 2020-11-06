package rs.dusk.engine.entity.character.player.chat

import rs.dusk.engine.client.send
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.engine.entity.character.player.social.ContactsImpl
import rs.dusk.engine.entity.character.player.social.SocialAffiliations
import rs.dusk.network.rs.codec.game.encode.message.ChatMessage
import rs.dusk.utility.inject

fun Player.message(text: String, type: ChatType = ChatType.Game, tile: Int = 0, name: String? = null) {
    send(ChatMessage(type.id, tile, text, name, name?.toLowerCase()?.replace(" ", "_")))
}

fun Player.updatePrivateStatus(status: ChatFilterStatus)
{

    val contacts: ContactsImpl by inject()

    contacts.setStatus(this, status)

}