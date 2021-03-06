package world.gregs.voidps.engine.client.update.encode.player

import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.engine.entity.character.update.VisualEncoder
import world.gregs.voidps.engine.entity.character.update.visual.ColourOverlay
import world.gregs.voidps.engine.entity.character.update.visual.PLAYER_COLOUR_OVERLAY_MASK

class PlayerColourOverlayEncoder : VisualEncoder<ColourOverlay>(PLAYER_COLOUR_OVERLAY_MASK) {

    override fun encode(writer: Writer, visual: ColourOverlay) {
        val (delay, duration, colour) = visual
        writer.apply {
            val hue = colour and 0xFF
            val saturation = colour shr 8 and 0xFF
            val luminance = colour shr 16 and 0xFF
            val multiplier = colour shr 24 and 0xFF
            writeByteInverse(hue)
            writeByte(saturation)
            writeByte(luminance)
            writeByte(multiplier)
            writeShortAddLittle(delay)
            writeShortAdd(duration)
        }
    }

}