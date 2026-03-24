import MG2D.Fenetre;
import MG2D.Couleur;
import MG2D.geometrie.Point;
import MG2D.geometrie.Rectangle;
import MG2D.geometrie.Texture;
import MG2D.geometrie.Texte;
import MG2D.geometrie.Triangle;
import java.awt.Font;

class Hud {
    private static final Couleur HUD_BG = new Couleur(10, 14, 20);
    private static final Couleur PANEL_BG = new Couleur(19, 27, 37);
    private static final Couleur PANEL_ALT = new Couleur(22, 31, 44);
    private static final Couleur FRAME_COLOR = new Couleur(86, 141, 193);
    private static final Couleur TEXT_COLOR = new Couleur(234, 241, 249);
    private static final Couleur ACCENT_COLOR = new Couleur(110, 200, 255);
    private static final Couleur READY_COLOR = new Couleur(118, 255, 213);
    private static final Couleur COOLING_COLOR = new Couleur(255, 193, 102);
    private static final int COOLDOWN_TRACK_WIDTH = 108;

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
    private final Rectangle footerBand;
    private final Rectangle cooldownTrack;
    private final Rectangle cooldownFill;
    private final Rectangle overlayPanel;
    private final Rectangle overlayFrame;
    private final Rectangle leftInfoPanel;
    private final Rectangle centerInfoPanel;
    private final Rectangle rightInfoPanel;
    private final Rectangle footerLeftPanel;
    private final Rectangle footerCenterPanel;
    private final Rectangle footerRightPanel;
    private final Rectangle rightInfoInset;
    private final Rectangle rightInfoAccent;
    private final Rectangle rightInfoDivider;

    private final Texte brandText;
    private final Texte scoreText;
    private final Texte dashText;
    private final Texte difficultyText;
    private final Texte hintText;
    private final Texte statsText;
    private final Texte livesLabel;
    private final Texte systemsLabel;

    private final Texte overlayTitle;
    private final Texte overlayLine1;
    private final Texte overlayLine2;
    private final Texte overlayLine3;
    private final Texte overlayLine4;
    private final Texte overlayLine5;
    private final Texte[] nameSlots;
    private final Texture[] hearts;
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

        hudBand = new Rectangle(HUD_BG, new Point(0, height - 112), width, 112, true);
        leftInfoPanel = new Rectangle(PANEL_BG, new Point(24, height - 96), 300, 68, true);
        centerInfoPanel = new Rectangle(PANEL_BG, new Point(width / 2 - 170, height - 96), 340, 68, true);
        rightInfoPanel = new Rectangle(PANEL_BG, new Point(width - 324, height - 96), 300, 68, true);
        footerBand = new Rectangle(HUD_BG, new Point(0, 0), width, 64, true);
        footerLeftPanel = new Rectangle(PANEL_BG, new Point(24, 10), 232, 44, true);
        footerCenterPanel = new Rectangle(PANEL_BG, new Point(width / 2 - 280, 10), 560, 44, true);
        footerRightPanel = new Rectangle(PANEL_BG, new Point(width - 356, 10), 320, 44, true);
        rightInfoInset = new Rectangle(new Couleur(13, 20, 30), new Point(width - 344, 14), 296, 36, true);
        rightInfoAccent = new Rectangle(ACCENT_COLOR, new Point(width - 338, 16), 4, 32, true);
        rightInfoDivider = new Rectangle(new Couleur(39, 58, 76), new Point(width - 198, 14), 2, 36, true);
        cooldownTrack = new Rectangle(new Couleur(37, 53, 67), new Point(width - 174, 20), COOLDOWN_TRACK_WIDTH, 10, true);
        cooldownFill = new Rectangle(READY_COLOR, new Point(width - 174, 20), COOLDOWN_TRACK_WIDTH, 10, true);
        overlayPanel = new Rectangle(PANEL_BG, new Point(-5000, -5000), 10, 10, true);
        overlayFrame = new Rectangle(FRAME_COLOR, new Point(-5000, -5000), 10, 10, false);

