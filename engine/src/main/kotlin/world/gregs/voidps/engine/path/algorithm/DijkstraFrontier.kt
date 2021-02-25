package world.gregs.voidps.engine.path.algorithm

import java.util.*

/**
 * All of the graph nodes visited or to be visited by the [Dijkstra] algorithm
 */
class DijkstraFrontier(size: Int) {
    private val queue = PriorityQueue<Weight>()
    private val visited = IntArray(size)

    fun isNotEmpty() = queue.isNotEmpty()

    fun add(index: Int, cost: Int) {
        queue.add(Weight(index, cost))
    }

    fun poll(): Pair<Int, Int> = queue.poll().let { it.index to it.cost }

    fun visit(index: Int, parentIndex: Int, cost: Int) {
        add(index, cost)
        visited[index] = Weight.pack(parentIndex, cost)
    }

    fun cost(index: Int) = Weight.getCost(visited[index])

    fun parent(index: Int) = Weight.getParentIndex(visited[index])

    fun reset(index: Int) {
        queue.clear()
        visited.fill(Weight.pack(0, MAX_COST))
        visited[index] = Weight.pack(index, 0)
        queue.add(Weight(index, 0))
    }

    private inline class Weight(val value: Int) : Comparable<Weight> {

        constructor(index: Int, cost: Int) : this(DijkstraTest.pack(index, cost))

        val index: Int
            get() = getParentIndex(value)
        val cost: Int
            get() = getCost(value)

        override fun compareTo(other: Weight): Int {
            return cost.compareTo(other.cost)
        }

        companion object {
            fun pack(index: Int, cost: Int) = cost or (index shl 16)

            fun getCost(value: Int) = value and 0xffff

            fun getParentIndex(value: Int) = value shr 16
        }
    }

    companion object {
        const val MAX_COST = 0xffff
    }
}