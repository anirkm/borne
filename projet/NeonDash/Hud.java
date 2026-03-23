import MG2D.Fenetre;
import MG2D.Couleur;
import MG2D.geometrie.Point;
import MG2D.geometrie.Rectangle;
import MG2D.geometrie.Texte;
import MG2D.geometrie.Triangle;
import java.awt.Font;

class Hud {
    private static final Couleur HUD_BG = new Couleur(6, 10, 28);
    private static final Couleur PANEL_BG = new Couleur(10, 16, 40);
    private static final Couleur PANEL_ALT = new Couleur(16, 24, 56);
    private static final Couleur FRAME_COLOR = new Couleur(42, 228, 255);
    private static final Couleur TEXT_COLOR = new Couleur(232, 246, 255);
    private static final Couleur ACCENT_COLOR = new Couleur(255, 189, 72);
    private static final Couleur READY_COLOR = new Couleur(77, 255, 219);
    private static final Couleur COOLING_COLOR = new Couleur(255, 108, 173);

    private final int width;
    private final int height;
    private final Font titleFont;
    private final Font bodyFont;
    private final Font smallFont;
    private final int panelLeft;
    private final int panelRight;
    private final int panelBottom;
    private final int panelTop;
    private final int[] nameSlotX;

    private Fenetre window;

    private final Rectangle hudBand;
    private final Rectangle cooldownTrack;
    private final Rectangle cooldownFill;
    private final Rectangle overlayPanel;
    private final Rectangle overlayFrame;
    private final Rectangle leftInfoPanel;
    private final Rectangle centerInfoPanel;
    private final Rectangle rightInfoPanel;

    private final Texte brandText;
    private final Texte scoreText;
    private final Texte dashText;
    private final Texte difficultyText;
    private final Texte hintText;

    private final Texte overlayTitle;
    private final Texte overlayLine1;
    private final Texte overlayLine2;
    private final Texte overlayLine3;
    private final Texte overlayLine4;
    private final Texte overlayLine5;
    private final Texte[] nameSlots;
    private final Triangle selector;

    Hud(
        int width,
        int height,
        int fieldLeft,
        int fieldBottom,
        int fieldRight,
        int fieldTop,
        Font titleFont,
        Font bodyFont,
        Font smallFont
    ) {
        this.width = width;
        this.height = height;
        this.titleFont = titleFont;
        this.bodyFont = bodyFont;
        this.smallFont = smallFont;
        this.panelLeft = fieldLeft + 90;
        this.panelRight = fieldRight - 90;
        this.panelBottom = fieldBottom + 80;
        this.panelTop = fieldTop - 70;

        int centerX = width / 2;
        this.nameSlotX = new int[] {
            centerX - 150,
            centerX - 35,
            centerX + 80,
            centerX + 240
        };

        hudBand = new Rectangle(HUD_BG, new Point(0, height - 104), width, 104, true);
        leftInfoPanel = new Rectangle(PANEL_BG, new Point(24, height - 90), 248, 64, true);
        centerInfoPanel = new Rectangle(PANEL_BG, new Point(width / 2 - 130, height - 90), 260, 64, true);
        rightInfoPanel = new Rectangle(PANEL_BG, new Point(width - 350, height - 90), 300, 64, true);
        cooldownTrack = new Rectangle(new Couleur(32, 42, 78), new Point(width - 330, height - 82), 226, 22, true);
        cooldownFill = new Rectangle(READY_COLOR, new Point(width - 330, height - 82), 226, 22, true);
        overlayPanel = new Rectangle(PANEL_BG, new Point(-5000, -5000), 10, 10, true);
        overlayFrame = new Rectangle(FRAME_COLOR, new Point(-5000, -5000), 10, 10, false);

        brandText = new Texte(ACCENT_COLOR, "NEON DASH", bodyFont, new Point(width / 2, height - 20));
        scoreText = new Texte(TEXT_COLOR, "Score  0 s", bodyFont, new Point(148, height - 56));
        difficultyText = new Texte(TEXT_COLOR, "Niveau 1", bodyFont, new Point(width / 2, height - 56));
        dashText = new Texte(TEXT_COLOR, "Dash PRET", bodyFont, new Point(width - 218, height - 34));
        hintText = new Texte(TEXT_COLOR, "", smallFont, new Point(width / 2, height - 82));

        overlayTitle = new Texte(TEXT_COLOR, "", titleFont, new Point(centerX, panelTop - 70));
        overlayLine1 = new Texte(TEXT_COLOR, "", bodyFont, new Point(centerX, panelTop - 150));
        overlayLine2 = new Texte(TEXT_COLOR, "", bodyFont, new Point(centerX, panelTop - 195));
        overlayLine3 = new Texte(TEXT_COLOR, "", bodyFont, new Point(centerX, panelTop - 240));
        overlayLine4 = new Texte(TEXT_COLOR, "", bodyFont, new Point(centerX, panelTop - 285));
        overlayLine5 = new Texte(ACCENT_COLOR, "", bodyFont, new Point(centerX, panelTop - 350));

        nameSlots = new Texte[4];
        nameSlots[0] = new Texte(TEXT_COLOR, "", titleFont, new Point(nameSlotX[0], panelBottom + 120));
        nameSlots[1] = new Texte(TEXT_COLOR, "", titleFont, new Point(nameSlotX[1], panelBottom + 120));
        nameSlots[2] = new Texte(TEXT_COLOR, "", titleFont, new Point(nameSlotX[2], panelBottom + 120));
        nameSlots[3] = new Texte(TEXT_COLOR, "", bodyFont, new Point(nameSlotX[3], panelBottom + 118));
        selector = new Triangle(
            ACCENT_COLOR,
            new Point(-5000, -5000),
            new Point(-4990, -5020),
            new Point(-5010, -5020),
            true
        );
    }

