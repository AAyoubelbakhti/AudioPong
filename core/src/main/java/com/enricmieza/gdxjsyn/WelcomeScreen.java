package com.enricmieza.gdxjsyn;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.TimeUtils;

public class WelcomeScreen implements Screen {
    private final GdxJsynGame game;
    private Music tutorialAudio;
    private float lastTouchTime = 0f;
    private final float DOUBLE_TAP_THRESHOLD = 0.5f;

    public WelcomeScreen(GdxJsynGame game) {
        this.game = game;
        tutorialAudio = Gdx.audio.newMusic(Gdx.files.internal("Audio.wav"));
        speakTutorial();
    }

    @Override
    public void show() {
        game.viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(),true);

    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.25f, 0.15f, 0.35f, 1f);
        game.batch.begin();
        game.font.draw(game.batch, "Tutorial del Pong", 100, 300);
        game.font.draw(game.batch, "Toca la derecha para empezar, izquierda para repetir", 50, 250);
        game.font.draw(game.batch, "Toca dos veces la izquierda para salir", 50, 200);
        game.batch.end();

        if (Gdx.input.justTouched()) {
            float x = Gdx.input.getX();
            int width = Gdx.graphics.getWidth();
            if (x > width / 2) {
                tutorialAudio.stop();
                game.setScreen(new MainScreen(game, game.getAudioDevice()));
            } else {
                float currentTime = TimeUtils.millis() / 1000.0f;
                if (currentTime - lastTouchTime < DOUBLE_TAP_THRESHOLD) {

                    Gdx.app.exit();
                } else {
                    speakTutorial();
                }
                lastTouchTime = currentTime;
            }
        }
    }

private void speakTutorial() {
    if (tutorialAudio != null) {
        tutorialAudio.stop();
        tutorialAudio.setPosition(0);
        tutorialAudio.play();
    } else {
        Gdx.app.error("AUDIO", "Error: archivo de audio no cargado");
    }
}

    @Override
    public void resize(int width, int height) {
        game.viewport.update(width, height, true);
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {
        if (tutorialAudio != null) {
            tutorialAudio.pause();
        }
    }

    @Override
    public void dispose() {
        if (tutorialAudio != null) {
            tutorialAudio.dispose();
        }
    }
}