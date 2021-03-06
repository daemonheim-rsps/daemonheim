package world.gregs.voidps.cache.config.decoder

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.Configs.MAP_SCENES
import world.gregs.voidps.cache.config.ConfigDecoder
import world.gregs.voidps.cache.config.data.MapSceneDefinition

class MapSceneDecoder(cache: Cache) : ConfigDecoder<MapSceneDefinition>(cache, MAP_SCENES) {

    override fun create() = MapSceneDefinition()

    override fun MapSceneDefinition.read(opcode: Int, buffer: Reader) {
        when (opcode) {
            1 -> sprite = buffer.readShort()
            2 -> colour = buffer.readUnsignedMedium()
            3 -> aBoolean1741 = true
            4 -> sprite = -1
        }
    }
}