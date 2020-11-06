package rs.dusk.handle

import io.netty.channel.ChannelHandlerContext
import rs.dusk.core.network.model.session.getSession
import rs.dusk.engine.client.Sessions
import rs.dusk.engine.entity.character.get
import rs.dusk.engine.entity.character.player.chat.ChatFilterStatus
import rs.dusk.engine.entity.character.player.social.ContactsImpl
import rs.dusk.engine.entity.character.set
import rs.dusk.network.rs.codec.game.GameMessageHandler
import rs.dusk.network.rs.codec.game.decode.message.ChatFilterUpdateMessage
import rs.dusk.utility.inject

/**
 * @author Jacob Rhiel <jacob.rhiel@gmail.com>
 * @created Oct 30, 2020
 */
class ChatFilterMessageHandler : GameMessageHandler<ChatFilterUpdateMessage>()
{

    private val sessions: Sessions by inject()

    private val contacts: ContactsImpl by inject()

    override fun handle(ctx: ChannelHandlerContext, msg: ChatFilterUpdateMessage)
    {

        val session = ctx.channel().getSession()

        val player = sessions.get(session) ?: return

        var (publicFlag, privateFlag, friendsFlag) = msg

        if(publicFlag !in 0..3)
            publicFlag = 0

        if(privateFlag !in 0..3)
            privateFlag = 0

        if(friendsFlag !in 0..3)
            friendsFlag = 0

        val publicStatus = ChatFilterStatus.byValue(publicFlag)

        val privateStatus = ChatFilterStatus.byValue(privateFlag)

        val friendsStatus = ChatFilterStatus.byValue(friendsFlag)

        player["previous_private_status"] = player["private_status", ChatFilterStatus.ON]

        player["public_status"] = publicStatus ?: ChatFilterStatus.ON

        player["private_status"] = privateStatus ?: ChatFilterStatus.ON

        player["friends_status"] = friendsStatus ?: ChatFilterStatus.ON

        contacts.setStatus(player, player["private_status", ChatFilterStatus.ON])

    }

}