package rs.dusk.handle

import io.netty.channel.ChannelHandlerContext
import rs.dusk.core.network.model.session.getSession
import rs.dusk.engine.client.Sessions
import rs.dusk.engine.client.send
import rs.dusk.network.rs.codec.game.GameMessageHandler
import rs.dusk.network.rs.codec.game.decode.message.PublicMessage
import rs.dusk.network.rs.codec.game.encode.message.PublicChatMessage
import rs.dusk.utility.inject

/**
 * @author Jacob Rhiel <jacob.rhiel@gmail.com>
 * @created Oct 27, 2020
 */
class PublicChatMessageHandler : GameMessageHandler<PublicMessage>()
{

    private val sessions: Sessions by inject()

    override fun handle(ctx: ChannelHandlerContext, msg: PublicMessage)
    {

        val session = ctx.channel().getSession()

        val player = sessions.get(session) ?: return

        val (message, effects) = msg

        player.send(PublicChatMessage(player.index, effects, 2, message.capitalize())) //rights are temporary..

    }

}