package rs.dusk.engine.entity.character.contain

import get
import rs.dusk.cache.config.data.ContainerDefinition
import rs.dusk.engine.client.send
import rs.dusk.game.container.Container
import rs.dusk.game.container.ContainerDefinitions
import rs.dusk.game.container.StackMode.Normal
import rs.dusk.game.entity.character.player.Player

fun Player.sendContainer(name: String, secondary: Boolean = false) {
    val definitions: ContainerDefinitions = get()
    val containerId = definitions.getId(name)
    val container = container(definitions.get(name), secondary)
    send(ContainerItemsMessage(containerId, container.getItems(), container.getAmounts(), secondary))
}

fun Player.hasContainer(name: String): Boolean {
    val definitions: ContainerDefinitions = get()
    val id = definitions.getId(name)
    return containers.containsKey(id)
}

fun Player.container(name: String, secondary: Boolean = false): Container {
    val definitions: ContainerDefinitions = get()
    val container = definitions.get(name)
    return container(container, secondary)
}

fun Player.container(detail: ContainerDefinition, secondary: Boolean = false): Container {
    return containers.getOrPut(if (secondary) -detail.id else detail.id) {
        Container(
            definitions = get(),
            capacity = get<ContainerDefinitions>().get(detail.id).length,
            listeners = mutableListOf({ updates -> send(ContainerItemUpdateMessage(detail.id, updates.map { Triple(it.index, it.item, it.amount) }, secondary)) }),
            stackMode = detail["stack", Normal]
        )
    }
}

val Player.inventory: Container
    get() = container("inventory")

val Player.equipment: Container
    get() = container("worn_equipment")

val Player.beastOfBurden: Container
    get() = container("beast_of_burden")