package com.phucvr.flappybird;

import static com.badlogic.gdx.math.MathUtils.random;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;

import java.util.Random;

public class FlappyGame extends ApplicationAdapter {
    private SpriteBatch spriteRenderer;
    private Texture gameBackground; // Фон
    private float backgroundX1, backgroundX2; // Положение двух частей фона
    private float backgroundSpeed; // Скорость движения фона

    private Texture[] wingStates;
    private int wingFlapState; // Состояние махания крыльями
    private float birdVerticalPosition; // Позиция птицы по Y
    private float birdFallSpeed; // Скорость падения птицы
    private float gravityForce; // Сила гравитации

    private Texture cloudTexture;
    private float[] cloudX; // Позиции облаков по X
    private float[] cloudY; // Позиции облаков по Y
    private float cloudSpeed; // Скорость движения облаков
    private int cloudCount; // Количество облаков

    private Texture upperPipe, lowerPipe;
    private float[] pipePositions; // Позиции труб по оси X
    private float[] pipeOffsets; // Случайные смещения труб
    private Rectangle[] upperPipeHitboxes, lowerPipeHitboxes;
    private int pipeCount; // Количество труб
    private float pipeSpeed; // Скорость движения труб
    private float pipeSpacing; // Расстояние между трубами
    private float pipeGapSize; // Разрыв между верхней и нижней трубами

    private int gameScore; // Счёт
    private int nextScoringPipe; // Текущая труба для начисления очков
    private BitmapFont scoreFont; // Шрифт для отображения счёта

    private int currentGameState; // Текущее состояние игры
    private Texture gameOverScreen;
    private Random pipeRandomizer;

    private Circle birdHitCircle;
    private boolean isGameRestarting = false;

    @Override
    public void create() {
        spriteRenderer = new SpriteBatch();
        gameBackground = new Texture("bg.png");

        backgroundX1 = 0;
        backgroundX2 = Gdx.graphics.getWidth();
        backgroundSpeed = 2;

        wingStates = new Texture[]{new Texture("bird.png"), new Texture("bird2.png")};
        wingFlapState = 0;
        birdVerticalPosition = Gdx.graphics.getHeight() / 2f;
        birdFallSpeed = 0;
        gravityForce = 2;

        upperPipe = new Texture("toptube.png");
        lowerPipe = new Texture("bottomtube.png");
        pipeCount = 4;
        pipePositions = new float[pipeCount];
        pipeOffsets = new float[pipeCount];
        pipeSpeed = 4;
        pipeGapSize = 800;
        pipeSpacing = Gdx.graphics.getWidth() * 3 / 4f;

        upperPipeHitboxes = new Rectangle[pipeCount];
        lowerPipeHitboxes = new Rectangle[pipeCount];
        pipeRandomizer = new Random();

        cloudTexture = new Texture("cloud.png");
        cloudCount = 3;
        cloudX = new float[cloudCount];
        cloudY = new float[cloudCount];
        cloudSpeed = 1;

        for (int i = 0; i < cloudCount; i++) {

            cloudX[i] = random.nextFloat() * Gdx.graphics.getWidth(); // Случайное начальное положение по X
            cloudY[i] = random.nextFloat() * Gdx.graphics.getHeight() * 0.5f + Gdx.graphics.getHeight() * 0.5f; // Высота на верхней половине экрана
        }

        for (int i = 0; i < pipeCount; i++) {
            pipePositions[i] = Gdx.graphics.getWidth() + i * pipeSpacing;
            pipeOffsets[i] = (pipeRandomizer.nextFloat() - 0.5f) * (Gdx.graphics.getHeight() - pipeGapSize - 200);
            upperPipeHitboxes[i] = new Rectangle();
            lowerPipeHitboxes[i] = new Rectangle();
        }

        currentGameState = 0;
        gameOverScreen = new Texture("gameOver.png");

        birdHitCircle = new Circle();
        gameScore = 0;
        nextScoringPipe = 0;
        scoreFont = new BitmapFont();
        scoreFont.setColor(Color.WHITE);
        scoreFont.getData().scale(5);
    }

