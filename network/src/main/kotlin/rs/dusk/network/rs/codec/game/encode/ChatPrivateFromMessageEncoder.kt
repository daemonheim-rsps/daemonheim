package rs.dusk.network.rs.codec.game.encode

import rs.dusk.cache.secure.Huffman
import rs.dusk.core.network.codec.packet.access.PacketWriter
import rs.dusk.core.network.model.packet.PacketType
import rs.dusk.network.rs.codec.game.GameMessageEncoder
import rs.dusk.network.rs.codec.game.GameOpcodes.PRIVATE_CHAT_FROM
import rs.dusk.network.rs.codec.game.encode.message.ChatPrivateFromMessage
import rs.dusk.utility.func.formatUsername
import rs.dusk.utility.inject
import kotlin.random.Random.Default.nextInt

/**
 * @author Jacob Rhiel <jacob.rhiel@gmail.com>
 * @created Oct 30, 2020 
 */
class ChatPrivateFromMessageEncoder : GameMessageEncoder<ChatPrivateFromMessage>()
{

    private val huffman: Huffman by inject()

    override fun encode(builder: PacketWriter, msg: ChatPrivateFromMessage)
    {

        val (username, rights, text) = msg

        val formatted = username.formatUsername()

        val different = username != formatted

        builder.apply {

            writeOpcode(PRIVATE_CHAT_FROM, PacketType.SHORT)

            writeByte(different)

            writeString(username)

            if(different)
                writeString(formatted)

            //A very small chance (1 in a few billion) of duplicate and a message not being sent correctly
            //If the value has been sent before then subsequent messages with the same value is ignored
            //Not entirely sure of it's use
            writeShort(nextInt())

            writeMedium(nextInt())

            writeByte(rights)

            huffman.compress(text, this)

        }

    }

}