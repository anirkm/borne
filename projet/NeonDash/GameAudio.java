import MG2D.audio.Bruitage;
import MG2D.audio.Musique;

class GameAudio {
    private static final long BREAK_COOLDOWN_MS = 45L;
    private static final long UI_COOLDOWN_MS = 90L;

    private Musique backgroundMusic;
    private long lastBreakMillis;
    private long lastUiMillis;

    public void startBackgroundMusic() {
        if (backgroundMusic != null) {
            return;
        }

        try {
            backgroundMusic = new Musique(Assets.MUSIC_BACKGROUND);
            backgroundMusic.lecture();
        } catch (Exception exception) {
            backgroundMusic = null;
        }
    }

    public void stopBackgroundMusic() {
        if (backgroundMusic == null) {
            return;
        }

        try {
            backgroundMusic.arret();
        } catch (Exception exception) {
        }
        backgroundMusic = null;
    }

    public void playStart() {
        playUi(Assets.SFX_START);
    }

    public void playDash() {
        play(Assets.SFX_DASH);
    }

    public void playDoubleDash() {
        play(Assets.SFX_DASH_DOUBLE);
    }

    public void playBreak() {
        long now = System.currentTimeMillis();
        if (now - lastBreakMillis < BREAK_COOLDOWN_MS) {
            return;
        }
        lastBreakMillis = now;
        play(Assets.SFX_BREAK);
    }

    public void playHit() {
        play(Assets.SFX_HIT);
    }

    public void playGameOver() {
        play(Assets.SFX_GAME_OVER);
    }

    public void playPauseToggle() {
        playUi(Assets.SFX_PAUSE);
    }

    private void playUi(String path) {
        long now = System.currentTimeMillis();
        if (now - lastUiMillis < UI_COOLDOWN_MS) {
            return;
        }
        lastUiMillis = now;
        play(path);
    }

    private void play(String path) {
        try {
            Bruitage sound = new Bruitage(path);
            sound.lecture();
        } catch (Exception exception) {
        }
    }
}
