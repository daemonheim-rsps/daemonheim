package world.gregs.voidps.network.decode

import io.ktor.utils.io.core.*
import kotlinx.coroutines.flow.MutableSharedFlow
import world.gregs.voidps.network.Decoder
import world.gregs.voidps.network.Instruction
import world.gregs.voidps.network.instruct.InteractNPC
import world.gregs.voidps.network.readBooleanAdd

class NPCOption4Decoder : Decoder(3) {

    override suspend fun decode(instructions: MutableSharedFlow<Instruction>, packet: ByteReadPacket) {
        val npcIndex = packet.readShortLittleEndian().toInt()
        val run = packet.readBooleanAdd()
        instructions.emit(InteractNPC(npcIndex, 4))
    }

}