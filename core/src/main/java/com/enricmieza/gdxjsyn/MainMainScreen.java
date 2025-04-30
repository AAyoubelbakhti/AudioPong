package com.enricmieza.gdxjsyn;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.ScreenUtils;
import com.jsyn.Synthesizer;
import com.jsyn.unitgen.*;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class MainMainScreen implements Screen {
    private final GdxJsynGame game;
    private final Synthesizer synth;
    private final LineOut lineOut;
    private ShapeRenderer shapeRenderer;
    private OrthographicCamera camera;
    private Viewport viewport;
    private static final float WORLD_WIDTH = 0.5f;
    private static final float WORLD_HEIGHT = 1.0f;
    private static final float PLAYER_WIDTH = 0.20f;
    private static final float PLAYER_Y_POSITION = 0.25f;
    private static final float BALL_RADIUS = 0.02f;
    private static final float INITIAL_BALL_SPEED_Y = 0.4f;
    private static final float INITIAL_BALL_SPEED_X = 0.25f;
    private static final float BALL_SPEED_INCREASE = 1.05f;
    private static final float PLAYER_MOVE_SPEED = 0.7f;

    private float playerPositionX;
    private float ballPositionX;
    private float ballPositionY;
    private float ballVelocityX;
    private float ballVelocityY;
    private boolean isGameOver;

    private UnitOscillator playerOscillator;
    private UnitOscillator ballOscillator;
    private Pan playerPanner;
    private Pan ballPanner;

    private static final float PLAYER_FREQUENCY = 110.0f;
    private static final float PLAYER_AMPLITUDE = 0.25f;
    private static final float BALL_BASE_FREQUENCY = 330.0f;
    private static final float BALL_MAX_FREQUENCY = 990.0f;
    private static final float BALL_BASE_AMPLITUDE = 0.35f;
    private static final float BOUNCE_AMP_BOOST = 1.6f;
    private static final float BOUNCE_DURATION = 0.08f;

    public MainMainScreen(GdxJsynGame game) {
        this.game = game;
        this.synth = game.getSynth();
        this.lineOut = game.getLineOut();

        playerOscillator = new SquareOscillator();
        playerOscillator.frequency.set(PLAYER_FREQUENCY);
        playerOscillator.amplitude.set(PLAYER_AMPLITUDE);

        ballOscillator = new SineOscillator();
        ballOscillator.frequency.set(BALL_BASE_FREQUENCY);
        ballOscillator.amplitude.set(BALL_BASE_AMPLITUDE);

        playerPanner = new Pan();
        ballPanner = new Pan();

        synth.add(playerOscillator);
        synth.add(ballOscillator);
        synth.add(playerPanner);
        synth.add(ballPanner);

        playerOscillator.output.connect(playerPanner.input);
        playerPanner.output.connect(0, lineOut.input, 0);
        playerPanner.output.connect(1, lineOut.input, 1);

        ballOscillator.output.connect(ballPanner.input);
        ballPanner.output.connect(0, lineOut.input, 0);
        ballPanner.output.connect(1, lineOut.input, 1);
        camera = new OrthographicCamera();
        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        shapeRenderer = new ShapeRenderer();
        camera.position.set(WORLD_WIDTH / 2f, WORLD_HEIGHT / 2f, 0);
        camera.update();
        resetGame();
    }

    private void resetGame() {
        playerPositionX = WORLD_WIDTH / 2f;
        ballPositionX = WORLD_WIDTH / 2f;
        ballPositionY = WORLD_HEIGHT * 0.75f;
        ballVelocityY = -INITIAL_BALL_SPEED_Y;
        ballVelocityX = MathUtils.random(-1f, 1f) * INITIAL_BALL_SPEED_X;
        if (Math.abs(ballVelocityX) < 0.1f) {
            ballVelocityX = (ballVelocityX > 0 ? 1f : -1f) * 0.1f;
        }

        isGameOver = false;

        Gdx.app.log("GAME", "Juego Reiniciado");
        if (playerOscillator != null)
            playerOscillator.amplitude.set(PLAYER_AMPLITUDE);
        if (ballOscillator != null)
            ballOscillator.amplitude.set(BALL_BASE_AMPLITUDE);
    }

    @Override
    public void show() {
        Gdx.app.log("AUDIO_FRONTON", "Mostrando MainMainScreen (Audio Frontón).");
        try {
            if (lineOut != null && !lineOut.isEnabled()) {
                Gdx.app.log("JSYN", "LineOut inactivo, iniciando en show().");
                lineOut.start();
            }

            if (isGameOver) {
                if (playerOscillator != null)
                    playerOscillator.amplitude.set(0.0);
                if (ballOscillator != null)
                    ballOscillator.amplitude.set(0.0);
            } else {
                if (playerOscillator != null)
                    playerOscillator.amplitude.set(PLAYER_AMPLITUDE);
                if (ballOscillator != null)
                    ballOscillator.amplitude.set(BALL_BASE_AMPLITUDE);
            }

        } catch (Exception e) {
            Gdx.app.error("AUDIO_FRONTON", "Error en show() inicializando audio", e);
        }
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.1f, 0.1f, 0.15f, 1f);
        handleKeyInput(delta);
        updateGame(delta);
        updateAudio();
        camera.update();
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.BLUE);
        shapeRenderer.circle(ballPositionX, ballPositionY, BALL_RADIUS, 30);
        shapeRenderer.end();

        drawDebugInfo();
    }

    private void handleKeyInput(float delta) {
        if (isGameOver) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
                resetGame();
            }
            return;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            playerPositionX += PLAYER_MOVE_SPEED * delta;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            playerPositionX -= PLAYER_MOVE_SPEED * delta;
        }

        playerPositionX = MathUtils.clamp(playerPositionX, PLAYER_WIDTH / 2f, WORLD_WIDTH - PLAYER_WIDTH / 2f);
    }

    private void updateGame(float delta) {
        if (isGameOver) {
            return;
        }

        ballPositionX += ballVelocityX * delta;
        ballPositionY += ballVelocityY * delta;

        boolean bounced = false;

        if (ballPositionX - BALL_RADIUS < 0 && ballVelocityX < 0) {
            ballPositionX = BALL_RADIUS;
            ballVelocityX *= -1;
            playWallBounceSound(ballPositionX);
            bounced = true;
        } else if (ballPositionX + BALL_RADIUS > WORLD_WIDTH && ballVelocityX > 0) {
            ballPositionX = WORLD_WIDTH - BALL_RADIUS;
            ballVelocityX *= -1;
            playWallBounceSound(ballPositionX);
            bounced = true;
        }

        if (ballPositionY + BALL_RADIUS > WORLD_HEIGHT && ballVelocityY > 0) {
            ballPositionY = WORLD_HEIGHT - BALL_RADIUS;
            ballVelocityY *= -1;
            if (!bounced)
                playWallBounceSound(ballPositionX);
            bounced = true;
        }

        if (ballVelocityY < 0 && ballPositionY - BALL_RADIUS <= PLAYER_Y_POSITION) {
            float playerLeftEdge = playerPositionX - PLAYER_WIDTH / 2f;
            float playerRightEdge = playerPositionX + PLAYER_WIDTH / 2f;

            if (ballPositionX >= playerLeftEdge && ballPositionX <= playerRightEdge) {
                ballPositionY = PLAYER_Y_POSITION + BALL_RADIUS;
                ballVelocityY *= -1;

                float hitOffset = (ballPositionX - playerPositionX) / (PLAYER_WIDTH / 2f);
                ballVelocityX += hitOffset * Math.abs(ballVelocityY) * 0.5f;
                ballVelocityX = MathUtils.clamp(ballVelocityX, -INITIAL_BALL_SPEED_Y * 2, INITIAL_BALL_SPEED_Y * 2);

                ballVelocityX *= BALL_SPEED_INCREASE;
                ballVelocityY *= BALL_SPEED_INCREASE;

                playPlayerHitSound(ballPositionX);

            } else {
                isGameOver = true;
                playGameOverSound();
                if (playerOscillator != null)
                    playerOscillator.amplitude.set(0.0);
                if (ballOscillator != null)
                    ballOscillator.amplitude.set(0.0);
            }
        }
    }

    private void updateAudio() {
        if (isGameOver) {
            return;
        }

        float playerPanValue = (playerPositionX / WORLD_WIDTH) * 2.0f - 1.0f;
        playerPanner.pan.set(MathUtils.clamp(playerPanValue, -1f, 1f));

        float ballPanValue = (ballPositionX / WORLD_WIDTH) * 2.0f - 1.0f;
        ballPanner.pan.set(MathUtils.clamp(ballPanValue, -1f, 1f));

        float normalizedY = MathUtils.clamp(ballPositionY / WORLD_HEIGHT, 0f, 1f);
        float targetFrequency = BALL_BASE_FREQUENCY + normalizedY * (BALL_MAX_FREQUENCY - BALL_BASE_FREQUENCY);
        ballOscillator.frequency.set(targetFrequency);
    }

    private void drawDebugInfo() {

    }

    private void playWallBounceSound(float positionX) {

    }

    private void playPlayerHitSound(float positionX) {

    }

    private void playGameOverSound() {

    }

    @Override
    public void resize(int width, int height) {
viewport.update(width, height, true);
}

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
    }
}