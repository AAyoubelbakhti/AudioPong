package com.enricmieza.gdxjsyn;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.jsyn.unitgen.LineOut;
import com.jsyn.unitgen.UnitOscillator;

public class MainScreen implements Screen {
    private final GdxJsynGame game;
    private final FitViewport viewport;

    private Texture backgroundTexture;
    private Texture ballTexture;
    private Texture paddleTexture;
    private Sprite ballSprite;
    private Sprite paddleSprite;

    private Vector2 ballVelocity;
    private Rectangle ballRect;
    private Rectangle paddleRect;

    private Vector2 touchPos;
    private boolean gameOver;
    private float gameTime;
    private float bestTime;

    private static final float PADDLE_WIDTH = 3f;
    private static final float PADDLE_HEIGHT = 0.5f;
    private static final float BALL_SIZE = 1f;
    private static final float INITIAL_BALL_SPEED = 5f;
    private static final float BALL_SPEED_INCREASE_FACTOR = 8f;
    private static final float BASE_FREQUENCY = 220f;
    private static final float MAX_FREQUENCY = 880f;

    public MainScreen(GdxJsynGame game) {
        this.game = game;
        this.viewport = game.viewport;

        touchPos = new Vector2();
        ballVelocity = new Vector2();
        ballRect = new Rectangle();
        paddleRect = new Rectangle();
        gameOver = false;
        gameTime = 0f;
        bestTime = 0f;

    }

    @Override
    public void show() {
        backgroundTexture = new Texture("background.png");
        ballTexture = new Texture("ball.png");
        paddleTexture = new Texture("paddle.png");

        ballSprite = new Sprite(ballTexture);
        ballSprite.setSize(BALL_SIZE, BALL_SIZE);

        paddleSprite = new Sprite(paddleTexture);
        paddleSprite.setSize(PADDLE_WIDTH, PADDLE_HEIGHT);
        paddleSprite.setPosition(
            viewport.getWorldWidth() / 2 - PADDLE_WIDTH / 2,
            0.5f
        );



        try {
            game.getLineOut().start();
            Gdx.app.log("JSYN_MAIN", "LineOut iniciado en show().");

            game.getBallOscillator().amplitude.set(0.0);
        } catch (Exception e) {
            Gdx.app.error("JSYN_MAIN", "Error iniciando LineOut en show()", e);
        }

        restart();
    }

    @Override
    public void render(float delta) {
        handleInput();
        update(delta);
        draw();
    }

    private void handleInput() {
        if (Gdx.input.isTouched() && !gameOver) {
            touchPos.set(Gdx.input.getX(), Gdx.input.getY());
            viewport.unproject(touchPos);

            paddleSprite.setX(MathUtils.clamp(
                touchPos.x - PADDLE_WIDTH / 2,
                0,
                viewport.getWorldWidth() - PADDLE_WIDTH
            ));
        }

        if (gameOver && Gdx.input.justTouched()) {
            restart();
        }
    }

    private void update(float delta) {
        if (gameOver) return;

        gameTime += delta;
        ballSprite.translate(ballVelocity.x * delta, ballVelocity.y * delta);
        ballRect.set(ballSprite.getX(), ballSprite.getY(), ballSprite.getWidth(), ballSprite.getHeight());
        paddleRect.set(paddleSprite.getX(), paddleSprite.getY(), paddleSprite.getWidth(), paddleSprite.getHeight());

        float worldWidth = viewport.getWorldWidth();
        float worldHeight = viewport.getWorldHeight();


        if (ballSprite.getX() <= 0 || ballSprite.getX() + ballSprite.getWidth() >= worldWidth) {
            ballVelocity.x = -ballVelocity.x;
            ballSprite.setX(MathUtils.clamp(ballSprite.getX(), 0, worldWidth - ballSprite.getWidth()));
            //playBounceSound();
        }
        if (ballSprite.getY() + ballSprite.getHeight() >= worldHeight) {
            ballVelocity.y = -ballVelocity.y;
            ballSprite.setY(worldHeight - ballSprite.getHeight());
            //playBounceSound();
        }

        if (ballRect.overlaps(paddleRect) && ballVelocity.y < 0) {

            ballSprite.setY(paddleRect.y + paddleRect.height);
            ballVelocity.y = -ballVelocity.y;
            float hitCenterX = ballRect.x + ballRect.width / 2;
            float paddleCenterX = paddleRect.x + paddleRect.width / 2;
            float hitOffset = hitCenterX - paddleCenterX;
            float normalizedHitPosition = MathUtils.clamp(hitOffset / (PADDLE_WIDTH / 2), -1f, 1f);

            ballVelocity.x = normalizedHitPosition * BALL_SPEED_INCREASE_FACTOR;
            ballVelocity.y *= 1.05f;

            //playBounceSound();
        }

        if (ballSprite.getY() < 0) {
            gameOver = true;
            game.getBallOscillator().amplitude.set(0.0);
            if (gameTime > bestTime) {
                bestTime = gameTime;
            }
        }

        if (!gameOver) {
            //updateBallSound();
        }
    }

