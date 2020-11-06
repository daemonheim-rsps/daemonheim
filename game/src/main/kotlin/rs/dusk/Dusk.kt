package rs.dusk

import com.github.michaelbull.logging.InlineLogger
import org.koin.core.context.startKoin
import org.koin.logger.slf4jLogger
import rs.dusk.engine.GameLoop
import rs.dusk.engine.action.schedulerModule
import rs.dusk.engine.client.cacheConfigModule
import rs.dusk.engine.client.cacheDefinitionModule
import rs.dusk.engine.client.cacheModule
import rs.dusk.engine.client.clientSessionModule
import rs.dusk.engine.client.ui.detail.interfaceModule
import rs.dusk.engine.client.update.updatingTasksModule
import rs.dusk.engine.client.variable.variablesModule
import rs.dusk.engine.data.file.fileLoaderModule
import rs.dusk.engine.data.file.ymlPlayerModule
import rs.dusk.engine.data.playerLoaderModule
import rs.dusk.engine.entity.character.player.social.socialModule
import rs.dusk.engine.entity.character.player.social.FriendsChatChannels
import rs.dusk.engine.entity.character.player.social.socialModule
import rs.dusk.engine.entity.character.update.visualUpdatingModule
import rs.dusk.engine.entity.definition.detailsModule
import rs.dusk.engine.entity.list.entityListModule
import rs.dusk.engine.entity.obj.objectFactoryModule
import rs.dusk.engine.event.EventBus
import rs.dusk.engine.event.eventModule
import rs.dusk.engine.map.area.lineOfSightModule
import rs.dusk.engine.map.chunk.batchedChunkModule
import rs.dusk.engine.map.chunk.instanceModule
import rs.dusk.engine.map.collision.collisionModule
import rs.dusk.engine.map.instance.instancePoolModule
import rs.dusk.engine.map.region.regionModule
import rs.dusk.engine.map.region.xteaModule
import rs.dusk.engine.path.pathFindModule
import rs.dusk.engine.storage.databaseModule
import rs.dusk.engine.task.SyncTask
import rs.dusk.engine.task.TaskExecutor
import rs.dusk.engine.task.executorModule
import rs.dusk.handle.*
import rs.dusk.network.GameServer
import rs.dusk.network.codec.game.GameCodec
import rs.dusk.network.codec.game.GameOpcodes
import rs.dusk.network.codec.game.gameCodec
import rs.dusk.network.codec.login.LoginCodec
import rs.dusk.network.networkCodecs
import rs.dusk.script.scriptModule
import rs.dusk.utility.get
import rs.dusk.utility.getIntProperty
import rs.dusk.utility.getProperty
import rs.dusk.world.interact.entity.player.spawn.login.loginQueueModule
import rs.dusk.world.interact.entity.player.spawn.logout.logoutModule
import java.util.concurrent.Executors

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 18, 2020
 */
object Dusk {

    const val name = "Dusk"
    private val logger = InlineLogger()

    @JvmStatic
    fun main(args: Array<String>) {
        val startTime = System.currentTimeMillis()
        preload()

        val server = GameServer(getIntProperty("port"))
        val bus: EventBus = get()
        val executor: TaskExecutor = get()
        val service = Executors.newSingleThreadScheduledExecutor()
        val start: SyncTask = get()
        val engine = GameLoop(bus, executor, service)

        server.run()
        engine.setup(start)
        engine.start()
        logger.info { "${getProperty("name")} loaded in ${System.currentTimeMillis() - startTime}ms" }

		val friendsChatChannels: FriendsChatChannels = get()

		friendsChatChannels.init()
    }

    private fun preload() {
        startKoin {
            slf4jLogger()
            modules(
                eventModule,
                cacheModule,
                fileLoaderModule,
                ymlPlayerModule,
                entityListModule,
                scriptModule,
                clientSessionModule,
                networkCodecs,
                gameCodec,
                playerLoaderModule,
                xteaModule,
                visualUpdatingModule,
                updatingTasksModule,
                loginQueueModule,
                regionModule,
                collisionModule,
                cacheDefinitionModule,
                cacheConfigModule,
                pathFindModule,
                schedulerModule,
                batchedChunkModule,
                executorModule,
                interfaceModule,
                variablesModule,
                instanceModule,
                instancePoolModule,
                detailsModule,
                databaseModule,
                logoutModule,
                objectFactoryModule,
				lineOfSightModule,
				socialModule
			)
			fileProperties("/game.properties")
			fileProperties("/private.properties")
		}
	registerGameHandlers()
        registerLoginHandlers()
    }

    private fun registerLoginHandlers() {
        val login: LoginCodec = get()
        login.registerHandler(GameOpcodes.GAME_LOGIN, GameLoginHandler())
    }

