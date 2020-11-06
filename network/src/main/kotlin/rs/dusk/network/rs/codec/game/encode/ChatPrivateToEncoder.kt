package rs.dusk.network.rs.codec.game.encode

import rs.dusk.cache.secure.Huffman
import rs.dusk.core.network.codec.packet.access.PacketWriter
import rs.dusk.core.network.model.packet.PacketType
import rs.dusk.network.rs.codec.game.GameMessageEncoder
import rs.dusk.network.rs.codec.game.GameOpcodes.PRIVATE_CHAT_TO
import rs.dusk.network.rs.codec.game.encode.message.ChatPrivateToMessage
import rs.dusk.utility.inject

/**
 * @author Jacob Rhiel <jacob.rhiel@gmail.com>
 * @created Oct 30, 2020 
 */
class ChatPrivateToEncoder : GameMessageEncoder<ChatPrivateToMessage>()
{

    private val huffman: Huffman by inject()

    override fun encode(builder: PacketWriter, msg: ChatPrivateToMessage)
    {

        val (name, text) = msg

        builder.apply {

            writeOpcode(PRIVATE_CHAT_TO, PacketType.SHORT)

            writeString(name)

            huffman.compress(text, this)

        }

    }

}