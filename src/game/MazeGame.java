package game;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.swing.JFrame;

import acm.graphics.GImage;
import acm.graphics.GLabel;
import acm.graphics.GRect;
import acm.program.GraphicsProgram;
import component.Finish;
import component.Player;
import component.Wall;
import maze.LevelManager;
import maze.MazeReader;
import player.SoundPlayer;

@SuppressWarnings("serial")
public class MazeGame extends GraphicsProgram {

	public static final int WIDTH = 1440;
	public static final int HEIGHT = 900;

	private Player player;
	private Set<Wall> walls;
	private Finish finish;
	private GRect background;

	private LevelManager levelManager;
	private final LevelManager defaultLevelManager;

	public MazeGame(LevelManager defaultLevelManager) {
		this.defaultLevelManager = defaultLevelManager;
	}

	@Override
	public void run() {
		setTitle("???");
		setSize(WIDTH + 17, HEIGHT + 63);
		setBackground(Color.CYAN);
		background = new GRect(0, 0);
		add(background);
		SoundPlayer.init();
		SoundPlayer.playBgSong();

		showMenu();
	}

	private void showMenu() {
		GRect blackBackground = new GRect(0, 0, getWidth(), getHeight());
		blackBackground.setFilled(true);
		add(blackBackground);

		Font font = new Font("Verdana", 0, 20);

		GImage titleImage = new GImage("title.png", 0, 0);
		titleImage.setSize(700, 80);
		titleImage.setLocation((getWidth() - titleImage.getWidth()) / 2, getHeight() * 0.4);
		add(titleImage);

		GLabel defaultGame = new GLabel("start");
		defaultGame.setColor(Color.WHITE);
		defaultGame.setFont(font);
		defaultGame.setLocation((getWidth() - defaultGame.getWidth()) / 2,
				getHeight() * 0.6);

		defaultGame.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent event) {
				remove(blackBackground);
				remove(titleImage);
				remove(defaultGame);

				levelManager = defaultLevelManager;
				showMaze(levelManager.restart());
			}
		});
		add(defaultGame);
	}

	private void showMaze(MazeReader reader) {
		if (player != null) {
			remove(player);
		}
		if (walls != null) {
			walls.forEach(this::remove);
		}
		if (finish != null) {
			remove(finish);
		}
		player = reader.getPlayer();
		walls = reader.getWalls();
		finish = reader.getFinish();
		setSize(reader.getSize().width + 17, reader.getSize().height + 63);
		background.setSize(getWidth(), getHeight());

		walls.forEach(this::add);
		add(finish);
		add(player);

		GLabel level = new GLabel("level #" + (levelManager.getIndex() + 1));
		level.setColor(Color.WHITE);
		level.setFont(new Font("Verdana", Font.BOLD, 40));
		level.setLocation((getWidth() - level.getWidth()) / 2, (getHeight() - level.getDescent()) / 2);

		GRect levelBackground = new GRect(level.getBounds().getX() - 2, level.getBounds().getY() - 2,
				level.getBounds().getWidth() + 4, level.getBounds().getHeight() + 4);
		levelBackground.setFilled(true);
		add(levelBackground);
		add(level);

		new Thread(() -> {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			remove(levelBackground);
			remove(level);
		}).start();

		player.addMouseListener(playerMouseListener);
	}

	private void lose() {
		remove(player);
		background.removeMouseMotionListener(motionListener);

		if (levelManager.isCurrentLevelLast()) {
			System.out.println("lose");

			GImage scaryImage = new GImage("scarymaze.jpg", 0, 0);
			scaryImage.setSize(getWidth(), getHeight());
			add(scaryImage);
			SoundPlayer.playScream();
			scaryImage.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent event) {
					remove(scaryImage);
					showMenu();
				}
			});
		} else {
			showMenu();
		}
	}

	private MouseListener playerMouseListener = new MouseAdapter() {
		public void mouseEntered(MouseEvent event) {
			System.out.println("playerMouseListener -> mouseEntered -> " + event.getSource().hashCode());
			player.removeMouseListener(this);
			background.addMouseMotionListener(motionListener);
			player.addMouseMotionListener(motionListener);
		}
	};

	private MouseMotionListener motionListener = new MouseMotionListener() {
		public void mouseMoved(MouseEvent event) {
			player.setLocation(event.getX() - player.getWidth() / 2, event.getY() - player.getHeight() / 2);
			if (player.getBounds().intersects(finish.getBounds())) {
				win();
				return;
			}

			for (Wall wall : walls) {
				if (wall.getBounds().intersects(player.getBounds())) {
					lose();
					return;
				}
			}
		}

		public void mouseDragged(MouseEvent event) {
			mouseMoved(event);
		}
	};

	private void win() {
		remove(player);
		background.removeMouseMotionListener(motionListener);
		if (levelManager.isCurrentLevelLast()) {
			GImage scaryImage = new GImage("scarymaze.jpg", 0, 0);
			scaryImage.setSize(getWidth(), getHeight());
			add(scaryImage);
			SoundPlayer.playScream();
			scaryImage.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent event) {
					remove(scaryImage);
					showMenu();
				}
			});
		} else {
			showMaze(levelManager.nextLevel());
		}
	}

	public static void main(String[] args) {

		List<String> fileNames = Arrays.asList("maze1.maze", "maze2.maze", "maze3.maze");

		try {
			LevelManager manager = new LevelManager(fileNames);
			new MazeGame(manager).start();
		} catch (FileNotFoundException e) {
			new JFrame().setVisible(true);
		}
	}
}
