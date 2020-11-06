package rs.dusk.engine.entity.character.player.social

class Relationships : Relations {
    /**
     * List of befriended players
     */
    private val friends = HashMap<Name, FriendsChat.Ranks>()

    /**
     * List of ignored players
     */
    private val ignores = HashMap<Name, Boolean>()


    override fun addFriend(name: Name, rank: FriendsChat.Ranks) {
        friends[name] = rank
    }

    override fun isFriend(name: Name): Boolean {
        return friends.containsKey(name)
    }

    override fun removeFriend(name: Name) {
        friends.remove(name)
    }

    override fun getFriend(name: Name): FriendsChat.Ranks? {
        return friends[name]
    }

    override fun getFriends(): List<Name> {
        return friends.keys.toList()
    }

    override fun friendCount(): Int {
        return friends.size
    }

    override fun addIgnore(name: Name, temp: Boolean) {
        ignores[name] = temp
    }

    override fun isIgnored(name: Name): Boolean {
        return ignores.contains(name)
    }

    override fun removeIgnore(name: Name) {
        ignores.remove(name)
    }

    override fun getIgnores(): List<Name> {
        return ignores.keys.toList()
    }

    override fun ignoreCount(): Int {
        return ignores.size
    }

    override fun removeIgnores() {
        val iterator = ignores.iterator()
        var next: MutableMap.MutableEntry<Name, Boolean>
        while(iterator.hasNext()) {
            next = iterator.next()
            if(next.value) {
                iterator.remove()
            }
        }
    }
}