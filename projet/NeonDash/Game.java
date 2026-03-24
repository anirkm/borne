import MG2D.FenetrePleinEcran;
import MG2D.Couleur;
import MG2D.geometrie.Dessin;
import MG2D.geometrie.Ligne;
import MG2D.geometrie.Point;
import MG2D.geometrie.Rectangle;
import MG2D.geometrie.Texture;
import java.awt.Font;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.util.ArrayList;
import java.util.Random;

public class Game {
    private enum GameState {
        INTRO,
        PLAYING,
        PAUSED,
        GAME_OVER,
        NAME_ENTRY
    }

    private static final int FRAME_TIME_MS = 8;
    private static final int MAX_PARTICLES = 220;
    private static final double MAX_FRAME_DELTA = 0.033;
    private static final double PHYSICS_STEP = 0.008;
    private static final int MAX_LIVES = 3;
    private static final double HIT_RECOVERY_SECONDS = 1.25;

    private final FenetrePleinEcran window;
    private final int width;
    private final int height;
    private final int fieldLeft;
    private final int fieldBottom;
    private final int fieldRight;
    private final int fieldTop;
    private final ClavierBorneArcade keyboard;
    private final ArrayList<Hazard> hazards;
    private final ArrayList<Particle> particles;
    private final Random random;
    private final Player player;
    private final Spawner spawner;
    private final Hud hud;
    private final GameAudio audio;
    private final HighScoreTable highScores;
    private final ArrayList<Dessin> arenaForeground;
    private GameState state;
    private long lastFrameNanos;
    private long runStartMillis;
    private int finalScore;
    private char[] nameBuffer;
    private int nameSelection;
    private boolean layersNeedRefresh;
    private double playerTrailTimer;
    private double hazardSparkTimer;
    private double scoreValue;
    private double comboTimer;
    private int comboMultiplier;
    private int shatteredCount;
    private int livesRemaining;
    private String hudMessage;
    private double hudMessageTimer;
    private double ambientTime;
    private double recoveryTimer;

    public Game() {
        GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()[0];
        width = device.getDisplayMode().getWidth();
        height = device.getDisplayMode().getHeight();
        fieldLeft = Math.max(72, width / 18);
        fieldRight = width - fieldLeft;
        fieldBottom = Math.max(72, height / 12);
        fieldTop = height - Math.max(138, height / 7);

        window = new FenetrePleinEcran("NeonDash");
        keyboard = new ClavierBorneArcade();
        hazards = new ArrayList<Hazard>();
        particles = new ArrayList<Particle>();
        arenaForeground = new ArrayList<Dessin>();
        random = new Random();

        window.addKeyListener(keyboard);
        window.getP().addKeyListener(keyboard);

        buildArena();

        int playerRadius = Math.max(18, Math.min(width, height) / 48);
        player = new Player((fieldLeft + fieldRight) / 2, (fieldBottom + fieldTop) / 2, playerRadius);
        player.addTo(window);

        spawner = new Spawner();
        audio = new GameAudio();
        audio.startBackgroundMusic();
        hud = new Hud(
            width,
            height,
            fieldLeft,
            fieldBottom,
            fieldRight,
            fieldTop,
            loadFont(Assets.TITLE_FONT, 46.0f, new Font("Dialog", Font.BOLD, 46)),
            loadFont(Assets.BODY_FONT, 24.0f, new Font("Dialog", Font.PLAIN, 24)),
            loadFont(Assets.BODY_FONT, 18.0f, new Font("Dialog", Font.PLAIN, 18))
        );
        hud.addTo(window);

        highScores = HighScoreTable.load("highscore");
        state = GameState.INTRO;
        finalScore = 0;
        nameBuffer = new char[] {'A', 'A', 'A'};
        nameSelection = 0;
        layersNeedRefresh = false;
        playerTrailTimer = 0.0;
        hazardSparkTimer = 0.0;
        scoreValue = 0.0;
        comboTimer = 0.0;
        comboMultiplier = 1;
        shatteredCount = 0;
        livesRemaining = MAX_LIVES;
        hudMessage = "";
        hudMessageTimer = 0.0;
        ambientTime = 0.0;
        recoveryTimer = 0.0;

        hud.updatePlaying(0, livesRemaining, 1.0, player.getDashCharges(), player.getMaxDashCharges(), spawner.getDifficultyLevel(0), 1, 0, "");
        hud.showIntro(highScores.getBestScore());
        refreshForegroundLayers();
        player.tickVisual(0.0);
        window.rafraichir();
    }

