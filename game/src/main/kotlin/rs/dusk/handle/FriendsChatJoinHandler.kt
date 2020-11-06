package rs.dusk.handle

import io.netty.channel.ChannelHandlerContext
import rs.dusk.core.network.model.session.getSession
import rs.dusk.engine.client.Sessions
import rs.dusk.engine.entity.character.player.chat.message
import rs.dusk.engine.entity.character.player.social.FriendsChatChannels
import rs.dusk.engine.entity.character.player.social.NamesList
import rs.dusk.network.rs.codec.game.GameMessageHandler
import rs.dusk.network.rs.codec.game.decode.message.FriendChatJoinMessage
import rs.dusk.utility.inject

/**
 * @author Jacob Rhiel <jacob.rhiel@gmail.com>
 * @created Oct 29, 2020
 */
class FriendsChatJoinHandler : GameMessageHandler<FriendChatJoinMessage>()
{

    private val sessions: Sessions by inject()

    private val channels: FriendsChatChannels by inject()

    private val namesList: NamesList by inject()

    override fun handle(ctx: ChannelHandlerContext, msg: FriendChatJoinMessage)
    {

        val (name) = msg

        val session = ctx.channel().getSession()
        val player = sessions.get(session) ?: return

        if(name.isBlank()) {
            player.channel?.leave(player, false)
        } else {
            val targetName = namesList.getName(name)
            if(targetName == null) {
                player.message("Could not find player with the username '${name}'.")
                return
            }
            channels.join(player, targetName)
        }

    }

}