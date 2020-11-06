package rs.dusk.engine.entity.character.player.social

import org.slf4j.LoggerFactory
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.engine.entity.character.player.Players
import rs.dusk.engine.entity.character.player.chat.ChatType
import rs.dusk.engine.entity.character.player.chat.message
import rs.dusk.engine.entity.character.player.social.Duration.HOUR
import rs.dusk.engine.entity.character.player.social.Duration.MINUTE
import rs.dusk.utility.func.formatUsername
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class FriendsChatChannels(private val players: Players) : FriendChannels {
    override val channels = ArrayList<FriendsChat>(100)
    private val blocks = HashMap<Name, Int>()
    private var nextUnblock = System.nanoTime() + MINUTE
    private var nextClean = System.nanoTime() + 30 * MINUTE

    override fun init() {
        executor.scheduleWithFixedDelay({
            try {
                channels.forEach {
                    it.update()
                }
            } catch (t: Throwable) {
                logger.error("Error processing refresh queue: ", t)
            }
        }, CHANNEL_SETTINGS_DELAY, CHANNEL_SETTINGS_DELAY, TimeUnit.SECONDS)
    }

    override fun find(name: Name): FriendsChat? {
        return channels.filter { it.channelName != null }.firstOrNull { it.owner == name }
    }

    override fun join(player: Player, friendsName: Name) {
        //Stop if blocked for spam joining
        if(isBlocked(player.names)) {
            player.message("You are temporarily blocked from joining channels - please try again later!", ChatType.FriendsChatMessage)
            return
        }

        //Notify of attempt
        player.message("Attempting to join channel...", ChatType.FriendsChatMessage)

        //Attempt to find the channel
        val channel = find(friendsName)

        //If channel is disabled or doesn't exist
        if(channel?.channelName == null) {
            player.message("The channel you tried to join does not exist.", ChatType.FriendsChatMessage)
            return
        }

        try {
            if(player.channel == channel) {
                //Resend
                channel.display(player)
            } else if(player.channel == null) {
                //Join
                channel.join(player)
                increment(player.names)
            }

            checkClean()
        } catch (t: Throwable) {
            t.printStackTrace()
            player.message("Error joining friends chat channel - please try again later!", ChatType.FriendsChatMessage)
        }
    }

    override fun name(player: Player, name: String): String? {
        //We don't need to rank check because we're only ever accessing the players own channel (or an admin)
        val formatted = name.formatUsername()

        //Attempt to find the channel
        var channel = get(player)

        //If players channel doesn't exist
        return if(channel == null) {
            //Create channel
            channel = FriendsChatChannel(players, player.relations)
            channel.setup(player, formatted)
            channels.add(channel)
            //Notify
            player.message("Your friends chat channel has now been enabled!", ChatType.FriendsChatMessage)
            player.message("Join your channel by clicking 'Join Chat' and typing: ${player.names.name}", ChatType.FriendsChatMessage)
            formatted
        } else {
            channel.rename(player, formatted)
        }
    }

    override fun canSetup(player: Player): Boolean {
        //Get the players channel (current if admin)
        val channel = get(player)

        //If the player is in another channel they can't open (excluding admins)
        if(player.channel != null && player.channel != channel) {
            player.message("Only the friends chat owner can access this.", ChatType.FriendsChatMessage)
            return false
        }

        return true
    }

    override fun demote(player: Player, member: Name) {
        get(player)?.demote(player, member)
    }

    override fun promote(player: Player, member: Name, rank: Int) {
        get(player)?.promote(player, member, rank)
    }

    override fun rejoin(player: Player) {
        val name = player.channel?.owner ?: player.friendsChat ?: return
        join(player, name)
    }

    override fun disable(player: Player) {
        val channel = player.channel ?: return
        if(!channel.isOwner(player)) {
            player.message("Only an owner can disable their channel.", ChatType.FriendsChatMessage)
            return
        }

        channel.disable(player)
    }

    override fun increment(name: Name) {
        blocks[name] = if(blocks.containsKey(name)) {
            blocks[name]!! + 1
        } else {
            1
        }
    }

    override fun isBlocked(name: Name): Boolean {
        //Check if the unblock timer has passed
        if(nextUnblock < System.nanoTime()) {
            //Reset timer
            nextUnblock = System.nanoTime() + MINUTE
            //Clear current blocks
            clearBlocks()
        }
        return blocks.getOrDefault(name, 0) >= MAX_FAILED_ATTEMPTS
    }

    override fun clearBlocks() {
        blocks.clear()
    }

    override fun get(player: Player): FriendsChat? {
        return if(player.administrator && player.channel != null) player.channel else find(player.names)
    }

    /**
     * Roughly every hour all the channels are checked for expired bans and cleaned
     * Note: This is blocking and might need to be ran asynchronously if channels list get's too big
     */
    private fun checkClean() {
        //Check clean timer
        if(nextClean < System.nanoTime()) {
            //Reset timer
            nextClean = System.nanoTime() + HOUR
            //Clean channels
            channels.forEach {
                it.clean()
            }
        }
    }

    companion object {
        const val CHANNEL_SETTINGS_DELAY = 60L
        private const val MAX_FAILED_ATTEMPTS = 10
        private val logger = LoggerFactory.getLogger(FriendsChatChannels::class.java)!!
        private val executor = Executors.newSingleThreadScheduledExecutor()
    }

}