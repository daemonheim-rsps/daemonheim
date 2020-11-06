package rs.dusk.engine.entity.character.player.chat

/**
 * @author Jacob Rhiel <jacob.rhiel@gmail.com>
 * @created Oct 30, 2020
 */
enum class ChatFilterStatus(val value: Int)
{

    /**
     * This status flag means that the selected option is on 'on'
     */
    ON(0),

    /**
     * This status flag means that the selected option is on 'friends'
     */
    FRIENDS(1),

    /**
     * This status flag means that the selected option is on 'off'
     */
    OFF(2),

    /**
     * This status flag means that the selected option is on 'hide'
     */
    HIDE(3),

    /**
     * This flag is only used for the 'game' option, this is the flag that means we should not filter messages
     */
    NO_FILTER(0),

    /**
     * This flag is only used for the 'game' option, this is the flag that means we should filter messages
     */
    FILTER(1)

    ;

    companion object
    {

        fun byValue(value: Int) = values().firstOrNull { it.value == value }

    }

}