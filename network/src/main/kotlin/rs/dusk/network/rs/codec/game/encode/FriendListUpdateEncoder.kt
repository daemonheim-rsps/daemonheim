package rs.dusk.network.rs.codec.game.encode

import rs.dusk.core.network.codec.packet.access.PacketWriter
import rs.dusk.core.network.model.packet.PacketType
import rs.dusk.network.rs.codec.game.GameMessageEncoder
import rs.dusk.network.rs.codec.game.GameOpcodes.FRIEND_LIST
import rs.dusk.network.rs.codec.game.encode.message.FriendListUpdateMessage

/**
 * @author Jacob Rhiel <jacob.rhiel@gmail.com>
 * @created Oct 30, 2020
 */
class FriendListUpdateEncoder : GameMessageEncoder<FriendListUpdateMessage>()
{

    override fun encode(builder: PacketWriter, msg: FriendListUpdateMessage)
    {

        val (worldId, displayName, previousName, renamed, friendsChatRank, referred) = msg

        builder.apply {

            writeOpcode(FRIEND_LIST, PacketType.SHORT)

            writeByte(renamed)

            writeString(displayName.capitalize())

            writeString(previousName.capitalize())

            writeShort(worldId)

            writeByte(friendsChatRank)

            writeByte(referred)

            if(worldId > 0)
            {

                writeString("World $worldId")

                writeByte(0)

            }

        }

    }

}