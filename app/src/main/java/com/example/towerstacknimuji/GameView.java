package com.example.towerstacknimuji;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.Random;

public class GameView extends View {

    // =========================================================
    // PAINT
    // =========================================================

    private final Paint paint;

    // =========================================================
    // RANDOMIZER
    // =========================================================

    private final Random random = new Random();

    // =========================================================
    // SCREEN
    // =========================================================

    private float screenWidth;
    private float screenHeight;

    // =========================================================
    // GAME STATE
    // =========================================================

    private boolean gameStarted = false;
    private boolean gameOver = false;

    private int score = 0;

    // =========================================================
    // TOWER SELECTION
    // =========================================================

    private boolean showTowerSelect = false;

    /*
     * 0 = tower_core.png
     * 1 = tower_core1.png
     * 2 = tower_core2.png
     * 3 = tower_core3.png
     * 4 = tower_core4.png
     * 5 = tower_core5.png
     */
    private int selectedTowerType = 0;

    // =========================================================
    // WORLD BACKGROUND
    // =========================================================

    private Bitmap worldBackground;

    private static final float BACKGROUND_SCROLL_SPEED = 0.48f;

    private float backgroundScale = 1f;

    private float backgroundWidth;
    private float backgroundHeight;

    // =========================================================
    // TOWER IMAGES
    // =========================================================

    private static final int TOWER_COUNT = 6;

    private Bitmap[] towerImages;

    private Bitmap[] croppedTowerImages;

    private RectF[] towerButtonRects =
            new RectF[TOWER_COUNT];

    // =========================================================
    // TOWER
    // =========================================================

    private final ArrayList<Block> tower =
            new ArrayList<>();

    private Block movingBlock;

    // =========================================================
    // BLOCK SIZE
    // =========================================================

    private float blockWidth;

    private float blockHeight;

    private static final float BLOCK_WIDTH_RATIO = 0.42f;

    private static final float BLOCK_HEIGHT_RATIO = 0.15f;

    // =========================================================
    // MOVEMENT
    // =========================================================

    private float blockSpeed = 6f;

    private boolean movingRight = true;

    private boolean spawnFromLeft = true;

    // =========================================================
    // CAMERA
    // =========================================================

    private float cameraOffset = 0f;

    private float cameraVelocity = 0f;

    private static final float CAMERA_FOLLOW_STRENGTH = 0.012f;

    private static final float CAMERA_MAX_SPEED = 8f;

    // =========================================================
    // FALLING EXCESS
    // =========================================================

    private boolean excessFalling = false;

    private float excessX;
    private float excessY;

    private float excessWidth;
    private float excessHeight;

    private float excessVelocityX;
    private float excessVelocityY;

    private float excessRotation;

    private float excessSourceLeft;
    private float excessSourceRight;

    private int excessTowerType = 0;

    // =========================================================
    // TOWER WIGGLE
    // =========================================================

    /*
     * The tower begins to gently wiggle when it reaches 15
     * stacked pieces.
     */
    private static final int WIGGLE_START_STACK = 15;

    /*
     * Reduced wiggle amount.
     *
     * The tower will rotate only around ±1.5 degrees.
     */
    private static final float WIGGLE_MAX_ANGLE = 1.5f;

    /*
     * Controls how quickly the tower wiggles.
     */
    private static final float WIGGLE_SPEED = 0.075f;

    private float wiggleTime = 0f;

    private float towerWiggleAngle = 0f;

    // =========================================================
    // COLLAPSE
    // =========================================================

    /*
     * Stores every tower piece that is currently falling
     * during the collapse animation.
     */
    private final ArrayList<CollapsePiece> collapsingPieces =
            new ArrayList<>();

    private boolean collapseStarted = false;

    private boolean collapseFinished = false;

    /*
     * How long the collapse animation has been running.
     */
    private float collapseTime = 0f;

    /*
     * Gravity applied to collapsing tower pieces.
     */
    private static final float COLLAPSE_GRAVITY = 0.65f;

    /*
     * Maximum time before the collapse is considered finished.
     */
    private static final float COLLAPSE_MAX_TIME = 4.5f;

    // =========================================================
    // COLLAPSE PIECE CLASS
    // =========================================================

    private static class CollapsePiece {

        Block block;

        float x;
        float y;

        float width;
        float height;

        float velocityX;
        float velocityY;

        float rotation;
        float rotationVelocity;

        float delay;

        int towerType;

        float sourceLeft;
        float sourceRight;

