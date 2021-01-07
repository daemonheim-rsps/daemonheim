package rs.dusk.engine.entity.character.player.chat

import rs.dusk.engine.entity.character.player.Player
import rs.dusk.engine.entity.character.player.social.Contacts
import rs.dusk.utility.get

fun Player.updatePrivateStatus(status: ChatFilterStatus) = get<Contacts>().setStatus(this, status)