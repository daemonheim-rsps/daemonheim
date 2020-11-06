package rs.dusk.engine.data.sql

import com.github.michaelbull.logging.InlineLogger
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.koin.dsl.module
import rs.dusk.engine.data.StorageStrategy
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.engine.entity.character.player.PlayerDetails
import rs.dusk.engine.entity.character.player.social.Names
import rs.dusk.engine.entity.character.player.social.RelationshipManager
import rs.dusk.engine.map.Tile
import rs.dusk.utility.DetailsTable
import rs.dusk.utility.PlayersTable

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 03, 2020
 */
class SQLPlayerStorage(url: String, user: String, password: String) : StorageStrategy<Player> {

    private val logger = InlineLogger()

    init {
        Database.connect(url = url, driver = "org.postgresql.Driver", user = user, password = password)
        logger.info { "Database connection established." }
    }

    override fun load(name: String): Player? {
        return transaction {
            val details = DetailsTable.select { DetailsTable.username eq name }.firstOrNull()
            if (details == null) {
                return@transaction null
            } else {
                val playerId = details[DetailsTable.id].value
                val player = PlayersTable.select { PlayersTable.id eq playerId }.firstOrNull() ?: throw IllegalStateException("Unable to find player $name")
                val x = player[PlayersTable.x]
                val y = player[PlayersTable.y]
                val plane = player[PlayersTable.plane]
                val tile = Tile(x, y, plane)
                val names = Names(name)
                Player(id = playerId, tile = tile,
                    details = PlayerDetails(names, 2),
                    names = names,
                    relations = RelationshipManager().create(names)
                )
            }
        }
    }

    override fun save(name: String, data: Player) {
        PlayersTable.update({ PlayersTable.id eq data.id }) {
            it[x] = data.tile.x
            it[y] = data.tile.y
            it[plane] = data.tile.plane
        }
    }

}

@Suppress("USELESS_CAST")
val sqlPlayerModule = module {
    single { SQLPlayerStorage(getProperty("databaseUrl"), getProperty("databaseUser"), getProperty("databasePassword")) as StorageStrategy<Player> }
}
