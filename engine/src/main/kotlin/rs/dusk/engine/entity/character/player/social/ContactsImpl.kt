package rs.dusk.engine.entity.character.player.social

import rs.dusk.engine.entity.character.player.Player
import rs.dusk.engine.entity.character.player.chat.ChatFilterStatus

/**
 * Manages friends and online status.
 */
class ContactsImpl(private val affiliate: SocialAffiliations, private val status: SocialStatus) : Contacts {

    override fun addFriend(player: Player, friend: Name) {
        affiliate.addFriend(player, friend)
    }

    override fun addIgnore(player: Player, ignore: Name, temporary: Boolean) {
        affiliate.addIgnore(player, ignore, temporary)
    }

    override fun removeFriend(player: Player, friend: Name) {
        affiliate.removeFriend(player, friend)
    }

    override fun removeIgnore(player: Player, ignore: Name) {
        affiliate.removeIgnore(player, ignore)
    }

    override fun setStatus(player: Player, status: ChatFilterStatus) {
        this.status.setStatus(player, status)
    }

    /*override fun disconnect(player: Player) {
        player.relations.removeIgnores()
    }*/
}