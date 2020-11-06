package rs.dusk.network.rs.codec.game.encode

import rs.dusk.core.network.codec.packet.access.PacketWriter
import rs.dusk.core.network.model.packet.PacketType
import rs.dusk.network.rs.codec.game.GameMessageEncoder
import rs.dusk.network.rs.codec.game.GameOpcodes.FRIEND_LIST_APPEND
import rs.dusk.network.rs.codec.game.encode.message.FriendsChatListAppendMessage

class FriendsChatListAppendEncoder : GameMessageEncoder<FriendsChatListAppendMessage>() {

    override fun encode(builder: PacketWriter, message: FriendsChatListAppendMessage) {
        val (worldId, username, displayName, rank) = message
        builder.apply {
            writeOpcode(FRIEND_LIST_APPEND, PacketType.BYTE)
            writeFriendDetails(this, worldId, username, displayName, rank)
        }
    }

    companion object
    {

        fun writeFriendDetails(builder: PacketWriter, worldId: Int, username: String, displayName: String, rank: Int)
        {
            builder.apply {
                writeString(username)
                val different = username != displayName
                writeByte(different)
                if (different) {
                    writeString(username)
                }
                writeShort(worldId)//World
                writeByte(rank)//Rank
                if (rank != -128) {
                    writeString("Dusk $worldId")//Server status
                }
            }
        }

    }

}