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
import com.jsyn.JSyn;
import com.jsyn.Synthesizer;
import com.jsyn.devices.AudioDeviceManager;
import com.jsyn.unitgen.*;


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

    private Synthesizer synth;
    private UnitOscillator ballOscillator;
    private LineOut lineOut;

    private static final float PADDLE_WIDTH = 3f;
    private static final float PADDLE_HEIGHT = 0.5f;
    private static final float BALL_SIZE = 1f;
    private static final float INITIAL_BALL_SPEED = 5f;
    private static final float BALL_SPEED_INCREASE_FACTOR = 8f;
    private static final float BOUNCE_SOUND_DELAY = 0.1f;
    private static final float BASE_FREQUENCY = 220f;
    private static final float MAX_FREQUENCY = 880f;

    public MainScreen(GdxJsynGame game, AudioDeviceManager device) {
        this.game = game;
        this.viewport = game.viewport;
        if (device == null) {
            synth = JSyn.createSynthesizer();
        } else {
            synth = JSyn.createSynthesizer(device);
        }

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

        synth.start();
        ballOscillator = new SineOscillator();
        lineOut = new LineOut();

        synth.add(ballOscillator);
        synth.add(lineOut);
        ballOscillator.output.connect(0, lineOut.input, 0);
        ballOscillator.output.connect(0, lineOut.input, 1);



        ballOscillator.frequency.set(440);
        ballOscillator.amplitude.set(0);
        lineOut.start();

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
            playBounceSound();
        }
        if (ballSprite.getY() + ballSprite.getHeight() >= worldHeight) {
            ballVelocity.y = -ballVelocity.y;
            playBounceSound();
        }
        if (ballRect.overlaps(paddleRect) && ballVelocity.y < 0) {
            ballVelocity.y = -ballVelocity.y;
            float hitPosition = (ballSprite.getX() + ballSprite.getWidth() / 2) - paddleSprite.getX();
            float normalizedHitPosition = (hitPosition / PADDLE_WIDTH) * 2 - 1;
            ballVelocity.x = normalizedHitPosition * BALL_SPEED_INCREASE_FACTOR;
            playBounceSound();
        }


        if (ballSprite.getY() < 0) {
            gameOver = true;
            if (gameTime > bestTime) {
                bestTime = gameTime;
            }
        }

        updateBallSound();
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
        game.font.draw(game.batch, "Tiempo: " + String.format("%.1f", gameTime), 0.5f, worldHeight - 0.5f);
        game.font.draw(game.batch, "Mejor: " + String.format("%.1f", bestTime), worldWidth - 3f, worldHeight - 0.5f);

        if (gameOver) {
            game.font.draw(game.batch, "¡FIN DEL JUEGO!", worldWidth / 2 - 2f, worldHeight / 2);
            game.font.draw(game.batch, "Toca para reiniciar", worldWidth / 2 - 2f, worldHeight / 2 - 1f);
        }

        game.batch.end();
    }

    private void playBounceSound() {
        ballOscillator.amplitude.set(0.5);
        float speed = ballVelocity.len();
        ballOscillator.frequency.set(440 + speed * 40);
    );

    private void updateBallSound() {
        float worldWidth = viewport.getWorldWidth();
        float worldHeight = viewport.getWorldHeight();
        float normalizedX = ballSprite.getX() / worldWidth;
        float normalizedY = ballSprite.getY() / worldHeight;
        ballOscillator.frequency.set(BASE_FREQUENCY + normalizedY * (MAX_FREQUENCY - BASE_FREQUENCY));
        ballOscillator.amplitude.set(0.1f + normalizedX * 0.3f);
    }

    private void restart() {
        gameOver = false;
        gameTime = 0f;
        ballSprite.setCenter(viewport.getWorldWidth() / 2, viewport.getWorldHeight() / 2);
        float angle = MathUtils.random(MathUtils.PI / 4, 3 * MathUtils.PI / 4);
        ballVelocity.set(MathUtils.cos(angle) * INITIAL_BALL_SPEED, MathUtils.sin(angle) * INITIAL_BALL_SPEED);
        lineOut.start();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void pause() {
        if (!gameOver) {
            lineOut.stop();
        }
    }

    @Override
    public void resume() {
        if (!gameOver){
           lineOut.start();
        }
    }

    @Override
    public void hide() {
         lineOut.stop();
    }

    @Override
    public void dispose() {
        backgroundTexture.dispose();
        ballTexture.dispose();
        paddleTexture.dispose();
        ballSprite.getTexture().dispose();
        paddleSprite.getTexture().dispose();
        lineOut.stop();
        synth.stop();
    }
}
