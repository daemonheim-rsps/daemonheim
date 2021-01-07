package rs.dusk.engine.entity.character.player.social

import rs.dusk.engine.entity.character.get
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.engine.entity.character.player.Players
import rs.dusk.engine.entity.character.player.chat.ChatFilterStatus
import rs.dusk.engine.entity.character.set

class SocialStatus(private val players: Players) : OnlineStatus
{

    override fun setStatus(player: Player, status: ChatFilterStatus)
    {

        //todo: if lobby is added add support

//        player.send(FriendStatusMessage(status.value)) FIXME

        val old = player["previous_private_status", ChatFilterStatus.ON]

        if(old != status) {
            player["private_status"] = status
            players.all
                .filter {
                    it != player && it.friends(player) && if (old == ChatFilterStatus.FRIENDS && status == ChatFilterStatus.ON || old == ChatFilterStatus.ON && status == ChatFilterStatus.FRIENDS) !player.friends(
                        it
                    ) else if (old == ChatFilterStatus.OFF && status == ChatFilterStatus.FRIENDS) player.friends(it) else true
                }
                .forEach {
                    it.sendFriend(player.names, players.get(player.names))
                }
        }
    }

}