    private fun registerGameHandlers() {
        val game: GameCodec = get()
        game.registerHandler(GameOpcodes.CONSOLE_COMMAND, ConsoleCommandHandler())
        game.registerHandler(GameOpcodes.DIALOGUE_CONTINUE, DialogueContinueHandler())
        game.registerHandler(GameOpcodes.FLOOR_ITEM_OPTION_1, FloorItemOptionHandler())
        game.registerHandler(GameOpcodes.FLOOR_ITEM_OPTION_2, FloorItemOptionHandler())
        game.registerHandler(GameOpcodes.FLOOR_ITEM_OPTION_4, FloorItemOptionHandler())
        game.registerHandler(GameOpcodes.FLOOR_ITEM_OPTION_5, FloorItemOptionHandler())
        game.registerHandler(GameOpcodes.FLOOR_ITEM_OPTION_6, FloorItemOptionHandler())
        game.registerHandler(GameOpcodes.ENTER_INTEGER, IntEntryHandler())
        game.registerHandler(GameOpcodes.SCREEN_CLOSE, InterfaceClosedHandler())
        game.registerHandler(GameOpcodes.INTERFACE_OPTION_1, InterfaceOptionHandler())
        game.registerHandler(GameOpcodes.INTERFACE_OPTION_2, InterfaceOptionHandler())
        game.registerHandler(GameOpcodes.INTERFACE_OPTION_3, InterfaceOptionHandler())
        game.registerHandler(GameOpcodes.INTERFACE_OPTION_4, InterfaceOptionHandler())
        game.registerHandler(GameOpcodes.INTERFACE_OPTION_5, InterfaceOptionHandler())
        game.registerHandler(GameOpcodes.INTERFACE_OPTION_6, InterfaceOptionHandler())
        game.registerHandler(GameOpcodes.INTERFACE_OPTION_7, InterfaceOptionHandler())
        game.registerHandler(GameOpcodes.INTERFACE_OPTION_8, InterfaceOptionHandler())
        game.registerHandler(GameOpcodes.INTERFACE_OPTION_9, InterfaceOptionHandler())
        game.registerHandler(GameOpcodes.INTERFACE_OPTION_10, InterfaceOptionHandler())
        game.registerHandler(GameOpcodes.SWITCH_INTERFACE_COMPONENTS, InterfaceSwitchHandler())
        game.registerHandler(GameOpcodes.NPC_OPTION_1, NPCOptionHandler())
        game.registerHandler(GameOpcodes.NPC_OPTION_2, NPCOptionHandler())
        game.registerHandler(GameOpcodes.NPC_OPTION_3, NPCOptionHandler())
        game.registerHandler(GameOpcodes.NPC_OPTION_4, NPCOptionHandler())
        game.registerHandler(GameOpcodes.NPC_OPTION_5, NPCOptionHandler())
        game.registerHandler(GameOpcodes.NPC_OPTION_6, NPCOptionHandler())
        game.registerHandler(GameOpcodes.OBJECT_OPTION_1, ObjectOptionHandler())
        game.registerHandler(GameOpcodes.OBJECT_OPTION_2, ObjectOptionHandler())
        game.registerHandler(GameOpcodes.OBJECT_OPTION_3, ObjectOptionHandler())
        game.registerHandler(GameOpcodes.OBJECT_OPTION_4, ObjectOptionHandler())
        game.registerHandler(GameOpcodes.OBJECT_OPTION_5, ObjectOptionHandler())
        game.registerHandler(GameOpcodes.OBJECT_OPTION_6, ObjectOptionHandler())
        game.registerHandler(GameOpcodes.PLAYER_OPTION_1, PlayerOptionHandler())
        game.registerHandler(GameOpcodes.PLAYER_OPTION_2, PlayerOptionHandler())
        game.registerHandler(GameOpcodes.PLAYER_OPTION_3, PlayerOptionHandler())
        game.registerHandler(GameOpcodes.PLAYER_OPTION_4, PlayerOptionHandler())
        game.registerHandler(GameOpcodes.PLAYER_OPTION_5, PlayerOptionHandler())
        game.registerHandler(GameOpcodes.PLAYER_OPTION_6, PlayerOptionHandler())
        game.registerHandler(GameOpcodes.PLAYER_OPTION_7, PlayerOptionHandler())
        game.registerHandler(GameOpcodes.PLAYER_OPTION_8, PlayerOptionHandler())
        game.registerHandler(GameOpcodes.PLAYER_OPTION_9, PlayerOptionHandler())
        game.registerHandler(GameOpcodes.PLAYER_OPTION_10, PlayerOptionHandler())
        game.registerHandler(GameOpcodes.DONE_LOADING_REGION, RegionLoadedHandler())
        game.registerHandler(GameOpcodes.SCREEN_CHANGE, ScreenChangeHandler())
        game.registerHandler(GameOpcodes.STRING_ENTRY, StringEntryHandler())
        game.registerHandler(GameOpcodes.WALK, WalkMapHandler())
        game.registerHandler(GameOpcodes.MINI_MAP_WALK, WalkMiniMapHandler())
    }
}