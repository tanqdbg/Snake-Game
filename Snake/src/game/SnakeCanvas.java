package game;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;
import java.util.Random;

import javax.swing.JOptionPane;


@SuppressWarnings("serial")
public class SnakeCanvas extends Canvas implements Runnable,KeyListener {
	 
	private final int BOX_HEIGHT = 15;		//This shows the size of the boxes
	private final int BOX_WIDTH = 15;		//This shows the size of the boxes
	private final int GRID_WIDTH =25; 		// This shows how many boxes is in the grid
	private final int GRID_HEIGTH =25;		// This shows how many boxes is in the grid
	private LinkedList<Point> snake;
	private LinkedList<Point> rottenApples;
	private Point fruit;
	private int direction = Direction.NO_DIRECTION;
	private Thread runThread; 				// This method make multitasking of the program
	
	private int score= 0;
	private int level = 150;
	public String highScore="";
	
	private Image menuImage = null;
	
	private boolean isInMenu = true;
	private boolean isAtEndGame = false;
	private boolean won = false;
		
	public void paint(Graphics g){
		if(runThread == null){
			this.setPreferredSize(new Dimension(640,480));
			this.addKeyListener(this);
			runThread = new Thread(this);
			runThread.start();
		}
			
		if(isInMenu ){
			//draw menu
			drawMenu(g);
		}
		else if(isAtEndGame){
			//draw the end game screen
			drawEndGame(g);
		}
		
		if (highScore.equals("")){
			highScore = this.getHighScore();
		} else {
			//draw everything else
			if(snake == null){
				snake = new LinkedList<Point>();
				rottenApples = new LinkedList<Point>();
				generateDefaultSnake();
				placeFruit();
				rottenApples.clear();
			}
			
			drawFruit(g);
			drawGrid(g);
			drawSnake(g);
			drawRottenApples(g);
			drawScore(g);
		}		
	}
	public void drawEndGame(Graphics g){
		BufferedImage endGameImage= new BufferedImage(this.getPreferredSize().width, this.getPreferredSize().height, BufferedImage.TYPE_INT_ARGB);
		Graphics endGameGraphics = endGameImage.getGraphics();
		endGameGraphics.setColor(Color.BLACK);
		if (won) {
			endGameGraphics.drawString("You win!!!", this.getPreferredSize().
					width /2, this.getPreferredSize().height /2);
		}
		else{
			endGameGraphics.drawString("You lost. Your score: "+ this.score, 
					this.getPreferredSize().width /3, this.getPreferredSize().height  - 93);
			endGameGraphics.drawString("Press \" space \" to start a new game!", 
					this.getPreferredSize().width /3, (this.getPreferredSize().height) -80);
			g.drawImage(endGameImage, 0, 0, this);
		}
	}
		
	public void drawMenu(Graphics g){
		if(this.menuImage == null){
			try{
				URL imagePath = SnakeCanvas.class.getResource("StonehengeBeta.png");
				this.menuImage = Toolkit.getDefaultToolkit().getImage(imagePath);
			
			}
			catch(Exception e){
				// image doesn't exist
				e.printStackTrace();
			}
		}
		g.drawImage(menuImage, 0, 0, 640, 480, this);
	}
	
		
	public void update(Graphics g){
		
		// this is the default update which will contain our double buffering
		Graphics offScreenGraphics; // these are the graphics we will use to draw offscreen
		BufferedImage offscreen= null;
		Dimension d= this.getSize();
		
		offscreen= new BufferedImage(d.width,  d.height,  BufferedImage.TYPE_INT_ARGB);
		offScreenGraphics = offscreen.getGraphics();
		offScreenGraphics.setColor(this.getBackground());
		offScreenGraphics.fillRect(0, 0, d.width, d.height);
		offScreenGraphics.setColor(this.getForeground());
		paint(offScreenGraphics);
		
		// flip
		g.drawImage(offscreen, 0, 0,this);
		
	}
		
	public void generateDefaultSnake(){
		score = 0;
		level = 150;
		snake.clear();
		
		snake.add(new Point(0, 2));
		snake.add(new Point(0, 1));
		snake.add(new Point(0, 0));
		direction= Direction.NO_DIRECTION;
	}
		
		
		
