package com.enricmieza.gdxjsyn;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;

import com.jsyn.JSyn;
import com.jsyn.Synthesizer;
import com.jsyn.devices.AudioDeviceManager;
import com.jsyn.unitgen.*;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class MainScreen implements Screen {
    private final GdxJsynGame game;
    private SpriteBatch batch;
    public BitmapFont font;
    private Texture image;
    private Synthesizer synth;
    private UnitOscillator osc1;
    private LineOut lineOut;

    public MainScreen(GdxJsynGame game, AudioDeviceManager device) {
        this.game = game;
        if( device==null ) {
            // Default device: funciona bé en Desktop
            synth = JSyn.createSynthesizer();
        } else {
            // El Launcher de cada plataforma ens passa el seu Device
            // De moment funciona en Android
            synth = JSyn.createSynthesizer(device);
        }
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        font = new BitmapFont();
        image = new Texture("libgdx.png");

        // Iniciem sintetitzador
        synth.start();

        // Afegir un oscil·lador
        osc1 = new SineOscillator(); // Pots provar tb amb TriangleOscillator() o SawtoothOscillator()
        osc1.frequency.set(440); // Freqüència de 440 Hz (La4)
        osc1.amplitude.set(0.5); // Volum

        // Connexió a la sortida d'àudio
        lineOut = new LineOut();
        synth.add(osc1);
        synth.add(lineOut);
        osc1.output.connect(0, lineOut.input, 0);
        osc1.output.connect(0, lineOut.input, 1);
    }

    @Override
    public void render(float delta) {
        // ...activitat GUI...
        ScreenUtils.clear(0.25f, 0.15f, 0.35f, 1f);
        batch.begin();
        batch.draw(image, 140, 210);
        font.draw(batch, "Proves amb libGDX i JSyn", 100, 150f);
        font.draw(batch, "Prova a clicar i arrossegar per la pantalla", 100, 100);
        font.draw(batch, "Experimenta amb la freqüència i l'amplitud de la ona", 100, 50);
        batch.end();

        // al detectar un touch apaguem o engeguem
        if( Gdx.input.isTouched() ) {
            //synth.sound = true;
            lineOut.start();
            float x = Gdx.input.getX();
            float y = Gdx.input.getY();
            int height = Gdx.graphics.getHeight();
            int width = Gdx.graphics.getWidth();

            // l'amplitud la fem en funció (inversa) de la distància X
            // modificació logarítmica (equivalent als dB)
            final float a = 999;
            float linx = x/width;
            float logx = (float) ( Math.log(1+a*linx)/Math.log(1+a) );
            osc1.amplitude.set(1-logx);
            //synth.amplitude = 1-linx; //lineal per test
            // la freqüència base és a l'extrem inferior (440 Hz)
            // augmenta al doble quan arriba al limit superior
            osc1.frequency.set( 440 * (1 + ((float)height - y) / (float)height ) );
        } else {
            //synth.sound = false;
            lineOut.stop();
        }
    }

    @Override
    public void dispose() {
        synth.stop();
        batch.dispose();
        image.dispose();
    }
}
