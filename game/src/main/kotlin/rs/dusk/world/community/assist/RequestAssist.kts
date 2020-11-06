import com.github.michaelbull.logging.InlineLogger
import rs.dusk.engine.action.ActionType
import rs.dusk.engine.action.action
import rs.dusk.engine.client.ui.awaitInterfaces
import rs.dusk.engine.client.ui.close
import rs.dusk.engine.client.variable.*
import rs.dusk.engine.entity.character.clear
import rs.dusk.engine.entity.character.get
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.engine.entity.character.player.PlayerOption
import rs.dusk.engine.entity.character.player.chat.ChatType
import rs.dusk.engine.entity.character.player.delay.Delay
import rs.dusk.engine.entity.character.player.delay.delayed
import rs.dusk.engine.entity.character.player.delay.remaining
import rs.dusk.engine.entity.character.player.skill.Skill
import rs.dusk.engine.entity.character.player.social.hasFriend
import rs.dusk.engine.entity.character.remove
import rs.dusk.engine.entity.character.set
import rs.dusk.engine.entity.character.update.visual.player.name
import rs.dusk.engine.entity.character.update.visual.setAnimation
import rs.dusk.engine.entity.character.update.visual.setGraphic
import rs.dusk.engine.event.then
import rs.dusk.engine.event.where
import rs.dusk.network.codec.game.encode.message
import rs.dusk.utility.TICKS
import rs.dusk.utility.func.plural
import rs.dusk.world.community.assist.Assistance.canAssist
import rs.dusk.world.community.assist.Assistance.exceededMaximum
import rs.dusk.world.community.assist.Assistance.getHoursRemaining
import rs.dusk.world.community.assist.Assistance.hasEarnedMaximumExperience
import rs.dusk.world.community.assist.Assistance.maximumExperience
import rs.dusk.world.community.assist.Assistance.redirectSkillExperience
import rs.dusk.world.community.assist.Assistance.stopRedirectingSkillExp
import rs.dusk.world.community.assist.Assistance.toggleInventory
import java.util.concurrent.TimeUnit
import kotlin.math.min

/**
 * Requesting assistance from a player, accepting the request and redirecting earned experience
 */

val skills = listOf(
    Skill.Runecrafting,
    Skill.Crafting,
    Skill.Fletching,
    Skill.Construction,
    Skill.Farming,
    Skill.Magic,
    Skill.Smithing,
    Skill.Cooking,
    Skill.Herblore
)
val logger = InlineLogger()

IntVariable(4103, Variable.Type.VARBIT, true).register("total_xp_earned")

PlayerOption where { option == "Req Assist" } then {
    val filter = target["assist_filter", "on"]
    if (filter == "off" || (filter == "friends" && !target.hasFriend(player))) {
        return@then
    }
    if (player.requests.has(target, "assist")) {
        player.message("Sending assistance response.", ChatType.GameAssist)
    } else {
        if (requestingTooQuickly(player) || refuseRequest(target, player)) {
            return@then
        }
        player.message("Sending assistance request.", ChatType.GameAssist)
        target.message("is requesting your assistance.", ChatType.Assist, name = player.name)
    }
    target.requests.add(player, "assist") { requester, acceptor ->
        setupAssisted(requester, acceptor)
        setupAssistant(acceptor, requester)
    }
}

fun requestingTooQuickly(player: Player): Boolean {
    if (player.delayed(Delay.RequestAssist)) {
        val time = TICKS.toSeconds(player.remaining(Delay.RequestAssist))
        player.message("You have only just made an assistance request", ChatType.GameAssist)
        player.message("You have to wait $time ${"second".plural(time)} before making a new request.", ChatType.GameAssist)
        return true
    }
    return false
}

fun refuseRequest(target: Player, player: Player): Boolean {
    if (hasEarnedMaximumExperience(target)) {
        val hours = getHoursRemaining(target)
        player.message("${target.name} is unable to assist at the moment.", ChatType.GameAssist)
        target.message("An assist request has been refused. You can assist again in $hours ${"hour".plural(hours)}.", ChatType.GameAssist)
        return true
    }
    return false
}

