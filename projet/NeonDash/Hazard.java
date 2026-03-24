import MG2D.Fenetre;
import MG2D.Couleur;
import MG2D.geometrie.Ligne;
import MG2D.geometrie.Point;
import MG2D.geometrie.Texture;

class Hazard {
    private final int radius;
    private final int collisionRadius;
    private final double velocityX;
    private final double velocityY;
    private final Ligne trail;
    private final Texture blade;
    private final int spriteSize;

    private double x;
    private double y;
    private boolean grazeAwarded;

    Hazard(
        int startX,
        int startY,
        int radius,
        double velocityX,
        double velocityY,
        Couleur trailColor,
        String spritePath
    ) {
        this.x = startX;
        this.y = startY;
        this.radius = radius;
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        this.spriteSize = Math.max(50, radius * 5);
        this.collisionRadius = Math.max(13, radius - 2);
        this.trail = new Ligne(trailColor, new Point(startX, startY), new Point(startX, startY));
        this.blade = new Texture(spritePath, new Point(startX - spriteSize / 2, startY - spriteSize / 2), spriteSize, spriteSize);
        this.grazeAwarded = false;
    }

    public void addTo(Fenetre window) {
        window.ajouter(trail);
        window.ajouter(blade);
    }

    public void removeFrom(Fenetre window) {
        window.supprimer(trail);
        window.supprimer(blade);
    }

    public void update(double delta) {
        double previousX = x;
        double previousY = y;
        x += velocityX * delta;
        y += velocityY * delta;
        trail.setA(new Point((int) Math.round(previousX), (int) Math.round(previousY)));
        trail.setB(new Point((int) Math.round(x), (int) Math.round(y)));
        blade.setA(new Point((int) Math.round(x - spriteSize / 2.0), (int) Math.round(y - spriteSize / 2.0)));
    }

    public boolean isOffscreen(int left, int bottom, int right, int top) {
        int margin = spriteSize / 2 + 8;
        return x < left - margin
            || x > right + margin
            || y < bottom - margin
            || y > top + margin;
    }

    public boolean collides(Player player) {
        double dx = player.getX() - x;
        double dy = player.getY() - y;
        double combined = player.getCollisionRadius() + collisionRadius;
        return dx * dx + dy * dy <= combined * combined;
    }

    public boolean tryGraze(Player player, double margin) {
        if (grazeAwarded) {
            return false;
        }

        double dx = player.getX() - x;
        double dy = player.getY() - y;
        double collision = player.getCollisionRadius() + collisionRadius;
        double grazeRadius = collision + margin;
        double distanceSquared = dx * dx + dy * dy;

        if (distanceSquared > collision * collision && distanceSquared <= grazeRadius * grazeRadius) {
            grazeAwarded = true;
            return true;
        }

        return false;
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

    public int getCollisionRadius() {
        return collisionRadius;
    }
}
