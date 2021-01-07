package rs.dusk.engine.entity.character.player.social

import rs.dusk.engine.entity.character.get
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.engine.entity.character.player.Players
import rs.dusk.engine.entity.character.player.chat.ChatFilterStatus
import rs.dusk.network.codec.game.encode.message

class SocialAffiliations(private val players: Players) : Affiliate {

    override fun addFriend(player: Player, friend: Name)
    {

        if (!player.relations.isIgnored(friend))
        {

            if(player.relations.friendCount() >= MAX_IGNORES)
            {
                player.message("Your friends list is full. Max of 100 for free users, and $MAX_IGNORES for members.")
                return
            }

            player.relations.addFriend(friend)

            notifyFriends(player)

            val friendAccount = players.get(friend)

            if (friendAccount != null)
            {
                //Send updated ui to player
                player.sendFriend(friendAccount.names, friendAccount)
            }

            //Update friends chat
            //World.channels?.promote(player, friend, 0)

        }
    }

    override fun removeFriend(player: Player, friend: Name) {
        player.relations.removeFriend(friend)
        //if (player.online) {
            notifyFriends(player)
            //Update friends chat
           // World.channels?.demote(player, friend)
       // }
    }

    private fun notifyFriends(player: Player) {
        val status = player["private_status", ChatFilterStatus.ON]
        players.all.filter {
            it != player && it.friends(player) && status != ChatFilterStatus.OFF
        }.forEach {
            it.sendFriend(player.names, players.get(player.names))
        }
    }

    override fun addIgnore(player: Player, ignore: Name, temporary: Boolean) {
        if (!player.relations.isFriend(ignore)) {

            if(player.relations.ignoreCount() >= MAX_FRIENDS) {
                player.message("Your ignore list is full. Max of $MAX_FRIENDS.")
                return
            }

            player.relations.addIgnore(ignore, temporary)

            //if (player.online) {
            //    notifyIgnores(player)

                //Send updated ui to player
            //    player.sendIgnore(ignore)
            //}
        }
    }

    override fun removeIgnore(player: Player, ignore: Name) {
        player.relations.removeIgnore(ignore)
        //if (player.online) {
        //    notifyIgnores(player)
       // }
    }

    private fun notifyIgnores(player: Player) {
        //If not private, notify
        //players.all.filter { it != player && it.friends(player) && player.status != 2 }.forEach {
        //    it.sendFriend(player.names, players.get(player.names))
        //}
    }

    companion object {
        private const val MAX_FRIENDS = 100
        private const val MAX_IGNORES = 200
    }
}