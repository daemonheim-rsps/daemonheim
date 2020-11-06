package rs.dusk.engine.entity.character.player.social

import rs.dusk.engine.entity.character.player.Player
import rs.dusk.engine.entity.character.player.chat.ChatFilterStatus

interface OnlineStatus
{

    /**
     * Changes a players online status
     * @param player The player who's removing changing status
     * @param status The new status value (0 = on, 1 = friends, 2 = private)
     */
    fun setStatus(player: Player, status: ChatFilterStatus)

}