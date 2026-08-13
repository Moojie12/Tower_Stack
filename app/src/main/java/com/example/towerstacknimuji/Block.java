package com.example.towerstacknimuji;

public class Block {

    // =========================================================
    // SIZE SETTINGS
    // =========================================================

    /*
     * Overall size multiplier reference.
     *
     * GameView controls the actual block dimensions.
     *
     * 1.0f  = original size
     * 1.15f = 15% larger
     */
    public static final float SIZE_MULTIPLIER = 1.15f;

    // =========================================================
    // POSITION
    // =========================================================

    private float x;
    private float y;

    // =========================================================
    // SIZE
    // =========================================================

    private float width;
    private float height;

    // =========================================================
    // IMAGE SOURCE RANGE
    // =========================================================

    /*
     * These values determine which horizontal part of the
     * tower image is displayed.
     *
     * 0.0f = far left of image
     * 1.0f = far right of image
     *
     * Example:
     *
     * 0.0 -> 1.0 = complete tower image
     * 0.2 -> 0.8 = middle portion only
     */
    private float sourceLeft;
    private float sourceRight;

    // =========================================================
    // TOWER TYPE
    // =========================================================

    /*
     * Determines which tower image this block uses.
     *
     * 0 = tower_core.png
     * 1 = tower_core1.png
     * 2 = tower_core2.png
     * 3 = tower_core3.png
     * 4 = tower_core4.png
     * 5 = tower_core5.png
     */
    private int towerType;

    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public Block(
            float x,
            float y,
            float width,
            float height
    ) {

        this.x = x;
        this.y = y;

        /*
         * GameView determines the actual size.
         *
         * Do not apply SIZE_MULTIPLIER here because that
         * would cause moving and placed blocks to have
         * inconsistent sizes.
         */
        this.width = width;
        this.height = height;

        // -----------------------------------------------------
        // IMAGE
        // -----------------------------------------------------

        /*
         * A newly created block displays its complete image.
         */
        this.sourceLeft = 0f;
        this.sourceRight = 1f;

        // -----------------------------------------------------
        // TOWER TYPE
        // -----------------------------------------------------

        /*
         * Default to tower_core.png.
         *
         * GameView changes this using setTowerType().
         */
        this.towerType = 0;
    }

    // =========================================================
    // POSITION
    // =========================================================

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }

    // =========================================================
    // SIZE
    // =========================================================

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    // =========================================================
    // EDGES
    // =========================================================

    /*
     * Left edge of the block.
     */
    public float getLeft() {
        return x;
    }

    /*
     * Right edge of the block.
     */
    public float getRight() {
        return x + width;
    }

    /*
     * Top edge of the block.
     */
    public float getTop() {
        return y;
    }

    /*
     * Bottom edge of the block.
     */
    public float getBottom() {
        return y + height;
    }

    // =========================================================
    // IMAGE SOURCE RANGE
    // =========================================================

    public float getSourceLeft() {
        return sourceLeft;
    }

    public float getSourceRight() {
        return sourceRight;
    }

    /*
     * Sets the portion of the tower image that should be
     * displayed.
     *
     * Values are automatically restricted to 0.0 - 1.0.
     */
    public void setSourceRange(
            float sourceLeft,
            float sourceRight
    ) {

        // -----------------------------------------------------
        // CLAMP LEFT
        // -----------------------------------------------------

        sourceLeft =
                Math.max(
                        0f,
                        Math.min(
                                1f,
                                sourceLeft
                        )
                );

        // -----------------------------------------------------
        // CLAMP RIGHT
        // -----------------------------------------------------

        sourceRight =
                Math.max(
                        0f,
                        Math.min(
                                1f,
                                sourceRight
                        )
                );

        // -----------------------------------------------------
        // PREVENT REVERSED RANGE
        // -----------------------------------------------------

        if (sourceRight < sourceLeft) {

            float temp =
                    sourceLeft;

            sourceLeft =
                    sourceRight;

            sourceRight =
                    temp;
        }

        this.sourceLeft =
                sourceLeft;

        this.sourceRight =
                sourceRight;
    }

    // =========================================================
    // TOWER TYPE
    // =========================================================

    /*
     * Returns the tower design used by this block.
     */
    public int getTowerType() {
        return towerType;
    }

    /*
     * Sets the tower design used by this block.
     *
     * Valid tower types:
     *
     * 0 = tower_core.png
     * 1 = tower_core1.png
     * 2 = tower_core2.png
     * 3 = tower_core3.png
     * 4 = tower_core4.png
     * 5 = tower_core5.png
     */
    public void setTowerType(int towerType) {

        /*
         * Prevent invalid tower types.
         *
         * Anything below 0 becomes 0.
         * Anything above 5 becomes 5.
         */
        if (towerType < 0) {
            towerType = 0;
        }

        if (towerType > 5) {
            towerType = 5;
        }

        this.towerType = towerType;
    }
}