        CollapsePiece(
                Block block,
                float x,
                float y,
                float width,
                float height,
                float velocityX,
                float velocityY,
                float rotation,
                float rotationVelocity,
                float delay
        ) {

            this.block = block;

            this.x = x;
            this.y = y;

            this.width = width;
            this.height = height;

            this.velocityX = velocityX;
            this.velocityY = velocityY;

            this.rotation = rotation;

            this.rotationVelocity =
                    rotationVelocity;

            this.delay = delay;

            this.towerType =
                    block.getTowerType();

            this.sourceLeft =
                    block.getSourceLeft();

            this.sourceRight =
                    block.getSourceRight();
        }
    }

    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public GameView(Context context) {

        super(context);

        paint = new Paint(
                Paint.ANTI_ALIAS_FLAG |
                        Paint.FILTER_BITMAP_FLAG
        );

        setFocusable(true);

        // =====================================================
        // WORLD BACKGROUND
        // =====================================================

        worldBackground =
                BitmapFactory.decodeResource(
                        getResources(),
                        R.drawable.bg_tower_world_strip
                );

        // =====================================================
        // TOWER IMAGES
        // =====================================================

        towerImages =
                new Bitmap[TOWER_COUNT];

        croppedTowerImages =
                new Bitmap[TOWER_COUNT];

        // -----------------------------------------------------
        // TOWER 1
        // -----------------------------------------------------

        towerImages[0] =
                BitmapFactory.decodeResource(
                        getResources(),
                        R.drawable.tower_core
                );

        // -----------------------------------------------------
        // TOWER 2
        // -----------------------------------------------------

        towerImages[1] =
                BitmapFactory.decodeResource(
                        getResources(),
                        R.drawable.tower_core1
                );

        // -----------------------------------------------------
        // TOWER 3
        // -----------------------------------------------------

        towerImages[2] =
                BitmapFactory.decodeResource(
                        getResources(),
                        R.drawable.tower_core2
                );

        // -----------------------------------------------------
        // TOWER 4
        // -----------------------------------------------------

        towerImages[3] =
                BitmapFactory.decodeResource(
                        getResources(),
                        R.drawable.tower_core3
                );

        // -----------------------------------------------------
        // TOWER 5
        // -----------------------------------------------------

        towerImages[4] =
                BitmapFactory.decodeResource(
                        getResources(),
                        R.drawable.tower_core4
                );

        // -----------------------------------------------------
        // TOWER 6
        // -----------------------------------------------------

        towerImages[5] =
                BitmapFactory.decodeResource(
                        getResources(),
                        R.drawable.tower_core5
                );

        // =====================================================
        // CROP TRANSPARENT AREAS
        // =====================================================

        for (
                int i = 0;
                i < towerImages.length;
                i++
        ) {

            croppedTowerImages[i] =
                    cropTransparentArea(
                            towerImages[i]
                    );
        }
    }

    // =========================================================
    // DRAW LOOP
    // =========================================================

    @Override
    protected void onDraw(Canvas canvas) {

        super.onDraw(canvas);

        screenWidth =
                getWidth();

        screenHeight =
                getHeight();

        // =====================================================
        // UPDATE GAME
        // =====================================================

        if (gameStarted) {

            if (!gameOver) {

                updateGame();

            } else if (collapseStarted &&
                    !collapseFinished) {

                updateCollapse();
            }
        }

        // =====================================================
        // DRAW BACKGROUND
        // =====================================================

        drawBackground(canvas);

        // =====================================================
        // START / TOWER SELECT SCREEN
        // =====================================================

        if (!gameStarted) {

            if (showTowerSelect) {

                drawTowerSelectScreen(canvas);

            } else {

                drawStartScreen(canvas);
            }

            return;
        }

        // =====================================================
        // DRAW GAME
        // =====================================================

        drawGame(canvas);

        // =====================================================
        // GAME OVER
        // =====================================================

        if (gameOver) {

            drawGameOver(canvas);
        }

        // =====================================================
        // CONTINUE ANIMATION
        // =====================================================

        if (
                !gameOver ||
                        (
                                collapseStarted &&
                                        !collapseFinished
                        )
        ) {

            postInvalidateOnAnimation();
        }
    }

    // =========================================================
    // CROP TRANSPARENT IMAGE AREA
    // =========================================================

    private Bitmap cropTransparentArea(
            Bitmap source
    ) {

        if (source == null) {
            return null;
        }

        int width =
                source.getWidth();

        int height =
                source.getHeight();

        int minX = width;
        int minY = height;

        int maxX = -1;
        int maxY = -1;

        for (
                int y = 0;
                y < height;
                y++
        ) {

            for (
                    int x = 0;
                    x < width;
                    x++
            ) {

                int pixel =
                        source.getPixel(
                                x,
                                y
                        );

                int alpha =
                        Color.alpha(pixel);

                if (alpha > 10) {

                    if (x < minX) {
                        minX = x;
                    }

                    if (x > maxX) {
                        maxX = x;
                    }

                    if (y < minY) {
                        minY = y;
                    }

                    if (y > maxY) {
                        maxY = y;
                    }
                }
            }
        }

        // -----------------------------------------------------
        // NO VISIBLE PIXELS
        // -----------------------------------------------------

        if (
                maxX < minX ||
                        maxY < minY
        ) {

            return source;
        }

        // -----------------------------------------------------
        // CREATE CROPPED BITMAP
        // -----------------------------------------------------

        int croppedWidth =
                maxX - minX + 1;

        int croppedHeight =
                maxY - minY + 1;

        return Bitmap.createBitmap(
                source,
                minX,
                minY,
                croppedWidth,
                croppedHeight
        );
    }

    // =========================================================
    // BACKGROUND
    // =========================================================

    private void drawBackground(
            Canvas canvas
    ) {

        if (worldBackground == null) {

            canvas.drawColor(
                    Color.rgb(
                            10,
                            15,
                            30
                    )
            );

            return;
        }

        // =====================================================
        // CALCULATE BACKGROUND SCALE
        // =====================================================

        backgroundScale =
                screenWidth /
                        worldBackground.getWidth();

        backgroundWidth =
                worldBackground.getWidth()
                        * backgroundScale;

        backgroundHeight =
                worldBackground.getHeight()
                        * backgroundScale;

        // =====================================================
        // INITIAL POSITION
        // =====================================================

        float initialTop =
                screenHeight -
                        backgroundHeight;

        // =====================================================
        // BACKGROUND MOVEMENT
        // =====================================================

        float backgroundOffset =
                cameraOffset *
                        BACKGROUND_SCROLL_SPEED;

        float maximumDownMovement =
                Math.max(
                        0f,
                        backgroundHeight -
                                screenHeight
                );

        backgroundOffset =
                Math.min(
                        backgroundOffset,
                        maximumDownMovement
                );

        float backgroundTop =
                initialTop +
                        backgroundOffset;

        // =====================================================
        // DRAW BACKGROUND
        // =====================================================

        RectF destination =
                new RectF(
                        0,
                        backgroundTop,
                        backgroundWidth,
                        backgroundTop +
                                backgroundHeight
                );

        paint.setAlpha(255);

        paint.setColor(Color.WHITE);

        canvas.drawBitmap(
                worldBackground,
                null,
                destination,
                paint
        );

        // =====================================================
        // DARK CINEMATIC OVERLAY
        // =====================================================

        paint.setColor(
                Color.argb(
                        45,
                        0,
                        0,
                        0
                )
        );

        canvas.drawRect(
                0,
                0,
                screenWidth,
                screenHeight,
                paint
        );
    }

