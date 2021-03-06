package world.gregs.voidps.world.interact.entity.player.combat.range.special

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.visual.setAnimation
import world.gregs.voidps.engine.entity.character.update.visual.setGraphic
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.start
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.*
import world.gregs.voidps.world.interact.entity.proj.shoot
import kotlin.math.floor

fun isThrowingAxe(weapon: Item?) = weapon != null && (weapon.name.endsWith("morrigans_throwing_axe"))

on<HitDamageModifier>({ player -> type == "range" && player.specialAttack && isThrowingAxe(weapon) }, Priority.HIGH) { _: Player ->
    damage = floor(damage * 1.2)
}

on<CombatSwing>({ player -> !swung() && player.specialAttack && isThrowingAxe(player.weapon) }, Priority.MEDIUM) { player: Player ->
    val speed = player.weapon.def["attack_speed", 4]
    delay = if (player.attackType == "rapid") speed - 1 else speed
    if (!drainSpecialEnergy(player, MAX_SPECIAL_ATTACK / 2)) {
        delay = -1
        return@on
    }
    val ammo = player.ammo
    player.setAnimation("throw_morrigans_throwing_axe_special")
    player.setGraphic("${ammo}_special", height = 100)
    player.shoot(name = ammo, target = target, delay = 40, height = 15, endHeight = target.height, curve = 8)
    player.hit(target)
}

on<CombatHit>({ isThrowingAxe(weapon) && special }) { character: Character ->
    if (damage <= 0) {
        return@on
    }
    character.start("hamstring", 100)
}