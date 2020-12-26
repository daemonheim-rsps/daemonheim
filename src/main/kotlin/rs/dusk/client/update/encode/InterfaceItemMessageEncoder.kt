package rs.dusk.client.update.encode

import rs.dusk.core.network.buffer.Endian
import rs.dusk.core.network.buffer.Modifier
import rs.dusk.core.network.packet.access.PacketWriter
import rs.dusk.network.codec.game.GameMessageEncoder
import rs.dusk.network.codec.game.encode.message.InterfaceItemMessage
import rs.dusk.network.rs.codec.game.GameOpcodes.INTERFACE_ITEM

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since August 2, 2020
 */
class InterfaceItemMessageEncoder : GameMessageEncoder<InterfaceItemMessage>() {

    override fun encode(builder: PacketWriter, msg: InterfaceItemMessage) {
        val (id, component, item, amount) = msg
        builder.apply {
            writeOpcode(INTERFACE_ITEM)
            writeShort(item, order = Endian.LITTLE)
            writeInt(amount)
            writeInt(id shl 16 or component, Modifier.INVERSE, Endian.MIDDLE)
        }
    }
}