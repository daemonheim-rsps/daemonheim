package world.gregs.voidps.engine.entity.obj

import com.github.michaelbull.logging.InlineLogger
import org.koin.dsl.module
import world.gregs.voidps.engine.action.Scheduler
import world.gregs.voidps.engine.action.delay
import world.gregs.voidps.engine.data.file.FileLoader
import world.gregs.voidps.engine.entity.*
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.chunk.ChunkBatches
import world.gregs.voidps.engine.map.chunk.ChunkUpdate
import world.gregs.voidps.engine.map.collision.GameObjectCollision
import world.gregs.voidps.engine.map.region.Region
import world.gregs.voidps.engine.timedLoad
import world.gregs.voidps.network.encode.addObject
import world.gregs.voidps.network.encode.removeObject
import world.gregs.voidps.utility.get
import world.gregs.voidps.utility.getProperty

val customObjectModule = module {
    single(createdAtStart = true) {
        CustomObjects(get(), get(), get(), get(), get())
    }
}

class CustomObjects(
    private val objects: Objects,
    private val scheduler: Scheduler,
    private val batches: ChunkBatches,
    private val factory: GameObjectFactory,
    private val collision: GameObjectCollision,
) {
    private val logger = InlineLogger()
    lateinit var spawns: Map<Region, List<GameObject>>

    fun load(region: Region) {
        val spawns = spawns[region] ?: return
        spawns.forEach { gameObject ->
            spawnObject(gameObject.id, gameObject.tile, gameObject.type, gameObject.rotation)
        }
    }

    /**
     * Spawns an object, optionally removing after a set time
     */
    fun spawn(
        id: Int,
        tile: Tile,
        type: Int,
        rotation: Int,
        ticks: Int = -1,
        owner: String? = null
    ) {
        val gameObject = factory.spawn(id, tile, type, rotation, owner)
        spawnCustom(gameObject)
        // Revert
        if (ticks >= 0) {
            objects.setTimer(gameObject, scheduler.launch {
                try {
                    delay(ticks)
                } finally {
                    despawn(gameObject)
                }
            })
        }
    }

    private fun spawnCustom(gameObject: GameObject) {
        if (gameObject.id == -1) {
            val removal = objects[gameObject.tile].firstOrNull { it.tile == gameObject.tile && it.type == gameObject.type && it.rotation == gameObject.rotation }
            if (removal == null) {
                logger.debug { "Cannot find object to despawn $gameObject" }
            } else {
                despawn(removal)
            }
        } else {
            respawn(gameObject)
        }
    }

    private fun despawn(gameObject: GameObject) {
        val update = removeObject(gameObject)
        batches.update(gameObject.tile.chunk, update)
        remove(gameObject, update)
        collision.modifyCollision(gameObject, GameObjectCollision.REMOVE_MASK)
        gameObject.events.emit(Unregistered)
    }

    private fun remove(gameObject: GameObject, update: ChunkUpdate) {
        gameObject.remove<ChunkUpdate>("update")?.let {
            batches.removeInitial(gameObject.tile.chunk, it)
        }
        if (objects.isOriginal(gameObject)) {
            batches.addInitial(gameObject.tile.chunk, update)
            gameObject["update"] = update
        }
        objects.removeTemp(gameObject)
    }

    private fun respawn(gameObject: GameObject) {
        val update = addObject(gameObject)
        batches.update(gameObject.tile.chunk, update)
        collision.modifyCollision(gameObject, GameObjectCollision.ADD_MASK)
        gameObject.events.emit(Registered)
    }

    private fun add(gameObject: GameObject, update: ChunkUpdate) {
        gameObject.remove<ChunkUpdate>("update")?.let {
            batches.removeInitial(gameObject.tile.chunk, it)
        }
        if (!objects.isOriginal(gameObject)) {
            batches.addInitial(gameObject.tile.chunk, update)
            gameObject["update"] = update
        }
        objects.addTemp(gameObject)
    }

    /**
     * Removes an object, optionally reverting after a set time
     */
    fun remove(
        original: GameObject,
        ticks: Int = -1,
        owner: String? = null
    ) {
        despawn(original)
        // Revert
        if (ticks >= 0) {
            objects.setTimer(original, scheduler.launch {
                try {
                    delay(ticks)
                } finally {
                    respawn(original)
                }
            })
        }
    }

    /**
     * Replaces one object with another, optionally reverting after a set time
     */
    fun replace(
        original: GameObject,
        id: Int,
        tile: Tile,
        type: Int = 0,
        rotation: Int = 0,
        ticks: Int = -1,
        owner: String? = null
    ) {
        val replacement = factory.spawn(id, tile, type, rotation, owner)

        switch(original, replacement)
        // Revert
        if (ticks >= 0) {
            objects.setTimer(replacement, scheduler.launch {
                try {
                    delay(ticks)
                } finally {
                    switch(replacement, original)
                }
            })
        }
    }

    /**
     * Replaces two objects, linking them to the same job so both revert after timeout
     */
    fun replace(
        firstOriginal: GameObject,
        firstReplacement: Int,
        firstTile: Tile,
        firstRotation: Int,
        secondOriginal: GameObject,
        secondReplacement: Int,
        secondTile: Tile,
        secondRotation: Int,
        ticks: Int,
        firstOwner: String? = null,
        secondOwner: String? = null
    ) {
        val firstReplacement = factory.spawn(firstReplacement, firstTile, firstOriginal.type, firstRotation, firstOwner)
        val secondReplacement = factory.spawn(secondReplacement, secondTile, secondOriginal.type, secondRotation, secondOwner)
        switch(firstOriginal, firstReplacement)
        switch(secondOriginal, secondReplacement)
        // Revert
        if (ticks >= 0) {
            val job = scheduler.launch {
                try {
                    delay(ticks)
                } finally {
                    switch(firstReplacement, firstOriginal)
                    switch(secondReplacement, secondOriginal)
                }
            }
            objects.setTimer(firstReplacement, job)
            objects.setTimer(secondReplacement, job)
        }

    }

    private fun switch(original: GameObject, replacement: GameObject) {
        val removeUpdate = removeObject(original)
        if (original.tile != replacement.tile) {
            batches.update(original.tile.chunk, removeUpdate)
        }
        val addUpdate = addObject(replacement)
        batches.update(replacement.tile.chunk, addUpdate)
        remove(original, removeUpdate)
        add(replacement, addUpdate)
        collision.modifyCollision(original, GameObjectCollision.REMOVE_MASK)
        original.events.emit(Unregistered)
        collision.modifyCollision(replacement, GameObjectCollision.ADD_MASK)
        replacement.events.emit(Registered)
    }

    init {
        load()
    }

    fun load() = timedLoad("object spawn") {
        val data: Array<GameObject> = get<FileLoader>().load(getProperty("objectsPath"))
        this.spawns = data.groupBy { obj -> obj.tile.region }
        data.size
    }
}

