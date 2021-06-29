package world.gregs.voidps.engine.entity.character.npc

import com.github.michaelbull.logging.InlineLogger
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet
import world.gregs.voidps.cache.definition.data.NPCDefinition
import world.gregs.voidps.engine.entity.*
import world.gregs.voidps.engine.entity.character.IndexAllocator
import world.gregs.voidps.engine.entity.character.update.visual.npc.turn
import world.gregs.voidps.engine.entity.definition.NPCDefinitions
import world.gregs.voidps.engine.entity.list.MAX_NPCS
import world.gregs.voidps.engine.entity.list.PooledMapList
import world.gregs.voidps.engine.event.EventHandlerStore
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.area.Area
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.path.TraversalType
import world.gregs.voidps.engine.path.strat.DistanceTargetStrategy
import world.gregs.voidps.engine.path.strat.RectangleTargetStrategy
import world.gregs.voidps.engine.path.traverse.LargeTraversal
import world.gregs.voidps.engine.path.traverse.MediumTraversal
import world.gregs.voidps.engine.path.traverse.ShoreTraversal
import world.gregs.voidps.engine.path.traverse.SmallTraversal
import java.util.*

data class NPCs(
    private val definitions: NPCDefinitions,
    private val collisions: Collisions,
    private val store: EventHandlerStore
) : PooledMapList<NPC> {
    private val indexer = IndexAllocator(MAX_NPCS)

    override val data: Int2ObjectOpenHashMap<ObjectLinkedOpenHashSet<NPC?>> = Int2ObjectOpenHashMap(MAX_NPCS)
    override val pool: LinkedList<ObjectLinkedOpenHashSet<NPC?>> = LinkedList()
    override val indexed: Array<NPC?> = arrayOfNulls(MAX_NPCS)
    private val logger = InlineLogger()

    fun add(name: String, area: Area, direction: Direction = Direction.NONE, delay: Int = 60): NPC? {
        val def = definitions.get(name)
        val traversal = getTraversal(def)
        val tile = if (area.area <= 0.0) area.random() else area.random(traversal)
        if (tile == null) {
            logger.warn { "No free area found for npc spawn $name $area" }
            return null
        }
        val npc = add(name, tile, direction) ?: return null
        npc["area"] = area
        if (delay >= 0) {
            npc["respawn_delay"] = delay
            npc["respawn_direction"] = direction
        }
        npc.events.emit(Registered)
        return npc
    }

    fun add(name: String, tile: Tile, direction: Direction = Direction.NONE): NPC? {
        val def = definitions.get(name)
        if (def.id == -1) {
            logger.warn { "No npc found for name $name" }
            return null
        }
        val npc = NPC(def.id, tile, getSize(def))
        npc["spawn_tile"] = tile
        store.populate(npc)
        npc.movement.traversal = getTraversal(def)
        val dir = if (direction == Direction.NONE) Direction.all.random() else direction
        npc.interactTarget = if (def.name.contains("banker", true)) {
            DistanceTargetStrategy(1, npc.tile.add(dir.delta))
        } else {
            RectangleTargetStrategy(collisions, npc)
        }
        npc.index = indexer.obtain() ?: return null
        npc.turn(dir.delta.x, dir.delta.y)
        collisions.add(npc)
        super.add(npc)
        return npc
    }

    private fun getTraversal(definition: NPCDefinition) = when {
        definition.name.equals("fishing spot", true) -> ShoreTraversal(collisions)
        definition.size == 1 -> SmallTraversal(TraversalType.Land, true, collisions)
        definition.size == 2 -> MediumTraversal(TraversalType.Land, true, collisions)
        else -> LargeTraversal(TraversalType.Land, true, getSize(definition), collisions)
    }

    private fun getSize(definition: NPCDefinition) = Size(definition.size, definition.size)

    override fun remove(npc: NPC) {
        super.remove(npc)
        collisions.remove(npc)
    }
}