	public void move(){
		if (direction == Direction.NO_DIRECTION ){
			return;
		}
		Point head = snake.peekFirst();
		Point newPoint = head;
		switch(direction){
		case Direction.NORTH :
			newPoint = new Point(head.x, head.y - 1);
			break;
		case Direction.SOUTH : 
			newPoint = new Point(head.x, head.y + 1);
			break;
		case Direction.WEST:
			newPoint= new Point(head.x - 1, head.y);
			break;
		case Direction.EAST:
			newPoint= new Point(head.x + 1, head.y);
			break;
		}
		if(this.direction != Direction.NO_DIRECTION){
			snake.remove(snake.peekLast());
		}
		
		if(newPoint.equals(fruit)){
			//the snake has hit fruit
			score += 10;
			level -=10;
			Point addPoint = (Point) newPoint.clone();
			
			switch(direction){
			case Direction.NORTH :
				newPoint = new Point(head.x, head.y - 1);
				break;
			case Direction.SOUTH : 
				newPoint = new Point(head.x, head.y + 1);
				break;
			case Direction.WEST:
				newPoint= new Point(head.x - 1, head.y);
				break;
			case Direction.EAST:
				newPoint= new Point(head.x + 1, head.y);
				break;
			}
			snake.push(addPoint);
			placeFruit();
					
		} else if(newPoint.x < 0 || newPoint.x > GRID_WIDTH - 1){
			// we went out of bounds oob, reset game
			checkScore();
			won = false;
			isAtEndGame = true;
			return;
		} else if(newPoint.y < 0 || newPoint.y > GRID_HEIGTH - 1 ){
			// we went out of bounds oob, reset game
			checkScore();
			won = false;
			isAtEndGame = true;
			return;
		} else if(snake.contains(newPoint)){
			// we ran into ourselves, reset game
			if(direction != Direction.NO_DIRECTION){
				checkScore();
				won = false;
				isAtEndGame = true;
				return;
			}
		} else if(rottenApples.contains(newPoint)){
			// we ate a rotten apple, reset game
			if(direction != Direction.NO_DIRECTION){
				checkScore();
				won = false;
				isAtEndGame = true;
				return;
			}
		} else if(snake.size() == (GRID_WIDTH * GRID_HEIGTH)-rottenApples.size()){
			// we won!	
			checkScore();
			won = true;
			isAtEndGame = true;
			return;
		}
		snake.push(newPoint);
	}
	
	public void drawScore(Graphics g){
		g.drawString("Score: " + score, 0, BOX_HEIGHT * GRID_HEIGTH + 10 );
		g.drawString("Highscore: " + highScore, 0, BOX_HEIGHT * GRID_HEIGTH + 20 );
	}
		
	public void checkScore(){
		if(highScore.equals("")){
			return;
		}
		
		
		if(score >Integer.parseInt((highScore.split(":")[1]))){
			String name= JOptionPane.showInputDialog("You set a new highscore. What is your name?");
			highScore = name + ":" +score;
			
			File scoreFile = new File("highscore.dat");
			if(!scoreFile.exists()){
				try {
					scoreFile.createNewFile();
				} catch (IOException e) {
					
					e.printStackTrace();
				}
			}
			FileWriter writeFile = null;
			BufferedWriter writer = null;
			try {
			writeFile = new FileWriter(scoreFile);
			writer = new BufferedWriter(writeFile);
			writer.write(this.highScore);
			} 
			catch (Exception e) {
				//errors 
			}
			finally{
				try{
					if(writer != null){
						writer.close();
					}
				}
				catch(Exception e){
					
				}
				
			}
		}
	}
	
	public void drawGrid(Graphics g){
		if (isInMenu==false) {
			
		
		// drawing outside rectangle
		g.drawRect(0, 0, GRID_WIDTH * BOX_WIDTH , GRID_HEIGTH * BOX_HEIGHT);
		// drawing the vertical lines
		for (int x = BOX_WIDTH; x < BOX_WIDTH * GRID_WIDTH; x+=BOX_HEIGHT) {
			g.drawLine(x, 0, x, BOX_HEIGHT * GRID_HEIGTH);
		}
		//drawing the horizontal lines
		for (int y= BOX_HEIGHT; y < GRID_HEIGTH *BOX_HEIGHT; y+=BOX_HEIGHT) {
			g.drawLine(0, y,GRID_WIDTH * BOX_WIDTH , y);
		}
		}
	
	
	}
	
