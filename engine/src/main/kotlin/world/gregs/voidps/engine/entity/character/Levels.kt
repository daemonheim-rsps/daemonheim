package world.gregs.voidps.engine.entity.character

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import world.gregs.voidps.engine.entity.character.player.skill.*
import world.gregs.voidps.engine.event.Events
import kotlin.math.max
import kotlin.math.min

class Levels(
    @JsonProperty("levelOffsets")
    val offsets: MutableMap<Skill, Int> = mutableMapOf(),
) {

    interface Level {
        fun getMaxLevel(skill: Skill): Int
    }

    @JsonIgnore
    private lateinit var level: Level

    @JsonIgnore
    private lateinit var events: Events

    fun link(events: Events, level: Level) {
        this.events = events
        this.level = level
    }

    fun get(skill: Skill): Int {
        return getMax(skill) + getOffset(skill)
    }

    fun getMax(skill: Skill): Int {
        return level.getMaxLevel(skill)
    }

    fun getOffset(skill: Skill): Int {
        return offsets[skill] ?: 0
    }

    fun setOffset(skill: Skill, offset: Int) {
        if (offset == 0) {
            clearOffset(skill)
        } else {
            val previous = get(skill)
            offsets[skill] = offset
            notify(skill, previous)
        }
    }

    fun clear() {
        for (skill in offsets.keys) {
            notify(skill, get(skill))
        }
        offsets.clear()
    }

    fun clearOffset(skill: Skill) {
        val previous = get(skill)
        offsets.remove(skill)
        notify(skill, previous)
    }

    fun restore(skill: Skill, amount: Int = 0, multiplier: Double = 0.0) {
        val offset = multiply(getMax(skill), multiplier)
        val boost = calculateAmount(amount, offset)
        val minimumBoost = min(0, getOffset(skill))
        modify(skill, boost, minimumBoost, 0)
    }

    fun boost(skill: Skill, amount: Int = 0, multiplier: Double = 0.0, stack: Boolean = false) {
        val offset = multiply(minimumLevel(skill), multiplier)
        val boost = calculateAmount(amount, offset)
        val maximumBoost = if (stack) min(MAXIMUM_BOOST_LEVEL, getOffset(skill) + boost) else max(getOffset(skill), boost)
        modify(skill, boost, 0, maximumBoost)
    }

    fun drain(skill: Skill, amount: Int = 0, multiplier: Double = 0.0, stack: Boolean = true) {
        val offset = multiply(maximumLevel(skill), multiplier)
        val drain = calculateAmount(amount, offset)
        val minimumDrain = if (stack) max(-getMax(skill), getOffset(skill) - drain) else min(getOffset(skill), -drain)
        modify(skill, -drain, minimumDrain, 0)
    }

    private fun notify(skill: Skill, previous: Int) {
        val level = get(skill)
        events.emit(LevelChanged(skill, previous, level))
    }

    private fun minimumLevel(skill: Skill): Int {
        val currentLevel = get(skill)
        val maxLevel = getMax(skill)
        return min(currentLevel, maxLevel)
    }

    private fun maximumLevel(skill: Skill): Int {
        val currentLevel = get(skill)
        val maxLevel = getMax(skill)
        return max(currentLevel, maxLevel)
    }

    private fun modify(skill: Skill, amount: Int, min: Int, max: Int) {
        val current = getOffset(skill)
        val combined = current + amount
        setOffset(skill, combined.coerceIn(min, max))
    }

    private fun multiply(level: Int, multiplier: Double) = if (multiplier > 0.0) (level * multiplier).toInt() else 0

    private fun calculateAmount(amount: Int, offset: Int) = max(0, amount) + offset

    companion object {
        private const val MAXIMUM_BOOST_LEVEL = 24
    }
}
