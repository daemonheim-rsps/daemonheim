package rs.dusk.engine.data

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyOrder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.koin.dsl.module
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.engine.entity.character.player.social.socialModule
import rs.dusk.engine.event.eventModule
import rs.dusk.engine.map.collision.collisionModule
import rs.dusk.engine.path.strat.FollowTargetStrategy
import rs.dusk.engine.path.strat.RectangleTargetStrategy
import rs.dusk.engine.script.KoinMock
import rs.dusk.network.codec.game.encode.ContextMenuOptionEncoder

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 03, 2020
 */
internal class PlayerLoaderTest : KoinMock() {

    override val modules = listOf(eventModule, collisionModule, module { single { mockk<ContextMenuOptionEncoder>(relaxed = true) } }, socialModule)


    @Test
    fun `load strategy`() {
        // Given
        val strategy = mockk<StorageStrategy<Player>>(relaxed = true)
        every { strategy.load("test") } returns mockk(relaxed = true)
        val loader = PlayerLoader(mockk(), mockk(), mockk(), mockk(), strategy, mockk(), mockk(), mockk(), mockk(), mockk(), mockk(), mockk(), mockk(), mockk(), mockk(), mockk())
        // When
        loader.loadPlayer("test")
        // Then
        verify { strategy.load("test") }
    }

    @Test
    fun `load empty`() {
        // Given
        val strategy = mockk<StorageStrategy<Player>>(relaxed = true)
        every { strategy.load("test") } returns null
        val loader = PlayerLoader(mockk(), mockk(), mockk(), mockk(), strategy, mockk(), mockk(), mockk(), mockk(), mockk(), mockk(), mockk(), mockk(), mockk(), mockk(), mockk())
        // When
        val result = loader.loadPlayer("test")
        // Then
        assertEquals(-1, result.id)
        assertEquals(-1, result.index)
        assertEquals(0, result.tile.x)
        assertEquals(0, result.tile.y)
        assertEquals(0, result.tile.plane)
        verifyOrder {
            strategy.load("test")
        }
    }

    @Test
    fun `load default`() {
        setProperty("homeX", 100)
        setProperty("homeY", 100)
        setProperty("homePlane", 1)
        // Given
        val strategy = mockk<StorageStrategy<Player>>(relaxed = true)
        every { strategy.load("test") } returns null
        val loader = PlayerLoader(mockk(), mockk(), mockk(), mockk(), strategy, mockk(), mockk(), mockk(), mockk(), mockk(), mockk(), mockk(), mockk(), mockk(), mockk(), mockk())
        // When
        val result = loader.loadPlayer("test")
        // Then
        assertEquals(-1, result.id)
        assertEquals(-1, result.index)
        assertEquals(100, result.tile.x)
        assertEquals(100, result.tile.y)
        assertEquals(1, result.tile.plane)
        assert(result.interactTarget is RectangleTargetStrategy)
        assertEquals(FollowTargetStrategy(result), result.followTarget)
        verifyOrder {
            strategy.load("test")
        }
    }
}