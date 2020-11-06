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
import rs.dusk.engine.entity.character.player.social.Names
import rs.dusk.engine.entity.character.player.social.NamesList
import rs.dusk.network.rs.codec.game.GameMessageHandler
import rs.dusk.network.rs.codec.game.decode.message.PrivateQuickChatMessage
import rs.dusk.network.rs.codec.game.encode.message.ChatPrivateQuickChatFromMessage
import rs.dusk.network.rs.codec.game.encode.message.ChatPrivateQuickChatToMessage
import rs.dusk.network.rs.codec.game.encode.message.ChatPrivateToMessage
import rs.dusk.utility.inject

/**
 * @author Jacob Rhiel <jacob.rhiel@gmail.com>
 * @created Oct 31, 2020
 */
class PrivateQuickChatMessageHandler : GameMessageHandler<PrivateQuickChatMessage>()
{

    private val names: NamesList by inject()

    private val sessions: Sessions by inject()

    private val players: Players by inject()

    private val contacts: ContactsImpl by inject()

    override fun handle(ctx: ChannelHandlerContext, msg: PrivateQuickChatMessage)
    {

        val (username, file, data) = msg

        val name = names.getName(username)

        val session = ctx.channel().getSession()

        val player = sessions.get(session) ?: return

        if(name == null)
            return player.message("Could not find player with the username '$username`.")

        val friend = players.get(name) ?: return

        if(player["private_status", ChatFilterStatus.ON] == ChatFilterStatus.OFF)
            contacts.setStatus(player, ChatFilterStatus.ON)

        player.send(ChatPrivateQuickChatToMessage(name.name, file, data))

        friend.send(ChatPrivateQuickChatFromMessage(player.names.name, player.details.rights, file, data))


    }

}