    // =========================================================
    // START SCREEN
    // =========================================================

    private void drawStartScreen(
            Canvas canvas
    ) {

        paint.setTextAlign(
                Paint.Align.CENTER
        );

        paint.setColor(Color.WHITE);

        paint.setFakeBoldText(true);

        paint.setTextSize(58);

        canvas.drawText(
                "TOWER",
                screenWidth / 2f,
                screenHeight / 2f - 90,
                paint
        );

        canvas.drawText(
                "STACK",
                screenWidth / 2f,
                screenHeight / 2f - 25,
                paint
        );

        paint.setFakeBoldText(false);

        paint.setTextSize(20);

        canvas.drawText(
                "BUILD ABOVE THE CLOUDS",
                screenWidth / 2f,
                screenHeight / 2f + 20,
                paint
        );

        float buttonWidth = 280;

        float buttonHeight = 70;

        float buttonLeft =
                (screenWidth - buttonWidth)
                        / 2f;

        float buttonTop =
                screenHeight / 2f + 70;

        paint.setStyle(
                Paint.Style.STROKE
        );

        paint.setStrokeWidth(3);

        paint.setColor(Color.WHITE);

        RectF button =
                new RectF(
                        buttonLeft,
                        buttonTop,
                        buttonLeft +
                                buttonWidth,
                        buttonTop +
                                buttonHeight
                );

        canvas.drawRoundRect(
                button,
                18,
                18,
                paint
        );

        paint.setStyle(
                Paint.Style.FILL
        );

        paint.setTextSize(25);

        canvas.drawText(
                "TAP TO START",
                screenWidth / 2f,
                buttonTop + 45,
                paint
        );
    }

    // =========================================================
    // TOWER SELECT SCREEN
    // =========================================================

    private void drawTowerSelectScreen(
            Canvas canvas
    ) {

        // =====================================================
        // TITLE
        // =====================================================

        paint.setTextAlign(
                Paint.Align.CENTER
        );

        paint.setColor(Color.WHITE);

        paint.setFakeBoldText(true);

        paint.setTextSize(34);

        canvas.drawText(
                "CHOOSE YOUR BUILD",
                screenWidth / 2f,
                58f,
                paint
        );

        // =====================================================
        // SUBTITLE
        // =====================================================

        paint.setFakeBoldText(false);

        paint.setTextSize(18);

        canvas.drawText(
                "TAP A CHARACTER TO START",
                screenWidth / 2f,
                88f,
                paint
        );

        // =====================================================
        // GRID LAYOUT
        // =====================================================

        int optionCount =
                towerImages.length;

        int columns = 2;

        int rows =
                (int) Math.ceil(
                        optionCount /
                                (float) columns
                );

        float horizontalPadding =
                screenWidth * 0.08f;

        float spacing = 18f;

        float topReservedSpace = 110f;

        float bottomReservedSpace = 25f;

        float availableGridHeight =
                screenHeight -
                        topReservedSpace -
                        bottomReservedSpace;

        float cellHeight =
                (availableGridHeight -
                        spacing * (rows - 1))
                        / rows;

        cellHeight =
                Math.max(
                        110f,
                        cellHeight
                );

        float cellWidth =
                (
                        screenWidth
                                - horizontalPadding * 2f
                                - spacing *
                                (columns - 1)
                ) / columns;

        float totalGridHeight =
                rows * cellHeight
                        + (rows - 1) * spacing;

        float startY =
                topReservedSpace
                        + (
                        availableGridHeight -
                                totalGridHeight
                ) / 2f;

        // =====================================================
        // DRAW OPTIONS
        // =====================================================

        for (
                int i = 0;
                i < optionCount;
                i++
        ) {

            int col =
                    i % columns;

            int row =
                    i / columns;

            float left =
                    horizontalPadding
                            + col *
                            (cellWidth + spacing);

            float top =
                    startY
                            + row *
                            (cellHeight + spacing);

            RectF buttonRect =
                    new RectF(
                            left,
                            top,
                            left + cellWidth,
                            top + cellHeight
                    );

            towerButtonRects[i] =
                    buttonRect;

            boolean isSelected =
                    (i == selectedTowerType);

            // =================================================
            // PANEL
            // =================================================

            paint.setStyle(
                    Paint.Style.FILL
            );

            paint.setColor(
                    Color.argb(
                            isSelected ? 90 : 55,
                            255,
                            255,
                            255
                    )
            );

            canvas.drawRoundRect(
                    buttonRect,
                    16,
                    16,
                    paint
            );

            // =================================================
            // BORDER
            // =================================================

            paint.setStyle(
                    Paint.Style.STROKE
            );

            paint.setStrokeWidth(
                    isSelected ? 5 : 2
            );

            paint.setColor(
                    isSelected
                            ? Color.YELLOW
                            : Color.WHITE
            );

            canvas.drawRoundRect(
                    buttonRect,
                    16,
                    16,
                    paint
            );

            // =================================================
            // THUMBNAIL
            // =================================================

            Bitmap thumbnail =
                    croppedTowerImages[i] != null
                            ? croppedTowerImages[i]
                            : towerImages[i];

            if (thumbnail != null) {

                float imagePadding = 14f;

                float labelAreaHeight = 38f;

                float imageAreaLeft =
                        left + imagePadding;

                float imageAreaTop =
                        top + imagePadding;

                float imageAreaRight =
                        left + cellWidth -
                                imagePadding;

                float imageAreaBottom =
                        top + cellHeight -
                                labelAreaHeight -
                                imagePadding;

                float imageAreaWidth =
                        imageAreaRight -
                                imageAreaLeft;

                float imageAreaHeight =
                        imageAreaBottom -
                                imageAreaTop;

                float scaleX =
                        imageAreaWidth /
                                thumbnail.getWidth();

                float scaleY =
                        imageAreaHeight /
                                thumbnail.getHeight();

                float scale =
                        Math.min(
                                scaleX,
                                scaleY
                        );

                scale =
                        Math.min(
                                scale,
                                1.0f
                        );

                float drawWidth =
                        thumbnail.getWidth()
                                * scale;

                float drawHeight =
                        thumbnail.getHeight()
                                * scale;

                float thumbLeft =
                        imageAreaLeft +
                                (
                                        imageAreaWidth -
                                                drawWidth
                                ) / 2f;

                float thumbTop =
                        imageAreaTop +
                                (
                                        imageAreaHeight -
                                                drawHeight
                                ) / 2f;

                RectF thumbRect =
                        new RectF(
                                thumbLeft,
                                thumbTop,
                                thumbLeft +
                                        drawWidth,
                                thumbTop +
                                        drawHeight
                        );

                paint.setStyle(
                        Paint.Style.FILL
                );

                paint.setAlpha(255);

                paint.setColor(
                        Color.WHITE
                );

                canvas.drawBitmap(
                        thumbnail,
                        null,
                        thumbRect,
                        paint
                );
            }

            // =================================================
            // LABEL
            // =================================================

            paint.setTextAlign(
                    Paint.Align.CENTER
            );

            paint.setColor(
                    Color.WHITE
            );

            paint.setFakeBoldText(true);

            paint.setTextSize(21);

            canvas.drawText(
                    "TOWER " + (i + 1),
                    left + cellWidth / 2f,
                    top + cellHeight - 14f,
                    paint
            );

            paint.setFakeBoldText(false);
        }

        paint.setStyle(
                Paint.Style.FILL
        );

        paint.setAlpha(255);
    }

