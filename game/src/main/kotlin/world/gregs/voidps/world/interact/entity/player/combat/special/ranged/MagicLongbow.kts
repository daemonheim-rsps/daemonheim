import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.visual.setAnimation
import world.gregs.voidps.engine.entity.character.update.visual.setGraphic
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.*
import world.gregs.voidps.world.interact.entity.player.combat.special.drainSpecialEnergy
import world.gregs.voidps.world.interact.entity.player.combat.special.specialAttack
import world.gregs.voidps.world.interact.entity.proj.shoot

fun isMagicLong(weapon: Item?) = weapon != null && (weapon.name.startsWith("magic_longbow") || weapon.name.startsWith("magic_composite_bow"))

on<HitChanceModifier>({ player -> player != target && type == "range" && player.specialAttack && isMagicLong(weapon) }, Priority.HIGHEST) { _: Player ->
    chance = 1.0
}

on<CombatSwing>({ player -> player.specialAttack && isMagicLong(player.weapon) }, Priority.MEDIUM) { player: Player ->
    val speed = player.weapon.def["attack_speed", 4]
    delay = if (player.attackType == "rapid") speed - 1 else speed
    if (!drainSpecialEnergy(player, 350)) {
        return@on
    }
    player.setAnimation("bow_shoot")
    player.setGraphic("magic_longbow_special", height = 100)
    player.shoot(name = "special_attack_arrow", target = target, delay = 40, height = 43, endHeight = target.height, curve = 8)
    player.hit(target)
}