package world.gregs.voidps.tools.map.render.draw

import world.gregs.voidps.buffer.read.BufferReader
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.Indices.DEFAULTS
import world.gregs.voidps.cache.Indices.WORLD_MAP
import world.gregs.voidps.cache.config.data.WorldMapInfoDefinition
import world.gregs.voidps.cache.config.decoder.WorldMapInfoDecoder
import world.gregs.voidps.cache.definition.data.IndexedSprite
import world.gregs.voidps.cache.definition.data.MapObject
import world.gregs.voidps.cache.definition.decoder.ObjectDecoder
import world.gregs.voidps.cache.definition.decoder.SpriteDecoder
import world.gregs.voidps.cache.definition.decoder.WorldMapDetailsDecoder
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.region.Region
import java.awt.Graphics
import java.awt.image.BufferedImage

class MinimapIconPainter(
    private val objectDefinitions: ObjectDecoder,
    private val worldMapDefinitions: WorldMapDetailsDecoder,
    private val worldMapInfoDefinitions: WorldMapInfoDecoder,
    private val spriteDefinitions: SpriteDecoder
) {

    data class MapIcon(val x: Int, val y: Int, val plane: Int, val bi: BufferedImage)

    private val nameAreas = mutableMapOf<Int, MutableList<WorldMapInfoDefinition>>()

    fun startup(cache: Cache) {
        val stringId = "${worldMapDefinitions.get(DEFAULTS).map}_staticelements"

        val archiveId = cache.getArchiveId(WORLD_MAP, stringId)
        var length = cache.archiveCount(WORLD_MAP, archiveId)
        val positions = IntArray(length)
        val ids = IntArray(length)

        val aBoolean1313 = false
        var counter = 0
        var index = 0
        while (length > counter) {
            val file = cache.getFile(WORLD_MAP, archiveId, index++) ?: continue
            val buffer = BufferReader(file)
            val position = buffer.readInt()
            val id = buffer.readShort()
            val skip = buffer.readUnsignedByte()
            if(skip == 1) {
                val x = position shr 14 and 0x3fff
                val y = position and 0x3fff
                val plane = position shr 28 and 0x3
//                println("Skip $x $y $plane")
            }
            if (aBoolean1313 && skip == 1) {
                length--
            } else {
                positions[counter] = position
                ids[counter] = id
                counter++
            }
        }

//        loadAreaNames(length, positions, ids)
    }

    private fun loadAreaNames(length: Int, positions: IntArray, ids: IntArray) {
        val sections = worldMapDefinitions.get(28).sections ?: return
        for (i in 0 until length) {
            val x = positions[i] shr 14 and 0x3fff
            val y = positions[i] and 0x3fff
            val plane = positions[i] shr 28 and 0x3
            for (it in sections) {
                if (plane == it.plane && it.minX <= x && x <= it.maxX && y >= it.minY && y <= it.maxY) {
                    val key = Tile.getId(x, y, plane)
                    val list = nameAreas.getOrPut(key) { mutableListOf() }
                    list.add(worldMapInfoDefinitions.get(ids[i]))
                    break
                }
            }
        }
    }

    fun paint(g: Graphics, region: Region, plane: Int, objects: Map<Int, List<MapObject>?>) {
        val iconScale = 2
        for (regionX in region.x - 1..region.x + 1) {
            for (regionY in region.y - 1..region.y + 1) {
                val id = Region.getId(regionX, regionY)
                val images = loadIcons(regionX, regionY, objects[id] ?: continue)
                for (x in 0..64) {
                    for (y in 0..64) {
                        val it = images[Tile.getId(x, y, plane)] ?: continue
                        val width = it.bi.width * iconScale
                        val height = it.bi.height * iconScale
                        val regionX = (region.x - 1) * 64
                        val regionY = (region.y - 1) * 64
                        g.drawImage(it.bi, (it.x - regionX) * 4 - width / 2, (it.y - regionY) * 4 + height / 2, width, -height, null)
                    }
                }
            }
        }
    }

    private fun loadIcons(regionX: Int, regionY: Int, objects: List<MapObject>?): Map<Int, MapIcon> {
        val images = mutableMapOf<Int, MapIcon>()
        objects?.forEach {
            val definition = objectDefinitions.getOrNull(it.id) ?: return@forEach
            if (definition.mapDefinitionId != -1) {
                val mapInfo = worldMapInfoDefinitions.get(definition.mapDefinitionId)
                val sprite = mapInfo.toSprite(false)
                if (sprite != null) {
                    val x = regionX * 64 + it.x
                    val y = regionY * 64 + it.y
                    images[Tile.getId(it.x, it.y, it.plane)] = MapIcon(x, y, it.plane, sprite.toBufferedImage())
                }
            }
        }
        return images
    }

    private fun WorldMapInfoDefinition.toSprite(bool: Boolean): IndexedSprite? {
        val i = if (!bool) spriteId else highlightSpriteId
        return if (i > 0) spriteDefinitions.get(i).sprites?.firstOrNull() else null
    }

}