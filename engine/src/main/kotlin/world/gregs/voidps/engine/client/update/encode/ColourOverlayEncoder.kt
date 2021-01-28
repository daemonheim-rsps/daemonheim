package world.gregs.voidps.engine.client.update.encode

import world.gregs.voidps.buffer.Endian
import world.gregs.voidps.buffer.Modifier
import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.engine.entity.character.update.VisualEncoder
import world.gregs.voidps.engine.entity.character.update.visual.ColourOverlay

/**
 * @author GregHib <greg@gregs.world>
 * @since April 25, 2020
 */
class ColourOverlayEncoder(private val npc: Boolean, mask: Int) : VisualEncoder<ColourOverlay>(mask) {

    override fun encode(writer: Writer, visual: ColourOverlay) {
        val (delay, duration, colour) = visual
        writer.apply {
            val hue = colour and 0xFF
            val saturation = colour shr 8 and 0xFF
            val luminance = colour shr 16 and 0xFF
            val multiplier = colour shr 24 and 0xFF
            writeByte(hue, Modifier.INVERSE)
            writeByte(saturation, if (npc) Modifier.SUBTRACT else Modifier.NONE)
            writeByte(luminance, if (npc) Modifier.INVERSE else Modifier.NONE)
            writeByte(multiplier)
            writeShort(delay, if (npc) Modifier.NONE else Modifier.ADD, if (npc) Endian.BIG else  Endian.LITTLE)
            writeShort(duration, if (npc) Modifier.NONE else Modifier.ADD)
        }
    }

}