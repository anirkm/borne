import MG2D.Fenetre;
import MG2D.Couleur;
import java.util.ArrayList;
import java.util.Random;

class Spawner {
    private static final int MAX_WALL_SIZE = 6;

    private static final Couleur[] CORE_COLORS = {
        new Couleur(255, 96, 189),
        new Couleur(255, 176, 52),
        new Couleur(112, 173, 255)
    };

    private static final Couleur[] GLOW_COLORS = {
        new Couleur(255, 150, 218),
        new Couleur(255, 225, 122),
        new Couleur(164, 212, 255)
    };

    private static final Couleur[] TRAIL_COLORS = {
        new Couleur(255, 96, 189),
        new Couleur(255, 176, 52),
        new Couleur(112, 173, 255)
    };

    private double spawnTimer;

    public void reset() {
        spawnTimer = 0.0;
    }

    public int getDifficultyLevel(int elapsedSeconds) {
        return 1 + elapsedSeconds / 12;
    }

    public boolean update(
        double delta,
        ArrayList<Hazard> hazards,
        Random random,
        int elapsedSeconds,
        int left,
        int bottom,
        int right,
        int top,
        Fenetre window
    ) {
        boolean spawnedAny = false;
        spawnTimer += delta;

        double interval = getSpawnInterval(elapsedSeconds);
        int maxHazards = getMaxHazards(elapsedSeconds);

        while (spawnTimer >= interval && hazards.size() < maxHazards) {
            spawnTimer -= interval;
            ArrayList<Hazard> wave = createWave(random, elapsedSeconds, left, bottom, right, top, maxHazards - hazards.size());
            for (int index = 0; index < wave.size(); index++) {
                Hazard hazard = wave.get(index);
                hazards.add(hazard);
                hazard.addTo(window);
                spawnedAny = true;
            }
        }

        if (spawnTimer > interval * 2.0) {
            spawnTimer = interval * 2.0;
        }

        return spawnedAny;
    }

    private double getSpawnInterval(int elapsedSeconds) {
        double interval = 0.92 - elapsedSeconds * 0.022;
        if (interval < 0.24) {
            return 0.24;
        }
        return interval;
    }

    private int getMaxHazards(int elapsedSeconds) {
        int limit = 5 + elapsedSeconds / 4;
        if (limit > 20) {
            return 20;
        }
        return limit;
    }

    private ArrayList<Hazard> createWave(Random random, int elapsedSeconds, int left, int bottom, int right, int top, int room) {
        ArrayList<Hazard> wave = new ArrayList<Hazard>();
        int patternRoll = random.nextInt(100);

        if (elapsedSeconds >= 15 && room >= 5 && patternRoll < 28) {
            addLaneSweepWave(wave, random, elapsedSeconds, left, bottom, right, top, room);
            return wave;
        }

        if (elapsedSeconds >= 9 && room >= 3 && patternRoll < 60) {
            addSpreadWave(wave, random, elapsedSeconds, left, bottom, right, top, room);
            return wave;
        }

        if (elapsedSeconds >= 5 && room >= 2 && patternRoll < 84) {
            addPincerWave(wave, random, elapsedSeconds, left, bottom, right, top, room);
            return wave;
        }

        int startSide = random.nextInt(4);
        wave.add(createTraveler(random, elapsedSeconds, left, bottom, right, top, 18 + random.nextInt(10), 1.0, startSide, pickDifferentSide(random, startSide)));
        return wave;
    }

    private void addPincerWave(ArrayList<Hazard> wave, Random random, int elapsedSeconds, int left, int bottom, int right, int top, int room) {
        boolean horizontal = random.nextBoolean();
        int radius = 18 + random.nextInt(10);
        int inset = radius + 14;

        if (horizontal) {
            int y1 = randomBetween(random, bottom + inset, top - inset);
            int y2 = clamp(y1 + (random.nextBoolean() ? 120 : -120), bottom + inset, top - inset);
            wave.add(createHorizontalTraveler(left + inset, right - inset, y1, elapsedSeconds, radius, 1.04, random));
            if (room >= 2) {
                wave.add(createHorizontalTraveler(right - inset, left + inset, y2, elapsedSeconds, radius, 1.04, random));
            }
        } else {
            int x1 = randomBetween(random, left + inset, right - inset);
            int x2 = clamp(x1 + (random.nextBoolean() ? 150 : -150), left + inset, right - inset);
            wave.add(createVerticalTraveler(bottom + inset, top - inset, x1, elapsedSeconds, radius, 1.04, random));
            if (room >= 2) {
                wave.add(createVerticalTraveler(top - inset, bottom + inset, x2, elapsedSeconds, radius, 1.04, random));
            }
        }
    }

