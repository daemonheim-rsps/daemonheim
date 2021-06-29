package world.gregs.voidps.engine.path.strat

import world.gregs.voidps.engine.entity.Size
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.map.Distance
import world.gregs.voidps.engine.map.Distance.getNearest
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.path.algorithm.BresenhamsLine
import world.gregs.voidps.utility.get

/**
 * Checks if within distance of a target
 */
data class CombatTargetStrategy(
    private val target: Character,
    private var attackDistance: Int,
    private var closeCombat: Boolean
) : TileTargetStrategy {

    override val tile: Tile
        get() = target.tile
    override val size: Size
        get() = target.size

    override fun reached(currentX: Int, currentY: Int, plane: Int, size: Size): Boolean {
        return isWithinAttackDistance(currentX, currentY, plane, size, target, attackDistance, closeCombat)
    }

    companion object {
        fun isWithinAttackDistance(source: Character, target: Character, attackDistance: Int, walls: Boolean): Boolean {
            return isWithinAttackDistance(source.tile.x, source.tile.y, source.tile.plane, source.size, target, attackDistance, walls)
        }
        /**
         * @param walls ranged, magic or halberds
         */
        fun isWithinAttackDistance(x: Int, y: Int, plane: Int, size: Size, target: Character, attackDistance: Int, walls: Boolean): Boolean {
            // under
            if (((x >= target.tile.x && x < target.tile.x + target.size.width) || (x + size.width > target.tile.x && x + size.width < target.tile.x + target.size.width)) &&
                ((y >= target.tile.y && y < target.tile.y + target.size.height) || (y + size.height > target.tile.y && x + size.height < target.tile.y + target.size.height))) {
                return false
            }
            val targetX = getNearest(target.tile.x, target.size.width, x)
            val targetY = getNearest(target.tile.y, target.size.height, y)
            val sourceX = getNearest(x, size.width, targetX)
            val sourceY = getNearest(y, size.height, targetY)
            if (Distance.chebyshev(sourceX, sourceY, targetX, targetY) > attackDistance) {
                return false
            }
            if (!get<BresenhamsLine>().withinSight(sourceX, sourceY, plane, targetX, targetY, target.tile.plane, walls)) {
                return false
            }
            // diagonal
            if (attackDistance <= 1) {
                if (x >= target.tile.x + target.size.width && y >= target.tile.y + target.size.height) {// ne
                    return false
                }
                if (x + size.width <= target.tile.x && y >= target.tile.y + target.size.height) {// nw
                    return false
                }
                if (x >= target.tile.x + target.size.width && y + size.height <= target.tile.y) {// se
                    return false
                }
                if (x + size.width <= target.tile.x && y + size.height <= target.tile.y) {// sw
                    return false
                }
            }
            return true
        }
    }
}