/**
 * Removes an existing map [GameObject].
 * The removal can be permanent if [ticks] is -1 or temporary
 * [owner] is also optional to allow for an object to removed just for one player.
 */
fun GameObject.remove(ticks: Int = -1, owner: String? = null) {
    get<CustomObjects>().remove(this, ticks, owner)
}

/**
 * Replaces an existing map objects with [id] [tile] [type] and [rotation] provided.
 * The replacement can be permanent if [ticks] is -1 or temporary
 * [owner] is also optional to allow for an object to replaced just for one player.
 */
fun GameObject.replace(id: Int, tile: Tile = this.tile, type: Int = this.type, rotation: Int = this.rotation, ticks: Int = -1, owner: String? = null) {
    get<CustomObjects>().replace(this, id, tile, type, rotation, ticks, owner)
}

/**
 * Replaces two existing map objects with replacements provided.
 * The replacements can be permanent if [ticks] is -1 or temporary
 * [owner] is also optional to allow for objects to replaced just for one player.
 */
fun replaceObjectPair(
    firstOriginal: GameObject,
    firstReplacement: Int,
    firstTile: Tile,
    firstRotation: Int,
    secondOriginal: GameObject,
    secondReplacement: Int,
    secondTile: Tile,
    secondRotation: Int,
    ticks: Int,
    owner: String? = null
) = get<CustomObjects>().replace(
    firstOriginal,
    firstReplacement,
    firstTile,
    firstRotation,
    secondOriginal,
    secondReplacement,
    secondTile,
    secondRotation,
    ticks,
    owner
)

/**
 * Spawns a temporary object with [id] [tile] [type] and [rotation] provided.
 * Can be removed after [ticks] or -1 for permanent (until server restarts or removed)
 */
fun spawnObject(
    id: Int,
    tile: Tile,
    type: Int,
    rotation: Int,
    ticks: Int = -1,
    owner: String? = null
) = get<CustomObjects>().spawn(
    id,
    tile,
    type,
    rotation,
    ticks,
    owner
)