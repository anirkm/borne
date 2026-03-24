import MG2D.Couleur;
import MG2D.Fenetre;
import MG2D.geometrie.Ligne;
import MG2D.geometrie.Point;
import MG2D.geometrie.Texture;

class Player {
    private static final int DIR_UP = 0;
    private static final int DIR_UP_RIGHT = 1;
    private static final int DIR_RIGHT = 2;
    private static final int DIR_DOWN_RIGHT = 3;
    private static final int DIR_DOWN = 4;
    private static final int DIR_DOWN_LEFT = 5;
    private static final int DIR_LEFT = 6;
    private static final int DIR_UP_LEFT = 7;
    private static final Couleur WAKE_COLOR = new Couleur(94, 232, 255);
    private static final Couleur DASH_WAKE_COLOR = new Couleur(228, 255, 255);
    private static final double BASE_SPEED = 365.0;
    private static final double DASH_SPEED = 1040.0;
    private static final double DASH_DURATION = 0.18;
    private static final double DASH_COOLDOWN = 0.90;
    private static final int MAX_DASH_CHARGES = 2;

    private final int radius;
    private final int shipWidth;
    private final int shipHeight;
    private final int shadowWidth;
    private final int shadowHeight;
    private final int dashWidth;
    private final int dashHeight;
    private final Texture shadow;
    private final Texture[] shipVariants;
    private final Texture[] dashVariants;
    private final Ligne wakeLeft;
    private final Ligne wakeRight;

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
    private boolean doubleDashTriggered;
    private int currentDirection;
    private int dashCharges;

    Player(int startX, int startY, int radius) {
        this.radius = radius;
        this.shipWidth = radius * 3;
        this.shipHeight = radius * 3;
        this.shadowWidth = shipWidth + 24;
        this.shadowHeight = shipHeight + 16;
        this.dashWidth = shipWidth + 30;
        this.dashHeight = shipHeight + 20;
        this.shadow = new Texture("player_shadow.png", new Point(startX - shadowWidth / 2, startY - shadowHeight / 2), shadowWidth, shadowHeight);
        this.shipVariants = new Texture[] {
            new Texture("player_ship_up.png", new Point(-5000, -5000), shipWidth, shipHeight),
            new Texture("player_ship_up_right.png", new Point(-5000, -5000), shipWidth, shipHeight),
            new Texture("player_ship_right.png", new Point(-5000, -5000), shipWidth, shipHeight),
            new Texture("player_ship_down_right.png", new Point(-5000, -5000), shipWidth, shipHeight),
            new Texture("player_ship_down.png", new Point(-5000, -5000), shipWidth, shipHeight),
            new Texture("player_ship_down_left.png", new Point(-5000, -5000), shipWidth, shipHeight),
            new Texture("player_ship_left.png", new Point(-5000, -5000), shipWidth, shipHeight),
            new Texture("player_ship_up_left.png", new Point(-5000, -5000), shipWidth, shipHeight)
        };
        this.dashVariants = new Texture[] {
            new Texture("player_ship_dash_up.png", new Point(-5000, -5000), dashWidth, dashHeight),
            new Texture("player_ship_dash_up_right.png", new Point(-5000, -5000), dashWidth, dashHeight),
            new Texture("player_ship_dash_right.png", new Point(-5000, -5000), dashWidth, dashHeight),
            new Texture("player_ship_dash_down_right.png", new Point(-5000, -5000), dashWidth, dashHeight),
            new Texture("player_ship_dash_down.png", new Point(-5000, -5000), dashWidth, dashHeight),
            new Texture("player_ship_dash_down_left.png", new Point(-5000, -5000), dashWidth, dashHeight),
            new Texture("player_ship_dash_left.png", new Point(-5000, -5000), dashWidth, dashHeight),
            new Texture("player_ship_dash_up_left.png", new Point(-5000, -5000), dashWidth, dashHeight)
        };
        this.wakeLeft = new Ligne(WAKE_COLOR, new Point(startX, startY), new Point(startX, startY));
        this.wakeRight = new Ligne(WAKE_COLOR, new Point(startX, startY), new Point(startX, startY));
        reset(startX, startY);
    }

