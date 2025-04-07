package com.enricmieza.gdxjsyn;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.jsyn.JSyn;
import com.jsyn.Synthesizer;
import com.jsyn.devices.AudioDeviceManager;
import com.jsyn.unitgen.LineOut;
import com.jsyn.unitgen.SineOscillator;
import com.jsyn.unitgen.UnitOscillator;
import com.badlogic.gdx.graphics.OrthographicCamera;

public class GdxJsynGame extends Game {
    public SpriteBatch batch;
    public BitmapFont font;
    private AudioDeviceManager audioDevice;
    public FitViewport viewport;
    public OrthographicCamera camera;
    private Synthesizer synth;
    private UnitOscillator ballOscillator;
    private LineOut lineOut;

    public GdxJsynGame(AudioDeviceManager device) {
        this.audioDevice = device;
    }

    @Override
    public void create() {
        batch = new SpriteBatch();
        font = new BitmapFont();
        camera = new OrthographicCamera();
        viewport = new FitViewport(800, 600, camera);
        if (audioDevice == null) {
            synth = JSyn.createSynthesizer();
        } else {

            synth = JSyn.createSynthesizer(audioDevice);
        }
        ballOscillator = new SineOscillator();
        lineOut = new LineOut();
        synth.add(ballOscillator);
        synth.add(lineOut);
        ballOscillator.output.connect(0, lineOut.input, 0);
        ballOscillator.output.connect(0, lineOut.input, 1);
        ballOscillator.frequency.set(440);
        ballOscillator.amplitude.set(0.0); 
        synth.start();
        // lineOut.start();
        this.setScreen(new WelcomeScreen(this));
    }

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
        if (screen != null) {
            screen.dispose();
        }
        Gdx.app.log("JSYN_DISPOSE", "Deteniendo Synthesizer");
        if (lineOut != null) {
            try {
                lineOut.stop();
            } catch (Exception e) {
                Gdx.app.error("JSYN_DISPOSE", "Error deteniendo LineOut", e);
            }
        }
        if (synth != null) {
            synth.stop();
            Gdx.app.log("JSYN_DISPOSE", "Synthesizer detenido");
        }
    }

    public Synthesizer getSynth() {
        return synth;
    }

    public UnitOscillator getBallOscillator() {
        return ballOscillator;
    }

    public LineOut getLineOut() {
        return lineOut;
    }

    public AudioDeviceManager getAudioDevice() {
        return audioDevice;
    }
}