package rs.dusk.handle

import io.netty.channel.ChannelHandlerContext
import rs.dusk.core.network.model.session.getSession
import rs.dusk.engine.client.Sessions
import rs.dusk.engine.entity.character.set
import rs.dusk.network.rs.codec.game.GameMessageHandler
import rs.dusk.network.rs.codec.game.decode.message.ChatTypeMessage
import rs.dusk.utility.inject

/**
 * @author Jacob Rhiel <jacob.rhiel@gmail.com>
 * @created Oct 27, 2020
 */
class ChatTypeMessageHandler : GameMessageHandler<ChatTypeMessage>()
{

    private val sessions: Sessions by inject()

    override fun handle(ctx: ChannelHandlerContext, msg: ChatTypeMessage)
    {

        val session = ctx.channel().getSession()

        val player = sessions.get(session) ?: return

        player["chat_message_type"] = msg.type

    }

}