    public void addTo(Fenetre window) {
        window.ajouter(shadow);
        window.ajouter(wakeLeft);
        window.ajouter(wakeRight);
        for (int index = 0; index < shipVariants.length; index++) {
            window.ajouter(shipVariants[index]);
            window.ajouter(dashVariants[index]);
        }
    }

    public void reset(int startX, int startY) {
        x = startX;
        y = startY;
        pulseTime = 0.0;
        cooldownRemaining = 0.0;
        dashRemaining = 0.0;
        dashCharges = MAX_DASH_CHARGES;
        lastDirectionX = 0.0;
        lastDirectionY = 1.0;
        dashDirectionX = 0.0;
        dashDirectionY = 1.0;
        movementX = 0.0;
        movementY = 0.0;
        dashTriggered = false;
        doubleDashTriggered = false;
        currentDirection = DIR_UP;
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

        if (dashCharges < MAX_DASH_CHARGES && cooldownRemaining > 0.0) {
            cooldownRemaining = Math.max(0.0, cooldownRemaining - delta);
            if (cooldownRemaining == 0.0) {
                dashCharges++;
                if (dashCharges < MAX_DASH_CHARGES) {
                    cooldownRemaining = DASH_COOLDOWN;
                }
            }
        }

        if (inputX != 0.0 || inputY != 0.0) {
            double length = Math.sqrt(inputX * inputX + inputY * inputY);
            inputX /= length;
            inputY /= length;
            lastDirectionX = inputX;
            lastDirectionY = inputY;
        }

        if (keyboard.getBoutonJ1ATape() && dashCharges > 0) {
            if (lastDirectionX == 0.0 && lastDirectionY == 0.0) {
                lastDirectionY = 1.0;
            }
            boolean spentLastCharge = dashCharges == 1;
            dashDirectionX = lastDirectionX;
            dashDirectionY = lastDirectionY;
            dashRemaining = DASH_DURATION;
            dashCharges--;
            if (dashCharges < MAX_DASH_CHARGES && cooldownRemaining <= 0.0) {
                cooldownRemaining = DASH_COOLDOWN;
            }
            dashTriggered = true;
            doubleDashTriggered = spentLastCharge;
        }

        double currentMovementX = inputX;
        double currentMovementY = inputY;
        double speed = BASE_SPEED;

        if (dashRemaining > 0.0) {
            dashRemaining = Math.max(0.0, dashRemaining - delta);
            currentMovementX = dashDirectionX;
            currentMovementY = dashDirectionY;
            speed = DASH_SPEED;
        }

        movementX = currentMovementX;
        movementY = currentMovementY;
        x += currentMovementX * speed * delta;
        y += currentMovementY * speed * delta;

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
        window.supprimer(shadow);
        window.supprimer(wakeLeft);
        window.supprimer(wakeRight);
        window.ajouter(shadow);
        window.ajouter(wakeLeft);
        window.ajouter(wakeRight);
        for (int index = 0; index < shipVariants.length; index++) {
            window.supprimer(shipVariants[index]);
            window.supprimer(dashVariants[index]);
            window.ajouter(shipVariants[index]);
            window.ajouter(dashVariants[index]);
        }
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
        if (dashCharges >= MAX_DASH_CHARGES || cooldownRemaining <= 0.0) {
            return 1.0;
        }
        return 1.0 - (cooldownRemaining / DASH_COOLDOWN);
    }

    public int getDashCharges() {
        return dashCharges;
    }

    public int getMaxDashCharges() {
        return MAX_DASH_CHARGES;
    }

