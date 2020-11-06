package rs.dusk.handle

import io.netty.channel.ChannelHandlerContext
import rs.dusk.core.network.model.session.getSession
import rs.dusk.engine.client.Sessions
import rs.dusk.engine.client.send
import rs.dusk.engine.client.update.task.viewport.ViewportUpdating
import rs.dusk.engine.entity.character.player.Players
import rs.dusk.network.rs.codec.game.GameMessageHandler
import rs.dusk.network.rs.codec.game.decode.message.PublicQuickChatMessage
import rs.dusk.network.rs.codec.game.encode.message.ChatPublicQuickChatMessage
import rs.dusk.utility.inject

/**
 * @author Jacob Rhiel <jacob.rhiel@gmail.com>
 * @created Oct 31, 2020 
 */
class PublicQuickChatMessageHandler : GameMessageHandler<PublicQuickChatMessage>()
{

    private val sessions: Sessions by inject()

    private val players: Players by inject()

    override fun handle(ctx: ChannelHandlerContext, msg: PublicQuickChatMessage)
    {

        var (script, file, data) = msg

        val session = ctx.channel().getSession()

        val player = sessions.get(session) ?: return

        when(script) {
            0 -> {//Public

                players.all
                    .filter {
                        it.tile.within(player.tile, ViewportUpdating.VIEW_CHAT_RADIUS)
                    }
                    .forEach { it.send(ChatPublicQuickChatMessage(player.index, 0x8000, player.details.rights, file, data)) }

            }
            1 -> {//Friends chat
                player.channel?.quickMessage(player, file, data)
            }
        }

    }

}