    private void addSpreadWave(ArrayList<Hazard> wave, Random random, int elapsedSeconds, int left, int bottom, int right, int top, int room) {
        int side = random.nextInt(4);
        int count = Math.min(room, 3);

        for (int index = 0; index < count; index++) {
            int radius = 16 + random.nextInt(9);
            int startSide = side;
            int targetSide = (side + 2) % 4;
            if (index == 1 && random.nextBoolean()) {
                targetSide = pickDifferentSide(random, startSide);
            }
            wave.add(createTraveler(random, elapsedSeconds, left, bottom, right, top, radius, 0.92 + index * 0.07, startSide, targetSide));
        }
    }

    private void addLaneSweepWave(ArrayList<Hazard> wave, Random random, int elapsedSeconds, int left, int bottom, int right, int top, int room) {
        boolean horizontal = random.nextBoolean();
        int wallSize = Math.min(room + 1, MAX_WALL_SIZE);
        int gapIndex = random.nextInt(wallSize);
        int radius = 18;
        int inset = radius + 12;

        if (horizontal) {
            boolean leftToRight = random.nextBoolean();
            int startX = leftToRight ? left + inset : right - inset;
            int targetX = leftToRight ? right - inset : left + inset;
            for (int index = 0; index < wallSize; index++) {
                if (index == gapIndex) {
                    continue;
                }
                int y = bottom + inset + index * Math.max(1, (top - bottom - inset * 2) / Math.max(1, wallSize - 1));
                wave.add(createHorizontalTraveler(startX, targetX, y, elapsedSeconds, radius, 0.94, random));
            }
        } else {
            boolean bottomToTop = random.nextBoolean();
            int startY = bottomToTop ? bottom + inset : top - inset;
            int targetY = bottomToTop ? top - inset : bottom + inset;
            for (int index = 0; index < wallSize; index++) {
                if (index == gapIndex) {
                    continue;
                }
                int x = left + inset + index * Math.max(1, (right - left - inset * 2) / Math.max(1, wallSize - 1));
                wave.add(createVerticalTraveler(startY, targetY, x, elapsedSeconds, radius, 0.94, random));
            }
        }
    }

    private Hazard createTraveler(
        Random random,
        int elapsedSeconds,
        int left,
        int bottom,
        int right,
        int top,
        int radius,
        double speedMultiplier,
        int startSide,
        int targetSide
    ) {
        int[] start = edgePoint(random, startSide, radius, left, bottom, right, top);
        int[] target = edgePoint(random, targetSide, radius, left, bottom, right, top);
        return createTravelingHazard(start[0], start[1], target[0], target[1], elapsedSeconds, radius, speedMultiplier, random);
    }

    private Hazard createHorizontalTraveler(int startX, int targetX, int y, int elapsedSeconds, int radius, double speedMultiplier, Random random) {
        return createTravelingHazard(startX, y, targetX, y, elapsedSeconds, radius, speedMultiplier, random);
    }

    private Hazard createVerticalTraveler(int startY, int targetY, int x, int elapsedSeconds, int radius, double speedMultiplier, Random random) {
        return createTravelingHazard(x, startY, x, targetY, elapsedSeconds, radius, speedMultiplier, random);
    }

    private Hazard createTravelingHazard(
        int startX,
        int startY,
        int targetX,
        int targetY,
        int elapsedSeconds,
        int radius,
        double speedMultiplier,
        Random random
    ) {
        double directionX = targetX - startX;
        double directionY = targetY - startY;
        double length = Math.sqrt(directionX * directionX + directionY * directionY);

        if (length == 0.0) {
            directionX = 1.0;
            directionY = 0.0;
            length = 1.0;
        }

        directionX /= length;
        directionY /= length;

        double speed = (235.0 + elapsedSeconds * 12.0 + random.nextInt(75)) * speedMultiplier;
        int palette = random.nextInt(CORE_COLORS.length);

        return new Hazard(
            startX,
            startY,
            radius,
            directionX * speed,
            directionY * speed,
            TRAIL_COLORS[palette],
            CORE_COLORS[palette],
            GLOW_COLORS[palette]
        );
    }

    private int[] edgePoint(Random random, int side, int radius, int left, int bottom, int right, int top) {
        int inset = radius + 14;

        if (side == 0) {
            return new int[] {left + inset, randomBetween(random, bottom + inset, top - inset)};
        }
        if (side == 1) {
            return new int[] {right - inset, randomBetween(random, bottom + inset, top - inset)};
        }
        if (side == 2) {
            return new int[] {randomBetween(random, left + inset, right - inset), bottom + inset};
        }
        return new int[] {randomBetween(random, left + inset, right - inset), top - inset};
    }

    private int pickDifferentSide(Random random, int currentSide) {
        int side = random.nextInt(4);
        while (side == currentSide) {
            side = random.nextInt(4);
        }
        return side;
    }

    private int randomBetween(Random random, int min, int max) {
        if (max <= min) {
            return min;
        }
        return min + random.nextInt(max - min + 1);
    }

    private int clamp(int value, int min, int max) {
        if (value < min) {
            return min;
        }
        if (value > max) {
            return max;
        }
        return value;
    }
}