fun setupAssisted(player: Player, assistant: Player) = player.action {
    player.message("You are being assisted by ${assistant.name}.", ChatType.GameAssist)
    player["assistant"] = assistant
    player["assist_point"] = player.tile
    setAssistAreaStatus(player, true)
    delay(2)
    player.setAnimation(7299)
}

fun setupAssistant(player: Player, assisted: Player) = player.action(ActionType.Assisting) {
    try {
        interceptExperience(player, assisted)
        player["assisted"] = assisted
        player.message("You are assisting ${assisted.name}.", ChatType.GameAssist)
        player.interfaces.apply {
            open("assist_xp")
            sendText("assist_xp", "description", "The Assist System is available for you to use.")
            sendText("assist_xp", "title", "Assist System XP Display - You are assisting ${assisted.name}")
        }
        applyExistingSkillRedirects(player, assisted)
        setAssistAreaStatus(player, true)
        player.sendVar("total_xp_earned")
        player.setAnimation(7299)
        player.setGraphic(1247)
        toggleInventory(player, enabled = false)
        player.awaitInterfaces()
    } finally {
        cancelAssist(player, assisted)
    }
}

fun applyExistingSkillRedirects(player: Player, assisted: Player) {
    var clearedAny = false
    for (skill in skills) {
        val key = "assist_toggle_${skill.name.toLowerCase()}"
        if (player.getVar(key, false)) {
            if (!canAssist(player, assisted, skill)) {
                player.setVar(key, false)
                clearedAny = true
            } else {
                redirectSkillExperience(assisted, skill)
            }
        }
    }
    if (clearedAny) {
        player.message("You can only assist skills which are higher than whom you are helping.")
    }
}

fun cancelAssist(assistant: Player?, assisted: Player?) {
    if (assistant != null) {
        toggleInventory(assistant, enabled = true)
        assistant.close("assist_xp")
        assistant.message("You have stopped assisting ${assisted?.name}.", ChatType.GameAssist)
        setAssistAreaStatus(assistant, false)
        assistant.clear("assisted")
    }
    if (assisted != null) {
        assisted.message("${assistant?.name} has stopped assisting you.", ChatType.GameAssist)
        stopInterceptingExperience(assisted)
        stopRedirectingAllExp(assisted)
        setAssistAreaStatus(assisted, false)
        assisted.clear("assistant")
        assisted.clear("assist_point")
    }
    if (assistant == null || assisted == null) {
        logger.error { "Assisting cancellation error $assistant $assisted" }
    }
}

fun interceptExperience(player: Player, assisted: Player) {
    val listener: (Skill, Double) -> Unit = { skill, experience ->
        val active = player.getVar("assist_toggle_${skill.name.toLowerCase()}", false)
        var gained = player.getVar("total_xp_earned", 0.0)
        if (active && !exceededMaximum(gained)) {
            val exp = min(experience, (maximumExperience - gained) / 10)
            gained += exp * 10.0
            val maxed = exceededMaximum(gained)
            player.experience.add(skill, exp)
            player.setVar("total_xp_earned", gained)
            if (maxed) {
                player.interfaces.sendText(
                    "assist_xp", "description",
                    """
                        You've earned the maximum XP from the Assist System with a 24-hour period.
                        You can assist again in 24 hours.
                    """
                )
                player["assist_timeout", true] = System.currentTimeMillis() + TimeUnit.HOURS.toMillis(24)
                stopRedirectingAllExp(assisted)
            }
        }
    }
    assisted.experience.addBlockedListener(listener)
    assisted["assist_listener"] = listener
}


fun stopInterceptingExperience(assisted: Player) {
    val listener: ((Skill, Double) -> Unit)? = assisted.remove("assist_listener")
    if (listener != null) {
        assisted.experience.removeBlockedListener(listener)
    }
}

fun stopRedirectingAllExp(player: Player) {
    for (skill in skills) {
        stopRedirectingSkillExp(player, skill)
    }
}

fun setAssistAreaStatus(player: Player, visible: Boolean) {
    player.interfaces.sendVisibility("area_status_icon", "assist", visible)
}