package world.gregs.voidps.tools.map.view.draw

import world.gregs.voidps.tools.map.view.RegionLoader
import java.awt.Color
import java.awt.Graphics
import kotlin.math.max
import kotlin.math.min

class WorldMap(private val view: MapView) {

    val regions = RegionLoader(view)
    private val chunks = false

    private var minRegionX = 0
    private var minRegionY = 0
    private var maxRegionX = 0
    private var maxRegionY = 0

    /**
     * Updates the regions within view removing regions that have left the view
     */
    fun updateView() {
        val minX = minRegionX
        val minY = minRegionY
        val maxX = maxRegionX
        val maxY = maxRegionY
        minRegionX = view.viewToRegionX(view.minX).coerceAtLeast(mapRegionMinX)
        minRegionY = view.viewToRegionY(view.minY).coerceAtLeast(mapRegionMinY)
        maxRegionX = view.viewToRegionX(view.maxX).coerceAtMost(mapRegionMaxX)
        maxRegionY = view.viewToRegionY(view.maxY).coerceAtMost(mapRegionMaxY)

        if (minRegionX > minX) {
            regions.remove(minX until min(minRegionX, maxX), minY..maxY)
        }
        if (minRegionY > minY) {
            regions.remove(minX..maxX, minY until min(minRegionY, maxY))
        }
        if (maxRegionX < maxX) {
            regions.remove(max(maxRegionX + 1, minX)..maxX, minY..maxY)
        }
        if (maxRegionY < maxY) {
            regions.remove(minX..maxX, max(maxRegionY + 1, minY)..maxY)
        }
    }

    fun draw(g: Graphics) {
        for (regionX in minRegionX..maxRegionX) {
            for (regionY in minRegionY..maxRegionY) {
                val mapY = view.regionToMapY(regionY) + 1
                val viewX = view.regionToViewX(regionX)
                val viewY = view.imageToViewY(view.mapToImageY(mapY))
                val region = regions.getRegion(regionX, flipRegionY(regionY), view.plane)
                g.drawImage(region, viewX, viewY, view.regionToImageX(1), view.regionToImageY(1), null)

                // Chunks
                if (chunks) {
                    g.color = Color.ORANGE
                    for (i in 0..64 step 8) {
                        g.drawLine(viewX + view.mapToImageX(i), viewY, viewX + view.mapToImageX(i), view.mapToViewY(mapY + 64))
                        g.drawLine(viewX, viewY + view.mapToImageY(i), viewX + view.mapToImageX(64), viewY + view.mapToImageY(i))
                    }
                }
                // Regions
                g.color = Color.MAGENTA
                g.drawLine(viewX, viewY, viewX, view.mapToViewY(mapY + 64))
                g.drawLine(viewX, viewY, viewX + view.mapToImageX(64), viewY)
            }
        }
    }

    companion object {
        private const val mapRegionMinX = 0
        private const val mapRegionMinY = 0
        private const val mapRegionMaxX = 256
        private const val mapRegionMaxY = 256

        fun flipRegionY(regionY: Int) = mapRegionMaxY - regionY
    }
}