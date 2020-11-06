package rs.dusk.world.community.trade

import com.github.michaelbull.logging.InlineLogger
import kotlinx.coroutines.CancellationException
import rs.dusk.engine.action.ActionType
import rs.dusk.engine.action.Suspension
import rs.dusk.engine.action.action
import rs.dusk.engine.client.ui.closeType
import rs.dusk.engine.client.ui.detail.InterfaceDetails
import rs.dusk.engine.client.variable.*
import rs.dusk.engine.entity.character.clear
import rs.dusk.engine.entity.character.contain.Container
import rs.dusk.engine.entity.character.contain.ContainerModification
import rs.dusk.engine.entity.character.contain.inventory
import rs.dusk.engine.entity.character.get
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.engine.entity.character.player.PlayerOption
import rs.dusk.engine.entity.character.player.chat.ChatType
import rs.dusk.engine.entity.character.player.chat.message
import rs.dusk.engine.entity.character.player.social.hasFriend
import rs.dusk.engine.entity.character.set
import rs.dusk.engine.entity.character.update.visual.player.name
import rs.dusk.engine.entity.definition.ContainerDefinitions
import rs.dusk.engine.entity.definition.ItemDefinitions
import rs.dusk.engine.event.then
import rs.dusk.engine.event.where
import rs.dusk.network.codec.game.encode.message
import rs.dusk.network.codec.game.encode.sendScript
import rs.dusk.utility.inject
import rs.dusk.world.command.Command
import rs.dusk.world.community.trade.*
import rs.dusk.world.community.trade.lend.Loan.lendItem
import rs.dusk.world.interact.entity.player.display.Tab

/**
 * Requesting to trade with another player, accepting the request and setting up the trade
 * When an offer is updated the change is persisted to the other player
 */

val logger = InlineLogger()
val decoder: ItemDefinitions by inject()

BooleanVariable(1042, Variable.Type.VARP).register("offer_modified")
BooleanVariable(1043, Variable.Type.VARP).register("other_offer_modified")

IntVariable(729, Variable.Type.VARC).register("offer_value")
IntVariable(697, Variable.Type.VARC).register("other_offer_value")

IntVariable(203, Variable.Type.VARCSTR).register("other_trader_name")

PlayerOption where { option == "Trade with" } then {
    val filter = target["trade_filter", "on"]
    if (filter == "off" || (filter == "friends" && !target.hasFriend(player))) {
        return@then
    }
    if (player.requests.has(target, "trade")) {
        player.message("Sending trade offer...", ChatType.GameTrade)
    } else {
        player.message("Sending trade offer...", ChatType.GameTrade)
        target.message("wishes to trade with you.", ChatType.Trade, name = player.name)
    }
    target.requests.add(player, "trade") { requester, acceptor ->
        startTrade(requester, acceptor)
        startTrade(acceptor, requester)
    }
}

Command where { prefix == "trade" } then {
    startTrade(player, player)
}

fun startTrade(player: Player, other: Player) {
    reset(player, other)
    player.setVar("other_trader_name", other.name)
    player["trade_partner"] = other

    val offerListener: (List<ContainerModification>) -> Unit = updateOffer(player, other)
    val loanListener: (List<ContainerModification>) -> Unit = updateLoan(player, other)
    val inventoryListener: (List<ContainerModification>) -> Unit = {
        updateInventorySpaces(other, player)
    }

    player.offer.listeners.add(offerListener)
    player.loan.listeners.add(loanListener)
    player.inventory.listeners.add(inventoryListener)

    player.action(ActionType.Trade) {
        try {
            sendMain(player, other)
            await<Unit>(Suspension.Infinite)
            tradeItems(player, other)
        } catch (e: CancellationException) {
            cancel(player)
            other.action.cancel(ActionType.Trade)
        } finally {
            player.offer.listeners.remove(offerListener)
            player.loan.listeners.remove(loanListener)
            player.inventory.listeners.remove(inventoryListener)
            reset(player, other)
            player.closeType("main_screen")
            player.interfaces.close("trade_side")
            player.interfaces.open("inventory")
        }
    }
}

fun tradeItems(player: Player, other: Player) {
    if (!player.otherOffer.moveAll(player.inventory)) {
        logger.info { "Issue exchanging items $player ${player.otherOffer} ${player.otherLoan} ${player.inventory}" }
    }
    loanItem(player, other)
}

