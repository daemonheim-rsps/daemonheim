package rs.dusk.network.rs.codec.game.encode

import rs.dusk.core.network.codec.packet.access.PacketWriter
import rs.dusk.core.network.model.packet.PacketType
import rs.dusk.network.rs.codec.game.GameMessageEncoder
import rs.dusk.network.rs.codec.game.GameOpcodes.FRIENDS_CHAT_UPDATE
import rs.dusk.network.rs.codec.game.encode.FriendsChatListAppendEncoder.Companion.writeFriendDetails
import rs.dusk.network.rs.codec.game.encode.message.FriendsChatUpdate

class FriendsChatUpdateEncoder : GameMessageEncoder<FriendsChatUpdate>()
{

    override fun encode(builder: PacketWriter, message: FriendsChatUpdate)
    {

        val (owner, channelName, kickRank, memberCount, members) = message

        builder.apply {
            writeOpcode(FRIENDS_CHAT_UPDATE, PacketType.SHORT)
            writeString(owner)
            writeByte(0)
            writeLong(channelName)
            writeByte(kickRank)//Who can kick on chat
            writeByte(memberCount)//Player count
            members.forEach { friend ->
                writeFriendDetails(this, friend.worldId, friend.username, friend.displayName, friend.rank)
            }
        }
    }

}