    @Override
    public void render() {
        spriteRenderer.begin();
        updateBackground();
        spriteRenderer.draw(gameBackground, backgroundX1, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        spriteRenderer.draw(gameBackground, backgroundX2, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        if (currentGameState == 1) {
            if (Gdx.input.justTouched()) {
                birdFallSpeed = -25;
            }
            for (int i = 0; i < pipeCount; i++) {
                if (pipePositions[i] < -upperPipe.getWidth()) {
                    pipePositions[i] += pipeCount * pipeSpacing;
                    pipeOffsets[i] = (pipeRandomizer.nextFloat() - 0.5f) * (Gdx.graphics.getHeight() - pipeGapSize - 200);
                } else {
                    pipePositions[i] -= pipeSpeed;
                }
                int g;
                for ( g = 0; g < cloudCount; g++) {
                    spriteRenderer.draw(cloudTexture, cloudX[g], cloudY[g], cloudTexture.getWidth(), cloudTexture.getHeight());
                    cloudX[g] -= cloudSpeed;

                    if (cloudX[g] + cloudTexture.getWidth() < 0) {
                        cloudX[g] = Gdx.graphics.getWidth();
                        cloudY[g] = random.nextFloat() * Gdx.graphics.getHeight() * 0.5f + Gdx.graphics.getHeight() * 0.5f;
                    }
                }

                spriteRenderer.draw(upperPipe, pipePositions[i], Gdx.graphics.getHeight() / 2f + pipeGapSize / 2f + pipeOffsets[i]);
                spriteRenderer.draw(lowerPipe, pipePositions[i], Gdx.graphics.getHeight() / 2f - pipeGapSize / 2f - lowerPipe.getHeight() + pipeOffsets[i]);


                upperPipeHitboxes[i].set(pipePositions[i], Gdx.graphics.getHeight() / 2f + pipeGapSize / 2f + pipeOffsets[i], upperPipe.getWidth(), upperPipe.getHeight());
                lowerPipeHitboxes[i].set(pipePositions[i], Gdx.graphics.getHeight() / 2f - pipeGapSize / 2f - lowerPipe.getHeight() + pipeOffsets[i], lowerPipe.getWidth(), lowerPipe.getHeight());
            }

            // счёт
            if (pipePositions[nextScoringPipe] < Gdx.graphics.getWidth() / 2f) {
                gameScore++;
                nextScoringPipe = (nextScoringPipe + 1) % pipeCount;
            }
            wingFlapState = wingFlapState == 0 ? 1 : 0;

            birdFallSpeed += gravityForce;
            birdVerticalPosition -= birdFallSpeed;

            if (birdVerticalPosition <= 0) {
                currentGameState = 2;
            }
        } else if (currentGameState == 0) {
            if (Gdx.input.justTouched()) {
                currentGameState = 1;
            }
        } else if (currentGameState == 2) {
            spriteRenderer.draw(gameOverScreen, Gdx.graphics.getWidth() / 2f - gameOverScreen.getWidth() / 2f, Gdx.graphics.getHeight() / 2f - gameOverScreen.getHeight() / 2f);

            // Воскрешение птицы
            if(currentGameState==2){
                Gdx.app.log("Game State", "End screen displayed, waiting for touch...");
                if (Gdx.input.justTouched()) {
                    Gdx.app.log("Input", "Screen touched, restarting game...");
                    resetGame();
                    currentGameState = 1;
                }
            }
        }
        spriteRenderer.draw(wingStates[wingFlapState], Gdx.graphics.getWidth() / 2f - wingStates[wingFlapState].getWidth() / 2f, birdVerticalPosition);
        scoreFont.draw(spriteRenderer, String.valueOf(gameScore), 100, Gdx.graphics.getHeight() - 100);
        spriteRenderer.end();

        // Проверка столкновений
        birdHitCircle.set(Gdx.graphics.getWidth() / 2f, birdVerticalPosition + wingStates[wingFlapState].getHeight() / 2f, wingStates[wingFlapState].getWidth() / 2f);
        if (currentGameState == 1 && !isGameRestarting) { // Проверяем только если игра активна и не перезапускается
            birdHitCircle.set(Gdx.graphics.getWidth() / 2f, birdVerticalPosition + wingStates[wingFlapState].getHeight() / 2f, wingStates[wingFlapState].getWidth() / 2f);
            for (int i = 0; i < pipeCount; i++) {
                if (Intersector.overlaps(birdHitCircle, upperPipeHitboxes[i]) || Intersector.overlaps(birdHitCircle, lowerPipeHitboxes[i])) {
                    currentGameState = 2;
                }
            }
        }
        if (isGameRestarting) {
            isGameRestarting = false;
        }
    }   private void updateBackground() {
        if (currentGameState == 1) {
            backgroundX1 -= backgroundSpeed;
            backgroundX2 -= backgroundSpeed;

            if (backgroundX1 + Gdx.graphics.getWidth() <= 0) {
                backgroundX1 = backgroundX2 + Gdx.graphics.getWidth();
            }

            if (backgroundX2 + Gdx.graphics.getWidth() <= 0) {
                backgroundX2 = backgroundX1 + Gdx.graphics.getWidth();
            }
        }
    }

    private void resetGame() {
        birdVerticalPosition = Gdx.graphics.getHeight() / 2f;
        birdFallSpeed = 0;
        gameScore = 0;
        nextScoringPipe = 0;
        for (int i = 0; i < pipeCount; i++) {
            pipePositions[i] = Gdx.graphics.getWidth() + i * pipeSpacing;
            pipeOffsets[i] = (pipeRandomizer.nextFloat() - 0.5f) * (Gdx.graphics.getHeight() - pipeGapSize - 200);
        }
        isGameRestarting = true;
    }

    @Override
    public void dispose() {
        spriteRenderer.dispose();
        gameBackground.dispose();
        for (Texture texture : wingStates) {
            texture.dispose();
        }
        upperPipe.dispose();
        lowerPipe.dispose();
        gameOverScreen.dispose();
        scoreFont.dispose();
    }
}
