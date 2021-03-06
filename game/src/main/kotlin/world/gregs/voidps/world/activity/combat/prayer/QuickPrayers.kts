package world.gregs.voidps.world.activity.combat.prayer

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.cache.config.decoder.StructDecoder
import world.gregs.voidps.cache.definition.decoder.EnumDecoder
import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.client.variable.*
import world.gregs.voidps.engine.entity.*
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Level.has
import world.gregs.voidps.engine.entity.character.player.skill.Level.hasMax
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.network.encode.message
import world.gregs.voidps.utility.inject
import world.gregs.voidps.world.activity.combat.prayer.PrayerConfigs.ACTIVE_CURSES
import world.gregs.voidps.world.activity.combat.prayer.PrayerConfigs.ACTIVE_PRAYERS
import world.gregs.voidps.world.activity.combat.prayer.PrayerConfigs.QUICK_CURSES
import world.gregs.voidps.world.activity.combat.prayer.PrayerConfigs.QUICK_PRAYERS
import world.gregs.voidps.world.activity.combat.prayer.PrayerConfigs.SELECTING_QUICK_PRAYERS
import world.gregs.voidps.world.activity.combat.prayer.PrayerConfigs.TEMP_QUICK_PRAYERS
import world.gregs.voidps.world.activity.combat.prayer.PrayerConfigs.USING_QUICK_PRAYERS
import world.gregs.voidps.world.interact.entity.player.display.Tab
import world.gregs.voidps.world.interact.entity.sound.playSound

/**
 * Prayers
 * Handles the activation of prayers and selection of quick prayers
 */
val enums: EnumDecoder by inject()
val structs: StructDecoder by inject()

val nameRegex = "<br>(.*?)<br>".toRegex()
val prayerEnumId = 2279
val curseEnumId = 863

val logger = InlineLogger()

fun loadPrayerNames(enumId: Int): List<String> {
    val list = mutableListOf<Pair<Int, String>>()
    enums.get(enumId).map?.forEach { (_, value) ->
        val strut = structs.get(value as Int).params
        val name = getPrayerName(strut)!!
        list.add(value to name)
    }
    list.sortBy { it.first }
    return list.map { it.second }
}

val prayerNames = loadPrayerNames(prayerEnumId)
BitwiseVariable(1395, Variable.Type.VARP, values = prayerNames).register(ACTIVE_PRAYERS)
BitwiseVariable(1397, Variable.Type.VARP, true, values = prayerNames).register(QUICK_PRAYERS)

val curseNames = loadPrayerNames(curseEnumId)
BitwiseVariable(1582, Variable.Type.VARP, values = curseNames).register(ACTIVE_CURSES)
BitwiseVariable(1587, Variable.Type.VARP, true, values = curseNames).register(QUICK_CURSES)

val prayerGroups = setOf(
    setOf("Steel Skin", "Piety", "Thick Skin", "Chivalry", "Rock Skin", "Augury", "Rigour"),
    setOf("Burst of Strength", "Piety", "Chivalry", "Ultimate Strength", "Superhuman Strength"),
    setOf("Improved Reflexes", "Incredible Reflexes", "Piety", "Clarity of Thought", "Chivalry"),
    setOf("Rigour", "Sharp Eye", "Hawk Eye", "Eagle Eye"),
    setOf("Mystic Will", "Mystic Might", "Mystic Lore", "Augury"),
    setOf("Rapid Renewal", "Rapid Heal"),
    setOf("Smite", "Protect from Missiles", "Protect from Melee", "Redemption", "Protect from Magic", "Retribution"),
    setOf("Redemption", "Retribution", "Smite", "Protect from Summoning")
)

val cursesGroups = setOf(
    setOf("Wrath", "Soul Split"),
    setOf("Soul Split", "Deflect Summoning", "Wrath"),
    setOf("Leech Strength", "Turmoil"),
    setOf("Leech Attack", "Turmoil", "Sap Warrior"),
    setOf("Soul Split", "Deflect Missiles", "Wrath", "Deflect Melee", "Deflect Magic"),
    setOf("Turmoil", "Sap Mage", "Leech Magic"),
    setOf("Turmoil", "Sap Ranger", "Leech Ranged"),
    setOf("Turmoil", "Leech Defence"),
    setOf("Sap Spirit", "Leech Special Attack", "Turmoil")
)

on<InterfaceOption>({ name == "prayer_list" && component == "regular_prayers" }) { player: Player ->
    val prayers = player.getActivePrayerVarKey()
    player.togglePrayer(itemIndex, prayers, false)
}

