package rs.dusk.engine.entity.character.player.social

import rs.dusk.engine.entity.character.player.Player
import rs.dusk.engine.entity.character.player.Players


class SocialPresence(private val players: Players)
{

    /**
     * Sends a connecting player their friends and ignores
     * @param player The player connecting
     */
  /*  override fun connect(player: Player) {
        //Unlock list if empty (if not empty it get's unlocked anyway)
        if (player.relations.friendCount() == 0) {
            player.send(FriendListUnlock())
        } else {
            //Send friends
            player.sendFriends(players)
        }
        *//*
        TODO - Unlocks aren't needed (they only work because when client reads size it defaults to 0 if out of buffer bounds)
        Shouldn't sendFriends send all in one go rather than one at a time?
         *//*
        if(player.relations.ignoreCount() == 0) {
            player.send(IgnoreListUnlock())
        } else {
            //Send ignores
            player.sendIgnores()
        }
    }

    override fun disconnect(player: Player) {
    }*/
}