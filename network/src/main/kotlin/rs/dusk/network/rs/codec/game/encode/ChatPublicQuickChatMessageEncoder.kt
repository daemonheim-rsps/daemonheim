package rs.dusk.network.rs.codec.game.encode

import rs.dusk.core.network.codec.packet.access.PacketWriter
import rs.dusk.core.network.model.packet.PacketType
import rs.dusk.network.rs.codec.game.GameMessageEncoder
import rs.dusk.network.rs.codec.game.GameOpcodes.PUBLIC_CHAT
import rs.dusk.network.rs.codec.game.encode.message.ChatPublicQuickChatMessage

/**
 * @author Jacob Rhiel <jacob.rhiel@gmail.com>
 * @created Oct 31, 2020
 */
class ChatPublicQuickChatMessageEncoder : GameMessageEncoder<ChatPublicQuickChatMessage>()
{
    
    override fun encode(builder: PacketWriter, msg: ChatPublicQuickChatMessage)
    {

        val (client, effects, rights, file, data) = msg

        builder.apply { 
            
            writeOpcode(PUBLIC_CHAT, PacketType.BYTE)
            
            writeShort(client)
            
            writeShort(effects)
            
            writeByte(rights)
            
            writeShort(file)
            
            if(data != null)
                writeBytes(data)
            
        }

    }
    
}