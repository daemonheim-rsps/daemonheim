import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.action.action
import world.gregs.voidps.engine.client.ui.awaitDialogues
import world.gregs.voidps.engine.client.variable.getVar
import world.gregs.voidps.engine.entity.*
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.Died
import world.gregs.voidps.engine.entity.character.move.cantReach
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCClick
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.visual.watch
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.path.strat.CombatTargetStrategy
import world.gregs.voidps.engine.path.strat.CombatTargetStrategy.Companion.isWithinAttackDistance
import world.gregs.voidps.network.encode.message
import world.gregs.voidps.world.interact.entity.combat.CombatHit
import world.gregs.voidps.world.interact.entity.combat.CombatSwing
import world.gregs.voidps.world.interact.entity.combat.attackStyle
import world.gregs.voidps.world.interact.entity.combat.canAttack

on<NPCClick>({ option == "Attack" }) { player: Player ->
    cancel = true
    player.attack(npc)
}

on<CombatHit>({ type != "poison" && (it is Player && it.getVar("auto_retaliate", false) || it is NPC) }) { character: Character ->
    if (character.action.type != ActionType.Combat) {
        character.attack(source)
    }
}

fun Character.attack(target: Character) {
    val source = this
    action(ActionType.Combat) {
        source["target"] = target
        val deathHandler = target.events.on<Character, Died> {
            cancel(ActionType.Combat)
        }
        try {
            watch(target)
            while (isActive && (source is NPC || source is Player && source.awaitDialogues())) {
                if (!withinRange(source, target)) {
                    delay()
                    continue
                }
                if (source.remaining("skilling_delay") > 0L) {
                    delay()
                    continue
                }
                if (!canAttack(source, target)) {
                    break
                }
                val swing = CombatSwing(target)
                events.emit(swing)
                val nextDelay = swing.delay
                if (nextDelay == null || nextDelay < 0) {
                    break
                }
                start("skilling_delay", nextDelay, quiet = true)
            }
        } finally {
            clear("target")
            target.events.remove(deathHandler)
            watch(null)
        }
    }
}

fun withinRange(source: Character, target: Character): Boolean {
    val range = if (source is NPC) source.def["attack_range", 1] else source["attack_range", 1]
    val maxDistance = (range + if (source.attackStyle == "long_range") 2 else 0).coerceAtMost(10)
    val closeCombat = maxDistance == 1
    if (!isWithinAttackDistance(source, target, maxDistance + if (source.movement.moving) 1 else 0, closeCombat)) {
        if (source.movement.steps.isNotEmpty()) {
            return false
        }
        val strategy = CombatTargetStrategy(target, maxDistance, closeCombat)
        if (source is Player) {
            source.dialogues.clear()
        }
        source.movement.strategy = strategy
        source.movement.action = {
            if (source is Player && (source.cantReach(strategy) || source.movement.result == null)) {
                source.message("You can't reach that.")
            }
        }
        return false
    }
    return true
}