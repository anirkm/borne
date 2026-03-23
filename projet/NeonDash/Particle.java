import MG2D.Fenetre;
import MG2D.geometrie.Cercle;
import MG2D.Couleur;
import MG2D.geometrie.Point;

class Particle {
    private final Cercle sprite;
    private final Couleur startColor;
    private final Couleur endColor;
    private final double maxLife;
    private final double startRadius;
    private final double endRadius;

    private double x;
    private double y;
    private double velocityX;
    private double velocityY;
    private double life;

    Particle(
        double startX,
        double startY,
        double velocityX,
        double velocityY,
        double maxLife,
        double startRadius,
        double endRadius,
        Couleur startColor,
        Couleur endColor
    ) {
        this.x = startX;
        this.y = startY;
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        this.maxLife = maxLife;
        this.life = maxLife;
        this.startRadius = startRadius;
        this.endRadius = endRadius;
        this.startColor = startColor;
        this.endColor = endColor;
        this.sprite = new Cercle(startColor, new Point((int) Math.round(startX), (int) Math.round(startY)), (int) Math.round(startRadius), true);
    }

    public void addTo(Fenetre window) {
        window.ajouter(sprite);
    }

    public void removeFrom(Fenetre window) {
        window.supprimer(sprite);
    }

    public boolean update(double delta) {
        life -= delta;
        if (life <= 0.0) {
            return false;
        }

        x += velocityX * delta;
        y += velocityY * delta;

        double ratio = life / maxLife;
        double radius = endRadius + (startRadius - endRadius) * ratio;
        if (radius < 1.0) {
            radius = 1.0;
        }

        velocityX *= 0.96;
        velocityY *= 0.96;

        sprite.setO(new Point((int) Math.round(x), (int) Math.round(y)));
        sprite.setRayon((int) Math.round(radius));
        sprite.setCouleur(blend(ratio));
        return true;
    }

    private Couleur blend(double ratio) {
        double clamped = Math.max(0.0, Math.min(1.0, ratio));
        int red = (int) Math.round(endColor.getRed() + (startColor.getRed() - endColor.getRed()) * clamped);
        int green = (int) Math.round(endColor.getGreen() + (startColor.getGreen() - endColor.getGreen()) * clamped);
        int blue = (int) Math.round(endColor.getBlue() + (startColor.getBlue() - endColor.getBlue()) * clamped);
        return new Couleur(red, green, blue);
    }
}
