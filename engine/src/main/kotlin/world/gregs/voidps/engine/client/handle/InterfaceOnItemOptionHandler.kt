package world.gregs.voidps.engine.client.handle

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.cache.definition.decoder.InterfaceDecoder
import world.gregs.voidps.engine.client.ui.interact.InterfaceOnItem
import world.gregs.voidps.engine.entity.character.contain.container
import world.gregs.voidps.engine.entity.character.contain.hasContainer
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.definition.*
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.sync
import world.gregs.voidps.network.Handler
import world.gregs.voidps.network.instruct.InteractInterfaceItem
import world.gregs.voidps.utility.inject

/**
 * @author Jacob Rhiel <jacob.rhiel@gmail.com>
 * @created Jun 20, 2021
 */
class InterfaceOnItemOptionHandler : Handler<InteractInterfaceItem>() {

    private val items: ItemDefinitions by inject()
    private val decoder: InterfaceDecoder by inject()
    private val itemDefinitions: ItemDefinitions by inject()
    private val interfaceDefinitions: InterfaceDefinitions by inject()
    private val containerDefinitions: ContainerDefinitions by inject()
    private val logger = InlineLogger()

    override fun validate(player: Player, instruction: InteractInterfaceItem) {
        val (fromItem, toItem, fromSlot, toSlot, fromInterfaceId, fromComponentId, toInterfaceId, toComponentId) = instruction

        val fromItemName = items.getNameOrNull(fromItem) ?: return
        val toItemName = items.getNameOrNull(toItem) ?: return

        val fItem: Item
        val tItem: Item

        // Has interface open
        if (!player.interfaces.contains(fromInterfaceId)) {
            logger.info { "Interface $fromInterfaceId not found for player $player" }
            return
        }

        // Interface has that component
        val definition = decoder.get(fromInterfaceId)
        val componentDef = definition.components?.get(fromComponentId)
        if (componentDef == null) {
            logger.info { "Interface $fromInterfaceId component $fromComponentId not found for player $player" }
            return
        }

        // Get the string ids of the interface and component
        val name = interfaceDefinitions.getName(fromInterfaceId)
        val componentName = definition.getComponentName(fromComponentId)
        val component = definition.getComponentOrNull(componentName)

        // If an from item is provided
        var item = Item.EMPTY
        var containerName = ""
        if (fromItem != -1 && fromSlot != -1) {
            // Check the component name is valid
            if (component == null) {
                logger.info { "Interface $name component $fromComponentId not found for player $player" }
                return
            }
            // Check the component container exists
            containerName = component["container", ""]
            if (!player.hasContainer(containerName)) {
                logger.info { "Interface $name container $containerName not found for player $player" }
                return
            }

            // Check the item exists in the container
            val def = containerDefinitions.get(containerName)
            if (fromSlot > def.length) {
                logger.info { "Invalid interface $name container $containerName ${def.length} slot $fromSlot not found for player $player" }
                return
            }

            var found = false
            val primary = player.container(def, secondary = false)
            if (primary.isValidId(fromSlot, itemDefinitions.getName(fromItem))) {
                found = true
                item = primary.getItem(fromSlot)
            } else {
                val secondary = player.container(def, secondary = true)
                if (secondary.isValidId(fromSlot, itemDefinitions.getName(fromItem))) {
                    found = true
                    item = secondary.getItem(fromSlot)
                }
            }
            if (!found) {
                logger.info { "Interface $name container item $item $fromSlot not found for player $player" }
                return
            }
        }
        fItem = item

        // If an to item is provided
        item = Item.EMPTY
        containerName = ""
        if (toItem != -1 && toSlot != -1) {
            // Check the component name is valid
            if (component == null) {
                logger.info { "Interface $name component $fromComponentId not found for player $player" }
                return
            }
            // Check the component container exists
            containerName = component["container", ""]
            if (!player.hasContainer(containerName)) {
                logger.info { "Interface $name container $containerName not found for player $player" }
                return
            }

            // Check the item exists in the container
            val def = containerDefinitions.get(containerName)
            if (toSlot > def.length) {
                logger.info { "Invalid interface $name container $containerName ${def.length} slot $toSlot not found for player $player" }
                return
            }

            var found = false
            val primary = player.container(def, secondary = false)
            if (primary.isValidId(toSlot, itemDefinitions.getName(toItem))) {
                found = true
                item = primary.getItem(toSlot)
            } else {
                val secondary = player.container(def, secondary = true)
                if (secondary.isValidId(toSlot, itemDefinitions.getName(toItem))) {
                    found = true
                    item = secondary.getItem(toSlot)
                }
            }
            if (!found) {
                logger.info { "Interface $name container item $item $toSlot not found for player $player" }
                return
            }
        }
        tItem = item

        sync {
            player.events.emit(
                InterfaceOnItem(
                    fromItem = fItem,
                    toItem = tItem,
                    fromSlot,
                    toSlot,
                    fromInterfaceId,
                    fromComponentId,
                    toInterfaceId,
                    toComponentId,
                    fromContainer = containerName,
                    toContainer = containerName//todo
                )
            )
        }
    }

}