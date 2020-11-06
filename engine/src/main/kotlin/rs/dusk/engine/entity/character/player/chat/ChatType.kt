package rs.dusk.engine.entity.character.player.chat

enum class ChatType(val id: Int) {
    Game(0),
    GameFilter(109),
    ChatAdmin(1),
    Chat(2),
    PrivateFrom(3),
    PrivateRed(4),
    PrivateTo(5),
    PrivateFromAdmin(6),
    FriendsChat(7),
    FriendsChatGame(8),
    QuickChat(9),
    FriendsChatMessage(11),
    Console(99),
    Trade(100),
    GameTrade(103),
    ChallengeDuel(101),
    Assist(102),
    GameAssist(104),
    ChallengeClanWar(107),
    Alliance(108)
}