    // =========================================================
    // GET TAPPED TOWER INDEX
    // =========================================================

    private int getTappedTowerIndex(
            float touchX,
            float touchY
    ) {

        if (towerButtonRects == null) {
            return -1;
        }

        for (
                int i = 0;
                i < towerButtonRects.length;
                i++
        ) {

            RectF rect =
                    towerButtonRects[i];

            if (
                    rect != null &&
                            rect.contains(
                                    touchX,
                                    touchY
                            )
            ) {

                return i;
            }
        }

        return -1;
    }

    // =========================================================
    // START GAME
    // =========================================================

    private void startGame() {

        gameStarted = true;

        gameOver = false;

        showTowerSelect = false;

        score = 0;

        cameraOffset = 0;

        cameraVelocity = 0;

        spawnFromLeft = true;

        tower.clear();

        movingBlock = null;

        excessFalling = false;

        collapsingPieces.clear();

        collapseStarted = false;

        collapseFinished = false;

        collapseTime = 0f;

        wiggleTime = 0f;

        towerWiggleAngle = 0f;

        blockSpeed = 6f;

        // =====================================================
        // BLOCK DIMENSIONS
        // =====================================================

        blockWidth =
                screenWidth *
                        BLOCK_WIDTH_RATIO;

        blockHeight =
                screenHeight *
                        BLOCK_HEIGHT_RATIO;

        blockHeight =
                Math.max(
                        90,
                        Math.min(
                                blockHeight,
                                125
                        )
                );

        // =====================================================
        // FIRST BLOCK
        // =====================================================

        float firstX =
                (
                        screenWidth -
                                blockWidth
                ) / 2f;

        float firstY =
                screenHeight -
                        blockHeight -
                        80;

        Block firstBlock =
                new Block(
                        firstX,
                        firstY,
                        blockWidth,
                        blockHeight
                );

        firstBlock.setTowerType(
                selectedTowerType
        );

        firstBlock.setSourceRange(
                0f,
                1f
        );

        tower.add(firstBlock);

        // =====================================================
        // FIRST MOVING BLOCK
        // =====================================================

        createMovingBlock();

        invalidate();
    }

    // =========================================================
    // CREATE MOVING BLOCK
    // =========================================================

    private void createMovingBlock() {

        if (tower.isEmpty()) {
            return;
        }

        Block previous =
                tower.get(
                        tower.size() - 1
                );

        float width =
                previous.getWidth();

        float y =
                previous.getY()
                        - blockHeight
                        + 5f;

        movingBlock =
                new Block(
                        0,
                        y,
                        width,
                        blockHeight
                );

        movingBlock.setTowerType(
                selectedTowerType
        );

        movingBlock.setSourceRange(
                0f,
                1f
        );

        if (spawnFromLeft) {

            movingBlock.setX(0);

            movingRight = true;

        } else {

            movingBlock.setX(
                    screenWidth -
                            movingBlock.getWidth()
            );

            movingRight = false;
        }

        spawnFromLeft =
                !spawnFromLeft;
    }

