package rs.dusk.network.rs.codec.game.encode

import rs.dusk.core.network.codec.packet.access.PacketWriter
import rs.dusk.core.network.model.packet.PacketType
import rs.dusk.network.rs.codec.game.GameMessageEncoder
import rs.dusk.network.rs.codec.game.GameOpcodes.FRIENDS_QUICK_CHAT_MESSAGE
import rs.dusk.network.rs.codec.game.encode.message.FriendsChatQuickChatMessage

/**
 * @author Jacob Rhiel <jacob.rhiel@gmail.com>
 * @created Nov 02, 2020
 */
class FriendsChatQuickMessageEncoder : GameMessageEncoder<FriendsChatQuickChatMessage>()
{

    override fun encode(builder: PacketWriter, msg: FriendsChatQuickChatMessage)
    {

        builder.apply {
            writeOpcode(FRIENDS_QUICK_CHAT_MESSAGE, PacketType.BYTE)
            writeBytes(msg.data)
        }

    }

}