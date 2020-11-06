package rs.dusk.handle

import io.netty.channel.ChannelHandlerContext
import rs.dusk.core.network.model.session.getSession
import rs.dusk.engine.client.Sessions
import rs.dusk.engine.entity.character.player.chat.message
import rs.dusk.engine.entity.character.player.social.ContactsImpl
import rs.dusk.engine.entity.character.player.social.NamesList
import rs.dusk.network.rs.codec.game.GameMessageHandler
import rs.dusk.network.rs.codec.game.decode.message.IgnoreListRemoveMessage
import rs.dusk.utility.inject

/**
 * @author Jacob Rhiel <jacob.rhiel@gmail.com>
 * @created Oct 30, 2020 
 */
class IgnoreListRemoveMessageHandler : GameMessageHandler<IgnoreListRemoveMessage>()
{

    private val sessions: Sessions by inject()

    private val namesList: NamesList by inject()

    private val contactsImpl: ContactsImpl by inject()

    override fun handle(ctx: ChannelHandlerContext, msg: IgnoreListRemoveMessage)
    {

        val session = ctx.channel().getSession()

        val player = sessions.get(session) ?: return

        val name = namesList.getName(msg.name)

        if (name == null) {

            player.message("Could not find player with the username '${msg.name}'.")

            return

        }

        contactsImpl.removeIgnore(player, name)

    }

}