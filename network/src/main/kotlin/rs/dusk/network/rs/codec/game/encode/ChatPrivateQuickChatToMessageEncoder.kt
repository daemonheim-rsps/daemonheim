package rs.dusk.network.rs.codec.game.encode

import rs.dusk.core.network.codec.packet.access.PacketWriter
import rs.dusk.core.network.model.packet.PacketType
import rs.dusk.network.rs.codec.game.GameMessageEncoder
import rs.dusk.network.rs.codec.game.GameOpcodes.PRIVATE_QUICK_CHAT_TO
import rs.dusk.network.rs.codec.game.encode.message.ChatPrivateQuickChatToMessage

/**
 * @author Jacob Rhiel <jacob.rhiel@gmail.com>
 * @created Oct 31, 2020
 */
class ChatPrivateQuickChatToMessageEncoder : GameMessageEncoder<ChatPrivateQuickChatToMessage>()
{

    override fun encode(builder: PacketWriter, msg: ChatPrivateQuickChatToMessage)
    {

        val (name, file, data) = msg

        builder.apply {

            writeOpcode(PRIVATE_QUICK_CHAT_TO, PacketType.BYTE)

            writeString(name)

            writeShort(file)

            if(data != null)
                writeBytes(data)

        }

    }

}