package rs.dusk.handle

import io.netty.channel.ChannelHandlerContext
import rs.dusk.core.network.model.session.getSession
import rs.dusk.engine.client.Sessions
import rs.dusk.engine.client.send
import rs.dusk.engine.entity.character.get
import rs.dusk.engine.entity.character.player.Players
import rs.dusk.engine.entity.character.player.chat.ChatFilterStatus
import rs.dusk.engine.entity.character.player.chat.message
import rs.dusk.engine.entity.character.player.social.ContactsImpl
import rs.dusk.engine.entity.character.player.social.NamesList
import rs.dusk.network.rs.codec.game.GameMessageHandler
import rs.dusk.network.rs.codec.game.decode.message.PrivateMessage
import rs.dusk.network.rs.codec.game.encode.message.ChatPrivateFromMessage
import rs.dusk.network.rs.codec.game.encode.message.ChatPrivateToMessage
import rs.dusk.utility.inject

/**
 * @author Jacob Rhiel <jacob.rhiel@gmail.com>
 * @created Oct 30, 2020
 */
class PrivateMessageMessageHandler : GameMessageHandler<PrivateMessage>()
{

    private val sessions: Sessions by inject()

    private val namesList: NamesList by inject()

    private val contactsImpl: ContactsImpl by inject()

    private val players: Players by inject()

    override fun handle(ctx: ChannelHandlerContext, msg: PrivateMessage)
    {

        val (displayName, message) = msg

        val session = ctx.channel().getSession()

        val player = sessions.get(session) ?: return

        val name = namesList.getName(displayName)

        if(name == null)
        {

            //todo: change if offline before message is relayed,
            //im sure other cases

            player.message("Could not find player with the username '$displayName'.")

            return

        }

        val friend = players.get(name) ?: return

        val text = message.capitalize()

        val friendStatus = player["friend_status", ChatFilterStatus.ON]

        if(friendStatus == ChatFilterStatus.OFF)
            contactsImpl.setStatus(player, ChatFilterStatus.FRIENDS)

        player.send(ChatPrivateToMessage(friend.names.name, text))

        friend.send(ChatPrivateFromMessage(player.names.name, player.details.rights, text))

    }

}