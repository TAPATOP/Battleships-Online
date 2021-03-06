package Source;

public enum ServerResponseType {
    OK,
    INVALID,

    DISCONNECTED,
    OTHER_PLAYER_CONNECTED,

    DEPLOYED_DESTROYER,
    DEPLOYED_CRUISER,
    DEPLOYED_BATTLESHIP,
    DEPLOYED_CARRIER,

    MISS,
    HIT,
    DESTROYED,
    DESTROYED_LAST_SHIP,
    RECORD_SHOT,
    GAME_OVER,

    NOTHING_OF_IMPORTANCE
}
