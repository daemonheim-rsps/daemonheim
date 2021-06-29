package world.gregs.voidps.world.interact.entity.combat

import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.client.variable.getVar
import world.gregs.voidps.engine.delay
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.contain.equipment
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp
import world.gregs.voidps.engine.entity.character.update.visual.Hit
import world.gregs.voidps.engine.entity.character.update.visual.hit
import world.gregs.voidps.engine.entity.character.update.visual.setAnimation
import world.gregs.voidps.engine.entity.get
import world.gregs.voidps.engine.entity.getOrNull
import world.gregs.voidps.engine.entity.item.EquipSlot
import world.gregs.voidps.engine.entity.item.FloorItems
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.item.equipped
import world.gregs.voidps.engine.entity.set
import world.gregs.voidps.network.encode.message
import world.gregs.voidps.utility.get
import world.gregs.voidps.world.interact.entity.player.combat.range.special.specialAttack
import world.gregs.voidps.world.interact.entity.player.equip.weaponStyle
import world.gregs.voidps.world.interact.entity.proj.ShootProjectile
import world.gregs.voidps.world.interact.entity.sound.playSound
import kotlin.collections.set
import kotlin.math.floor
import kotlin.random.Random
import kotlin.random.nextInt

val Character.height: Int
    get() = (this as? NPC)?.def?.getOrNull("height") as? Int ?: ShootProjectile.DEFAULT_HEIGHT

fun canAttack(player: Character, target: Character): Boolean {
    if (target is NPC && get<NPCs>().getAtIndex(target.index) == null) {
        return false
    }
    if (target.action.type == ActionType.Death) {
        return false
    }
    // PVP area, slayer requirements, in combat etc..
    return true
}

private fun getWeaponType(source: Character, weapon: Item?): String {
    if (source.spell.isNotBlank()) {
        return "spell"
    }
    return when (weapon?.def?.weaponStyle()) {
        13, 16, 17, 18, 19 -> "range"
        20 -> if (source.attackType == "aim_and_fire") "range" else "melee"
        21 -> when (source.attackType) {
            "flare" -> "range"
            "blaze" -> "blaze"
            else -> "melee"
        }
        else -> "melee"
    }
}

fun Character.hit(
    target: Character,
    weapon: Item? = (this as? Player)?.weapon,
    type: String = getWeaponType(this, weapon),
    delay: Int = if (type == "melee") 0 else 2,
    special: Boolean = (this as? Player)?.specialAttack ?: false
) {
    var damage = hit(this, target, type, weapon)
    damage = damage.coerceAtMost(target.levels.get(Skill.Constitution))
    if (this is Player) {
        grant(this, type, damage)
    }
    delay(target, delay) {
        hit(this, target, damage, type, weapon, special)
    }
}

private fun grant(player: Player, type: String, damage: Int) {
    if (type == "spell" || type == "blaze") {
        val base = 0.0
        if (player.getVar("defensive_cast", false)) {
            player.exp(Skill.Magic, base + damage / 7.5)
            player.exp(Skill.Defence, damage / 10.0)
        } else {
            player.exp(Skill.Magic, base + damage / 5.0)
        }
    } else if (type == "range") {
        if (player.attackType == "long_range") {
            player.exp(Skill.Range, damage / 5.0)
            player.exp(Skill.Defence, damage / 5.0)
        } else {
            player.exp(Skill.Range, damage / 2.5)
        }
    } else if (type == "melee") {
        when (player.attackStyle) {
            "accurate" -> player.exp(Skill.Attack, damage / 2.5)
            "aggressive" -> player.exp(Skill.Strength, damage / 2.5)
            "controlled" -> {
                player.exp(Skill.Attack, damage / 7.5)
                player.exp(Skill.Strength, damage / 7.5)
                player.exp(Skill.Defence, damage / 7.5)
            }
            "defensive" -> player.exp(Skill.Defence, damage / 2.5)
        }
    }
    player.exp(Skill.Constitution, damage / 7.5)
}

fun Character.hit(damage: Int, type: String = "damage") {
    hit(this, this, damage, type)
}

