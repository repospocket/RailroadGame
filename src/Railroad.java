import processing.core.*;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import org.apache.commons.math3.distribution.PoissonDistribution;

public class Railroad extends PApplet {

  public Player player;
  public ArrayList<Track> tracks;
  public int health = 5;
  boolean lowhealthlock = false;
  int gameover = 0;
  int damagedeffect = 0;
  float grid = 80;
  boolean dialog = false;
  boolean waitForInput = false;
  public int width = 1000, height = 480;
  int time, oldtime = 0; // oldtime for old games accumulated time for reset purposes
  float ycontran = height - grid * 2, ycontranup = grid; // player's y's and x's constrains for adding and removing
                                                         // tracks
  PImage bricks;
  PImage tracksimg;
  PImage water;
  PImage PlayerImg;
  PoissonDistribution pp = new PoissonDistribution(11);

  public void showhealth() {
    fill(255, 255, 0);
    text(Integer.toString(health), 910, 45);
    text("Health", 800, 45);

  }

  public void time() {
    time = millis() / 1000 - oldtime;
    fill(255, 0, 0);
    textSize(32);
    text(time, width / 2, 50);
  }

  public void setup() {

    promptdifficulty("Choose difficulty: (default 11)");

    player = new Player(width / 2 - grid / 2 + 15, height - grid, grid);
    tracks = new ArrayList<Track>();
    tracks.add(0, new Track(0, color(0, 0, 139))); // blank lane
    tracks.add(1, new Track(1, "Track")); // track
    tracks.add(2, new Track(2, "Track"));
    tracks.add(3, new Track(3, "Track"));
    tracks.add(4, new Track(4, "Track"));
    tracks.add(5, new Track(5, color(0, 0, 139)));

    bricks = loadImage("bricks.jpg");
    tracksimg = loadImage("tracks.jpg");
    water = loadImage("grass.jpg");
    PlayerImg = loadImage("bug.png");
  }

  public void draw() {

    background(1, 100, 32);

    for (Track atrack : tracks) {
      atrack.run();
    }

    if (damagedeffect > 0) {
      damagedeffect--;
    } else {
      PlayerImg = loadImage("bug.png");
    }

    time();

    player.show();

    int trackIndex = (int) (player.y / grid);
    tracks.get(trackIndex).check(player);

    textSize(32);
    showhealth();

    // add two new tracks to help player when health is low
    if (health == 2 && lowhealthlock == false) {
      tracks.set(0, new Track(0, "Track"));
      tracks.set(5, new Track(5, "Track"));
      ycontran = height - grid;
      ycontranup = 0;
      lowhealthlock = true;
    }

    // change # of tracks to two for more challenge
    if (time > 60) {
      tracks.set(0, new Track(0, color(0, 0, 139)));
      tracks.set(1, new Track(1, color(0, 0, 139)));
      tracks.set(4, new Track(4, color(0, 0, 139)));
      tracks.set(5, new Track(5, color(0, 0, 139)));
      fill(255, 0, 0);
      text("Final Level", width / 2 - 50, 80);
      ycontran = height - grid * 3;
      ycontranup = grid * 2;
      pp = new PoissonDistribution(25); // increase trains speed for more challenge
    }

    // reset all parameters and get ready for a new game
    if (gameover == 1) {
      delay(2500);
      lowhealthlock = false;
      gameover = 0;
      health = 5;
      oldtime = oldtime + time;
      ycontran = height - grid * 2;
      ycontranup = grid;
      pp = new PoissonDistribution(11);
      setup();
      tracks.set(0, new Track(0, color(0, 0, 139)));
      tracks.set(1, new Track(1, "Track"));
      tracks.set(2, new Track(2, "Track"));
      tracks.set(3, new Track(3, "Track"));
      tracks.set(4, new Track(4, "Track"));
      tracks.set(5, new Track(5, color(0, 0, 139)));
    }

    // show score and flag for a new game
    if (health == 0) {
      image(water, 0, 0, 1000, 86);
      fill(255, 0, 0);
      textSize(50);
      text("SCORE:  " + time, width / 2 - 80, 60);
      textSize(120);
      text("Game Over", width / 2 - 260, height / 2);
      gameover = 1;
    }
    // brown tunnel image
    image(bricks, 0, 0, 150, 480);

  }

  private void promptdifficulty(String message) {
    String input = JOptionPane.showInputDialog(frame, message);

    try {
      int number = Integer.parseInt(input);
      pp = new PoissonDistribution(number);

    } catch (NumberFormatException e) {
      // handle invalid input
      JOptionPane.showMessageDialog(null, "Difficulty set to default: 11", "Warning", JOptionPane.WARNING_MESSAGE);

    }
  }