        brandText = new Texte(ACCENT_COLOR, "NEON DASH", bodyFont, new Point(width / 2, height - 18));
        scoreText = new Texte(TEXT_COLOR, "Score 0", bodyFont, new Point(44, height - 58), Texte.GAUCHE);
        difficultyText = new Texte(TEXT_COLOR, "Niveau 1", bodyFont, new Point(width / 2, height - 58));
        dashText = new Texte(TEXT_COLOR, "2/2", smallFont, new Point(width - 314, 23), Texte.GAUCHE);
        hintText = new Texte(TEXT_COLOR, "", smallFont, new Point(width / 2, 32));
        statsText = new Texte(ACCENT_COLOR, "", smallFont, new Point(width - 300, height - 58), Texte.GAUCHE);
        livesLabel = new Texte(ACCENT_COLOR, "Vies", smallFont, new Point(44, 32), Texte.GAUCHE);
        systemsLabel = new Texte(ACCENT_COLOR, "Dash", smallFont, new Point(width - 314, 42), Texte.GAUCHE);

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
        hearts = new Texture[] {
            new Texture(Assets.HUD_HEART_FULL, new Point(108, 11), 24, 21),
            new Texture(Assets.HUD_HEART_FULL, new Point(136, 11), 24, 21),
            new Texture(Assets.HUD_HEART_FULL, new Point(164, 11), 24, 21)
        };
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
        window.ajouter(footerBand);
        window.ajouter(leftInfoPanel);
        window.ajouter(centerInfoPanel);
        window.ajouter(rightInfoPanel);
        window.ajouter(footerLeftPanel);
        window.ajouter(footerCenterPanel);
        window.ajouter(footerRightPanel);
        window.ajouter(rightInfoInset);
        window.ajouter(rightInfoAccent);
        window.ajouter(rightInfoDivider);
        window.ajouter(cooldownTrack);
        window.ajouter(cooldownFill);
        window.ajouter(overlayPanel);
        window.ajouter(overlayFrame);
        window.ajouter(brandText);
        window.ajouter(scoreText);
        window.ajouter(difficultyText);
        window.ajouter(dashText);
        window.ajouter(hintText);
        window.ajouter(statsText);
        window.ajouter(livesLabel);
        window.ajouter(systemsLabel);
        window.ajouter(overlayTitle);
        window.ajouter(overlayLine1);
        window.ajouter(overlayLine2);
        window.ajouter(overlayLine3);
        window.ajouter(overlayLine4);
        window.ajouter(overlayLine5);
        window.ajouter(hearts[0]);
        window.ajouter(hearts[1]);
        window.ajouter(hearts[2]);
        window.ajouter(nameSlots[0]);
        window.ajouter(nameSlots[1]);
        window.ajouter(nameSlots[2]);
        window.ajouter(nameSlots[3]);
        window.ajouter(selector);
        hideOverlay();
    }

    public void bringToFront(Fenetre window) {
        window.supprimer(hudBand);
        window.supprimer(footerBand);
        window.supprimer(leftInfoPanel);
        window.supprimer(centerInfoPanel);
        window.supprimer(rightInfoPanel);
        window.supprimer(footerLeftPanel);
        window.supprimer(footerCenterPanel);
        window.supprimer(footerRightPanel);
        window.supprimer(rightInfoInset);
        window.supprimer(rightInfoAccent);
        window.supprimer(rightInfoDivider);
        window.supprimer(cooldownTrack);
        window.supprimer(cooldownFill);
        window.supprimer(overlayPanel);
        window.supprimer(overlayFrame);
        window.supprimer(brandText);
        window.supprimer(scoreText);
        window.supprimer(difficultyText);
        window.supprimer(dashText);
        window.supprimer(hintText);
        window.supprimer(statsText);
        window.supprimer(livesLabel);
        window.supprimer(systemsLabel);
        window.supprimer(overlayTitle);
        window.supprimer(overlayLine1);
        window.supprimer(overlayLine2);
        window.supprimer(overlayLine3);
        window.supprimer(overlayLine4);
        window.supprimer(overlayLine5);
        window.supprimer(hearts[0]);
        window.supprimer(hearts[1]);
        window.supprimer(hearts[2]);
        window.supprimer(nameSlots[0]);
        window.supprimer(nameSlots[1]);
        window.supprimer(nameSlots[2]);
        window.supprimer(nameSlots[3]);
        window.supprimer(selector);
        window.ajouter(hudBand);
        window.ajouter(footerBand);
        window.ajouter(leftInfoPanel);
        window.ajouter(centerInfoPanel);
        window.ajouter(rightInfoPanel);
        window.ajouter(footerLeftPanel);
        window.ajouter(footerCenterPanel);
        window.ajouter(footerRightPanel);
        window.ajouter(rightInfoInset);
        window.ajouter(rightInfoAccent);
        window.ajouter(rightInfoDivider);
        window.ajouter(cooldownTrack);
        window.ajouter(cooldownFill);
        window.ajouter(overlayPanel);
        window.ajouter(overlayFrame);
        window.ajouter(brandText);
        window.ajouter(scoreText);
        window.ajouter(difficultyText);
        window.ajouter(dashText);
        window.ajouter(hintText);
        window.ajouter(statsText);
        window.ajouter(livesLabel);
        window.ajouter(systemsLabel);
        window.ajouter(overlayTitle);
        window.ajouter(overlayLine1);
        window.ajouter(overlayLine2);
        window.ajouter(overlayLine3);
        window.ajouter(overlayLine4);
        window.ajouter(overlayLine5);
        window.ajouter(hearts[0]);
        window.ajouter(hearts[1]);
        window.ajouter(hearts[2]);
        window.ajouter(nameSlots[0]);
        window.ajouter(nameSlots[1]);
        window.ajouter(nameSlots[2]);
        window.ajouter(nameSlots[3]);
        window.ajouter(selector);
    }

    public void updatePlaying(int score, int livesRemaining, double dashRatio, int dashCharges, int dashCapacity, int difficultyLevel, int comboMultiplier, int shatteredCount, String message) {
        scoreText.setTexte("Score " + score);
        difficultyText.setTexte("Niveau " + difficultyLevel + "   Flux x" + comboMultiplier);
        hintText.setTexte(message == null || message.length() == 0 ? "PC  Fleches   F dash x2   G pause   Y menu" : message);
        statsText.setTexte("Brisees " + shatteredCount);

        for (int index = 0; index < hearts.length; index++) {
            hearts[index].setImg(index < livesRemaining ? Assets.HUD_HEART_FULL : Assets.HUD_HEART_EMPTY);
        }

        if (dashCharges >= dashCapacity) {
            dashText.setTexte(dashCharges + "/" + dashCapacity + " READY");
            cooldownFill.setCouleur(READY_COLOR);
        } else {
            int percent = (int) Math.round(dashRatio * 100.0);
            dashText.setTexte(dashCharges + "/" + dashCapacity + "  " + percent + "%");
            cooldownFill.setCouleur(COOLING_COLOR);
        }

        double stockRatio = (dashCharges + (dashCharges < dashCapacity ? dashRatio : 0.0)) / (double) dashCapacity;
        int fillWidth = (int) Math.round(COOLDOWN_TRACK_WIDTH * Math.max(0.0, Math.min(1.0, stockRatio)));
        cooldownFill.setLargeur(fillWidth);
    }

    public void showIntro(int bestScore) {
        setOverlayVisible(true);
        setOverlayText("NEON DASH");
        overlayLine1.setTexte("Survivez, frôlez et cassez les orbes au dash.");
        overlayLine2.setTexte("PC : Fleches pour bouger");
        overlayLine3.setTexte("F : double dash   G : pause   Y : retour menu");
        if (bestScore > 0) {
            overlayLine4.setTexte("Meilleur score : " + bestScore);
        } else {
            overlayLine4.setTexte("Aucun score enregistre pour le moment.");
        }
        overlayLine5.setTexte("3 vies, 2 charges de dash et restart instantane.");
        clearNameEntry();
    }

    public void showPaused(int score) {
        setOverlayVisible(true);
        setOverlayText("PAUSE");
        overlayLine1.setTexte("Score actuel : " + score);
        overlayLine2.setTexte("G pour reprendre la partie");
        overlayLine3.setTexte("F pour recommencer tout de suite");
        overlayLine4.setTexte("Y pour revenir au menu");
        overlayLine5.setTexte("La partie repart sans relancer le script.");
        clearNameEntry();
    }

    public void showGameOver(int score, boolean qualifies) {
        setOverlayVisible(true);
        setOverlayText("GAME OVER");
        overlayLine1.setTexte("Score final : " + score);
        if (qualifies) {
            overlayLine2.setTexte("F pour recommencer immediatement");
            overlayLine3.setTexte("G pour enregistrer vos initiales");
            overlayLine4.setTexte("Y pour revenir au menu");
            overlayLine5.setTexte("Le score sera sauvegarde au format AAA-score.");
        } else {
            overlayLine2.setTexte("F pour recommencer immediatement");
            overlayLine3.setTexte("Y pour revenir au menu");
            overlayLine4.setTexte("Vous pouvez enchainer sans relancer le jeu.");
            overlayLine5.setTexte("");
        }
        clearNameEntry();
    }

    public void showNameEntry(char[] name, int selectedIndex, int score) {
        setOverlayVisible(true);
        setOverlayText("HIGH SCORE");
        overlayLine1.setTexte("Score : " + score);
        overlayLine2.setTexte("PC : Fleches pour lettres et position");
        overlayLine3.setTexte("F sur OK pour sauvegarder");
        overlayLine4.setTexte("Y pour quitter sans enregistrer");
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
