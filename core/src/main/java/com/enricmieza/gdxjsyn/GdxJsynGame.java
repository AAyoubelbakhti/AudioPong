package com.enricmieza.gdxjsyn;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.jsyn.devices.AudioDeviceManager;

public class GdxJsynGame extends Game {
    public SpriteBatch batch;
    public BitmapFont font;
    private AudioDeviceManager audioDevice;
    
    public GdxJsynGame(AudioDeviceManager device) {
        this.audioDevice = device;
    }
    
    @Override
    public void create() {
        batch = new SpriteBatch();
        font = new BitmapFont();
        this.setScreen(new WelcomeScreen(this));
    }
    
    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
        if (screen != null) {
            screen.dispose();
        }
    }
    
    public AudioDeviceManager getAudioDevice() {
        return audioDevice;
    }
}