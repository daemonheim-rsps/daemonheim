package world.gregs.voidps.world.interact.entity.player.display

import world.gregs.voidps.engine.action.Suspension
import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.client.ui.event.InterfaceClosed
import world.gregs.voidps.engine.client.ui.event.InterfaceOpened
import world.gregs.voidps.engine.client.ui.event.InterfaceRefreshed
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.client.ui.sendVisibility
import world.gregs.voidps.engine.client.variable.ListVariable
import world.gregs.voidps.engine.client.variable.Variable
import world.gregs.voidps.engine.client.variable.setVar
import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on

ListVariable(168, Variable.Type.VARC, values = Tab.values().toList(), defaultValue = Tab.Inventory).register("tab")

val list = listOf(
    "chat_box",
    "chat_background",
    "filter_buttons",
    "private_chat",
    "health_orb",
    "prayer_orb",
    "energy_orb",
    "summoning_orb",
    "combat_styles",
    "task_system",
    "stats",
    "quest_journals",
    "inventory",
    "worn_equipment",
    "prayer_list",
    "modern_spellbook",
    "friends_list",
    "ignores_list",
    "friends_chat",
    "options",
    "emotes",
    "music_player",
    "notes",
    "area_status_icon"
)

on<Registered> { player: Player ->
    player.open(player.gameFrame.name)
}

fun String.toUnderscoreCase(): String {
    val builder = StringBuilder()
    for (i in 0 until length) {
        val char = this[i]
        if (char.isUpperCase()) {
            if (i != 0) {
                builder.append('_')
            }
            builder.append(char.toLowerCase())
        }
    }
    return builder.toString()
}

Tab.values().forEach { tab ->
    val name = tab.name.toUnderscoreCase()
    on<InterfaceOption>({ name == it.gameFrame.name && component == name && option == name }) { player: Player ->
        player.setVar("tab", tab, refresh = false)
    }
}

on<InterfaceOpened>({ name == it.gameFrame.name }) { player: Player ->
    list.forEach {
        player.open(it)
    }
}

on<InterfaceRefreshed>({ name == it.gameFrame.name }) { player: Player ->
    player.interfaces.sendVisibility(player.gameFrame.name, "wilderness_level", false)
}

on<InterfaceClosed>({ (it.action.suspension as? Suspension.Interface)?.id == name }) { player: Player ->
    player.action.resume()
}