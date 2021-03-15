package world.gregs.voidps.network.decode

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.Decoder
import world.gregs.voidps.network.PacketSize.BYTE

class KeysPressedDecoder : Decoder(BYTE) {

    override fun decode(player: Player, packet: Reader) {
        val keys = ArrayList<Pair<Int, Int>>()
        while (packet.readableBytes() > 0) {
            keys.add(packet.readUnsignedByte() to packet.readUnsignedShort())
        }
        handler?.keysPressed(player, keys)
    }

}