package rs.dusk.handle

import io.netty.channel.ChannelHandlerContext
import rs.dusk.core.network.model.session.getSession
import rs.dusk.engine.client.Sessions
import rs.dusk.engine.client.send
import rs.dusk.engine.client.update.task.viewport.ViewportUpdating
import rs.dusk.engine.entity.character.get
import rs.dusk.engine.entity.character.player.Players
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

        val players: Players by inject()

        val type = player["chat_message_type", 0]

        when(type)
        {

            0 -> {
                players.all
                    .filter {
                        it.tile.within(player.tile, ViewportUpdating.VIEW_CHAT_RADIUS)
                    }
                    .forEach { it.send(PublicChatMessage(player.index, effects, player.details.rights, message.capitalize())) }
            }

            1 -> player.channel?.message(player, message)

        }

    }

}