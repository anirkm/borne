import MG2D.Fenetre;
import MG2D.geometrie.Cercle;
import MG2D.Couleur;
import MG2D.geometrie.Ligne;
import MG2D.geometrie.Point;

class Hazard {
    private final int radius;
    private final double velocityX;
    private final double velocityY;
    private final Ligne trail;
    private final Cercle glow;
    private final Cercle core;

    private double x;
    private double y;

    Hazard(
        int startX,
        int startY,
        int radius,
        double velocityX,
        double velocityY,
        Couleur trailColor,
        Couleur coreColor,
        Couleur glowColor
    ) {
        this.x = startX;
        this.y = startY;
        this.radius = radius;
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        this.trail = new Ligne(trailColor, new Point(startX, startY), new Point(startX, startY));
        this.glow = new Cercle(glowColor, new Point(startX, startY), radius + 7, false);
        this.core = new Cercle(coreColor, new Point(startX, startY), radius, true);
    }

    public void addTo(Fenetre window) {
        window.ajouter(trail);
        window.ajouter(glow);
        window.ajouter(core);
    }

    public void removeFrom(Fenetre window) {
        window.supprimer(trail);
        window.supprimer(glow);
        window.supprimer(core);
    }

    public void update(double delta) {
        double previousX = x;
        double previousY = y;
        x += velocityX * delta;
        y += velocityY * delta;
        trail.setA(new Point((int) Math.round(previousX), (int) Math.round(previousY)));
        trail.setB(new Point((int) Math.round(x), (int) Math.round(y)));
        Point position = new Point((int) Math.round(x), (int) Math.round(y));
        glow.setO(position);
        core.setO(position);
    }

    public boolean isOffscreen(int left, int bottom, int right, int top) {
        int inset = radius + 4;
        return x < left + inset
            || x > right - inset
            || y < bottom + inset
            || y > top - inset;
    }

    public boolean collides(Player player) {
        double dx = player.getX() - x;
        double dy = player.getY() - y;
        double combined = player.getRadius() + radius - 2.0;
        return dx * dx + dy * dy <= combined * combined;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getVelocityX() {
        return velocityX;
    }

    public double getVelocityY() {
        return velocityY;
    }

    public int getRadius() {
        return radius;
    }
}
