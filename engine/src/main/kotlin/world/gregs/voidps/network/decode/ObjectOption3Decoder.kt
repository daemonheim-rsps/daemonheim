package world.gregs.voidps.network.decode

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.Decoder

class ObjectOption3Decoder : Decoder(7) {

    override fun decode(player: Player, packet: Reader) {
        handler?.objectOption(
            player = player,
            y = packet.readShortAdd(),
            objectId = packet.readShortAddLittle(),
            x = packet.readShortLittle(),
            run = packet.readBooleanAdd(),
            option = 3
        )
    }

}