    private void draw() {
        ScreenUtils.clear(0.2f, 0.2f, 0.2f, 1);
        viewport.apply();
        game.batch.setProjectionMatrix(viewport.getCamera().combined);
        game.batch.begin();
        float worldWidth = viewport.getWorldWidth();
        float worldHeight = viewport.getWorldHeight();

        game.batch.draw(backgroundTexture, 0, 0, worldWidth, worldHeight);
        paddleSprite.draw(game.batch);
        ballSprite.draw(game.batch);
        game.font.getData().setScale(0.05f);
        game.font.draw(game.batch, "Tiemepo: " + String.format("%.1f", gameTime), 0.5f, worldHeight - 0.5f);
        game.font.draw(game.batch, "Mejor: " + String.format("%.1f", bestTime), worldWidth - 3f, worldHeight - 0.5f);
        game.font.getData().setScale(1.0f);
        if (gameOver) {
            game.font.getData().setScale(0.08f);
            game.font.draw(game.batch, "¡FIN DEL JUEGO", worldWidth / 2 - 3f, worldHeight / 2 + 0.5f);
            game.font.draw(game.batch, "Toca para reiniciar", worldWidth / 2 - 3f, worldHeight / 2 - 0.5f);
            game.font.getData().setScale(1.0f);
        }

        game.batch.end();
    }

    private void playBounceSound() {
        UnitOscillator osc = game.getBallOscillator();
        float speed = ballVelocity.len();
        float targetFrequency = BASE_FREQUENCY + speed * 10;
        targetFrequency = MathUtils.clamp(targetFrequency, BASE_FREQUENCY, MAX_FREQUENCY * 1.5f);
        osc.frequency.set(targetFrequency);
        osc.amplitude.set(0.0);
        osc.amplitude.set(0.5);
        osc.amplitude.set(0.0, game.getSynth().createTimeStamp().makeRelative(0.1));
    }

    private void updateBallSound() {
        UnitOscillator osc = game.getBallOscillator();
        if (osc.amplitude.get() < 0.01) {
           float worldWidth = viewport.getWorldWidth();
           float worldHeight = viewport.getWorldHeight();
           float normalizedX = MathUtils.clamp(ballSprite.getX() / worldWidth, 0f, 1f);
           float normalizedY = MathUtils.clamp(ballSprite.getY() / worldHeight, 0f, 1f);
           osc.frequency.set(BASE_FREQUENCY + normalizedY * (MAX_FREQUENCY - BASE_FREQUENCY));
           osc.amplitude.set(0.05f + normalizedX * 0.1f);
        }
    }

    private void restart() {
        gameOver = false;
        gameTime = 0f;
        ballSprite.setCenter(viewport.getWorldWidth() / 2, viewport.getWorldHeight() / 2);

        float angle = MathUtils.random(MathUtils.PI * 0.2f, MathUtils.PI * 0.8f);
        if (MathUtils.randomBoolean()) angle += MathUtils.PI;

        ballVelocity.set(
                MathUtils.cos(angle) * INITIAL_BALL_SPEED,
                MathUtils.sin(angle) * INITIAL_BALL_SPEED
        );

        if (!game.getLineOut().isEnabled()) {
             try {
                game.getLineOut().start();
                Gdx.app.log("JSYN_MAIN", "LineOut iniciado en restart().");
             } catch (Exception e) {
                 Gdx.app.error("JSYN_MAIN", "Error iniciando LineOut en restart()", e);
             }
        }
        game.getBallOscillator().amplitude.set(0.05);
        //updateBallSound();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void pause() {
        if (!gameOver) {
             try {
                game.getLineOut().stop();
                Gdx.app.log("JSYN_MAIN", "LineOut detenido en pause().");
             } catch (Exception e) {
                Gdx.app.error("JSYN_MAIN", "Error deteniendo LineOut en pause()", e);
             }
        }
    }

    @Override
    public void resume() {

        if (!gameOver) {
             try {
                game.getLineOut().start();
                Gdx.app.log("JSYN_MAIN", "LineOut iniciado en resume().");
             } catch (Exception e) {
                 Gdx.app.error("JSYN_MAIN", "Error iniciando LineOut en resume()", e);
             }
        }
    }

    @Override
    public void hide() {
        try {
            game.getLineOut().stop();
            Gdx.app.log("JSYN_MAIN", "LineOut detenido en hide().");
         } catch (Exception e) {
             Gdx.app.error("JSYN_MAIN", "Error deteniendo LineOut en hide()", e);
         }
         disposeTextures();
    }

    @Override
    public void dispose() {
        disposeTextures();
        Gdx.app.log("MAIN_SCREEN", "Dispose llamado.");
    }

    private void disposeTextures() {
        if (backgroundTexture != null) {
            backgroundTexture.dispose();
            backgroundTexture = null;
        }
        if (ballTexture != null) {
            ballTexture.dispose();
            ballTexture = null;
        }
        if (paddleTexture != null) {
            paddleTexture.dispose();
            paddleTexture = null;
        }
    }
}
