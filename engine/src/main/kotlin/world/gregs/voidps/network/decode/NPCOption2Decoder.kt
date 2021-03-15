package world.gregs.voidps.network.decode

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.Decoder

class NPCOption2Decoder : Decoder(3) {

    override fun decode(player: Player, packet: Reader) {
        handler?.npcOption(
            player = player,
            npcIndex = packet.readShortAddLittle(),
            run = packet.readBooleanAdd(),
            option = 2
        )
    }

}