    // =========================================================
    // UPDATE GAME
    // =========================================================

    private void updateGame() {

        if (movingBlock != null) {

            moveMovingBlock();
        }

        if (excessFalling) {

            updateFallingExcess();
        }

        updateTowerWiggle();

        updateCamera();
    }

    // =========================================================
    // TOWER WIGGLE
    // =========================================================

    private void updateTowerWiggle() {

        /*
         * No wiggle before 15 blocks.
         */
        if (
                tower.size() <
                        WIGGLE_START_STACK
        ) {

            towerWiggleAngle = 0f;

            wiggleTime = 0f;

            return;
        }

        /*
         * Advance the wiggle animation.
         */
        wiggleTime +=
                WIGGLE_SPEED;

        /*
         * Smooth sine wave.
         *
         * Maximum movement is only ±1.5 degrees.
         */
        towerWiggleAngle =
                (float) Math.sin(
                        wiggleTime
                ) *
                        WIGGLE_MAX_ANGLE;
    }

    // =========================================================
    // MOVE MOVING BLOCK
    // =========================================================

    private void moveMovingBlock() {

        if (movingRight) {

            movingBlock.setX(
                    movingBlock.getX()
                            + blockSpeed
            );

            if (
                    movingBlock.getRight()
                            >= screenWidth
            ) {

                movingBlock.setX(
                        screenWidth -
                                movingBlock.getWidth()
                );

                movingRight = false;
            }

        } else {

            movingBlock.setX(
                    movingBlock.getX()
                            - blockSpeed
            );

            if (
                    movingBlock.getX() <= 0
            ) {

                movingBlock.setX(0);

                movingRight = true;
            }
        }
    }

    // =========================================================
    // DRAW GAME
    // =========================================================

    private void drawGame(
            Canvas canvas
    ) {

        // -----------------------------------------------------
        // NORMAL TOWER
        // -----------------------------------------------------

        if (!collapseStarted) {

            /*
             * If the tower has reached 15 stacks,
             * rotate the entire tower slightly around its
             * bottom center.
             */
            if (
                    tower.size() >=
                            WIGGLE_START_STACK
            ) {

                float pivotX =
                        screenWidth / 2f;

                float pivotY =
                        screenHeight -
                                80f +
                                cameraOffset;

                canvas.save();

                canvas.rotate(
                        towerWiggleAngle,
                        pivotX,
                        pivotY
                );

                for (Block block : tower) {

                    drawTowerPiece(
                            canvas,
                            block
                    );
                }

                if (movingBlock != null) {

                    drawTowerPiece(
                            canvas,
                            movingBlock
                    );
                }

                canvas.restore();

            } else {

                for (Block block : tower) {

                    drawTowerPiece(
                            canvas,
                            block
                    );
                }

                if (movingBlock != null) {

                    drawTowerPiece(
                            canvas,
                            movingBlock
                    );
                }
            }

        } else {

            // -------------------------------------------------
            // COLLAPSING PIECES
            // -------------------------------------------------

            drawCollapsingTower(canvas);
        }

        // -----------------------------------------------------
        // FALLING EXCESS
        // -----------------------------------------------------

        if (excessFalling) {

            drawFallingExcess(canvas);
        }

        // -----------------------------------------------------
        // SCORE
        // -----------------------------------------------------

        drawScore(canvas);
    }

    // =========================================================
    // DRAW TOWER PIECE
    // =========================================================

    private void drawTowerPiece(
            Canvas canvas,
            Block block
    ) {

        int towerType =
                block.getTowerType();

        if (
                towerType < 0 ||
                        towerType >=
                                croppedTowerImages.length
        ) {

            towerType = 0;
        }

        Bitmap towerBitmap =
                croppedTowerImages[towerType];

        if (towerBitmap == null) {
            return;
        }

        float y =
                block.getY()
                        + cameraOffset;

        float left =
                block.getX();

        float right =
                block.getRight();

        float top = y;

        float bottom =
                y +
                        block.getHeight();

        int sourceLeft =
                (int) (
                        block.getSourceLeft()
                                *
                                towerBitmap.getWidth()
                );

        int sourceRight =
                (int) (
                        block.getSourceRight()
                                *
                                towerBitmap.getWidth()
                );

        sourceLeft =
                Math.max(
                        0,
                        sourceLeft
                );

        sourceRight =
                Math.min(
                        towerBitmap.getWidth(),
                        sourceRight
                );

        if (
                sourceRight <= sourceLeft
        ) {

            return;
        }

        Rect source =
                new Rect(
                        sourceLeft,
                        0,
                        sourceRight,
                        towerBitmap.getHeight()
                );

        RectF destination =
                new RectF(
                        left,
                        top,
                        right,
                        bottom
                );

        paint.setAlpha(255);

        paint.setColor(Color.WHITE);

        canvas.drawBitmap(
                towerBitmap,
                source,
                destination,
                paint
        );
    }

    // =========================================================
    // DROP BLOCK
    // =========================================================

