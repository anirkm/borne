import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;

class HighScoreTable {
    private static final int MAX_ENTRIES = 10;

    private final String path;
    private final ArrayList<ScoreEntry> entries;

    private HighScoreTable(String path) {
        this.path = path;
        this.entries = new ArrayList<ScoreEntry>();
    }

    public static HighScoreTable load(String path) {
        HighScoreTable table = new HighScoreTable(path);
        table.read();
        return table;
    }

    public int getBestScore() {
        if (entries.isEmpty()) {
            return 0;
        }
        return entries.get(0).score;
    }

    public boolean qualifies(int score) {
        if (score <= 0) {
            return false;
        }
        if (entries.size() < MAX_ENTRIES) {
            return true;
        }
        return score > entries.get(entries.size() - 1).score;
    }

    public void record(String name, int score) {
        if (!qualifies(score)) {
            return;
        }

        entries.add(new ScoreEntry(sanitizeName(name), score));
        Collections.sort(entries);

        while (entries.size() > MAX_ENTRIES) {
            entries.remove(entries.size() - 1);
        }

        save();
    }

    private void read() {
        File file = new File(path);
        if (!file.exists()) {
            return;
        }

        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;

            while ((line = reader.readLine()) != null) {
                ScoreEntry entry = ScoreEntry.parse(line);
                if (entry != null) {
                    entries.add(entry);
                }
            }

            reader.close();
            Collections.sort(entries);
        } catch (Exception exception) {
            entries.clear();
        }
    }

    private void save() {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(path));
            for (int index = 0; index < entries.size(); index++) {
                writer.write(entries.get(index).toLine());
                if (index != entries.size() - 1) {
                    writer.write("\n");
                }
            }
            writer.close();
        } catch (Exception exception) {
            // Ignore save failures to keep the game playable.
        }
    }

    private String sanitizeName(String candidate) {
        String upper = candidate.toUpperCase();
        StringBuilder sanitized = new StringBuilder();

        for (int index = 0; index < upper.length() && sanitized.length() < 3; index++) {
            char current = upper.charAt(index);
            if (current >= 'A' && current <= 'Z') {
                sanitized.append(current);
            }
        }

        while (sanitized.length() < 3) {
            sanitized.append('A');
        }

        return sanitized.toString();
    }

    private static class ScoreEntry implements Comparable<ScoreEntry> {
        private final String name;
        private final int score;

        private ScoreEntry(String name, int score) {
            this.name = name;
            this.score = score;
        }

        private static ScoreEntry parse(String line) {
            String[] parts = line.split("-", 2);
            if (parts.length != 2) {
                return null;
            }

            try {
                int score = Integer.parseInt(parts[1].trim());
                String name = parts[0].trim();
                if (name.length() != 3) {
                    return null;
                }
                return new ScoreEntry(name, score);
            } catch (Exception exception) {
                return null;
            }
        }

        private String toLine() {
            return name + "-" + score;
        }

        public int compareTo(ScoreEntry other) {
            return other.score - score;
        }
    }
}