on<InterfaceOption>({ name == "prayer_list" && component == "quick_prayers" }) { player: Player ->
    player.togglePrayer(itemIndex, player.getQuickVarKey(), true)
}

fun Player.togglePrayer(prayerIndex: Int, listKey: String, quick: Boolean) {
    val curses = isCurses()
    val enum = if (curses) curseEnumId else prayerEnumId
    val params = getPrayerParameters(prayerIndex, enum) ?: return logger.warn { "Unable to find prayer $prayerIndex $listKey" }
    val name = getPrayerName(params) ?: return logger.warn { "Unable to find prayer button $prayerIndex $listKey $params" }
    val activated = hasVar(listKey, name)
    if (activated) {
        removeVar(listKey, name)
    } else {
        if (!quick && !has(Skill.Prayer, 1)) {
            message("You need to recharge your Prayer at an altar.")
            return
        }
        val requiredLevel = params[737] as? Int ?: 0
        if (!hasMax(Skill.Prayer, requiredLevel)) {
            val message = params[738] as? String
            message(message ?: "You need a prayer level of $requiredLevel to use $name.")
            return
        }
        for (group in if (curses) cursesGroups else prayerGroups) {
            if (group.contains(name)) {
                group.forEach {
                    removeVar(listKey, it, refresh = false)
                }
            }
        }
        addVar(listKey, name)
    }
}

/**
 * Quick prayers
 * Until the new quick prayer selection is confirmed old
 * quick prayers are stored in [TEMP_QUICK_PRAYERS]
 */
on<InterfaceOption>({ name == "prayer_orb" && component == "orb" && option == "Select Quick Prayers" }) { player: Player ->
    val selecting = player.toggleVar(SELECTING_QUICK_PRAYERS)
    if (selecting) {
        player.setVar("tab", Tab.PrayerList)
        player.sendVar(player.getQuickVarKey())
        player[TEMP_QUICK_PRAYERS] = player.getVar(player.getQuickVarKey(), 0)
    } else if (player.contains(TEMP_QUICK_PRAYERS)) {
        player.saveQuickPrayers()
    }
    if (selecting) {
        player.interfaceOptions.unlockAll("prayer_list", "quick_prayers", 0..29)
    } else {
        player.interfaceOptions.unlockAll("prayer_list", "regular_prayers", 0..29)
    }
}

on<InterfaceOption>({ name == "prayer_orb" && component == "orb" && option == "Turn Quick Prayers On" }) { player: Player ->
    if (player.levels.get(Skill.Prayer) == 0) {
        player.message("You've run out of prayer points.")
        player.setVar(USING_QUICK_PRAYERS, false)
        return@on
    }
    val active = player.toggleVar(USING_QUICK_PRAYERS)
    val activePrayers = player.getActivePrayerVarKey()
    if (active) {
        val quickPrayers: Int = player.getOrNull(TEMP_QUICK_PRAYERS) ?: player.getVar(player.getQuickVarKey(), 0)
        if (quickPrayers > 0) {
            player.setVar(activePrayers, quickPrayers)
        } else {
            player.message("You haven't selected any quick-prayers.")
            player.setVar(USING_QUICK_PRAYERS, false)
            return@on
        }
    } else {
        player.playSound("deactivate_prayer")
        player.clearVar<String>(activePrayers)
    }
}

on<InterfaceOption>({ name == "prayer_list" && component == "confirm" && option == "Confirm Selection" }) { player: Player ->
    player.saveQuickPrayers()
}

on<Unregistered>({ it.contains(TEMP_QUICK_PRAYERS) }) { player: Player ->
    player.cancelQuickPrayers()
}

fun Player.saveQuickPrayers() {
    setVar(SELECTING_QUICK_PRAYERS, false)
    clear(TEMP_QUICK_PRAYERS)
}

fun Player.cancelQuickPrayers() {
    setVar(getQuickVarKey(), get(TEMP_QUICK_PRAYERS, 0))
    clear(TEMP_QUICK_PRAYERS)
}

fun getPrayerName(params: HashMap<Long, Any>?): String? {
    val description = params?.getOrDefault(734, null) as? String ?: return null
    return nameRegex.find(description)?.groupValues?.lastOrNull()
}

fun getPrayerParameters(index: Int, enumId: Int): HashMap<Long, Any>? {
    val enum = enums.get(enumId).map!!
    return structs.get(enum[index] as Int).params
}

fun Player.getQuickVarKey(): String = if (isCurses()) QUICK_CURSES else QUICK_PRAYERS