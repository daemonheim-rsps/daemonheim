package world.gregs.voidps.world.interact.entity.player.combat.weapon

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.get
import world.gregs.voidps.engine.entity.item.EquipSlot
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.item.equipped
import world.gregs.voidps.engine.entity.set
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.network.encode.message
import world.gregs.voidps.world.interact.entity.combat.CombatSwing
import world.gregs.voidps.world.interact.entity.combat.ammo
import world.gregs.voidps.world.interact.entity.combat.removeAmmo
import world.gregs.voidps.world.interact.entity.combat.weapon

fun isBowOrCrossbow(item: Item) = item.name.endsWith("bow") || item.name == "seercull" || item.name == "hand_cannon"

fun ammoRequired(item: Item) = !item.name.startsWith("crystal_bow") && item.name != "zaryte_bow"

on<CombatSwing>({ player -> isBowOrCrossbow(player.weapon) && ammoRequired(player.weapon) }, Priority.HIGHEST) { player: Player ->
    player["required_ammo"] = player.weapon.def["ammo_required", 1]
}

on<CombatSwing>({ player -> isBowOrCrossbow(player.weapon) && ammoRequired(player.weapon) }, Priority.HIGH) { player: Player ->
    val required = player["required_ammo", 1]
    val ammo = player.equipped(EquipSlot.Ammo)
    player.ammo = ""
    if (ammo.amount < required) {
        player.message("There is no ammo left in your quiver.")
        delay = -1
        return@on
    }

    val weapon = player.weapon
    if (weapon.def.ammo?.contains(ammo.name) != true) {
        player.message("You can't use that ammo with your bow.")
        delay = -1
        return@on
    }

    removeAmmo(player, target, ammo.name, required)

    // Ammo is kept track of as EquipSlot.Ammo could've been used up
    player.ammo = when {
        ammo.name.endsWith("fire_arrows_lit") -> "fire_arrows_lit"
        ammo.name.endsWith("fire_arrows_unlit") -> "fire_arrows_unlit"
        else -> ammo.name
    }
}

on<CombatSwing>({ player -> !ammoRequired(player.weapon) }, Priority.HIGH) { player: Player ->
    player.ammo = when {
        player.weapon.name == "zaryte_bow" -> "zaryte_arrow"
        player.weapon.name.startsWith("crystal_bow") -> "special_arrow"
        else -> return@on
    }
}