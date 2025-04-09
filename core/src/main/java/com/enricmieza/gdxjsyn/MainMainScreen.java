package com.enricmieza.gdxjsyn;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.ScreenUtils;
import com.jsyn.Synthesizer;
import com.jsyn.unitgen.LineOut;
import com.jsyn.unitgen.UnitOscillator;

public class MainMainScreen implements Screen {

    private final GdxJsynGame game;
    private UnitOscillator oscillator;
    private LineOut lineOut;
    private Synthesizer synth;

    private final float MIN_FREQUENCY = 100.0f;
    private final float MAX_FREQUENCY = 1200.0f;
    private final float MIN_AMPLITUDE = 0.0f;
    private final float MAX_AMPLITUDE = 0.5f;

    private float currentFrequency;
    private float currentAmplitude;

    private final float FREQUENCY_CHANGE_RATE = 300.0f;
    private final float AMPLITUDE_CHANGE_RATE = 0.5f;

    public MainMainScreen(GdxJsynGame game) {
        this.game = game;
        this.synth = game.getSynth();
        this.lineOut = game.getLineOut();
        this.oscillator = game.getBallOscillator();

        if (this.oscillator == null || this.lineOut == null || this.synth == null) {
            Gdx.app.error("JSYN_MAINMAIN", "¡Error crítico! No se pudieron obtener los componentes JSyn desde GdxJsynGame.");
        } else {
            Gdx.app.log("JSYN_MAINMAIN", "Componentes JSyn obtenidos correctamente.");
        }

        currentFrequency = (MIN_FREQUENCY + MAX_FREQUENCY) / 2.0f;
        currentAmplitude = MIN_AMPLITUDE;
    }

    @Override
    public void show() {
        Gdx.app.log("JSYN_MAINMAIN", "Mostrando MainMainScreen.");
        try {
            if (!lineOut.isEnabled()) {
                Gdx.app.log("JSYN_MAINMAIN", "LineOut estaba inactivo. Iniciando en show().");
                lineOut.start();
            }
            if (oscillator != null) {
                oscillator.frequency.set(currentFrequency);
                oscillator.amplitude.set(currentAmplitude);
            }
        } catch (Exception e) {
            Gdx.app.error("JSYN_MAINMAIN", "Error al intentar iniciar LineOut o setear oscilador en show()", e);
        }
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.1f, 0.2f, 0.3f, 1f);

        handleKeyInput(delta);

        if (oscillator != null) {
            oscillator.frequency.set(currentFrequency);
            oscillator.amplitude.set(currentAmplitude);
        }

        game.viewport.apply();
        game.batch.setProjectionMatrix(game.camera.combined);

        game.batch.begin();
        game.font.getData().setScale(1.0f);
        game.font.draw(game.batch, "MainMainScreen - Usa las flechas", 50, game.viewport.getWorldHeight() - 50);
        game.font.draw(game.batch, String.format("Frec: %.1f Hz (Izq/Der)", currentFrequency), 50, game.viewport.getWorldHeight() - 100);
        game.font.draw(game.batch, String.format("Amp: %.2f (Arr/Abj)", currentAmplitude), 50, game.viewport.getWorldHeight() - 150);
        game.batch.end();
    }

    private void handleKeyInput(float delta) {
        if (oscillator == null) return;

        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            currentFrequency -= FREQUENCY_CHANGE_RATE * delta;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            currentFrequency += FREQUENCY_CHANGE_RATE * delta;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            currentAmplitude -= AMPLITUDE_CHANGE_RATE * delta;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            currentAmplitude += AMPLITUDE_CHANGE_RATE * delta;
        }

        currentFrequency = MathUtils.clamp(currentFrequency, MIN_FREQUENCY, MAX_FREQUENCY);
        currentAmplitude = MathUtils.clamp(currentAmplitude, MIN_AMPLITUDE, MAX_AMPLITUDE);
    }

    @Override
    public void resize(int width, int height) {
        game.viewport.update(width, height, true);
    }

    @Override
    public void pause() {
        Gdx.app.log("JSYN_MAINMAIN", "Pausando MainMainScreen.");
        if (oscillator != null) {
            oscillator.amplitude.set(0.0);
        }
    }

    @Override
    public void resume() {
        Gdx.app.log("JSYN_MAINMAIN", "Reanudando MainMainScreen.");
        try {
            if (lineOut != null && !lineOut.isEnabled()) {
                Gdx.app.log("JSYN_MAINMAIN", "LineOut estaba inactivo. Iniciando en resume().");
                lineOut.start();
            }
            if (oscillator != null) {
                 oscillator.amplitude.set(currentAmplitude);
                 oscillator.frequency.set(currentFrequency);
            }
        } catch (Exception e) {
            Gdx.app.error("JSYN_MAINMAIN", "Error al intentar iniciar LineOut o setear oscilador en resume()", e);
        }
    }

    @Override
    public void hide() {
        Gdx.app.log("JSYN_MAINMAIN", "Ocultando MainMainScreen.");
        if (oscillator != null) {
            oscillator.amplitude.set(0.0);
        }
    }

    @Override
    public void dispose() {
        Gdx.app.log("JSYN_MAINMAIN", "Disposing MainMainScreen.");
        if (oscillator != null) {
            oscillator.amplitude.set(0.0);
        }
    }
}