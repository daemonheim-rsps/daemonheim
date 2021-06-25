package world.gregs.voidps.network.decode

import io.ktor.utils.io.core.*
import kotlinx.coroutines.flow.MutableSharedFlow
import world.gregs.voidps.engine.client.ui.Interface
import world.gregs.voidps.network.*
import world.gregs.voidps.network.instruct.InteractInterfaceObject

class InterfaceOnObjectDecoder : Decoder(15) {

    override suspend fun decode(instructions: MutableSharedFlow<Instruction>, packet: ByteReadPacket) {
        val type = packet.readShort().toInt()
        val x = packet.readShortAddLittle()
        val packed = packet.readIntLittleEndian()
        val y = packet.readUnsignedShortAdd()
        val run = packet.readBooleanSubtract()
        val slot = packet.readShortLittleEndian().toInt()
        val id = packet.readUnsignedShortLittle()
        instructions.emit(InteractInterfaceObject(id, x, y, Interface.getId(packed), Interface.getComponentId(packed), type, slot))
    }

}