	public void drawSnake(Graphics g){
		if (isInMenu==false) {
		g.setColor(Color.GREEN);
		for (Point p: snake) {
			g.fillRect(p.x * BOX_WIDTH, p.y * BOX_HEIGHT, BOX_WIDTH, BOX_HEIGHT);
		}
		g.setColor(Color.BLACK);
		}
	}
	
	public void drawFruit(Graphics g){
		if (isInMenu==false) {
		g.setColor(Color.RED);
		g.fillOval(fruit.x * BOX_WIDTH, fruit.y * BOX_HEIGHT, BOX_WIDTH, BOX_HEIGHT);
		g.setColor(Color.BLACK);
		}
	}
	
	public void drawRottenApples (Graphics g){
		if (isInMenu==false) {
		g.setColor(Color.BLACK);
		for (Point p: rottenApples) {
			g.fillOval(p.x * BOX_WIDTH, p.y * BOX_HEIGHT, BOX_WIDTH, BOX_HEIGHT);
			}
		}
	}
	
	public void placeFruit(){
		Random rand= new Random();
		int randomX = rand.nextInt(GRID_WIDTH);
		int randomY= rand.nextInt(GRID_HEIGTH);
		int randomX2 = rand.nextInt(GRID_WIDTH);
		int randomY2= rand.nextInt(GRID_HEIGTH);
		Point randomPoint= new Point(randomX, randomY);
		Point randomPoint2= new Point(randomX2, randomY2);
		while(snake.contains(randomPoint2)){
			randomX2=rand.nextInt(GRID_WIDTH);
			randomY2= rand.nextInt(GRID_HEIGTH);
			randomPoint2 = new Point(randomX2, randomY2);
			
		}
		while(snake.contains(randomPoint) || rottenApples.contains(randomPoint)){
			randomX=rand.nextInt(GRID_WIDTH);
			randomY= rand.nextInt(GRID_HEIGTH);
			randomPoint = new Point(randomX, randomY);
		}
		fruit = randomPoint;
		rottenApples.add(randomPoint2);
	}
	// This method is constantly running on the background
	@Override
	public void run() {
		while(true){
			//runs indefinitely
			repaint();
			if(!isInMenu && ! isAtEndGame){
				move();
				//repaint();
			}
			
			try{
				Thread.currentThread();
				Thread.sleep(level);
			}
			catch(Exception e){
				
			}
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {
		// TODO Auto-generated method stub
		switch(e.getKeyCode()){
		case KeyEvent.VK_UP: 
			if(direction != Direction.SOUTH){
				direction= Direction.NORTH;
			}
			break;
		case KeyEvent.VK_DOWN:
			if( direction != Direction.NORTH){
				direction = Direction.SOUTH;
			}
			break;
		case KeyEvent.VK_LEFT:
			if(direction != Direction.EAST){
				direction = Direction.WEST;
						
			}
			break;
		case KeyEvent.VK_RIGHT:
			if(direction != Direction. WEST){
				direction = Direction.EAST;
			}
			break;
		case KeyEvent.VK_ENTER:
			if(isInMenu){
				isInMenu=false;
				repaint();
			}
			break;
		case KeyEvent.VK_ESCAPE:
			isInMenu = true;
			break;
		case KeyEvent.VK_SPACE:
			if (isAtEndGame){
			isAtEndGame = false;
			won = false;
			generateDefaultSnake();
			rottenApples.clear();
			repaint();
			}
			break;
		}
	}

	public String getHighScore() {
		
		//format: 100
		FileReader readFile = null;
		BufferedReader reader = null;
		try{
			 readFile= new FileReader("highscore.dat");
			 reader = new BufferedReader(readFile);
			return reader.readLine();
		}
		catch(Exception e){
			return "Nobody:0";
		}
		finally{
			try {
				if(reader != null){
				reader.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

}
