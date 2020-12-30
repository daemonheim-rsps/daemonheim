package rs.dusk.network.codec.game.decode

import io.netty.channel.ChannelHandlerContext
import rs.dusk.buffer.Endian
import rs.dusk.buffer.Modifier
import rs.dusk.network.codec.Decoder
import rs.dusk.network.packet.PacketReader

class InterfaceSwitchComponentsDecoder : Decoder(16) {

    override fun decode(context: ChannelHandlerContext, packet: PacketReader) {
        handler?.interfaceSwitch(
            context,
            packet.readShort(),
            packet.readShort(order = Endian.LITTLE),
            packet.readShort(Modifier.ADD),
            packet.readInt(order = Endian.MIDDLE),
            packet.readShort(order = Endian.LITTLE),
            packet.readInt(Modifier.INVERSE, Endian.MIDDLE)
        )
    }

}