    private void dropBlock() {

        if (movingBlock == null) {
            return;
        }

        if (tower.isEmpty()) {
            return;
        }

        Block previous =
                tower.get(
                        tower.size() - 1
                );

        excessTowerType =
                movingBlock.getTowerType();

        // =====================================================
        // POSITIONS
        // =====================================================

        final float PERFECT_FIT_TOLERANCE = 5f;

        float movingLeft =
                movingBlock.getLeft();

        float movingRight =
                movingBlock.getRight();

        float previousLeft =
                previous.getLeft();

        float previousRight =
                previous.getRight();

        // =====================================================
        // PERFECT FIT
        // =====================================================

        boolean perfectFit =
                Math.abs(
                        movingLeft -
                                previousLeft
                ) <=
                        PERFECT_FIT_TOLERANCE
                        &&
                        Math.abs(
                                movingRight -
                                        previousRight
                        ) <=
                                PERFECT_FIT_TOLERANCE;

        if (perfectFit) {

            float placedX =
                    previous.getLeft();

            float placedY =
                    previous.getY()
                            - blockHeight
                            + 20f;

            Block placedBlock =
                    new Block(
                            placedX,
                            placedY,
                            previous.getWidth(),
                            blockHeight
                    );

            placedBlock.setTowerType(
                    movingBlock.getTowerType()
            );

            placedBlock.setSourceRange(
                    0f,
                    1f
            );

            tower.add(placedBlock);

            score++;

            blockSpeed =
                    Math.min(
                            12f,
                            6f +
                                    score * 0.30f
                    );

            createMovingBlock();

            return;
        }

        // =====================================================
        // CALCULATE OVERLAP
        // =====================================================

        float overlapLeft =
                Math.max(
                        movingLeft,
                        previousLeft
                );

        float overlapRight =
                Math.min(
                        movingRight,
                        previousRight
                );

        float overlapWidth =
                overlapRight -
                        overlapLeft;

        // =====================================================
        // COMPLETE MISS
        // =====================================================

        if (overlapWidth <= 0) {

            /*
             * The player missed.
             *
             * Start the complete tower collapse instead of
             * simply shaking the tower.
             */
            movingBlock = null;

            startTowerCollapse();

            return;
        }

        // =====================================================
        // ACCURATE CUT CALCULATION
        // =====================================================

        float movingWidth =
                movingBlock.getWidth();

        float sourceStart =
                movingBlock.getSourceLeft();

        float sourceEnd =
                movingBlock.getSourceRight();

        float sourceRange =
                sourceEnd -
                        sourceStart;

        float cutLeft =
                overlapLeft -
                        movingLeft;

        float cutRight =
                movingRight -
                        overlapRight;

        float leftRatio =
                cutLeft /
                        movingWidth;

        float rightRatio =
                cutRight /
                        movingWidth;

        leftRatio =
                Math.max(
                        0f,
                        Math.min(
                                1f,
                                leftRatio
                        )
                );

        rightRatio =
                Math.max(
                        0f,
                        Math.min(
                                1f,
                                rightRatio
                        )
                );

        float newSourceLeft =
                sourceStart +
                        sourceRange *
                                leftRatio;

        float newSourceRight =
                sourceEnd -
                        sourceRange *
                                rightRatio;

        // =====================================================
        // LEFT EXCESS
        // =====================================================

        if (
                movingLeft <
                        previousLeft
        ) {

            excessX =
                    movingLeft;

            excessY =
                    movingBlock.getY()
                            + cameraOffset;

            excessWidth =
                    overlapLeft -
                            movingLeft;

            excessHeight =
                    blockHeight;

            excessSourceLeft =
                    movingBlock.getSourceLeft();

            excessSourceRight =
                    newSourceLeft;

            startFallingExcess(
                    -3.5f
            );
        }

        // =====================================================
        // RIGHT EXCESS
        // =====================================================

        if (
                movingRight >
                        previousRight
        ) {

            excessX =
                    overlapRight;

            excessY =
                    movingBlock.getY()
                            + cameraOffset;

            excessWidth =
                    movingRight -
                            overlapRight;

            excessHeight =
                    blockHeight;

            excessSourceLeft =
                    newSourceRight;

            excessSourceRight =
                    movingBlock.getSourceRight();

            startFallingExcess(
                    3.5f
            );
        }

        // =====================================================
        // PLACE OVERLAPPING BLOCK
        // =====================================================

        float placedY =
                previous.getY()
                        - blockHeight
                        + 20f;

        Block placedBlock =
                new Block(
                        overlapLeft,
                        placedY,
                        overlapWidth,
                        blockHeight
                );

        placedBlock.setTowerType(
                movingBlock.getTowerType()
        );

        placedBlock.setSourceRange(
                newSourceLeft,
                newSourceRight
        );

        tower.add(placedBlock);

        score++;

        // =====================================================
        // SPEED
        // =====================================================

        blockSpeed =
                Math.min(
                        12f,
                        6f +
                                score * 0.30f
                );

        // =====================================================
        // NEXT BLOCK
        // =====================================================

        createMovingBlock();
    }

    // =========================================================
    // START TOWER COLLAPSE
    // =========================================================

    private void startTowerCollapse() {

        if (collapseStarted) {
            return;
        }

        collapseStarted = true;

        collapseFinished = false;

        collapseTime = 0f;

        gameOver = true;

        movingBlock = null;

        collapsingPieces.clear();

        // =====================================================
        // CREATE FALLING PIECE FOR EVERY STACKED BLOCK
        // =====================================================

        for (
                int i = 0;
                i < tower.size();
                i++
        ) {

            Block block =
                    tower.get(i);

            /*
             * Lower blocks fall slightly later.
             *
             * This gives the tower a more natural
             * top-to-bottom collapse.
             */
            float delay =
                    (tower.size() - 1 - i)
                            * 0.035f;

            /*
             * Give each piece a small random horizontal
             * movement.
             */
            float velocityX =
                    -2.0f +
                            random.nextFloat()
                                    * 4.0f;

            /*
             * Higher pieces get slightly more upward force
             * before falling.
             */
            float velocityY =
                    -2.0f -
                            random.nextFloat()
                                    * 2.5f;

            /*
             * Small random rotation.
             */
            float rotation =
                    -3f +
                            random.nextFloat()
                                    * 6f;

            float rotationVelocity =
                    -4f +
                            random.nextFloat()
                                    * 8f;

            float drawY =
                    block.getY()
                            + cameraOffset;

            CollapsePiece piece =
                    new CollapsePiece(
                            block,
                            block.getX(),
                            drawY,
                            block.getWidth(),
                            block.getHeight(),
                            velocityX,
                            velocityY,
                            rotation,
                            rotationVelocity,
                            delay
                    );

            collapsingPieces.add(piece);
        }

        /*
         * Remove the normal tower from the normal drawing list.
         *
         * The pieces are now controlled by the collapse
         * animation.
         */
        tower.clear();

        /*
         * Reset wiggle.
         */
        towerWiggleAngle = 0f;

        wiggleTime = 0f;

        invalidate();
    }

