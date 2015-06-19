package game;
import java.awt.*;
import javax.swing.JFrame;

public class SnakeGame {
	JFrame frame; //Window of the game
	public final String TITLE = "Snake by Stonehenge";
	
	public SnakeGame() {
		SnakeApplet myApplet = new SnakeApplet();
		frame = new JFrame();
		myApplet.init();
		frame.add(myApplet, BorderLayout.CENTER);
		frame.pack();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		frame.setResizable(false);
		frame.setLocationRelativeTo(null);
		frame.setTitle("Snake");
	}
	
	public static void main(String[] args) {
		SnakeGame game = new SnakeGame();
	}
}