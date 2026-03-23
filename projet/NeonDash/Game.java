import MG2D.FenetrePleinEcran;
import MG2D.geometrie.Cercle;
import MG2D.Couleur;
import MG2D.geometrie.Dessin;
import MG2D.geometrie.Ligne;
import MG2D.geometrie.Point;
import MG2D.geometrie.Rectangle;
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

    private static final int FRAME_TIME_MS = 16;
    private static final int MAX_PARTICLES = 220;

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
        window.ajouter(player.getDashRing());
        window.ajouter(player.getBody());
        window.ajouter(player.getCore());
        window.ajouter(player.getPointer());

        spawner = new Spawner();
        hud = new Hud(
            width,
            height,
            fieldLeft,
            fieldBottom,
            fieldRight,
            fieldTop,
            loadFont("../../fonts/PrStart.ttf", 46.0f, new Font("Dialog", Font.BOLD, 46)),
            loadFont("../../fonts/Volter__28Goldfish_29.ttf", 24.0f, new Font("Dialog", Font.PLAIN, 24)),
            loadFont("../../fonts/Volter__28Goldfish_29.ttf", 18.0f, new Font("Dialog", Font.PLAIN, 18))
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

        hud.updatePlaying(0, 1.0, spawner.getDifficultyLevel(0));
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

            if (delta > 0.05) {
                delta = 0.05;
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
            hud.showPaused(finalScore);
            layersNeedRefresh = true;
            return;
        }

        player.update(delta, keyboard, fieldLeft, fieldBottom, fieldRight, fieldTop);
        emitPlayerTrail(delta);
        if (player.consumeDashTriggered()) {
            emitDashBurst();
        }
        finalScore = getElapsedScore();

        if (spawner.update(delta, hazards, random, finalScore, fieldLeft, fieldBottom, fieldRight, fieldTop, window)) {
            layersNeedRefresh = true;
        }

        updateHazards(delta);
        emitHazardSparks(delta);
        hud.updatePlaying(finalScore, player.getDashChargeRatio(), spawner.getDifficultyLevel(finalScore));

        if (hasCollision()) {
            emitImpactBurst(player.getX(), player.getY(), player.getDirectionX(), player.getDirectionY());
            state = GameState.GAME_OVER;
            hud.showGameOver(finalScore, highScores.qualifies(finalScore));
            layersNeedRefresh = true;
        }
    }

    private void updatePaused(double delta) {
        player.tickVisual(delta);

        if (keyboard.getBoutonJ1ZTape()) {
            exitToMenu();
        }

        if (keyboard.getBoutonJ1BTape()) {
            state = GameState.PLAYING;
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
            if (highScores.qualifies(finalScore)) {
                state = GameState.NAME_ENTRY;
                nameBuffer = new char[] {'A', 'A', 'A'};
                nameSelection = 0;
                hud.showNameEntry(nameBuffer, nameSelection, finalScore);
                layersNeedRefresh = true;
            } else {
                exitToMenu();
            }
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
                exitToMenu();
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
        clearHazards();
        clearParticles();
        player.reset((fieldLeft + fieldRight) / 2, (fieldBottom + fieldTop) / 2);
        player.bringToFront(window);
        spawner.reset();
        playerTrailTimer = 0.0;
        hazardSparkTimer = 0.0;
        runStartMillis = System.currentTimeMillis();
        finalScore = 0;
        hud.hideOverlay();
        hud.updatePlaying(0, player.getDashChargeRatio(), spawner.getDifficultyLevel(0));
        layersNeedRefresh = true;
        state = GameState.PLAYING;
    }

    private void updateHazards(double delta) {
        for (int index = hazards.size() - 1; index >= 0; index--) {
            Hazard hazard = hazards.get(index);
            hazard.update(delta);
            if (hazard.isOffscreen(fieldLeft, fieldBottom, fieldRight, fieldTop)) {
                hazard.removeFrom(window);
                hazards.remove(index);
            }
        }
    }

    private boolean hasCollision() {
        if (player.isInvulnerable()) {
            return false;
        }
        for (int index = 0; index < hazards.size(); index++) {
            if (hazards.get(index).collides(player)) {
                return true;
            }
        }
        return false;
    }

    private int getElapsedScore() {
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

    private void addForeground(Dessin layer) {
        arenaForeground.add(layer);
        window.ajouter(layer);
    }

    private void buildArena() {
        Couleur backgroundColor = new Couleur(3, 5, 15);
        Couleur backdropColor = new Couleur(7, 10, 24);
        Couleur playfieldColor = new Couleur(9, 17, 40);
        Couleur gridColorA = new Couleur(20, 36, 82);
        Couleur gridColorB = new Couleur(24, 28, 62);
        Couleur frameColor = new Couleur(36, 212, 255);
        Couleur innerFrameColor = new Couleur(255, 103, 173);
        Couleur outerPanelColor = new Couleur(7, 10, 24);
        Couleur maskColor = new Couleur(5, 7, 20);
        Random decorRandom = new Random(7L);

        window.ajouter(new Rectangle(backgroundColor, new Point(0, 0), width, height, true));
        window.ajouter(new Rectangle(backdropColor, new Point(0, 0), width, height, true));

        for (int index = 0; index < 110; index++) {
            int radius = 1 + decorRandom.nextInt(3);
            int x = decorRandom.nextInt(Math.max(1, fieldRight - fieldLeft - 60)) + fieldLeft + 30;
            int y = decorRandom.nextInt(Math.max(1, fieldTop - fieldBottom - 60)) + fieldBottom + 30;

            Couleur starColor = index % 3 == 0
                ? new Couleur(18, 110, 170)
                : (index % 3 == 1 ? new Couleur(82, 30, 92) : new Couleur(90, 68, 26));
            window.ajouter(new Cercle(starColor, new Point(x, y), radius, true));
        }

        window.ajouter(new Rectangle(new Couleur(4, 7, 22), new Point(fieldLeft - 24, fieldBottom - 24), fieldRight - fieldLeft + 48, fieldTop - fieldBottom + 48, true));
        window.ajouter(new Rectangle(playfieldColor, new Point(fieldLeft, fieldBottom), fieldRight - fieldLeft, fieldTop - fieldBottom, true));

        for (int x = fieldLeft + 60; x < fieldRight; x += 60) {
            window.ajouter(new Ligne(gridColorA, new Point(x, fieldBottom), new Point(x, fieldTop)));
        }

        for (int y = fieldBottom + 60; y < fieldTop; y += 60) {
            window.ajouter(new Ligne(gridColorB, new Point(fieldLeft, y), new Point(fieldRight, y)));
        }

        for (int offset = 20; offset < fieldRight - fieldLeft; offset += 140) {
            int x1 = fieldLeft + offset;
            int y1 = fieldBottom;
            int x2 = Math.min(fieldRight, x1 + 180);
            int y2 = Math.min(fieldTop, fieldBottom + 180);
            window.ajouter(new Ligne(new Couleur(14, 22, 56), new Point(x1, y1), new Point(x2, y2)));
        }

        addForeground(new Rectangle(outerPanelColor, new Point(0, 0), fieldLeft, height, true));
        addForeground(new Rectangle(outerPanelColor, new Point(fieldRight, 0), width - fieldRight, height, true));
        addForeground(new Rectangle(maskColor, new Point(fieldLeft, 0), fieldRight - fieldLeft, fieldBottom, true));
        addForeground(new Rectangle(maskColor, new Point(fieldLeft, fieldTop), fieldRight - fieldLeft, height - fieldTop, true));
        addForeground(new Rectangle(frameColor, new Point(fieldLeft, fieldBottom), fieldRight - fieldLeft, fieldTop - fieldBottom, false));
        addForeground(new Rectangle(innerFrameColor, new Point(fieldLeft + 8, fieldBottom + 8), fieldRight - fieldLeft - 16, fieldTop - fieldBottom - 16, false));
        addForeground(new Rectangle(new Couleur(10, 16, 42), new Point(fieldLeft - 18, fieldBottom - 18), fieldRight - fieldLeft + 36, 14, true));
        addForeground(new Rectangle(new Couleur(10, 16, 42), new Point(fieldLeft - 18, fieldTop + 4), fieldRight - fieldLeft + 36, 14, true));
        addForeground(new Rectangle(new Couleur(10, 16, 42), new Point(fieldLeft - 18, fieldBottom - 4), 14, fieldTop - fieldBottom + 8, true));
        addForeground(new Rectangle(new Couleur(10, 16, 42), new Point(fieldRight + 4, fieldBottom - 4), 14, fieldTop - fieldBottom + 8, true));
    }
}
