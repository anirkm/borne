import MG2D.Fenetre;
import MG2D.geometrie.Cercle;
import MG2D.Couleur;
import MG2D.geometrie.Point;
import MG2D.geometrie.Triangle;

class Player {
    private static final Couleur BODY_COLOR = new Couleur(76, 255, 224);
    private static final Couleur CORE_COLOR = new Couleur(252, 252, 255);
    private static final Couleur COOL_BODY_COLOR = new Couleur(136, 228, 246);
    private static final Couleur DASH_BODY_COLOR = new Couleur(255, 232, 120);
    private static final Couleur DASH_CORE_COLOR = new Couleur(255, 255, 255);
    private static final Couleur READY_COLOR = new Couleur(77, 255, 219);
    private static final Couleur COOLDOWN_COLOR = new Couleur(255, 108, 173);
    private static final Couleur DASH_COLOR = new Couleur(255, 230, 92);
    private static final Couleur POINTER_COLOR = new Couleur(224, 255, 247);
    private static final Couleur POINTER_DASH_COLOR = new Couleur(255, 255, 255);

    private static final double BASE_SPEED = 365.0;
    private static final double DASH_SPEED = 1040.0;
    private static final double DASH_DURATION = 0.18;
    private static final double DASH_COOLDOWN = 0.90;

    private final int radius;
    private final Cercle dashRing;
    private final Cercle body;
    private final Cercle core;
    private final Triangle pointer;

    private double x;
    private double y;
    private double pulseTime;
    private double cooldownRemaining;
    private double dashRemaining;
    private double lastDirectionX;
    private double lastDirectionY;
    private double dashDirectionX;
    private double dashDirectionY;
    private double movementX;
    private double movementY;
    private boolean dashTriggered;

    Player(int startX, int startY, int radius) {
        this.radius = radius;
        this.dashRing = new Cercle(READY_COLOR, new Point(startX, startY), radius + 6, false);
        this.body = new Cercle(BODY_COLOR, new Point(startX, startY), radius, true);
        this.core = new Cercle(CORE_COLOR, new Point(startX, startY), Math.max(4, radius / 3), true);
        this.pointer = new Triangle(
            POINTER_COLOR,
            new Point(startX, startY + radius + 16),
            new Point(startX - radius / 2, startY + radius / 2),
            new Point(startX + radius / 2, startY + radius / 2),
            true
        );
        reset(startX, startY);
    }

    public void reset(int startX, int startY) {
        x = startX;
        y = startY;
        pulseTime = 0.0;
        cooldownRemaining = 0.0;
        dashRemaining = 0.0;
        lastDirectionX = 0.0;
        lastDirectionY = 1.0;
        dashDirectionX = 0.0;
        dashDirectionY = 1.0;
        movementX = 0.0;
        movementY = 0.0;
        dashTriggered = false;
        syncVisuals();
    }

    public void update(double delta, ClavierBorneArcade keyboard, int left, int bottom, int right, int top) {
        double inputX = 0.0;
        double inputY = 0.0;

        if (keyboard.getJoyJ1GaucheEnfoncee()) {
            inputX -= 1.0;
        }
        if (keyboard.getJoyJ1DroiteEnfoncee()) {
            inputX += 1.0;
        }
        if (keyboard.getJoyJ1HautEnfoncee()) {
            inputY += 1.0;
        }
        if (keyboard.getJoyJ1BasEnfoncee()) {
            inputY -= 1.0;
        }

        if (cooldownRemaining > 0.0) {
            cooldownRemaining = Math.max(0.0, cooldownRemaining - delta);
        }

        if (inputX != 0.0 || inputY != 0.0) {
            double length = Math.sqrt(inputX * inputX + inputY * inputY);
            inputX /= length;
            inputY /= length;
            lastDirectionX = inputX;
            lastDirectionY = inputY;
        }

        if (keyboard.getBoutonJ1ATape() && cooldownRemaining <= 0.0) {
            if (lastDirectionX == 0.0 && lastDirectionY == 0.0) {
                lastDirectionY = 1.0;
            }
            dashDirectionX = lastDirectionX;
            dashDirectionY = lastDirectionY;
            dashRemaining = DASH_DURATION;
            cooldownRemaining = DASH_COOLDOWN;
            dashTriggered = true;
        }

        double movementX = inputX;
        double movementY = inputY;
        double speed = BASE_SPEED;

        if (dashRemaining > 0.0) {
            dashRemaining = Math.max(0.0, dashRemaining - delta);
            movementX = dashDirectionX;
            movementY = dashDirectionY;
            speed = DASH_SPEED;
        }

        this.movementX = movementX;
        this.movementY = movementY;
        x += movementX * speed * delta;
        y += movementY * speed * delta;

        if (x < left + radius) {
            x = left + radius;
        } else if (x > right - radius) {
            x = right - radius;
        }

        if (y < bottom + radius) {
            y = bottom + radius;
        } else if (y > top - radius) {
            y = top - radius;
        }

        pulseTime += delta;
        syncVisuals();
    }