    public void run() {
        lastFrameNanos = System.nanoTime();

        while (true) {
            long now = System.nanoTime();
            double delta = (now - lastFrameNanos) / 1_000_000_000.0;
            lastFrameNanos = now;

            if (delta > MAX_FRAME_DELTA) {
                delta = MAX_FRAME_DELTA;
            }

            update(delta);
            window.rafraichir();

            try {
                Thread.sleep(FRAME_TIME_MS);
            } catch (Exception exception) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    private void update(double delta) {
        updateArenaAmbience(delta);

        if (state == GameState.INTRO) {
            updateIntro(delta);
        } else if (state == GameState.PLAYING) {
            updatePlaying(delta);
        } else if (state == GameState.PAUSED) {
            updatePaused(delta);
        } else if (state == GameState.GAME_OVER) {
            updateGameOver(delta);
        } else if (state == GameState.NAME_ENTRY) {
            updateNameEntry(delta);
        }

        updateParticles(delta);
        if (layersNeedRefresh) {
            refreshForegroundLayers();
            layersNeedRefresh = false;
        }
    }

    private void updateIntro(double delta) {
        player.tickVisual(delta);

        if (keyboard.getBoutonJ1ZTape()) {
            exitToMenu();
        }

        if (keyboard.getBoutonJ1ATape()) {
            startRun();
        }
    }

    private void updatePlaying(double delta) {
        if (keyboard.getBoutonJ1ZTape()) {
            exitToMenu();
        }

        if (keyboard.getBoutonJ1BTape()) {
            state = GameState.PAUSED;
            audio.playPauseToggle();
            hud.showPaused(finalScore);
            layersNeedRefresh = true;
            return;
        }

        double remaining = delta;
        while (remaining > 0.0 && state == GameState.PLAYING) {
            double step = Math.min(PHYSICS_STEP, remaining);
            simulatePlayingStep(step);
            remaining -= step;
        }

        emitHazardSparks(delta);

        if (state != GameState.PLAYING) {
            return;
        }

        finalScore = (int) Math.floor(scoreValue);
        hud.updatePlaying(
            finalScore,
            livesRemaining,
            player.getDashChargeRatio(),
            player.getDashCharges(),
            player.getMaxDashCharges(),
            spawner.getDifficultyLevel(getElapsedSeconds()),
            comboMultiplier,
            shatteredCount,
            hudMessageTimer > 0.0 ? hudMessage : ""
        );
    }

    private void updatePaused(double delta) {
        player.tickVisual(delta);

        if (keyboard.getBoutonJ1ZTape()) {
            exitToMenu();
        }

        if (keyboard.getBoutonJ1ATape()) {
            startRun();
            return;
        }

        if (keyboard.getBoutonJ1BTape()) {
            state = GameState.PLAYING;
            audio.playPauseToggle();
            hud.hideOverlay();
            layersNeedRefresh = true;
        }
    }

    private void updateGameOver(double delta) {
        player.tickVisual(delta);

        if (keyboard.getBoutonJ1ZTape()) {
            exitToMenu();
        }

        if (keyboard.getBoutonJ1ATape()) {
            startRun();
            return;
        }

        if (highScores.qualifies(finalScore) && keyboard.getBoutonJ1BTape()) {
            state = GameState.NAME_ENTRY;
            nameBuffer = new char[] {'A', 'A', 'A'};
            nameSelection = 0;
            hud.showNameEntry(nameBuffer, nameSelection, finalScore);
            layersNeedRefresh = true;
        }
    }

    private void updateNameEntry(double delta) {
        player.tickVisual(delta);

        if (keyboard.getBoutonJ1ZTape()) {
            exitToMenu();
        }

        boolean changed = false;

        if (keyboard.getJoyJ1GaucheTape()) {
            nameSelection = Math.max(0, nameSelection - 1);
            changed = true;
        }

        if (keyboard.getJoyJ1DroiteTape()) {
            nameSelection = Math.min(3, nameSelection + 1);
            changed = true;
        }

        if (keyboard.getJoyJ1HautTape() && nameSelection < 3) {
            nameBuffer[nameSelection] = nextLetter(nameBuffer[nameSelection]);
            changed = true;
        }

        if (keyboard.getJoyJ1BasTape() && nameSelection < 3) {
            nameBuffer[nameSelection] = previousLetter(nameBuffer[nameSelection]);
            changed = true;
        }

        if (keyboard.getBoutonJ1ATape()) {
            if (nameSelection == 3) {
                highScores.record(new String(nameBuffer), finalScore);
                returnToIntro();
                return;
            } else {
                nameSelection = Math.min(3, nameSelection + 1);
                changed = true;
            }
        }

        if (changed) {
            hud.showNameEntry(nameBuffer, nameSelection, finalScore);
            layersNeedRefresh = true;
        }
    }

    private void startRun() {
        resetRunState();
        runStartMillis = System.currentTimeMillis();
        finalScore = 0;
        audio.playStart();
        hud.hideOverlay();
        hud.updatePlaying(0, livesRemaining, player.getDashChargeRatio(), player.getDashCharges(), player.getMaxDashCharges(), spawner.getDifficultyLevel(0), comboMultiplier, shatteredCount, "");
        layersNeedRefresh = true;
        state = GameState.PLAYING;
    }

    private void returnToIntro() {
        resetRunState();
        finalScore = 0;
        state = GameState.INTRO;
        hud.updatePlaying(0, livesRemaining, player.getDashChargeRatio(), player.getDashCharges(), player.getMaxDashCharges(), spawner.getDifficultyLevel(0), 1, 0, "");
        hud.showIntro(highScores.getBestScore());
        layersNeedRefresh = true;
    }

    private void resetRunState() {
        clearHazards();
        clearParticles();
        player.reset((fieldLeft + fieldRight) / 2, (fieldBottom + fieldTop) / 2);
        player.bringToFront(window);
        spawner.reset();
        playerTrailTimer = 0.0;
        hazardSparkTimer = 0.0;
        scoreValue = 0.0;
        comboTimer = 0.0;
        comboMultiplier = 1;
        shatteredCount = 0;
        livesRemaining = MAX_LIVES;
        hudMessage = "";
        hudMessageTimer = 0.0;
        recoveryTimer = 0.0;
    }

    private void simulatePlayingStep(double delta) {
        if (recoveryTimer > 0.0) {
            recoveryTimer = Math.max(0.0, recoveryTimer - delta);
        }

        tickScoreSystems(delta);
        player.update(delta, keyboard, fieldLeft, fieldBottom, fieldRight, fieldTop);
        emitPlayerTrail(delta);
        if (player.consumeDashTriggered()) {
            if (player.consumeDoubleDashTriggered()) {
                audio.playDoubleDash();
                emitDoubleDashBurst();
                setHudMessage("DOUBLE DASH", 0.45);
            } else {
                audio.playDash();
                emitDashBurst();
            }
        }

        int elapsedSeconds = getElapsedSeconds();
        if (spawner.update(delta, hazards, random, elapsedSeconds, fieldLeft, fieldBottom, fieldRight, fieldTop, window)) {
            layersNeedRefresh = true;
        }

        updateHazards(delta);
        if (hasCollision()) {
            handlePlayerHit();
        }
    }

    private void handlePlayerHit() {
        emitImpactBurst(player.getX(), player.getY(), player.getDirectionX(), player.getDirectionY());
        livesRemaining--;

        if (livesRemaining <= 0) {
            audio.playGameOver();
            triggerGameOver();
            return;
        }

        audio.playHit();
        clearHazards();
        clearParticles();
        player.reset((fieldLeft + fieldRight) / 2, (fieldBottom + fieldTop) / 2);
        player.bringToFront(window);
        recoveryTimer = HIT_RECOVERY_SECONDS;
        setHudMessage("COEUR PERDU  " + livesRemaining + " vies", 1.1);
        emitRespawnBurst();
        layersNeedRefresh = true;
    }

    private void triggerGameOver() {
        finalScore = (int) Math.floor(scoreValue);
        state = GameState.GAME_OVER;
        hud.showGameOver(finalScore, highScores.qualifies(finalScore));
        layersNeedRefresh = true;
    }

    private void updateHazards(double delta) {
        for (int index = hazards.size() - 1; index >= 0; index--) {
            Hazard hazard = hazards.get(index);
            hazard.update(delta);
            if (player.isInvulnerable() && (hazard.collides(player) || player.hitsDashSweep(hazard))) {
                shatterHazard(index, hazard);
                continue;
            }
            if (hazard.tryGraze(player, 24.0)) {
                rewardGraze(hazard);
            }
            if (hazard.isOffscreen(fieldLeft, fieldBottom, fieldRight, fieldTop)) {
                hazard.removeFrom(window);
                hazards.remove(index);
            }
        }
    }

    private boolean hasCollision() {
        if (player.isInvulnerable() || recoveryTimer > 0.0) {
            return false;
        }
        for (int index = 0; index < hazards.size(); index++) {
            if (hazards.get(index).collides(player)) {
                return true;
            }
        }
        return false;
    }

    private int getElapsedSeconds() {
        return (int) ((System.currentTimeMillis() - runStartMillis) / 1000L);
    }

    private void clearHazards() {
        for (int index = 0; index < hazards.size(); index++) {
            hazards.get(index).removeFrom(window);
        }
        hazards.clear();
        layersNeedRefresh = true;
    }

    private void clearParticles() {
        for (int index = 0; index < particles.size(); index++) {
            particles.get(index).removeFrom(window);
        }
        particles.clear();
        layersNeedRefresh = true;
    }

    private char nextLetter(char current) {
        if (current >= 'A' && current < 'Z') {
            return (char) (current + 1);
        }
        return 'A';
    }

    private char previousLetter(char current) {
        if (current > 'A' && current <= 'Z') {
            return (char) (current - 1);
        }
        return 'Z';
    }

    private void exitToMenu() {
        clearHazards();
        audio.stopBackgroundMusic();
        window.fermer();
        System.exit(0);
    }

    private Font loadFont(String relativePath, float size, Font fallback) {
        try {
            Font loaded = Font.createFont(Font.TRUETYPE_FONT, new File(relativePath));
            return loaded.deriveFont(size);
        } catch (Exception exception) {
            return fallback.deriveFont(size);
        }
    }

    private void addTextureIfPresent(String texturePath, Point origin, int textureWidth, int textureHeight) {
        if (new File(texturePath).exists()) {
            window.ajouter(new Texture(texturePath, origin, textureWidth, textureHeight));
        }
    }

    private void refreshForegroundLayers() {
        for (int index = 0; index < arenaForeground.size(); index++) {
            Dessin layer = arenaForeground.get(index);
            window.supprimer(layer);
            window.ajouter(layer);
        }
        player.bringToFront(window);
        hud.bringToFront(window);
    }

    private void updateParticles(double delta) {
        boolean removed = false;
        for (int index = particles.size() - 1; index >= 0; index--) {
            if (!particles.get(index).update(delta)) {
                particles.get(index).removeFrom(window);
                particles.remove(index);
                removed = true;
            }
        }

        if (removed) {
            layersNeedRefresh = true;
        }
    }

    private void addParticle(Particle particle) {
        if (particles.size() >= MAX_PARTICLES) {
            particles.get(0).removeFrom(window);
            particles.remove(0);
        }
        particles.add(particle);
        particle.addTo(window);
        layersNeedRefresh = true;
    }

    private void tickScoreSystems(double delta) {
        if (comboTimer > 0.0) {
            comboTimer = Math.max(0.0, comboTimer - delta);
            if (comboTimer == 0.0) {
                comboMultiplier = 1;
            }
        }

        if (hudMessageTimer > 0.0) {
            hudMessageTimer = Math.max(0.0, hudMessageTimer - delta);
            if (hudMessageTimer == 0.0) {
                hudMessage = "";
            }
        }

        scoreValue += delta * (12.0 + Math.max(0, comboMultiplier - 1) * 6.0);
        finalScore = (int) Math.floor(scoreValue);
    }

    private void extendCombo(int amount, double durationSeconds) {
        if (comboTimer <= 0.0) {
            comboMultiplier = 1;
        }
        comboMultiplier = Math.min(5, comboMultiplier + amount);
        comboTimer = Math.max(comboTimer, durationSeconds);
    }

    private void setHudMessage(String message, double durationSeconds) {
        hudMessage = message;
        hudMessageTimer = durationSeconds;
    }

    private void rewardGraze(Hazard hazard) {
        extendCombo(1, 1.7);
        int bonus = 18 + comboMultiplier * 8;
        scoreValue += bonus;
        finalScore = (int) Math.floor(scoreValue);
        setHudMessage("GRAZE +" + bonus + "   Flux x" + comboMultiplier, 0.8);
        emitGrazeBurst(hazard.getX(), hazard.getY());
    }

    private void shatterHazard(int index, Hazard hazard) {
        hazards.remove(index);
        hazard.removeFrom(window);
        audio.playBreak();
        shatteredCount++;
        extendCombo(1, 2.2);
        int bonus = 34 + comboMultiplier * 14;
        scoreValue += bonus;
        finalScore = (int) Math.floor(scoreValue);
        player.reduceCooldown(0.28);
        setHudMessage("BREAK +" + bonus + "   Dash recharge", 0.9);
        emitShatterBurst(hazard.getX(), hazard.getY(), hazard.getVelocityX(), hazard.getVelocityY());
        layersNeedRefresh = true;
    }

    private void updateArenaAmbience(double delta) {
        ambientTime += delta;
    }

    // Particles are kept intentionally lightweight: they add motion without overloading MG2D.
    private void emitPlayerTrail(double delta) {
        if (!player.isMoving()) {
            playerTrailTimer = 0.0;
            return;
        }

        playerTrailTimer += delta;
        double interval = player.isInvulnerable() ? 0.018 : 0.045;
        double directionX = player.getDirectionX();
        double directionY = player.getDirectionY();
        double tailX = player.getX() - directionX * Math.max(10.0, player.getRadius() - 3.0);
        double tailY = player.getY() - directionY * Math.max(10.0, player.getRadius() - 3.0);

        while (playerTrailTimer >= interval) {
            double jitterX = random.nextDouble() * 12.0 - 6.0;
            double jitterY = random.nextDouble() * 12.0 - 6.0;
            double speed = player.isInvulnerable() ? 140.0 : 72.0;
            Couleur start = player.isInvulnerable() ? new Couleur(255, 236, 132) : new Couleur(90, 255, 228);
            Couleur end = new Couleur(12, 28, 50);

            addParticle(new Particle(
                tailX + jitterX,
                tailY + jitterY,
                -directionX * speed + jitterX * 1.6,
                -directionY * speed + jitterY * 1.6,
                player.isInvulnerable() ? 0.18 : 0.24,
                player.isInvulnerable() ? 7.0 : 5.0,
                1.0,
                start,
                end
            ));
            playerTrailTimer -= interval;
        }
    }

    private void emitDashBurst() {
        for (int index = 0; index < 18; index++) {
            double angle = (Math.PI * 2.0 * index) / 18.0 + random.nextDouble() * 0.14;
            double speed = 170.0 + random.nextDouble() * 160.0;
            addParticle(new Particle(
                player.getX(),
                player.getY(),
                Math.cos(angle) * speed,
                Math.sin(angle) * speed,
                0.24 + random.nextDouble() * 0.08,
                8.0 + random.nextDouble() * 2.0,
                1.0,
                new Couleur(255, 241, 170),
                new Couleur(50, 30, 12)
            ));
        }
    }

    private void emitRespawnBurst() {
        for (int index = 0; index < 20; index++) {
            double angle = (Math.PI * 2.0 * index) / 20.0 + random.nextDouble() * 0.18;
            double speed = 95.0 + random.nextDouble() * 115.0;
            addParticle(new Particle(
                player.getX(),
                player.getY(),
                Math.cos(angle) * speed,
                Math.sin(angle) * speed,
                0.34 + random.nextDouble() * 0.08,
                9.0 + random.nextDouble() * 3.0,
                1.0,
                new Couleur(196, 252, 255),
                new Couleur(24, 46, 64)
            ));
        }
    }

    private void emitDoubleDashBurst() {
        double directionX = player.getDirectionX();
        double directionY = player.getDirectionY();

        for (int index = 0; index < 30; index++) {
            double angle = (Math.PI * 2.0 * index) / 30.0 + random.nextDouble() * 0.12;
            double speed = 210.0 + random.nextDouble() * 210.0;
            addParticle(new Particle(
                player.getX(),
                player.getY(),
                Math.cos(angle) * speed + directionX * 70.0,
                Math.sin(angle) * speed + directionY * 70.0,
                0.30 + random.nextDouble() * 0.10,
                11.0 + random.nextDouble() * 3.0,
                1.0,
                index % 2 == 0 ? new Couleur(196, 250, 255) : new Couleur(124, 242, 255),
                new Couleur(18, 36, 52)
            ));
        }

        for (int index = 0; index < 12; index++) {
            double spread = (index - 5.5) * 9.0;
            addParticle(new Particle(
                player.getX() - directionX * 8.0,
                player.getY() - directionY * 8.0,
                directionX * (260.0 + random.nextDouble() * 90.0) + spread * -directionY,
                directionY * (260.0 + random.nextDouble() * 90.0) + spread * directionX,
                0.24 + random.nextDouble() * 0.08,
                8.0 + random.nextDouble() * 2.0,
                1.0,
                new Couleur(255, 255, 255),
                new Couleur(80, 220, 255)
            ));
        }
    }

    private void emitHazardSparks(double delta) {
        if (hazards.isEmpty()) {
            hazardSparkTimer = 0.0;
            return;
        }

        hazardSparkTimer += delta;
        while (hazardSparkTimer >= 0.05) {
            hazardSparkTimer -= 0.05;
            int samples = Math.min(3, 1 + hazards.size() / 6);
            for (int index = 0; index < samples; index++) {
                Hazard hazard = hazards.get(random.nextInt(hazards.size()));
                double jitterX = random.nextDouble() * 14.0 - 7.0;
                double jitterY = random.nextDouble() * 14.0 - 7.0;
                int palette = random.nextInt(3);
                Couleur start = palette == 0
                    ? new Couleur(255, 118, 204)
                    : (palette == 1 ? new Couleur(255, 190, 82) : new Couleur(126, 193, 255));
                Couleur end = new Couleur(24, 18, 52);

                addParticle(new Particle(
                    hazard.getX() + jitterX,
                    hazard.getY() + jitterY,
                    -hazard.getVelocityX() * 0.18 + jitterX * 2.0,
                    -hazard.getVelocityY() * 0.18 + jitterY * 2.0,
                    0.16 + random.nextDouble() * 0.06,
                    4.0 + random.nextDouble() * 2.0,
                    1.0,
                    start,
                    end
                ));
            }
        }
    }

    private void emitImpactBurst(double centerX, double centerY, double forwardX, double forwardY) {
        for (int index = 0; index < 24; index++) {
            double angle = (Math.PI * 2.0 * index) / 24.0 + random.nextDouble() * 0.2;
            double speed = 150.0 + random.nextDouble() * 210.0;
            double biasX = forwardX * 55.0;
            double biasY = forwardY * 55.0;
            addParticle(new Particle(
                centerX,
                centerY,
                Math.cos(angle) * speed + biasX,
                Math.sin(angle) * speed + biasY,
                0.28 + random.nextDouble() * 0.10,
                9.0 + random.nextDouble() * 3.0,
                1.0,
                index % 2 == 0 ? new Couleur(255, 244, 180) : new Couleur(255, 128, 190),
                new Couleur(34, 18, 20)
            ));
        }
    }

    private void emitGrazeBurst(double centerX, double centerY) {
        for (int index = 0; index < 10; index++) {
            double angle = (Math.PI * 2.0 * index) / 10.0 + random.nextDouble() * 0.22;
            double speed = 70.0 + random.nextDouble() * 80.0;
            addParticle(new Particle(
                centerX,
                centerY,
                Math.cos(angle) * speed,
                Math.sin(angle) * speed,
                0.18 + random.nextDouble() * 0.07,
                5.0 + random.nextDouble() * 2.0,
                1.0,
                new Couleur(118, 255, 232),
                new Couleur(14, 28, 38)
            ));
        }
    }

    private void emitShatterBurst(double centerX, double centerY, double velocityX, double velocityY) {
        for (int index = 0; index < 16; index++) {
            double angle = (Math.PI * 2.0 * index) / 16.0 + random.nextDouble() * 0.18;
            double speed = 105.0 + random.nextDouble() * 125.0;
            addParticle(new Particle(
                centerX,
                centerY,
                Math.cos(angle) * speed + velocityX * 0.12,
                Math.sin(angle) * speed + velocityY * 0.12,
                0.24 + random.nextDouble() * 0.08,
                6.0 + random.nextDouble() * 2.5,
                1.0,
                index % 2 == 0 ? new Couleur(255, 174, 96) : new Couleur(255, 114, 188),
                new Couleur(18, 22, 36)
            ));
        }
    }

    private void addForeground(Dessin layer) {
        arenaForeground.add(layer);
        window.ajouter(layer);
    }

    private void buildArena() {
        Couleur backgroundColor = new Couleur(9, 12, 18);
        Couleur borderShadow = new Couleur(12, 14, 18);
        Couleur playfieldColor = new Couleur(16, 20, 28);
        Couleur centerGlow = new Couleur(212, 180, 132);
        Couleur sideGlow = new Couleur(120, 148, 164);
        Couleur frameColor = new Couleur(220, 212, 196);
        Couleur innerFrameColor = new Couleur(160, 132, 98);
        Couleur outerPanelColor = new Couleur(8, 10, 16);
        Couleur maskColor = new Couleur(10, 11, 16);
        int playfieldWidth = fieldRight - fieldLeft;
        int playfieldHeight = fieldTop - fieldBottom;

        window.ajouter(new Rectangle(backgroundColor, new Point(0, 0), width, height, true));
        addTextureIfPresent(Assets.ARENA_BACKGROUND, new Point(0, 0), width, height);
        window.ajouter(new Rectangle(borderShadow, new Point(fieldLeft - 34, fieldBottom - 34), playfieldWidth + 68, playfieldHeight + 68, true));
        window.ajouter(new Rectangle(playfieldColor, new Point(fieldLeft, fieldBottom), playfieldWidth, playfieldHeight, true));
        addTextureIfPresent(Assets.ARENA_FLOOR, new Point(fieldLeft, fieldBottom), playfieldWidth, playfieldHeight);

        int centerX = (fieldLeft + fieldRight) / 2;
        int leftGuide = fieldLeft + playfieldWidth / 4;
        int rightGuide = fieldRight - playfieldWidth / 4;
        window.ajouter(new Ligne(centerGlow, new Point(centerX, fieldBottom + 28), new Point(centerX, fieldTop - 28)));
        window.ajouter(new Ligne(sideGlow, new Point(leftGuide, fieldBottom + 44), new Point(leftGuide, fieldTop - 44)));
        window.ajouter(new Ligne(sideGlow, new Point(rightGuide, fieldBottom + 44), new Point(rightGuide, fieldTop - 44)));

        addForeground(new Rectangle(outerPanelColor, new Point(0, 0), fieldLeft, height, true));
        addForeground(new Rectangle(outerPanelColor, new Point(fieldRight, 0), width - fieldRight, height, true));
        addForeground(new Rectangle(maskColor, new Point(fieldLeft, 0), playfieldWidth, fieldBottom, true));
        addForeground(new Rectangle(maskColor, new Point(fieldLeft, fieldTop), playfieldWidth, height - fieldTop, true));
        addForeground(new Rectangle(frameColor, new Point(fieldLeft, fieldBottom), playfieldWidth, playfieldHeight, false));
        addForeground(new Rectangle(innerFrameColor, new Point(fieldLeft + 10, fieldBottom + 10), playfieldWidth - 20, playfieldHeight - 20, false));
        addForeground(new Rectangle(new Couleur(14, 12, 16), new Point(fieldLeft - 22, fieldBottom - 22), playfieldWidth + 44, 16, true));
        addForeground(new Rectangle(new Couleur(14, 12, 16), new Point(fieldLeft - 22, fieldTop + 6), playfieldWidth + 44, 16, true));
        addForeground(new Rectangle(new Couleur(14, 12, 16), new Point(fieldLeft - 22, fieldBottom - 6), 16, playfieldHeight + 12, true));
        addForeground(new Rectangle(new Couleur(14, 12, 16), new Point(fieldRight + 6, fieldBottom - 6), 16, playfieldHeight + 12, true));
    }
}
