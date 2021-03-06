package world.gregs.voidps.tools.map.obj

import org.koin.core.context.startKoin
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.definition.data.MapObject
import world.gregs.voidps.cache.definition.decoder.MapDecoder
import world.gregs.voidps.engine.client.cacheDefinitionModule
import world.gregs.voidps.engine.client.cacheModule
import world.gregs.voidps.engine.map.region.Region
import world.gregs.voidps.engine.map.region.regionModule
import world.gregs.voidps.engine.map.region.xteaModule

object ObjectUsageFinder {

    @JvmStatic
    fun main(args: Array<String>) {
        val koin = startKoin {
            fileProperties("/tool.properties")
            modules(cacheModule, cacheDefinitionModule, regionModule, xteaModule)
        }.koin
        val cache: Cache = koin.get()
        val mapDecoder: MapDecoder = koin.get()
        val objects = mutableMapOf<Region, List<MapObject>>()
        for (regionX in 0 until 256) {
            for (regionY in 0 until 256) {
                cache.getFile(5, "m${regionX}_${regionY}") ?: continue
                val region = Region(regionX, regionY)
                val def = mapDecoder.getOrNull(region.id) ?: continue
                objects[region] = def.objects
            }
        }

        val objectId = 33220
        for ((region, list) in objects) {
            val obj = list.firstOrNull { it.id == objectId } ?: continue
            println("Found in region ${region.id} ${region.tile.x + obj.x}, ${region.tile.y + obj.y}, ${obj.plane}")
        }
    }
}