    public void tickVisual(double delta) {
        pulseTime += delta;
        syncVisuals();
    }

    public void bringToFront(Fenetre window) {
        window.supprimer(dashRing);
        window.supprimer(body);
        window.supprimer(core);
        window.supprimer(pointer);
        window.ajouter(dashRing);
        window.ajouter(body);
        window.ajouter(core);
        window.ajouter(pointer);
    }

    public Cercle getDashRing() {
        return dashRing;
    }

    public Cercle getBody() {
        return body;
    }

    public Cercle getCore() {
        return core;
    }

    public Triangle getPointer() {
        return pointer;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public int getRadius() {
        return radius;
    }

    public double getDashChargeRatio() {
        if (cooldownRemaining <= 0.0) {
            return 1.0;
        }
        return 1.0 - (cooldownRemaining / DASH_COOLDOWN);
    }

    public boolean isInvulnerable() {
        return dashRemaining > 0.0;
    }

    public boolean isMoving() {
        return movementX != 0.0 || movementY != 0.0;
    }

    public double getDirectionX() {
        if (movementX != 0.0 || movementY != 0.0) {
            return movementX;
        }
        return lastDirectionX;
    }

    public double getDirectionY() {
        if (movementX != 0.0 || movementY != 0.0) {
            return movementY;
        }
        return lastDirectionY;
    }

    public boolean consumeDashTriggered() {
        boolean triggered = dashTriggered;
        dashTriggered = false;
        return triggered;
    }

    private void syncVisuals() {
        int pulseOffset = (int) Math.round(Math.sin(pulseTime * 8.0) * 3.0);
        int ringRadius = radius + 6 + pulseOffset;
        Couleur ringColor = READY_COLOR;
        Couleur bodyColor = BODY_COLOR;
        Couleur coreColor = CORE_COLOR;
        Couleur pointerColor = POINTER_COLOR;

        if (dashRemaining > 0.0) {
            ringRadius = radius + 14;
            ringColor = DASH_COLOR;
            bodyColor = DASH_BODY_COLOR;
            coreColor = DASH_CORE_COLOR;
            pointerColor = POINTER_DASH_COLOR;
        } else if (cooldownRemaining > 0.0) {
            ringColor = COOLDOWN_COLOR;
            bodyColor = COOL_BODY_COLOR;
        }

        double facingX = getDirectionX();
        double facingY = getDirectionY();
        if (facingX == 0.0 && facingY == 0.0) {
            facingY = 1.0;
        }
        double normalX = -facingY;
        double normalY = facingX;
        int tipDistance = dashRemaining > 0.0 ? radius + 24 : radius + 16 + pulseOffset;
        int baseDistance = dashRemaining > 0.0 ? radius + 1 : radius - 2;
        int wingSpan = dashRemaining > 0.0 ? radius / 2 + 8 : radius / 2 + 4;

        Point position = new Point((int) Math.round(x), (int) Math.round(y));
        body.setO(position);
        core.setO(position);
        dashRing.setO(position);
        dashRing.setRayon(ringRadius);
        dashRing.setCouleur(ringColor);
        body.setCouleur(bodyColor);
        core.setCouleur(coreColor);
        pointer.setA(new Point((int) Math.round(x + facingX * tipDistance), (int) Math.round(y + facingY * tipDistance)));
        pointer.setB(new Point((int) Math.round(x + facingX * baseDistance + normalX * wingSpan), (int) Math.round(y + facingY * baseDistance + normalY * wingSpan)));
        pointer.setC(new Point((int) Math.round(x + facingX * baseDistance - normalX * wingSpan), (int) Math.round(y + facingY * baseDistance - normalY * wingSpan)));
        pointer.setCouleur(pointerColor);
    }
}
