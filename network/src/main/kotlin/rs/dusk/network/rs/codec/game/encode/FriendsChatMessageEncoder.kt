package rs.dusk.network.rs.codec.game.encode

import rs.dusk.core.network.codec.packet.access.PacketWriter
import rs.dusk.core.network.model.packet.PacketType
import rs.dusk.network.rs.codec.game.GameMessageEncoder
import rs.dusk.network.rs.codec.game.GameOpcodes.FRIENDS_CHAT_MESSAGE
import rs.dusk.network.rs.codec.game.encode.message.FriendsChatMessage

/**
 * @author Jacob Rhiel <jacob.rhiel@gmail.com>
 * @created Nov 02, 2020
 */
class FriendsChatMessageEncoder : GameMessageEncoder<FriendsChatMessage>()
{

    override fun encode(builder: PacketWriter, msg: FriendsChatMessage)
    {

        val (data) = msg

        builder.apply {

            writeOpcode(FRIENDS_CHAT_MESSAGE, PacketType.BYTE)

            writeBytes(data)

        }

    }

}