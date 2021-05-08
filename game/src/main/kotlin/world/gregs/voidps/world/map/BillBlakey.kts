import world.gregs.voidps.engine.client.ui.dialogue.Expression
import world.gregs.voidps.engine.client.ui.dialogue.dialogue
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.EquipSlot
import world.gregs.voidps.engine.entity.item.equipped
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.network.encode.message
import world.gregs.voidps.world.interact.dialogue.type.npc

on<NPCOption>({ npc.name == "bill_blakey" && option == "Talk-to" }) { player: Player ->
    player.dialogue(npc) {
        if (player.equipped(EquipSlot.Amulet).name == "ghostspeak_amulet") {
            npc("""
                Woo, wooo. Woooo.
            """, Expression.Cheerful)
            player.message("The ghost seems barely aware of your existence, but you sense that resting here might recharge you for battle!")
        } else {
            npc("""
                How sweet I roamed from fen to fen, And tasted all the Myre's pride,
                'Till I the queen of love did ken, Who in the spirit beams did glide! 
            """, Expression.Cheerful)
            npc("""
                She shew'd me lilies in her hair, And blushing roses for her brow;
                She led me through her gardens fair, Where all her silver blooms do grow. 
            """, Expression.Cheerful)
            npc("""
                With sweet Myre dews my wings were wet, And Phoebus' kiss did slowly fade;
                She'd caught me in her silken net, And trap'd me as this silver shade. 
            """, Expression.Cheerful)
            npc("""
                She loves to sit and hear me sing, Then laughing, sports and plays with me;
                Then stretches out her silver wing, And mocks my loss of liberty. 
            """, Expression.Cheerful)
        }
    }
}