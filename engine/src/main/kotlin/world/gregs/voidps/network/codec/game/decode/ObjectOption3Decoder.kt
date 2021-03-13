package world.gregs.voidps.network.codec.game.decode

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.network.ClientSession
import world.gregs.voidps.network.codec.Decoder

class ObjectOption3Decoder : Decoder(7) {

    override fun decode(session: ClientSession, packet: Reader) {
        handler?.objectOption(
            session = session,
            y = packet.readShortAdd(),
            objectId = packet.readShortAddLittle(),
            x = packet.readShortLittle(),
            run = packet.readBooleanAdd(),
            option = 3
        )
    }

}