package rs.dusk.network.rs.codec.game.encode

import rs.dusk.core.network.codec.packet.access.PacketWriter
import rs.dusk.core.network.model.packet.PacketType
import rs.dusk.network.rs.codec.game.GameMessageEncoder
import rs.dusk.network.rs.codec.game.GameOpcodes.PRIVATE_QUICK_CHAT_FROM
import rs.dusk.network.rs.codec.game.encode.message.ChatPrivateQuickChatFromMessage
import rs.dusk.utility.func.formatUsername
import kotlin.random.Random

/**
 * @author Jacob Rhiel <jacob.rhiel@gmail.com>
 * @created Oct 31, 2020 
 */
class ChatPrivateQuickChatFromMessageEncoder : GameMessageEncoder<ChatPrivateQuickChatFromMessage>()
{

    override fun encode(builder: PacketWriter, msg: ChatPrivateQuickChatFromMessage)
    {

        val (username, rights, file, data) = msg

        val formatted = username.formatUsername()

        val different = username != formatted

        builder.apply {

            writeOpcode(PRIVATE_QUICK_CHAT_FROM, PacketType.BYTE)

            writeByte(different)

            writeString(username)

            if(different)
                writeString(formatted)

            //A very small chance (1 in a few billion) of duplicate and a message not being sent correctly
            //If the value has been sent before then subsequent messages with the same value is ignored
            //Not entirely sure of it's use
            writeShort(Random.nextInt())

            writeMedium(Random.nextInt())

            writeByte(rights)

            writeShort(file)

            if(data != null)
                writeBytes(data)

        }

    }

}