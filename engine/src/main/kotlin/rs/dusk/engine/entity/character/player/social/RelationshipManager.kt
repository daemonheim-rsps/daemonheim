package rs.dusk.engine.entity.character.player.social

class RelationshipManager : RelationsManager {
    override val relations = HashMap<Name, Relations>()

    override fun create(name: Name): Relations {
        val relation = Relationships()
        relations[name] = relation
        return relation
    }

    override fun get(name: Name): Relations? {
        return relations.getOrDefault(name, null)
    }
}