    public void addTo(Fenetre window) {
        this.window = window;
        window.ajouter(hudBand);
        window.ajouter(leftInfoPanel);
        window.ajouter(centerInfoPanel);
        window.ajouter(rightInfoPanel);
        window.ajouter(cooldownTrack);
        window.ajouter(cooldownFill);
        window.ajouter(overlayPanel);
        window.ajouter(overlayFrame);
        window.ajouter(brandText);
        window.ajouter(scoreText);
        window.ajouter(difficultyText);
        window.ajouter(dashText);
        window.ajouter(hintText);
        window.ajouter(overlayTitle);
        window.ajouter(overlayLine1);
        window.ajouter(overlayLine2);
        window.ajouter(overlayLine3);
        window.ajouter(overlayLine4);
        window.ajouter(overlayLine5);
        window.ajouter(nameSlots[0]);
        window.ajouter(nameSlots[1]);
        window.ajouter(nameSlots[2]);
        window.ajouter(nameSlots[3]);
        window.ajouter(selector);
        hideOverlay();
    }

    public void bringToFront(Fenetre window) {
        window.supprimer(hudBand);
        window.supprimer(leftInfoPanel);
        window.supprimer(centerInfoPanel);
        window.supprimer(rightInfoPanel);
        window.supprimer(cooldownTrack);
        window.supprimer(cooldownFill);
        window.supprimer(overlayPanel);
        window.supprimer(overlayFrame);
        window.supprimer(brandText);
        window.supprimer(scoreText);
        window.supprimer(difficultyText);
        window.supprimer(dashText);
        window.supprimer(hintText);
        window.supprimer(overlayTitle);
        window.supprimer(overlayLine1);
        window.supprimer(overlayLine2);
        window.supprimer(overlayLine3);
        window.supprimer(overlayLine4);
        window.supprimer(overlayLine5);
        window.supprimer(nameSlots[0]);
        window.supprimer(nameSlots[1]);
        window.supprimer(nameSlots[2]);
        window.supprimer(nameSlots[3]);
        window.supprimer(selector);
        window.ajouter(hudBand);
        window.ajouter(leftInfoPanel);
        window.ajouter(centerInfoPanel);
        window.ajouter(rightInfoPanel);
        window.ajouter(cooldownTrack);
        window.ajouter(cooldownFill);
        window.ajouter(overlayPanel);
        window.ajouter(overlayFrame);
        window.ajouter(brandText);
        window.ajouter(scoreText);
        window.ajouter(difficultyText);
        window.ajouter(dashText);
        window.ajouter(hintText);
        window.ajouter(overlayTitle);
        window.ajouter(overlayLine1);
        window.ajouter(overlayLine2);
        window.ajouter(overlayLine3);
        window.ajouter(overlayLine4);
        window.ajouter(overlayLine5);
        window.ajouter(nameSlots[0]);
        window.ajouter(nameSlots[1]);
        window.ajouter(nameSlots[2]);
        window.ajouter(nameSlots[3]);
        window.ajouter(selector);
    }

    public void updatePlaying(int score, double dashRatio, int difficultyLevel) {
        scoreText.setTexte("Score  " + score + " s");
        difficultyText.setTexte("Niveau " + difficultyLevel);
        hintText.setTexte("A traverse la vague   B pause   Z menu");

        if (dashRatio >= 1.0) {
            dashText.setTexte("Dash PRET");
            cooldownFill.setCouleur(READY_COLOR);
        } else {
            int percent = (int) Math.round(dashRatio * 100.0);
            dashText.setTexte("Dash " + percent + "%");
            cooldownFill.setCouleur(COOLING_COLOR);
        }

        int fillWidth = (int) Math.round(226.0 * Math.max(0.0, Math.min(1.0, dashRatio)));
        cooldownFill.setLargeur(fillWidth);
    }

