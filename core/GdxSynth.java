package com.enricmieza.gdxsound;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.AudioDevice;

public class GdxSynth extends Thread {
    public volatile boolean running = true;
    public volatile boolean sound = false;
    public float sampleRate = 48000.0f;
    public float amplitude = 1f;
    public float freq = 440.0f;
    public float step = 0.03f;
    private float phase = 0f;
    AudioDevice audioDevice;
    public float[] generaSinusoide(float frequency, float sampleRate, float durationInSeconds) {
        int numSamples = (int) (sampleRate * durationInSeconds);
        float[] samples = new float[numSamples];
        for (int i = 0; i < numSamples; i++) {
            float t = i / sampleRate;
            samples[i] = amplitude * (float) Math.sin(phase + 2*Math.PI*frequency*t);
        }
        // keep phase to avoid glitches
        phase += 2f * (float)Math.PI * frequency * numSamples/sampleRate;
        // TODO: keep small number (rest of 2^PI)
        return samples;
    }

    public float[] generaSilenci(float sampleRate, float durationInSeconds) {
        int numSamples = (int) (sampleRate * durationInSeconds);
        float[] samples = new float[numSamples];
        for (int i = 0; i < numSamples; i++) {
            samples[i] = 0f;
        }
        return samples;
    }

    @Override
    public void run() {
        // Crear un dispositiu d'àudio
        audioDevice = Gdx.audio.newAudioDevice((int) sampleRate, true);

        while(running) {
            if( sound ) {
                // creem sinusoide
                float[] sineWave = generaSinusoide(freq, sampleRate, step);
                audioDevice.writeSamples(sineWave, 0, sineWave.length);
            }
            else {
                // generem silenci (zeros)
                phase = 0f;
                float[] silenci = generaSilenci(sampleRate, step);
                audioDevice.writeSamples(silenci, 0, silenci.length);
            }
        }
    }

}
