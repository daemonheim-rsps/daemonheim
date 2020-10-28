package rs.dusk.network.rs.codec.game.encode

import rs.dusk.cache.secure.Huffman
import rs.dusk.core.network.codec.packet.access.PacketWriter
import rs.dusk.core.network.model.packet.PacketType
import rs.dusk.network.rs.codec.game.GameMessageEncoder
import rs.dusk.network.rs.codec.game.GameOpcodes.PUBLIC_CHAT
import rs.dusk.network.rs.codec.game.encode.message.PublicChatMessage
import rs.dusk.utility.inject

/**
 * @author Jacob Rhiel <jacob.rhiel@gmail.com>
 * @created Oct 27, 2020
 */
class PublicChatMessageEncoder : GameMessageEncoder<PublicChatMessage>()
{

    private val huffman: Huffman by inject()

    override fun encode(builder: PacketWriter, msg: PublicChatMessage)
    {

        val (index, effect, rights, message) = msg

        builder.apply {

            writeOpcode(PUBLIC_CHAT, PacketType.BYTE)

            writeShort(index)

            writeShort(effect)

            writeByte(rights)

            huffman.compress(message, this)

        }

    }

}