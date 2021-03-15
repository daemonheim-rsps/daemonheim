package world.gregs.voidps.network

import com.github.michaelbull.logging.InlineLogger
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.*
import org.mindrot.jbcrypt.BCrypt
import world.gregs.voidps.buffer.read.BufferReader
import world.gregs.voidps.cache.secure.RSA
import world.gregs.voidps.cache.secure.Xtea
import world.gregs.voidps.engine.action.Contexts
import world.gregs.voidps.engine.data.PlayerLoader
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.login.LoginQueue
import world.gregs.voidps.network.Client.Companion.string
import world.gregs.voidps.utility.getProperty
import world.gregs.voidps.utility.inject
import java.math.BigInteger
import java.util.concurrent.Executors

@ExperimentalUnsignedTypes
class Network(
    private val codec: NetworkCodec,
    private val revision: Int
) {

    private val logger = InlineLogger()
    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        logger.warn { throwable.message }
    }
    private lateinit var dispatcher: ExecutorCoroutineDispatcher
    private var running = false
    private val loginQueue: LoginQueue by inject()
    private val loader: PlayerLoader by inject()

    private val loginRSAModulus = BigInteger(getProperty("lsRsaModulus"), 16)
    private val loginRSAPrivate = BigInteger(getProperty("lsRsaPrivate"), 16)

    fun start(port: Int) = runBlocking {
        val executor = Executors.newCachedThreadPool()
        dispatcher = executor.asCoroutineDispatcher()
        val selector = ActorSelectorManager(dispatcher)
        val supervisor = SupervisorJob()
        val scope = CoroutineScope(coroutineContext + supervisor + exceptionHandler)
        with(scope) {
            val server = aSocket(selector).tcp().bind(port = port)
            running = true
            logger.info { "Listening for requests on port ${port}..." }
            while (running) {
                val socket = server.accept()
                logger.trace { "New connection accepted $socket" }
                val read = socket.openReadChannel()
                val write = socket.openWriteChannel(autoFlush = false)
                launch(Dispatchers.IO) {
                    connect(read, write)
                }
            }
        }
        running = true
    }

    suspend fun connect(read: ByteReadChannel, write: ByteWriteChannel) {
        synchronise(read, write)
        login(read, write)
    }

    suspend fun synchronise(read: ByteReadChannel, write: ByteWriteChannel) {
        val opcode = read.readByte().toInt()
        if (opcode != SYNCHRONISE) {
            logger.trace { "Invalid sync session id: $opcode" }
            write.finish(REJECT_SESSION)
            return
        }

        write.respond(ACCEPT_SESSION)
    }

    suspend fun login(read: ByteReadChannel, write: ByteWriteChannel) {
        val opcode = read.readByte().toInt()
        if (opcode != LOGIN && opcode != RECONNECT) {
            logger.trace { "Invalid request id: $opcode" }
            write.finish(REJECT_SESSION)
            return
        }
        val size = read.readShort().toInt()
        val array = ByteArray(size)
        val packet = ByteReadPacket(array)
        validateClient(read, packet, write)
    }

    suspend fun validateClient(read: ByteReadChannel, packet: ByteReadPacket, write: ByteWriteChannel) {
        val version = packet.readInt()
        if (version != revision) {
            write.finish(GAME_UPDATE)
            return
        }

        val rsa = decryptRSA(packet)
        validateSession(read, rsa, packet, write)
    }

    private fun decryptRSA(packet: ByteReadPacket): ByteReadPacket {
        val rsaBlockSize = packet.readShort().toInt() and 0xffff
        val data = packet.readBytes(rsaBlockSize)
        val rsa = RSA.crypt(data, loginRSAModulus, loginRSAPrivate)
        return ByteReadPacket(rsa)
    }

    suspend fun validateSession(read: ByteReadChannel, rsa: ByteReadPacket, packet: ByteReadPacket, write: ByteWriteChannel) {
        val sessionId = rsa.readUByte().toInt()
        if (sessionId != 10) {
            logger.debug { "Bad session id $sessionId" }
            write.finish(BAD_SESSION_ID)
            return
        }

        val isaacKeys = IntArray(4)
        for (i in isaacKeys.indices) {
            isaacKeys[i] = rsa.readInt()
        }

        val passwordMarker = rsa.readLong()
        if (passwordMarker != 0L) {
            logger.info { "Incorrect password marker $passwordMarker" }
            write.finish(BAD_SESSION_ID)
            return
        }
        val password: String = rsa.readString()
        val xtea = decryptXtea(packet, isaacKeys)

        val username = xtea.readString()
        xtea.readUByte()// social login
        val displayMode = xtea.readUByte().toInt()
        val client = createClient(write, isaacKeys)
        login(read, write, client, username, password, displayMode)
    }

    private fun createClient(write: ByteWriteChannel, isaacKeys: IntArray): Client {
        val inCipher = IsaacCipher(isaacKeys)
        for (i in isaacKeys.indices) {
            isaacKeys[i] += 50
        }
        val outCipher = IsaacCipher(isaacKeys)
        return Client(write, inCipher, outCipher)
    }

    private fun decryptXtea(packet: ByteReadPacket, isaacKeys: IntArray): ByteReadPacket {
        val remaining = packet.readBytes(packet.remaining.toInt())
        Xtea.decipher(remaining, isaacKeys)
        return ByteReadPacket(remaining)
    }

    suspend fun login(read: ByteReadChannel, write: ByteWriteChannel, client: Client, username: String, password: String, displayMode: Int) {
        if (loginQueue.isOnline(username)) {
            write.finish(ACCOUNT_ONLINE)
            return
        }
        val index = loginQueue.login(username)
        if (index == null) {
            loginQueue.logout(username, index)
            write.finish(WORLD_FULL)
            return
        }

        val player = loadPlayer(write, username, password, index) ?: return
        write.sendLoginDetails(username, index, 2)
        withContext(Contexts.Game) {
            player.gameFrame.displayMode = displayMode
            logger.info { "Player logged in $username index $index." }
            player.login(client)
        }
        try {
            readPackets(client, read, player)
        } finally {
            client.exit()
        }
    }

    private suspend fun ByteWriteChannel.sendLoginDetails(username: String, index: Int, rights: Int) {
        writeByte(SUCCESS)
        writeByte(13 + string(username))
        writeByte(rights)
        writeByte(0)// Unknown - something to do with skipping chat messages
        writeByte(0)
        writeByte(0)
        writeByte(0)
        writeByte(0)// Moves chat box position
        writeShort(index)
        writeByte(true)
        writeMedium(0)
        writeByte(true)
        writeString(username)
        flush()
    }

    suspend fun loadPlayer(write: ByteWriteChannel, username: String, password: String, index: Int): Player? {
        try {
            var account = loader.load(username)
            if (account == null) {
                account = loader.create(username, password)
            } else if (account.passwordHash.isBlank() || !BCrypt.checkpw(password, account.passwordHash)) {
                loginQueue.logout(username, index)
                write.finish(INVALID_CREDENTIALS)
                return null
            }
            loader.initPlayer(account, index)
            logger.info { "Player $username loaded and queued for login." }
            loginQueue.await()
            return account
        } catch (e: IllegalStateException) {
            loginQueue.logout(username, index)
            write.finish(COULD_NOT_COMPLETE_LOGIN)
            return null
        }
    }

    suspend fun readPackets(client: Client, read: ByteReadChannel, player: Player) {
        while (true) {
            val cipher = client.cipherIn.nextInt()
            val opcode = (read.readUByte() - cipher) and 0xff
            val decoder = codec.getDecoder(opcode)
            if (decoder == null) {
                logger.error { "Unable to identify length of packet $opcode" }
                return
            }
            val size = when (decoder.length) {
                -1 -> read.readUByte()
                -2 -> (read.readUByte() shl 8) or read.readUByte()
                else -> decoder.length
            }

            val packet = read.readPacket(size = size)
            decoder.decode(player, BufferReader(packet.readBytes()))
        }
    }

    fun shutdown() {
        running = false
        dispatcher.close()
    }

    private suspend fun ByteWriteChannel.respond(value: Int) {
        writeByte(value)
        flush()
    }

    private suspend fun ByteWriteChannel.finish(value: Int) {
        respond(value)
        close()
    }

    companion object {
        private const val SYNCHRONISE = 14
        private const val LOGIN = 16
        private const val RECONNECT = 18

        private const val ACCEPT_SESSION = 0
        private const val REJECT_SESSION = 0// Wrong?


        // Login responses
        private const val DATA_CHANGE = 0
        private const val VIDEO_AD = 1
        private const val SUCCESS = 2
        private const val INVALID_CREDENTIALS = 3
        private const val ACCOUNT_DISABLED = 4
        private const val ACCOUNT_ONLINE = 5
        private const val GAME_UPDATE = 6
        private const val WORLD_FULL = 7
        private const val LOGIN_SERVER_OFFLINE = 8
        private const val LOGIN_LIMIT_EXCEEDED = 9
        private const val BAD_SESSION_ID = 10
        private const val LOGIN_SERVER_REJECTED_SESSION = 11
        private const val MEMBERS_ACCOUNT_REQUIRED = 12
        private const val COULD_NOT_COMPLETE_LOGIN = 13
        private const val SERVER_BEING_UPDATED = 14
        private const val RECONNECTING = 15
        private const val LOGIN_ATTEMPTS_EXCEEDED = 16
        private const val MEMBERS_ONLY_AREA = 17
        private const val INVALID_LOGIN_SERVER = 20
        private const val TRANSFERRING_PROFILE = 21
    }
}