fun hit(source: Character, target: Character, damage: Int, type: String = "damage", weapon: Item? = null, special: Boolean = false) {
    source.events.emit(CombatDamage(target, type, damage, weapon, special))
    var damage = damage
    var soak = 0
    if (damage > 200) {
        val percent = when (type) {
            "melee" -> target["absorb_melee", 0] / 100.0
            "range" -> target["absorb_range", 0] / 100.0
            "magic" -> target["absorb_magic", 0] / 100.0
            else -> 0.0
        }
        soak = floor((damage - 200) * percent).toInt()
        damage -= soak
    }
    if (soak <= 0) {
        soak = -1
    }
    val dealers = target.get<MutableMap<Character, Int>>("damage_dealers")
    dealers[source] = dealers.getOrDefault(source, 0) + damage
    target.hit(
        source = source,
        amount = damage,
        mark = when (type) {
            "range" -> Hit.Mark.Range
            "melee" -> Hit.Mark.Melee
            "magic" -> Hit.Mark.Magic
            "poison" -> Hit.Mark.Poison
            "dragonfire", "damage" -> Hit.Mark.Regular
            else -> Hit.Mark.Missed
        },
        critical = (type == "melee" || type == "magic" || type == "range") && damage > (source["max_hit", 0] * 0.9),
        soak = soak
    )
    target.levels.drain(Skill.Constitution, damage)
    val name = (target as? NPC)?.def?.getOrNull("category") ?: "player"
    if (source is Player) {
        source.playSound("${name}_hit", delay = 40)
    }
    target.setAnimation("${name}_hit")
    target.events.emit(CombatHit(source, type, damage, weapon, special))
}

fun ammoRequired(item: Item) = !item.name.startsWith("crystal_bow") && item.name != "zaryte_bow" && !item.name.endsWith("sling") && !item.name.endsWith("chinchompa")

fun getStrengthBonus(source: Character, type: String, weapon: Item?): Int {
    return if (type == "blaze") {
        when (weapon?.name) {
            "green_salamander" -> 56
            "orange_salamander" -> 59
            "red_salamander" -> 77
            "black_salamander" -> 92
            else -> 0
        }
    } else if (type == "range" && source is Player && weapon != null && (weapon.name == source.ammo || !ammoRequired(weapon))) {
        weapon.def["range_str", 0]
    } else {
        source[if (type == "range") "range_str" else "str", 0]
    }
}

fun getMaximumHit(source: Character, target: Character? = null, type: String, weapon: Item?): Int {
    val strengthBonus = getStrengthBonus(source, type, weapon) + 64
    val baseMaxHit = if (type == "spell") {
        source["spell_damage", 0.0]
    } else {
        0.5 + (getEffectiveLevel(source, when (type) {
            "range" -> Skill.Range
            "spell", "blaze" -> Skill.Magic
            else -> Skill.Strength
        }, accuracy = false) * strengthBonus) / 64
    }
    val modifier = HitDamageModifier(target, type, strengthBonus, baseMaxHit, weapon)
    source.events.emit(modifier)
    source["max_hit"] = modifier.damage.toInt()
    return modifier.damage.toInt()
}

fun getMinimumHit(source: Character, target: Character? = null, type: String, weapon: Item?): Int {
    return 0
}

fun getEffectiveLevel(source: Character, skill: Skill, accuracy: Boolean): Int {
    val level = source.levels.get(skill).toDouble()
    val mod = HitEffectiveLevelModifier(skill, accuracy, level)
    source.events.emit(mod)
    return mod.level.toInt()
}

fun getRating(source: Character, target: Character?, type: String, weapon: Item?): Int {
    val offense = source == target
    var level = if (target == null) 8 else getEffectiveLevel(target, when (type) {
        "range" -> Skill.Range
        "spell", "blaze" -> if (offense && target is Player) Skill.Defence else Skill.Magic
        else -> Skill.Attack
    }, offense)
    val override = HitEffectiveLevelOverride(target, type, !offense, level)
    source.events.emit(override)
    level = override.level
    val style = if (type == "range") "range" else if (type == "spell") "magic" else target?.combatStyle ?: ""
    val equipmentBonus = target?.getOrNull(if (offense) style else "${style}_def") ?: 0
    val rating = level * (equipmentBonus + 64.0)
    val modifier = HitRatingModifier(target, type, offense, rating, weapon)
    source.events.emit(modifier)
    return modifier.rating.toInt()
}

