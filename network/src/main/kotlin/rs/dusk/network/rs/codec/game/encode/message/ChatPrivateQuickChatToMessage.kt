package rs.dusk.network.rs.codec.game.encode.message

import rs.dusk.core.network.model.message.Message

/**
 * @author Jacob Rhiel <jacob.rhiel@gmail.com>
 * @created Oct 31, 2020
 */
data class ChatPrivateQuickChatToMessage(
    val name: String,
    val file: Int,
    val data: ByteArray?
) : Message
{

    override fun equals(other: Any?): Boolean
    {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ChatPrivateQuickChatToMessage

        if (name != other.name) return false
        if (file != other.file) return false
        if (data != null) {
            if (other.data == null) return false
            if (!data.contentEquals(other.data)) return false
        } else if (other.data != null) return false

        return true
    }

    override fun hashCode(): Int
    {
        var result = name.hashCode()
        result = 31 * result + file
        result = 31 * result + (data?.contentHashCode() ?: 0)
        return result
    }

}