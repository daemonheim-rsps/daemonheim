package rs.dusk.network.rs.codec.game.encode.message

import rs.dusk.core.network.model.message.Message

/**
 * Public quick chat message
 * @param client The players client index
 * @param effects The colour and move effect combined
 * @param rights The players rights (0 = normal, 1 = player mod, 2 = admin)
 * @param file The quick chat file id
 * @param data Additional data
 */
data class ChatPublicQuickChatMessage(
    val client: Int, 
    val effects: Int,
    val rights: Int, 
    val file: Int,
    val data: ByteArray?
) : Message
{
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ChatPublicQuickChatMessage

        if (client != other.client) return false
        if (effects != other.effects) return false
        if (rights != other.rights) return false
        if (file != other.file) return false
        if (data != null) {
            if (other.data == null) return false
            if (!data.contentEquals(other.data)) return false
        } else if (other.data != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = client
        result = 31 * result + effects
        result = 31 * result + rights
        result = 31 * result + file
        result = 31 * result + (data?.contentHashCode() ?: 0)
        return result
    }
}