fun hitChance(source: Character, target: Character?, type: String, weapon: Item?): Double {
    val offensiveRating = getRating(source, source, type, weapon)
    val defensiveRating = getRating(source, target, type, weapon)
    val chance = if (offensiveRating > defensiveRating) {
        1.0 - (defensiveRating + 2.0) / (2.0 * (offensiveRating + 1.0))
    } else {
        offensiveRating / (2.0 * (defensiveRating + 1.0))
    }

    val modifier = HitChanceModifier(target, type, chance, weapon)
    source.events.emit(modifier)
    return modifier.chance
}

fun successfulHit(source: Character, target: Character?, type: String, weapon: Item?): Boolean {
    val verac = if (source is Player) source.hasFullVeracs() else if (source is NPC) source.name == "verac" else false
    val veracs = verac && Random.nextDouble() < 0.25
    if (veracs) {
        println("Veracs")
        return true
    }

    return Random.nextDouble() < hitChance(source, target, type, weapon)
}

private fun Player.hasFullVeracs(): Boolean {
    return notBroken(equipped(EquipSlot.Hat).name, "veracs_helm") &&
            notBroken(equipped(EquipSlot.Hat).name, "veracs_flail") &&
            notBroken(equipped(EquipSlot.Hat).name, "veracs_brassard") &&
            notBroken(equipped(EquipSlot.Hat).name, "veracs_plateskirt")
}

private fun notBroken(name: String, prefix: String): Boolean {
    return name.startsWith(prefix) && !name.endsWith("broken")
}

fun hit(source: Character, target: Character?, type: String, weapon: Item?): Int {
    return if (successfulHit(source, target, type, weapon)) {
        val maxHit = getMaximumHit(source, target, type, weapon)
        val minHit = getMinimumHit(source, target, type, weapon)
        Random.nextInt(minHit..maxHit)
    } else {
        0
    }
}

fun removeAmmo(player: Player, target: Character, ammo: String, required: Int) {
    if (ammo == "bolt_rack") {
        delay {
            player.equipment.remove(ammo, required)
        }
        return
    }
    when {
        player.equipped(EquipSlot.Cape).name == "avas_attractor" && !exceptions(ammo) -> remove(player, target, ammo, required, 0.6, 0.2)
        player.equipped(EquipSlot.Cape).name == "avas_accumulator" && !exceptions(ammo) -> remove(player, target, ammo, required, 0.72, 0.08)
        player.equipped(EquipSlot.Cape).name == "avas_alerter" -> remove(player, target, ammo, required, 0.8, 0.0)
        else -> {
            delay {
                player.equipment.remove(ammo, required)
                if (!player.equipment.contains(ammo)) {
                    player.message("That was your last one!")
                }
                get<FloorItems>().add(ammo, 1, target.tile)
            }
        }
    }
}

private fun exceptions(ammo: String) = ammo == "silver_bolts" || ammo == "bone_bolts"

private fun remove(player: Player, target: Character, ammo: String, required: Int, recoverChance: Double, dropChance: Double) {
    val random = Random.nextDouble()
    if (random > recoverChance) {
        delay {
            player.equipment.remove(ammo, required)
            if (!player.equipment.contains(ammo)) {
                player.message("That was your last one!")
            }
            if (random > 1.0 - dropChance) {
                get<FloorItems>().add(ammo, 1, target.tile)
            }
        }
    }
}

val Character.inMultiCombat: Boolean
    get() = false

val ItemDefinition.ammo: Set<String>?
    get() = (getOrNull("ammo") as? ArrayList<String>)?.toSet()

// E.g "accurate"
val Character.attackStyle: String
    get() = get("attack_style", "")

// E.g "flick"
val Character.attackType: String
    get() = get("attack_type", "")

// E.g "crush"
val Character.combatStyle: String
    get() = get("combat_style", "")

val Character.spell: String
    get() = get("spell", "")

var Player.weapon: Item
    get() = get("weapon", Item.EMPTY)
    set(value) = set("weapon", value)

var Player.ammo: String
    get() = get("ammo", "")
    set(value) = set("ammo", value)