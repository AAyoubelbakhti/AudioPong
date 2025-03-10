package com.enricmieza.gdxjsyn;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.TimeUtils;

import android.speech.tts.TextToSpeech;
import java.util.Locale;

public class WelcomeScreen implements Screen {
    private final GdxJsynGame game;
    private TextToSpeech tts;
    private float lastTouchTime = 0f;
    private final float DOUBLE_TAP_THRESHOLD = 0.5f;

    public WelcomeScreen(GdxJsynGame game) {
        this.game = game;
        
        tts = new TextToSpeech(Gdx.app.getApplicationContext(), status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(Locale.getDefault());
                speakTutorial();
            } else {
                Gdx.app.error("TTS", "Error al inicializar TextToSpeech");
            }
        });
    }
    
    @Override
    public void show() {
        
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
                game.setScreen(new MainScreen(game));
            } else {
                float currentTime = TimeUtils.nanoTime() / 1000000000.0f;
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
        if (tts != null) {
            tts.speak("Bienvenido al juego de Accesible Pong. Escucharás un tono indicando la posición de pelota. Tu objetivo es mover el dedo de izquierda a derecha en la parte inferior de la pantalla para devolverla y evitar recibir puntos. Cuando estés listo, toca la parte derecha de la pantalla para empezar el juego, la parte izquierda para repetir este tutorial, o la parte izquierda  2 veces para salir.", TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }
    
    @Override
    public void resize(int width, int height) {

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
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
    }
}