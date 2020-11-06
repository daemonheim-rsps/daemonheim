package rs.dusk.network.rs.codec.game.encode

import rs.dusk.core.network.codec.packet.access.PacketWriter
import rs.dusk.network.rs.codec.game.GameMessageEncoder
import rs.dusk.network.rs.codec.game.GameOpcodes.PRIVATE_STATUS
import rs.dusk.network.rs.codec.game.encode.message.FriendStatusMessage

/**
 * @author Jacob Rhiel <jacob.rhiel@gmail.com>
 * @created Oct 30, 2020
 */
class FriendStatusMessageEncoder : GameMessageEncoder<FriendStatusMessage>()
{

    override fun encode(builder: PacketWriter, msg: FriendStatusMessage)
    {

        builder.apply {

            writeOpcode(PRIVATE_STATUS)

            writeByte(msg.status)

        }

    }

}