package world.gregs.voidps.network.decode

import io.ktor.utils.io.core.*
import kotlinx.coroutines.flow.MutableSharedFlow
import world.gregs.voidps.engine.client.ui.Interface
import world.gregs.voidps.network.Decoder
import world.gregs.voidps.network.Instruction
import world.gregs.voidps.network.instruct.InteractInterfaceItem
import world.gregs.voidps.network.readIntInverseMiddle
import world.gregs.voidps.network.readShortAdd

class InterfaceOnInterfaceDecoder : Decoder(16) {

    override suspend fun decode(instructions: MutableSharedFlow<Instruction>, packet: ByteReadPacket) {
        val fromHash = packet.readInt()
        val toHash = packet.readIntInverseMiddle()
        val fromItem = packet.readShortAdd()
        val fromSlot = packet.readShort().toInt()
        val toItem = packet.readShortAdd()
        val toSlot = packet.readShort().toInt()
        val fromInterfaceParent = Interface.getId(fromHash)
        val fromInterfaceComponent = Interface.getComponentId(fromHash)
        val toInterfaceParent = Interface.getId(toHash)
        val toInterfaceComponent = Interface.getComponentId(toHash)
        instructions.emit(
            InteractInterfaceItem(
                fromSlot,
                toSlot,
                fromItem,
                toItem,
                fromInterfaceParent,
                fromInterfaceComponent,
                toInterfaceParent,
                toInterfaceComponent
            )
        )
    }

}