    // =========================================================
    // UPDATE COLLAPSE
    // =========================================================

    private void updateCollapse() {

        collapseTime += 0.016f;

        boolean anyPieceStillVisible =
                false;

        for (
                CollapsePiece piece :
                collapsingPieces
        ) {

            // -------------------------------------------------
            // DELAY
            // -------------------------------------------------

            if (
                    collapseTime <
                            piece.delay
            ) {

                anyPieceStillVisible = true;

                continue;
            }

            // -------------------------------------------------
            // GRAVITY
            // -------------------------------------------------

            piece.velocityY +=
                    COLLAPSE_GRAVITY;

            // -------------------------------------------------
            // MOVEMENT
            // -------------------------------------------------

            piece.x +=
                    piece.velocityX;

            piece.y +=
                    piece.velocityY;

            // -------------------------------------------------
            // ROTATION
            // -------------------------------------------------

            piece.rotation +=
                    piece.rotationVelocity;

            // -------------------------------------------------
            // GROUND
            // -------------------------------------------------

            float groundY =
                    screenHeight +
                            80f;

            if (
                    piece.y +
                            piece.height
                            <
                            groundY
            ) {

                anyPieceStillVisible = true;
            }
        }

        // =====================================================
        // FINISH COLLAPSE
        // =====================================================

        if (
                !anyPieceStillVisible ||
                        collapseTime >
                                COLLAPSE_MAX_TIME
        ) {

            collapseFinished = true;

            collapsingPieces.clear();
        }
    }

    // =========================================================
    // DRAW COLLAPSING TOWER
    // =========================================================

    private void drawCollapsingTower(
            Canvas canvas
    ) {

        for (
                CollapsePiece piece :
                collapsingPieces
        ) {

            if (
                    collapseTime <
                            piece.delay
            ) {

                continue;
            }

            drawCollapsePiece(
                    canvas,
                    piece
            );
        }
    }

    // =========================================================
    // DRAW COLLAPSING PIECE
    // =========================================================

    private void drawCollapsePiece(
            Canvas canvas,
            CollapsePiece piece
    ) {

        int towerType =
                piece.towerType;

        if (
                towerType < 0 ||
                        towerType >=
                                croppedTowerImages.length
        ) {

            towerType = 0;
        }

        Bitmap towerBitmap =
                croppedTowerImages[towerType];

        if (towerBitmap == null) {
            return;
        }

        // =====================================================
        // SOURCE
        // =====================================================

        int sourceLeft =
                (int) (
                        piece.sourceLeft *
                                towerBitmap.getWidth()
                );

        int sourceRight =
                (int) (
                        piece.sourceRight *
                                towerBitmap.getWidth()
                );

        sourceLeft =
                Math.max(
                        0,
                        sourceLeft
                );

        sourceRight =
                Math.min(
                        towerBitmap.getWidth(),
                        sourceRight
                );

        if (
                sourceRight <=
                        sourceLeft
        ) {

            return;
        }

        Rect source =
                new Rect(
                        sourceLeft,
                        0,
                        sourceRight,
                        towerBitmap.getHeight()
                );

        RectF destination =
                new RectF(
                        piece.x,
                        piece.y,
                        piece.x +
                                piece.width,
                        piece.y +
                                piece.height
                );

        // =====================================================
        // ROTATE PIECE
        // =====================================================

        canvas.save();

        float centerX =
                piece.x +
                        piece.width / 2f;

        float centerY =
                piece.y +
                        piece.height / 2f;

        canvas.rotate(
                piece.rotation,
                centerX,
                centerY
        );

        paint.setAlpha(255);

        paint.setColor(Color.WHITE);

        canvas.drawBitmap(
                towerBitmap,
                source,
                destination,
                paint
        );

        canvas.restore();
    }

    // =========================================================
    // FALLING EXCESS
    // =========================================================

    private void startFallingExcess(
            float velocityX
    ) {

        excessFalling = true;

        excessVelocityX =
                velocityX;

        excessVelocityY = 2f;

        excessRotation = 0;
    }

    // =========================================================
    // UPDATE FALLING EXCESS
    // =========================================================

    private void updateFallingExcess() {

        excessVelocityY += 0.5f;

        excessX +=
                excessVelocityX;

        excessY +=
                excessVelocityY;

        excessRotation += 7f;

        if (
                excessY >
                        screenHeight + 300
        ) {

            excessFalling = false;
        }
    }

    // =========================================================
    // DRAW FALLING EXCESS
    // =========================================================