    public void showIntro(int bestScore) {
        setOverlayVisible(true);
        setOverlayText("NEON DASH");
        overlayLine1.setTexte("Traversez les vagues et tenez le plus longtemps possible.");
        overlayLine2.setTexte("Joystick J1 : deplacement");
        overlayLine3.setTexte("A : dash invulnerable   B : pause   Z : quitter");
        if (bestScore > 0) {
            overlayLine4.setTexte("Meilleur score : " + bestScore + " s");
        } else {
            overlayLine4.setTexte("Aucun score enregistre pour le moment.");
        }
        overlayLine5.setTexte("Appuyez sur A pour lancer une partie.");
        clearNameEntry();
    }

    public void showPaused(int score) {
        setOverlayVisible(true);
        setOverlayText("PAUSE");
        overlayLine1.setTexte("Score actuel : " + score + " s");
        overlayLine2.setTexte("B pour reprendre la partie");
        overlayLine3.setTexte("Z pour revenir au menu");
        overlayLine4.setTexte("");
        overlayLine5.setTexte("Le dash se recharge aussi pendant la pause.");
        clearNameEntry();
    }

    public void showGameOver(int score, boolean qualifies) {
        setOverlayVisible(true);
        setOverlayText("GAME OVER");
        overlayLine1.setTexte("Score final : " + score + " s");
        if (qualifies) {
            overlayLine2.setTexte("Vous avez atteint le tableau des scores.");
            overlayLine3.setTexte("A pour entrer vos initiales");
        } else {
            overlayLine2.setTexte("A pour revenir au menu");
            overlayLine3.setTexte("Z pour quitter tout de suite");
        }
        overlayLine4.setTexte("Lisez les motifs et gardez votre dash pour le vrai danger.");
        overlayLine5.setTexte(qualifies ? "Le score sera sauvegarde au format AAA-score." : "");
        clearNameEntry();
    }

    public void showNameEntry(char[] name, int selectedIndex, int score) {
        setOverlayVisible(true);
        setOverlayText("HIGH SCORE");
        overlayLine1.setTexte("Score : " + score + " s");
        overlayLine2.setTexte("Joystick : changer les lettres et la position");
        overlayLine3.setTexte("A sur OK pour sauvegarder");
        overlayLine4.setTexte("Z pour quitter sans enregistrer");
        overlayLine5.setTexte("");

        nameSlots[0].setTexte(String.valueOf(name[0]));
        nameSlots[1].setTexte(String.valueOf(name[1]));
        nameSlots[2].setTexte(String.valueOf(name[2]));
        nameSlots[3].setTexte("OK");

        for (int index = 0; index < nameSlots.length; index++) {
            if (index == selectedIndex) {
                nameSlots[index].setCouleur(ACCENT_COLOR);
            } else {
                nameSlots[index].setCouleur(TEXT_COLOR);
            }
        }

        moveSelector(nameSlotX[selectedIndex], panelBottom + 185);
    }

    public void hideOverlay() {
        setOverlayVisible(false);
        overlayTitle.setTexte("");
        overlayLine1.setTexte("");
        overlayLine2.setTexte("");
        overlayLine3.setTexte("");
        overlayLine4.setTexte("");
        overlayLine5.setTexte("");
        clearNameEntry();
    }

    private void setOverlayText(String title) {
        overlayTitle.setTexte(title);
    }

    private void clearNameEntry() {
        nameSlots[0].setTexte("");
        nameSlots[1].setTexte("");
        nameSlots[2].setTexte("");
        nameSlots[3].setTexte("");
        moveSelector(-5000, -5000);
    }

    private void setOverlayVisible(boolean visible) {
        if (!visible) {
            overlayPanel.setA(new Point(-5000, -5000));
            overlayPanel.setB(new Point(-4990, -4990));
            overlayFrame.setA(new Point(-5000, -5000));
            overlayFrame.setB(new Point(-4990, -4990));
            return;
        }

        overlayPanel.setB(new Point(panelRight, panelTop));
        overlayPanel.setA(new Point(panelLeft, panelBottom));
        overlayPanel.setCouleur(PANEL_ALT);
        overlayFrame.setB(new Point(panelRight, panelTop));
        overlayFrame.setA(new Point(panelLeft, panelBottom));
    }

    private void moveSelector(int centerX, int topY) {
        selector.setA(new Point(centerX, topY));
        selector.setB(new Point(centerX - 16, topY - 28));
        selector.setC(new Point(centerX + 16, topY - 28));
    }
}
