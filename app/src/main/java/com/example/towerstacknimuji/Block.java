package com.example.towerstacknimuji;

public class Block {

    // =========================================================
    // SIZE SETTINGS
    // =========================================================

    /*
     * Overall size multiplier.
     *
     * This value is kept here as a reference for GameView.
     *
     * 1.0  = original size
     * 1.15 = 15% larger
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

    private float sourceLeft;
    private float sourceRight;

    // =========================================================
    // TOWER TYPE
    // =========================================================

    /*
     * 0 = tower_core.png
     * 1 = tower_core1.png
     * 2 = tower_core2.png
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
         * IMPORTANT:
         *
         * Do NOT multiply the dimensions here.
         *
         * GameView will control the final size so that
         * moving blocks and placed blocks stay exactly
         * the same size.
         */

        this.width = width;
        this.height = height;

        // Display the complete image initially.
        this.sourceLeft = 0f;
        this.sourceRight = 1f;

        // Default tower type.
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

    public float getLeft() {
        return x;
    }

    public float getRight() {
        return x + width;
    }

    public float getTop() {
        return y;
    }

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

    public void setSourceRange(
            float sourceLeft,
            float sourceRight
    ) {

        this.sourceLeft = sourceLeft;
        this.sourceRight = sourceRight;
    }

    // =========================================================
    // TOWER TYPE
    // =========================================================

    public int getTowerType() {
        return towerType;
    }

    public void setTowerType(int towerType) {

        /*
         * Prevent invalid tower types.
         *
         * 0 = tower_core.png
         * 1 = tower_core1.png
         * 2 = tower_core2.png
         */

        if (towerType < 0) {
            towerType = 0;
        }

        if (towerType > 2) {
            towerType = 2;
        }

        this.towerType = towerType;
    }
}