fun loanItem(player: Player, other: Player) {
    val loanItem = player.otherLoan.getItem(0)
    val duration = other.getVar("lend_time", -1)
    if(loanItem == -1 || duration == -1) {
        return
    }
    lendItem(player, other, loanItem, duration)
}

fun updateInventorySpaces(player: Player, other: Player) {
    player.interfaces.sendText("trade_main", "slots", "has ${other.inventory.spaces} free inventory slots.")
}

fun cancel(player: Player) {
    player.offer.moveAll(player.inventory)
    player.loan.moveAll(player.inventory)
}

fun sendMain(player: Player, other: Player) {
    player.interfaces.apply {
        open("trade_main")
        close("inventory")
        open("trade_side")
        sendText("trade_main", "title", "Trading with: ${other.name}")
        sendText("trade_main", "status", "")
    }
    updateInventorySpaces(player, other)
    player.interfaceOptions.apply {
        send("trade_main", "offer_options")
        unlockAll("trade_main", "offer_options", 0 until 28)

        send("trade_main", "other_options")
        unlockAll("trade_main", "other_options", 0 until 28)

        send("trade_side", "offer")
        unlockAll("trade_side", "offer", 0 until 28)

        unlockAll("trade_main", "loan_item")
        unlockAll("trade_main", "other_loan_item")
        unlockAll("trade_main", "loan_time")
    }
}

fun reset(player: Player, other: Player) {
    player.setVar("tab", Tab.Inventory)
    player.setVar("offer_value", 0)
    player.setVar("other_offer_value", 0)
    player.setVar("lend_time", 0)

    player.clear("accepted_trade")
    player.clear("trade_partner")

    player.requests.remove(other, "trade")
    player.requests.remove(other, "accept_trade")
    player.requests.remove(other, "confirm_trade")

    player.loan.clearAll()
    player.otherLoan.clearAll()
    player.offer.clearAll()
    player.otherOffer.clearAll()
}

/*
    Loan
 */
fun updateLoan(player: Player, other: Player): (List<ContainerModification>) -> Unit = { updates ->
    applyUpdates(other.otherLoan, updates)
    val warn = player["accepted_trade", false] && removedAnyItems(updates)
    modified(player, other, warn)
}

fun applyUpdates(container: Container, updates: List<ContainerModification>) {
    for ((index, _, _, item, amount) in updates) {
        container.set(index, item, amount)
    }
}

fun removedAnyItems(list: List<ContainerModification>) = list.any { (_, _, oldAmount, _, amount) -> amount < oldAmount }

fun modified(player: Player, other: Player, warned: Boolean) {
    if (warned) {
        player.setVar("offer_modified", true)
        other.setVar("other_offer_modified", true)
    }
    player.requests.remove(other, "accept_trade")
    player.interfaces.sendText("trade_main", "status", "")
}

/*
    Offer
 */
fun updateOffer(player: Player, other: Player): (List<ContainerModification>) -> Unit = { updates ->
    applyUpdates(other.otherOffer, updates)
    val warn = player["accepted_trade", false] && removedAnyItems(updates)
    if(warn) {
        highlightRemovedSlots(player, other, updates)
    }
    modified(player, other, warn)
    updateValue(player, other)
}

fun highlightRemovedSlots(player: Player, other: Player, updates: List<ContainerModification>) {
    for ((index, _, oldAmount, _, amount) in updates) {
        if(amount < oldAmount) {
            player.warn("trade_main", "offer_warning", index)
            other.warn("trade_main", "other_warning", index)
        }
    }
}

fun Player.warn(name: String, component: String, slot: Int) {
    val details: InterfaceDetails = rs.dusk.utility.get()
    val containerDefinitions: ContainerDefinitions = rs.dusk.utility.get()
    val comp = details.getComponent(name, component)
    val container = containerDefinitions.get(comp.container)
    println(listOf(comp.parent, comp.id, (comp.parent shl 16) or comp.id, container["width", 0.0], container["height", 0.0], slot))
    sendScript(143, (comp.parent shl 16) or comp.id, container["width", 0.0], container["height", 0.0], slot)
}

fun updateValue(player: Player, other: Player) {
    val value = player.offer.calculateValue(decoder)
    player.setVar("offer_value", value)
    other.setVar("other_offer_value", value)
}