    public void reduceCooldown(double amount) {
        if (dashCharges >= MAX_DASH_CHARGES) {
            return;
        }

        cooldownRemaining -= amount;
        while (dashCharges < MAX_DASH_CHARGES && cooldownRemaining <= 0.0) {
            dashCharges++;
            if (dashCharges < MAX_DASH_CHARGES) {
                cooldownRemaining += DASH_COOLDOWN;
            } else {
                cooldownRemaining = 0.0;
            }
        }
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

    public boolean consumeDoubleDashTriggered() {
        boolean triggered = doubleDashTriggered;
        doubleDashTriggered = false;
        return triggered;
    }

    private void syncVisuals() {
        double facingX = getDirectionX();
        double facingY = getDirectionY();
        if (facingX == 0.0 && facingY == 0.0) {
            facingY = 1.0;
        }

        double normalX = -facingY;
        double normalY = facingX;
        int shipLeft = (int) Math.round(x - shipWidth / 2.0);
        int shipBottom = (int) Math.round(y - shipHeight / 2.0);
        int shadowLeft = (int) Math.round(x - shadowWidth / 2.0);
        int shadowBottom = (int) Math.round(y - shadowHeight / 2.0 - 2.0);
        currentDirection = resolveDirection(facingX, facingY);

        shadow.setA(new Point(shadowLeft, shadowBottom));
        setShipPositions(shipLeft, shipBottom);

        if (dashRemaining > 0.0) {
            int dashLeft = (int) Math.round(x - dashWidth / 2.0 - facingX * 8.0);
            int dashBottom = (int) Math.round(y - dashHeight / 2.0 - facingY * 8.0);
            setDashPositions(dashLeft, dashBottom);
        } else {
            hideDashSprites();
        }

        int tailDistance = dashRemaining > 0.0 ? radius + 24 : radius + 16;
        int wingOffset = dashRemaining > 0.0 ? radius / 2 + 10 : radius / 2 + 6;
        int wakeLength = dashRemaining > 0.0 ? radius + 24 : (isMoving() ? radius + 14 : 0);
        Point leftStart = new Point(
            (int) Math.round(x - facingX * tailDistance + normalX * wingOffset),
            (int) Math.round(y - facingY * tailDistance + normalY * wingOffset)
        );
        Point rightStart = new Point(
            (int) Math.round(x - facingX * tailDistance - normalX * wingOffset),
            (int) Math.round(y - facingY * tailDistance - normalY * wingOffset)
        );
        Point leftEnd = new Point(
            (int) Math.round(leftStart.getX() - facingX * wakeLength),
            (int) Math.round(leftStart.getY() - facingY * wakeLength)
        );
        Point rightEnd = new Point(
            (int) Math.round(rightStart.getX() - facingX * wakeLength),
            (int) Math.round(rightStart.getY() - facingY * wakeLength)
        );

        wakeLeft.setA(leftStart);
        wakeLeft.setB(leftEnd);
        wakeRight.setA(rightStart);
        wakeRight.setB(rightEnd);
        Couleur wakeColor = dashRemaining > 0.0 ? DASH_WAKE_COLOR : WAKE_COLOR;
        wakeLeft.setCouleur(wakeColor);
        wakeRight.setCouleur(wakeColor);
    }

    private void setShipPositions(int shipLeft, int shipBottom) {
        for (int index = 0; index < shipVariants.length; index++) {
            if (index == currentDirection && dashRemaining <= 0.0) {
                shipVariants[index].setA(new Point(shipLeft, shipBottom));
            } else {
                shipVariants[index].setA(new Point(-5000, -5000));
            }
        }
    }

    private void setDashPositions(int dashLeft, int dashBottom) {
        for (int index = 0; index < dashVariants.length; index++) {
            if (index == currentDirection) {
                dashVariants[index].setA(new Point(dashLeft, dashBottom));
            } else {
                dashVariants[index].setA(new Point(-5000, -5000));
            }
        }
        for (int index = 0; index < shipVariants.length; index++) {
            shipVariants[index].setA(new Point(-5000, -5000));
        }
    }

    private void hideDashSprites() {
        for (int index = 0; index < dashVariants.length; index++) {
            dashVariants[index].setA(new Point(-5000, -5000));
        }
    }

    private int resolveDirection(double facingX, double facingY) {
        double angle = Math.toDegrees(Math.atan2(facingY, facingX));

        if (angle >= 67.5 && angle < 112.5) {
            return DIR_UP;
        }
        if (angle >= 22.5 && angle < 67.5) {
            return DIR_UP_RIGHT;
        }
        if (angle >= -22.5 && angle < 22.5) {
            return DIR_RIGHT;
        }
        if (angle >= -67.5 && angle < -22.5) {
            return DIR_DOWN_RIGHT;
        }
        if (angle >= -112.5 && angle < -67.5) {
            return DIR_DOWN;
        }
        if (angle >= -157.5 && angle < -112.5) {
            return DIR_DOWN_LEFT;
        }
        if (angle >= 112.5 && angle < 157.5) {
            return DIR_UP_LEFT;
        }
        return DIR_LEFT;
    }
}
