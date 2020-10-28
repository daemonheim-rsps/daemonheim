package rs.dusk.world.activity.skill

import rs.dusk.engine.client.ui.event.InterfaceOpened
import rs.dusk.engine.client.ui.open
import rs.dusk.engine.client.variable.*
import rs.dusk.engine.entity.character.get
import rs.dusk.engine.entity.character.player.skill.Skill.*
import rs.dusk.engine.entity.character.set
import rs.dusk.engine.event.then
import rs.dusk.engine.event.where
import rs.dusk.world.interact.entity.player.display.InterfaceOption

val menu = listOf(Attack, Strength, Range, Magic, Defence, Constitution, Prayer, Agility, Herblore, Thieving, Crafting, Runecrafting,
    Mining, Smithing, Fishing, Cooking, Firemaking, Woodcutting, Fletching, Slayer, Farming, Construction, Hunter, Summoning, Dungeoneering)

BitwiseVariable(1179, Variable.Type.VARP, true, values = listOf(
    Attack, Strength, Defence, Range, Prayer, Magic, Constitution, Agility, Herblore, Thieving, Crafting, Fletching, Mining,
    Smithing, Fishing, Cooking, Firemaking, Woodcutting, Runecrafting, Slayer, Farming, Construction, Hunter, Summoning, Dungeoneering
)).register("skill_stat_flash")
IntVariable(1230, Variable.Type.VARP).register("level_up_details")
IntVariable(965, Variable.Type.VARP).register("skill_guide")

InterfaceOpened where { name == "stats" } then {
    player.sendVar("skill_stat_flash")
    values().forEach {
        player.experience.update(it)
    }
}

InterfaceOption where { name == "stats" && option == "View" } then {
    val skill = valueOf(component.capitalize())
    val menuIndex = menu.indexOf(skill) + 1

    if(player.hasVar("skill_stat_flash", skill)) {
        val extra = 0//0 - normal, 2 - combat milestone, 4 - total milestone
        player.setVar("level_up_details", menuIndex * 8 + extra)
        player.open("skill_level_details")
        player.removeVar("skill_stat_flash", skill)
    } else {
        player.setVar("skill_guide", menuIndex)
        player["active_skill_guide"] = menuIndex
        player.open("skill_guide")
    }
}

InterfaceOption where { name == "skill_guide" && option == "Open subsection" } then {
    val index = componentId - 10
    val menuIndex = player["active_skill_guide", 1]
    player.setVar("skill_guide", menuIndex + index * 1024)
}