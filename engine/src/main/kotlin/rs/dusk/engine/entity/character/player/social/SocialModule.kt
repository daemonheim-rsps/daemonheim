package rs.dusk.engine.entity.character.player.social

import org.koin.dsl.module
import rs.dusk.engine.entity.character.player.Player

/**
 * @author Jacob Rhiel <jacob.rhiel@gmail.com>
 * @created Oct 29, 2020
 */
val socialModule = module {

    single { NamesList() }

    single { SocialTransmission(get()) }

    single { SocialPresence(get()) }

    single { SocialStatus(get()) }

    single { SocialAffiliations(get()) }

    single { FriendsChatChannels(get()) }

    single { ContactsImpl(get(), get()) }

    single { RelationshipManager() }

}

fun Player.hasFriend(player: Player) : Boolean
{

    return true

}