package world.gregs.voidps.network.codec.game.decode

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.codec.Decoder
import world.gregs.voidps.network.packet.PacketSize.BYTE

class ClanForumThreadDecoder : Decoder(BYTE) {

    override fun decode(player: Player, packet: Reader) {
        handler?.requestClanForumThread(
            player = player,
            packet.readString()
        )
    }

}