  // listen to input
  public void keyPressed() {
    if (keyCode == UP) {
      player.move(0, -1);
    } else if (keyCode == DOWN) {
      player.move(0, 1);
    } else if (keyCode == RIGHT) {
      player.move(1, 0);
    } else if (keyCode == LEFT) {
      player.move(-1, 0);
    }
  }

  public class Player extends Rectangle {

    Player(float x, float y, float w) {
      super(x, y, w - 10, w - 10);
    }

    public void show() {
      x = constrain(x, 0, width - grid);
      y = constrain(y, ycontranup, ycontran);
      image(PlayerImg, x, y, w, w);
    }

    public void move(float xdir, float ydir) {
      x += xdir * grid;
      y += ydir * grid;

    }
  }

  public class Track extends Rectangle {

    public Train aTrain;
    int col;

    // empty lane constructor
    Track(int index, int c) {
      super(0, index * grid - 10, width, grid + 20);
      col = c;
    }

    // track constructor
    Track(int index, String Track) {
      super(0, index * grid, width, grid);
      float offset = random(-2, 10); // random "initial" offset for each train so they wont start together
      float trian = grid * 4;
      aTrain = new Train(-trian * 5 - trian * offset, index * grid, trian, grid);
      col = color(0);
    }

    public void check(Player player) {
      if (aTrain != null && aTrain.intersects(player) && aTrain.gothitbythistrain == false) {
        aTrain.gothitbythistrain = true;
        health = health - 1;
        damagedeffect = 21;
        PlayerImg = loadImage("bug2.png");

      }
    }

    public void run() {
      if (aTrain != null) {

        image(tracksimg, x, y, w, h - 10);
      } else {
        image(water, x, y, w, h - 10);
      }

      if (aTrain != null) {
        aTrain.update();
        aTrain.show();
      }
    }
  }

  public class Train extends Rectangle {
    float speed;
    boolean gothitbythistrain = false;
    int e = (int) random(1, 4); // random for goblins to send or not send message **different than lying**
    int e2 = (int) random(-2000, -1000); // variable for when the goblin lie **bonus mark**
    int e3 = (int) random(1, 5); // variable when we allow the goblin to send inaccurate message
    String[] strings1 = { "Something coming!", "Watch out!", "What is this..", "I hear noise", "Look here!" };
    String[] strings2 = { "What's up?", "Today's boring", "I need fire", "I hear noise", "Who's this??" };
    String[] strings = { "train.png", "train1.png", "train2.png", "train3.png", "train4.png" };
    PImage trainimg;
    int randomIndex;

    Train(float x, float y, float w, float h) {
      super(x, y, w, h - 10);
      speed = pp.sample(); // Poison distribution for each of trains' speeds (L1)
      trainimg = loadImage(strings[randomIndex]);
    }

    public void update() {
      x = x + speed;

      if (x >= width) { // train out of screen to the right, values for new train

        String[] strings = { "train.png", "train1.png", "train2.png", "train3.png", "train4.png" };
        randomIndex = (int) (Math.random() * strings.length);
        trainimg = loadImage(strings[randomIndex]);
        float trian = grid * 3;
        PoissonDistribution ppp = new PoissonDistribution(15); // offset Poisson distribution for next train to arrive
                                                               // (L0)
        speed = pp.sample() + 1;
        this.x = -trian * ppp.sample();
        this.gothitbythistrain = false;
        e = (int) random(1, 4);
        e2 = (int) random(-2000, -1500);
        e3 = (int) random(1, 5);
      }
    }

    public void show() {
      image(trainimg, x, y, w, h);

      if (x + w < 150 && x > -500 && e != 2) { // if e random is 2 goblin message is not received!
        fill(0);
        textFont(createFont("Arial Bold", 28));
        text(strings1[randomIndex], 150, y + 40);
      }

      if (x + w < e2 && x > e2 - 500 && e3 == 1) { // goblin sometimes send other messages
        fill(0);
        textFont(createFont("Arial Bold", 28));
        text(strings2[randomIndex], 150, y + 40);
      }

      if (x + w < e2 && x > e2 - 500 && e3 == 4) { // goblin sometimes send other messages
        fill(0);

        textFont(createFont("Arial Bold", 28));
        text(strings2[randomIndex], 150, y + 40);
      }
    }
  }

  // rectangle to initiate everything as a rectangle
  class Rectangle {
    public float x, y, w, h;

    Rectangle(float x, float y, float w, float h) {
      this.x = x;
      this.w = w;
      this.y = y;
      this.h = h;
    }

    public boolean intersects(Rectangle other) {
      float left = x, right = x + w, top = y, bottom = y + h;

      float oleft = other.x;
      float oright = other.x + other.w;
      float otop = other.y;
      float obottom = other.y + other.h;

      return !(left >= oright ||
          right <= oleft ||
          top >= obottom ||
          bottom <= otop);

    }
  }

  public void settings() {
    size(1000, 480);
  }

  static public void main(String[] passedArgs) {
    PApplet.main("Railroad");
  }

}