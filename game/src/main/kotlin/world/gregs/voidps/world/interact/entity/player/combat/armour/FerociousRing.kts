package world.gregs.voidps.world.interact.entity.player.combat.armour

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.getOrNull
import world.gregs.voidps.engine.entity.item.EquipSlot
import world.gregs.voidps.engine.entity.item.equipped
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.map.area.MapArea
import world.gregs.voidps.world.interact.entity.combat.HitDamageModifier
import kotlin.math.floor

on<HitDamageModifier>(
    { it.getOrNull<MapArea>("map")?.name == "duradals_dungeon" && it.equipped(EquipSlot.Hands).name.startsWith("ferocious_ring") },
    priority = Priority.LOWER
) { _: Player ->
    damage = floor(damage * 1.04)
}