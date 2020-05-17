package rs.dusk.engine.view

import rs.dusk.engine.model.entity.index.player.Player
import rs.dusk.engine.model.world.Tile
import rs.dusk.engine.view.ViewportTask.Companion.VIEW_RADIUS
import java.util.*
import kotlin.collections.LinkedHashSet

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 21, 2020
 */
class PlayerTrackingSet(
    val tickMax: Int,
    override val maximum: Int,
    override val radius: Int = VIEW_RADIUS,
    override val add: LinkedHashSet<Player> = LinkedHashSet(),
    override val remove: MutableSet<Player> = mutableSetOf(),
    override val current: MutableSet<Player> = TreeSet(),// Ordered locals
    val local: MutableSet<Player> = mutableSetOf(),// Duplicate of current for O(1) lookup
    val lastSeen: MutableMap<Player, Tile> = mutableMapOf()
) : TrackingSet<Player> {

    override var total: Int = 0

    override fun prep(self: Player?) {
        remove.addAll(current)
        total = 0
        if (self != null) {
            track(self, null)
        }
    }

    override fun update() {
        remove.forEach {
            current.remove(it)
            local.remove(it)
            lastSeen[it] = it.movement.lastTile
        }
        add.forEach {
            local.add(it)
            current.add(it)
        }
        remove.clear()
        add.clear()
        total = current.size
    }

    override fun add(self: Player) {
        current.add(self)
        local.add(self)
    }

    override fun clear() {
        add.clear()
        remove.clear()
        current.clear()
        local.clear()
        total = 0
    }

    override fun track(entity: Player, self: Player?) {
        val visible = remove.remove(entity)
        if (visible) {
            total++
        } else if (self == null || entity != self) {
            if (add.size < tickMax) {
                add.add(entity)
                total++
            }
        }
    }
}