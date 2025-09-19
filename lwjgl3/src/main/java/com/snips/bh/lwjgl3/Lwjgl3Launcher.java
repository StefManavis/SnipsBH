import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.snips.bh.SnipsBHGame;

public class Lwjgl3Launcher {
    public static void main (String[] args) {
        Lwjgl3ApplicationConfiguration cfg = new Lwjgl3ApplicationConfiguration();
        cfg.setTitle("SnipsBH");
        cfg.setWindowedMode(1280, 720);
        cfg.useVsync(true);
        new Lwjgl3Application(new SnipsBHGame(), cfg);
    }
}
