import javax.sound.sampled.*;
import java.util.Random;

public class SoundManager {

    private static float volume = 0.8f; // 0.0 to 1.0
    private static boolean muted = false;

    public static void setVolume(float vol) {
        volume = Math.max(0.0f, Math.min(1.0f, vol));
    }

    public static float getVolume() {
        return volume;
    }

    public static void setMuted(boolean isMuted) {
        muted = isMuted;
    }

    public static boolean isMuted() {
        return muted;
    }

    // Generates a simple sine wave tone
    public static void playTone(int frequency, int durationMs) {
        if (muted || volume <= 0.01f) return;

        new Thread(() -> {
            try {
                float sampleRate = 44100;
                byte[] buf = new byte[1];
                AudioFormat af = new AudioFormat(sampleRate, 8, 1, true, false);
                SourceDataLine sdl = AudioSystem.getSourceDataLine(af);
                sdl.open(af);
                sdl.start();

                // Adjust volume (amplitude)
                double vol = volume * 127.0; 

                for (int i = 0; i < durationMs * (sampleRate / 1000); i++) {
                    double angle = i / (sampleRate / frequency) * 2.0 * Math.PI;
                    buf[0] = (byte) (Math.sin(angle) * vol);
                    sdl.write(buf, 0, 1);
                }
                sdl.drain();
                sdl.stop();
                sdl.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    // Generates white noise for static/glitch effects
    public static void playStatic(int durationMs) {
        if (muted || volume <= 0.01f) return;

        new Thread(() -> {
            try {
                float sampleRate = 44100;
                byte[] buf = new byte[1];
                AudioFormat af = new AudioFormat(sampleRate, 8, 1, true, false);
                SourceDataLine sdl = AudioSystem.getSourceDataLine(af);
                sdl.open(af);
                sdl.start();

                Random rand = new Random();
                double vol = volume * 0.5; // Static can be loud, reduce a bit

                for (int i = 0; i < durationMs * (sampleRate / 1000); i++) {
                    buf[0] = (byte) ((rand.nextBoolean() ? 1 : -1) * (rand.nextInt(127) * vol));
                    sdl.write(buf, 0, 1);
                }
                sdl.drain();
                sdl.stop();
                sdl.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public static void playKeyClick() {
        // High pitch short blip
        playTone(1200, 10);
    }

    public static void playStartup() {
        // Rising tones
        new Thread(() -> {
            playTone(400, 100);
            try { Thread.sleep(100); } catch (InterruptedException e) {}
            playTone(600, 100);
            try { Thread.sleep(100); } catch (InterruptedException e) {}
            playTone(1000, 200);
        }).start();
    }

    public static void playAlarm() {
        // Alternating tones
        new Thread(() -> {
            for(int i=0; i<3; i++) {
                playTone(800, 150);
                try { Thread.sleep(150); } catch (InterruptedException e) {}
                playTone(600, 150);
                try { Thread.sleep(150); } catch (InterruptedException e) {}
            }
        }).start();
    }
    
    public static void playScream() {
        if (muted || volume <= 0.01f) return;
        
        new Thread(() -> {
            try {
                float sampleRate = 44100;
                byte[] buf = new byte[1];
                AudioFormat af = new AudioFormat(sampleRate, 8, 1, true, false);
                SourceDataLine sdl = AudioSystem.getSourceDataLine(af);
                sdl.open(af);
                sdl.start();

                Random rand = new Random();
                double vol = volume * 1.0; // Max volume for scream

                // Mix of static and changing frequencies
                for (int i = 0; i < 2000 * (sampleRate / 1000); i++) {
                    // Modulate frequency randomly
                    double freq = 200 + rand.nextInt(800);
                    double angle = i / (sampleRate / freq) * 2.0 * Math.PI;
                    
                    // Mix sine wave with noise
                    byte signal = (byte) (Math.sin(angle) * vol);
                    byte noise = (byte) ((rand.nextBoolean() ? 1 : -1) * (rand.nextInt(127) * vol));
                    
                    buf[0] = (byte) ((signal + noise) / 2);
                    sdl.write(buf, 0, 1);
                }
                sdl.drain();
                sdl.stop();
                sdl.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}
