final class Assets {
    private static final String ROOT = "assets/";
    private static final String ARENA = ROOT + "arena/";
    private static final String PLAYER = ROOT + "player/";
    private static final String HAZARDS = ROOT + "hazards/";
    private static final String HUD = ROOT + "hud/";
    private static final String AUDIO = ROOT + "audio/";

    static final String TITLE_FONT = "../../fonts/PrStart.ttf";
    static final String BODY_FONT = "../../fonts/Volter__28Goldfish_29.ttf";

    static final String ARENA_BACKGROUND = ARENA + "background.png";
    static final String ARENA_FLOOR = ARENA + "floor.png";

    static final String PLAYER_SHADOW = PLAYER + "shadow.png";
    static final String[] PLAYER_SHIP_VARIANTS = {
        PLAYER + "ship_up.png",
        PLAYER + "ship_up_right.png",
        PLAYER + "ship_right.png",
        PLAYER + "ship_down_right.png",
        PLAYER + "ship_down.png",
        PLAYER + "ship_down_left.png",
        PLAYER + "ship_left.png",
        PLAYER + "ship_up_left.png"
    };
    static final String[] PLAYER_DASH_VARIANTS = {
        PLAYER + "dash_up.png",
        PLAYER + "dash_up_right.png",
        PLAYER + "dash_right.png",
        PLAYER + "dash_down_right.png",
        PLAYER + "dash_down.png",
        PLAYER + "dash_down_left.png",
        PLAYER + "dash_left.png",
        PLAYER + "dash_up_left.png"
    };

    static final String[] HAZARD_VARIANTS = {
        HAZARDS + "blade_rose.png",
        HAZARDS + "blade_amber.png",
        HAZARDS + "blade_azure.png"
    };

    static final String HUD_HEART_FULL = HUD + "heart_full.png";
    static final String HUD_HEART_EMPTY = HUD + "heart_empty.png";

    static final String SFX_START = AUDIO + "start.mp3";
    static final String SFX_DASH = AUDIO + "dash.mp3";
    static final String SFX_DASH_DOUBLE = AUDIO + "dash_double.mp3";
    static final String SFX_BREAK = AUDIO + "break.mp3";
    static final String SFX_HIT = AUDIO + "hit.mp3";
    static final String SFX_GAME_OVER = AUDIO + "game_over.mp3";
    static final String SFX_PAUSE = AUDIO + "pause.mp3";
    static final String MUSIC_BACKGROUND = AUDIO + "background_loop.mp3";

    private Assets() {
    }
}
