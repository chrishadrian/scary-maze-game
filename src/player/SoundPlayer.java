package player;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.advanced.AdvancedPlayer;

public class SoundPlayer {

	private static AdvancedPlayer player;
	private static AdvancedPlayer player2;

	public static void init() {
		try {
			player = new AdvancedPlayer(new FileInputStream("scream.mp3"));
			player2 = new AdvancedPlayer(new FileInputStream("horror.mp3"));

		} catch (FileNotFoundException | JavaLayerException e) {
			e.printStackTrace();
		}
	}

	public static void playScream() {
		new Thread(() -> {
			try {
				player.play();
				init();
			} catch (JavaLayerException e) {
				//e.printStackTrace();
			}
		}).start();
	}

	public static void playBgSong() {
		new Thread(() -> {
			try {
				player2.play();
				init();
			} catch (JavaLayerException e) {
				// e.printStackTrace();
			}
		}).start();
	}
}