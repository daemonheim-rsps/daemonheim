package rs.dusk.network.rs.codec.game.encode

import rs.dusk.core.network.codec.packet.access.PacketWriter
import rs.dusk.core.network.model.packet.PacketType
import rs.dusk.network.rs.codec.game.GameMessageEncoder
import rs.dusk.network.rs.codec.game.GameOpcodes
import rs.dusk.network.rs.codec.game.encode.message.FriendListEnableMessage

/**
 * @author Jacob Rhiel <jacob.rhiel@gmail.com>
 * @created Oct 30, 2020
 */
class FriendListEnableMessageEncoder : GameMessageEncoder<FriendListEnableMessage>()
{

    override fun encode(builder: PacketWriter, msg: FriendListEnableMessage)
    {

        builder.apply {

            writeOpcode(GameOpcodes.FRIEND_LIST, PacketType.SHORT)

        }

    }

}