    private void drawFallingExcess(
            Canvas canvas
    ) {

        int towerType =
                excessTowerType;

        if (
                towerType < 0 ||
                        towerType >=
                                croppedTowerImages.length
        ) {

            towerType = 0;
        }

        Bitmap towerBitmap =
                croppedTowerImages[towerType];

        if (towerBitmap == null) {
            return;
        }

        canvas.save();

        float centerX =
                excessX +
                        excessWidth / 2f;

        float centerY =
                excessY +
                        excessHeight / 2f;

        canvas.rotate(
                excessRotation,
                centerX,
                centerY
        );

        int sourceLeft =
                (int) (
                        excessSourceLeft *
                                towerBitmap.getWidth()
                );

        int sourceRight =
                (int) (
                        excessSourceRight *
                                towerBitmap.getWidth()
                );

        sourceLeft =
                Math.max(
                        0,
                        sourceLeft
                );

        sourceRight =
                Math.min(
                        towerBitmap.getWidth(),
                        sourceRight
                );

        if (
                sourceRight >
                        sourceLeft
        ) {

            Rect source =
                    new Rect(
                            sourceLeft,
                            0,
                            sourceRight,
                            towerBitmap.getHeight()
                    );

            RectF destination =
                    new RectF(
                            excessX,
                            excessY,
                            excessX +
                                    excessWidth,
                            excessY +
                                    excessHeight
                    );

            paint.setAlpha(255);

            paint.setColor(Color.WHITE);

            canvas.drawBitmap(
                    towerBitmap,
                    source,
                    destination,
                    paint
            );
        }

        canvas.restore();
    }

    // =========================================================
    // CAMERA
    // =========================================================

    private void updateCamera() {

        if (tower.isEmpty()) {
            return;
        }

        Block topBlock =
                tower.get(
                        tower.size() - 1
                );

        float visibleY =
                topBlock.getY()
                        + cameraOffset;

        float targetY =
                screenHeight * 0.35f;

        float difference =
                targetY -
                        visibleY;

        if (visibleY < targetY) {

            cameraVelocity +=
                    difference *
                            CAMERA_FOLLOW_STRENGTH;

            cameraVelocity =
                    Math.min(
                            cameraVelocity,
                            CAMERA_MAX_SPEED
                    );

        } else {

            cameraVelocity *= 0.85f;
        }

        cameraOffset +=
                cameraVelocity;

        if (
                Math.abs(
                        cameraVelocity
                ) < 0.01f
        ) {

            cameraVelocity = 0f;
        }
    }

    // =========================================================
    // SCORE
    // =========================================================

    private void drawScore(
            Canvas canvas
    ) {

        paint.setColor(Color.WHITE);

        paint.setTextAlign(
                Paint.Align.CENTER
        );

        paint.setFakeBoldText(true);

        // =====================================================
        // LARGE SCORE
        // =====================================================

        paint.setTextSize(52);

        canvas.drawText(
                String.valueOf(score),
                screenWidth / 2f,
                70,
                paint
        );

        paint.setFakeBoldText(false);

        // =====================================================
        // HEIGHT LABEL
        // =====================================================

        paint.setTextSize(18);

        canvas.drawText(
                "HEIGHT",
                screenWidth / 2f,
                96,
                paint
        );
    }

    // =========================================================
    // GAME OVER
    // =========================================================

    private void drawGameOver(
            Canvas canvas
    ) {

        paint.setColor(
                Color.argb(
                        170,
                        0,
                        0,
                        0
                )
        );

        canvas.drawRect(
                0,
                0,
                screenWidth,
                screenHeight,
                paint
        );

        paint.setTextAlign(
                Paint.Align.CENTER
        );

        paint.setColor(Color.WHITE);

        paint.setFakeBoldText(true);

        // =====================================================
        // TITLE
        // =====================================================

        paint.setTextSize(46);

        canvas.drawText(
                "TOWER FALLEN",
                screenWidth / 2f,
                screenHeight / 2f - 50,
                paint
        );

        paint.setFakeBoldText(false);

        // =====================================================
        // SCORE
        // =====================================================

        paint.setTextSize(27);

        canvas.drawText(
                "HEIGHT: " + score,
                screenWidth / 2f,
                screenHeight / 2f + 5,
                paint
        );

        // =====================================================
        // RESTART
        // =====================================================

        paint.setTextSize(22);

        canvas.drawText(
                "TAP TO REBUILD",
                screenWidth / 2f,
                screenHeight / 2f + 65,
                paint
        );
    }

    // =========================================================
    // TOUCH
    // =========================================================

    @Override
    public boolean onTouchEvent(
            MotionEvent event
    ) {

        if (
                event.getAction()
                        != MotionEvent.ACTION_DOWN
        ) {

            return true;
        }

        // =====================================================
        // START SCREEN -> TOWER SELECT
        // =====================================================

        if (
                !gameStarted &&
                        !showTowerSelect
        ) {

            showTowerSelect = true;

            invalidate();

            return true;
        }

        // =====================================================
        // TOWER SELECT
        // =====================================================

        if (
                !gameStarted &&
                        showTowerSelect
        ) {

            int tappedIndex =
                    getTappedTowerIndex(
                            event.getX(),
                            event.getY()
                    );

            if (tappedIndex != -1) {

                selectedTowerType =
                        tappedIndex;

                startGame();
            }

            return true;
        }

        // =====================================================
        // GAME OVER
        // =====================================================

        if (gameOver) {

            /*
             * Ignore taps while the tower is still collapsing.
             */
            if (
                    collapseStarted &&
                            !collapseFinished
            ) {

                return true;
            }

            /*
             * Once the collapse has finished, tapping returns
             * to the start screen.
             */
            gameStarted = false;

            gameOver = false;

            showTowerSelect = false;

            collapseStarted = false;

            collapseFinished = false;

            collapsingPieces.clear();

            invalidate();

            return true;
        }

        // =====================================================
        // DROP BLOCK
        // =====================================================

        dropBlock();

        return true;
    }
}