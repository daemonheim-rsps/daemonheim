package world.gregs.voidps

import kotlinx.coroutines.runBlocking
import world.gregs.voidps.engine.GameLoop
import world.gregs.voidps.engine.action.Scheduler
import world.gregs.voidps.engine.client.update.encode.ForceChatEncoder
import world.gregs.voidps.engine.client.update.encode.WatchEncoder
import world.gregs.voidps.engine.client.update.encode.npc.*
import world.gregs.voidps.engine.client.update.encode.player.*
import world.gregs.voidps.engine.client.update.task.npc.*
import world.gregs.voidps.engine.client.update.task.player.*
import world.gregs.voidps.engine.client.update.task.viewport.ViewportUpdating
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.login.LoginQueue
import world.gregs.voidps.engine.entity.character.update.Visual
import world.gregs.voidps.engine.entity.character.update.VisualEncoder
import world.gregs.voidps.engine.entity.character.update.visual.NPC_FORCE_CHAT_MASK
import world.gregs.voidps.engine.entity.character.update.visual.NPC_WATCH_MASK
import world.gregs.voidps.engine.entity.character.update.visual.PLAYER_FORCE_CHAT_MASK
import world.gregs.voidps.engine.entity.character.update.visual.PLAYER_WATCH_MASK
import world.gregs.voidps.engine.entity.character.update.visual.npc.TRANSFORM_MASK
import world.gregs.voidps.engine.entity.character.update.visual.npc.TURN_MASK
import world.gregs.voidps.engine.entity.character.update.visual.player.APPEARANCE_MASK
import world.gregs.voidps.engine.entity.character.update.visual.player.FACE_DIRECTION_MASK
import world.gregs.voidps.engine.entity.character.update.visual.player.MOVEMENT_TYPE_MASK
import world.gregs.voidps.engine.entity.character.update.visual.player.TEMPORARY_MOVE_TYPE_MASK
import world.gregs.voidps.engine.map.chunk.ChunkBatches
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.path.PathFinder
import world.gregs.voidps.engine.tick.AiTick
import world.gregs.voidps.engine.tick.Tick
import world.gregs.voidps.network.InstructionHandler
import world.gregs.voidps.network.InstructionTask

fun getTickStages(
    players: Players,
    npcs: NPCs,
    loginQueue: LoginQueue,
    batches: ChunkBatches,
    scheduler: Scheduler,
    pathFinder: PathFinder,
    collisions: Collisions
) = listOf(
    InstructionTask(players, InstructionHandler()),
    // Connections/Tick Input
    loginQueue,
    // Tick
    GameTick(scheduler),
    PlayerPathTask(players, pathFinder),
    NPCPathTask(npcs, pathFinder),
    PlayerMovementCallbackTask(players),
    PlayerMovementTask(players, collisions),
    NPCMovementTask(npcs, collisions),
    // Update
    batches,
    ViewportUpdating(),
    PlayerVisualsTask(players, playerVisualEncoders(), defaultPlayerVisuals()),
    NPCVisualsTask(npcs, npcVisualEncoders(), defaultNpcVisuals()),
    PlayerChangeTask(players),
    NPCChangeTask(npcs),
    PlayerUpdateTask(players),
    NPCUpdateTask(players),
    PlayerPostUpdateTask(players),
    NPCPostUpdateTask(npcs),
    AiTick()
)

private class GameTick(private val scheduler: Scheduler): Runnable {
    override fun run() {
        runBlocking {
            scheduler.tick()
        }
        GameLoop.flow.tryEmit(GameLoop.tick)
        World.events.emit(Tick(GameLoop.tick))
    }
}

private class AiTick: Runnable {
    override fun run() {
        World.events.emit(AiTick)
    }
}

private fun playerVisualEncoders() = castOf(
    WatchEncoder(PLAYER_WATCH_MASK),
    PlayerTimeBarEncoder(),
    ForceChatEncoder(PLAYER_FORCE_CHAT_MASK),
    PlayerHitsEncoder(),
    FaceEncoder(),
    PlayerForceMovementEncoder(),
    PlayerSecondaryGraphicEncoder(),
    PlayerColourOverlayEncoder(),
    TemporaryMoveTypeEncoder(),
    PlayerPrimaryGraphicEncoder(),
    PlayerAnimationEncoder(),
    AppearanceEncoder(),
    MovementTypeEncoder()
)

private fun defaultPlayerVisuals() = intArrayOf(
    FACE_DIRECTION_MASK,
    TEMPORARY_MOVE_TYPE_MASK,
    APPEARANCE_MASK,
    MOVEMENT_TYPE_MASK,
)

private fun npcVisualEncoders() = castOf(
    TransformEncoder(),
    NPCAnimationEncoder(),
    NPCPrimaryGraphicEncoder(),
    TurnEncoder(),
    NPCForceMovementEncoder(),
    NPCColourOverlayEncoder(),
    NPCHitsEncoder(),
    WatchEncoder(NPC_WATCH_MASK),
    ForceChatEncoder(NPC_FORCE_CHAT_MASK),
    NPCTimeBarEncoder(),
    NPCSecondaryGraphicEncoder()
)

private fun defaultNpcVisuals() = intArrayOf(
    TRANSFORM_MASK,
    TURN_MASK
)

@Suppress("UNCHECKED_CAST")
private fun castOf(vararg encoders: VisualEncoder<out Visual>) = encoders
    .map { it as VisualEncoder<Visual> }
    .toTypedArray()