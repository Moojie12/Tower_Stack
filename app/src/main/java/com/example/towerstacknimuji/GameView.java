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

    private final Random random =
            new Random();

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
    // BACKGROUNDS
    // =========================================================

    private Bitmap cityBackground;
    private Bitmap sunsetBackground;
    private Bitmap spaceBackground;

    // =========================================================
    // TOWER IMAGES
    // =========================================================

    /*
     * The game has three different tower designs.
     *
     * 0 = tower_core.png
     * 1 = tower_core1.png
     * 2 = tower_core2.png
     */

    private Bitmap[] towerImages;

    /*
     * Cropped versions of the three tower images.
     *
     * Transparent padding is removed from each image.
     */

    private Bitmap[] croppedTowerImages;

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

    /*
     * About 42% of the phone width.
     */

    private static final float BLOCK_WIDTH_RATIO = 0.42f;

    /*
     * About 15% of the phone height.
     */

    private static final float BLOCK_HEIGHT_RATIO = 0.15f;

    // =========================================================
    // MOVEMENT
    // =========================================================

    private float blockSpeed = 6f;

    private boolean movingRight = true;

    // =========================================================
    // CAMERA
    // =========================================================

    private float cameraOffset = 0f;

    private float cameraVelocity = 0f;

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

    /*
     * Remembers which tower image the falling piece came from.
     *
     * This is important when tower_core1 or tower_core2 gets cut.
     */

    private int excessTowerType = 0;

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
        // BACKGROUNDS
        // =====================================================

        cityBackground = BitmapFactory.decodeResource(
                getResources(),
                R.drawable.bg_city
        );

        sunsetBackground = BitmapFactory.decodeResource(
                getResources(),
                R.drawable.bg_sunset
        );

        spaceBackground = BitmapFactory.decodeResource(
                getResources(),
                R.drawable.bg_space
        );

        // =====================================================
        // TOWER IMAGES
        // =====================================================

        towerImages = new Bitmap[3];

        croppedTowerImages = new Bitmap[3];

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

        // =====================================================
        // CROP TRANSPARENT AREAS
        // =====================================================

        for (int i = 0;
             i < towerImages.length;
             i++) {

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

        screenWidth = getWidth();

        screenHeight = getHeight();

        drawBackground(canvas);

        if (!gameStarted) {

            drawStartScreen(canvas);

            return;
        }

        updateGame();

        drawGame(canvas);

        if (gameOver) {

            drawGameOver(canvas);

        } else {

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

        /*
         * Find the actual visible portion of the PNG.
         */

        for (int y = 0;
             y < height;
             y++) {

            for (int x = 0;
                 x < width;
                 x++) {

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

        /*
         * If no visible pixels were found,
         * return the original image.
         */

        if (
                maxX < minX ||
                        maxY < minY
        ) {

            return source;
        }

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

        Bitmap background;

        if (score < 10) {

            background = cityBackground;

        } else if (score < 20) {

            background = sunsetBackground;

        } else {

            background = spaceBackground;
        }

        if (background == null) {

            canvas.drawColor(
                    Color.rgb(
                            10,
                            15,
                            30
                    )
            );

            return;
        }

        float scaleX =
                screenWidth /
                        background.getWidth();

        float scaleY =
                screenHeight /
                        background.getHeight();

        float scale =
                Math.max(
                        scaleX,
                        scaleY
                );

        float width =
                background.getWidth()
                        * scale;

        float height =
                background.getHeight()
                        * scale;

        float left =
                (screenWidth - width)
                        / 2f;

        float top =
                (screenHeight - height)
                        / 2f;

        RectF destination =
                new RectF(
                        left,
                        top,
                        left + width,
                        top + height
                );

        /*
         * Make sure the background is opaque.
         */

        paint.setAlpha(255);

        canvas.drawBitmap(
                background,
                null,
                destination,
                paint
        );

        // -----------------------------------------------------
        // DARK CINEMATIC OVERLAY
        // -----------------------------------------------------

        paint.setColor(
                Color.argb(
                        55,
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

        paint.setTextSize(18);

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
                        buttonLeft + buttonWidth,
                        buttonTop + buttonHeight
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

        paint.setTextSize(23);

        canvas.drawText(
                "TAP TO START",
                screenWidth / 2f,
                buttonTop + 45,
                paint
        );
    }

    // =========================================================
    // START GAME
    // =========================================================

    private void startGame() {

        gameStarted = true;

        gameOver = false;

        score = 0;

        cameraOffset = 0;

        cameraVelocity = 0;

        tower.clear();

        movingBlock = null;

        excessFalling = false;

        // -----------------------------------------------------
        // BLOCK DIMENSIONS
        // -----------------------------------------------------

        blockWidth =
                screenWidth
                        * BLOCK_WIDTH_RATIO;

        blockHeight =
                screenHeight
                        * BLOCK_HEIGHT_RATIO;

        blockHeight =
                Math.max(
                        90,
                        Math.min(
                                blockHeight,
                                125
                        )
                );

        // -----------------------------------------------------
        // FIRST BLOCK
        // -----------------------------------------------------

        float firstX =
                (screenWidth - blockWidth)
                        / 2f;

        float firstY =
                screenHeight
                        - blockHeight
                        - 80;

        Block firstBlock =
                new Block(
                        firstX,
                        firstY,
                        blockWidth,
                        blockHeight
                );

        /*
         * The first block uses tower_core.png.
         */

        firstBlock.setTowerType(0);

        tower.add(firstBlock);

        // -----------------------------------------------------
        // CREATE MOVING BLOCK
        // -----------------------------------------------------

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

        /*
         * Place the moving block slightly above
         * the previous block.
         */

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

        // =====================================================
        // RANDOM TOWER TYPE
        // =====================================================

        /*
         * Randomly select:
         *
         * 0 = tower_core.png
         * 1 = tower_core1.png
         * 2 = tower_core2.png
         */

        int randomTower =
                random.nextInt(3);

        movingBlock.setTowerType(
                randomTower
        );

        // -----------------------------------------------------
        // START POSITION
        // -----------------------------------------------------

        movingBlock.setX(0);

        movingRight = true;
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

        updateCamera();
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
                        screenWidth
                                - movingBlock.getWidth()
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
        // TOWER
        // -----------------------------------------------------

        for (Block block : tower) {

            drawTowerPiece(
                    canvas,
                    block
            );
        }

        // -----------------------------------------------------
        // MOVING BLOCK
        // -----------------------------------------------------

        if (movingBlock != null) {

            drawTowerPiece(
                    canvas,
                    movingBlock
            );
        }

        // -----------------------------------------------------
        // FALLING EXCESS
        // -----------------------------------------------------

        if (excessFalling) {

            drawFallingExcess(
                    canvas
            );
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

        /*
         * Determine which tower image this Block uses.
         */

        int towerType =
                block.getTowerType();

        /*
         * Safety check.
         */

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

        // -----------------------------------------------------
        // POSITION
        // -----------------------------------------------------

        float y =
                block.getY()
                        + cameraOffset;

        float left =
                block.getX();

        float right =
                block.getRight();

        float top = y;

        float bottom =
                y + block.getHeight();

        // -----------------------------------------------------
        // SOURCE RANGE
        // -----------------------------------------------------

        int sourceLeft =
                (int) (
                        block.getSourceLeft()
                                * towerBitmap
                                .getWidth()
                );

        int sourceRight =
                (int) (
                        block.getSourceRight()
                                * towerBitmap
                                .getWidth()
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
                        towerBitmap
                                .getHeight()
                );

        RectF destination =
                new RectF(
                        left,
                        top,
                        right,
                        bottom
                );

        // -----------------------------------------------------
        // DRAW OPAQUE
        // -----------------------------------------------------

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

        /*
         * Remember the image used by the moving block.
         *
         * This is used if part of the block falls away.
         */

        excessTowerType =
                movingBlock.getTowerType();

        // =====================================================
        // PERFECT STACK TOLERANCE
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
        // CHECK PERFECT FIT
        // =====================================================

        boolean perfectFit =
                Math.abs(
                        movingLeft -
                                previousLeft
                ) <= PERFECT_FIT_TOLERANCE
                        &&
                        Math.abs(
                                movingRight -
                                        previousRight
                        ) <= PERFECT_FIT_TOLERANCE;

        // =====================================================
        // PERFECT FIT
        // =====================================================

        if (perfectFit) {

            /*
             * Snap perfectly into position.
             */

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

            /*
             * Keep the SAME tower design.
             */

            placedBlock.setTowerType(
                    movingBlock.getTowerType()
            );

            /*
             * Show the COMPLETE image.
             */

            placedBlock.setSourceRange(
                    0f,
                    1f
            );

            tower.add(
                    placedBlock
            );

            score++;

            // -------------------------------------------------
            // SPEED
            // -------------------------------------------------

            blockSpeed =
                    Math.min(
                            12f,
                            6f
                                    + score * 0.15f
                    );

            // -------------------------------------------------
            // NEXT BLOCK
            // -------------------------------------------------

            createMovingBlock();

            return;
        }

        // =====================================================
        // NORMAL OVERLAP
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

            gameOver = true;

            movingBlock = null;

            return;
        }

        // =====================================================
        // SOURCE RANGE
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

        float leftPercent =
                (
                        overlapLeft -
                                movingLeft
                )
                        / movingWidth;

        float rightPercent =
                (
                        overlapRight -
                                movingLeft
                )
                        / movingWidth;

        float newSourceLeft =
                sourceStart +
                        sourceRange *
                                leftPercent;

        float newSourceRight =
                sourceStart +
                        sourceRange *
                                rightPercent;

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
                    previousLeft -
                            movingLeft;

            excessHeight =
                    blockHeight;

            excessSourceLeft =
                    movingBlock
                            .getSourceLeft();

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
                    previousRight;

            excessY =
                    movingBlock.getY()
                            + cameraOffset;

            excessWidth =
                    movingRight -
                            previousRight;

            excessHeight =
                    blockHeight;

            excessSourceLeft =
                    newSourceRight;

            excessSourceRight =
                    movingBlock
                            .getSourceRight();

            startFallingExcess(
                    3.5f
            );
        }

        // =====================================================
        // PLACE NEW BLOCK
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

        /*
         * IMPORTANT:
         *
         * Keep the same random tower design that the
         * moving block was using.
         */

        placedBlock.setTowerType(
                movingBlock.getTowerType()
        );

        /*
         * Only show the part that remains after cutting.
         */

        placedBlock.setSourceRange(
                newSourceLeft,
                newSourceRight
        );

        tower.add(
                placedBlock
        );

        score++;

        // =====================================================
        // SPEED
        // =====================================================

        blockSpeed =
                Math.min(
                        12f,
                        6f +
                                score * 0.15f
                );

        // =====================================================
        // NEXT BLOCK
        // =====================================================

        createMovingBlock();
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

        /*
         * Get the correct tower image.
         */

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

        // -----------------------------------------------------
        // SOURCE
        // -----------------------------------------------------

        int sourceLeft =
                (int) (
                        excessSourceLeft *
                                towerBitmap
                                        .getWidth()
                );

        int sourceRight =
                (int) (
                        excessSourceRight *
                                towerBitmap
                                        .getWidth()
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
                            towerBitmap
                                    .getHeight()
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

            // -------------------------------------------------
            // DRAW OPAQUE
            // -------------------------------------------------

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

        // -----------------------------------------------------
        // CAMERA FOLLOW
        // -----------------------------------------------------

        if (visibleY < targetY) {

            cameraVelocity +=
                    difference * 0.012f;

            cameraVelocity =
                    Math.min(
                            cameraVelocity,
                            8f
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

        paint.setTextSize(40);

        canvas.drawText(
                String.valueOf(score),
                screenWidth / 2f,
                65,
                paint
        );

        paint.setFakeBoldText(false);

        paint.setTextSize(13);

        canvas.drawText(
                "HEIGHT",
                screenWidth / 2f,
                87,
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

        paint.setTextSize(42);

        canvas.drawText(
                "TOWER FALLEN",
                screenWidth / 2f,
                screenHeight / 2f - 50,
                paint
        );

        paint.setFakeBoldText(false);

        paint.setTextSize(23);

        canvas.drawText(
                "HEIGHT: " + score,
                screenWidth / 2f,
                screenHeight / 2f + 5,
                paint
        );

        paint.setTextSize(19);

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

        // -----------------------------------------------------
        // START
        // -----------------------------------------------------

        if (!gameStarted) {

            startGame();

            return true;
        }

        // -----------------------------------------------------
        // RESTART
        // -----------------------------------------------------

        if (gameOver) {

            startGame();

            return true;
        }

        // -----------------------------------------------------
        // DROP
        // -----------------------------------------------------

        dropBlock();

        return true;
    }
}