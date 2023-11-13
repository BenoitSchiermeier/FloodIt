import javalib.impworld.World;
import javalib.impworld.WorldScene;
import javalib.worldimages.OutlineMode;
import javalib.worldimages.Posn;
import javalib.worldimages.RectangleImage;
import javalib.worldimages.TextImage;
import tester.Tester;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

interface ICell {
  // set the left cell
  void setLeft(ICell left);

  // set the top cell
  void setTop(ICell top);

  // set the right cell
  void setRight(ICell right);

  // set the bottom cell
  void setBottom(ICell bottom);

  boolean shouldFlood(Color neighborColor, Color color);

  ArrayList<Cell> getNeighbors();

  boolean isEmptyCell();
}

class MtCell implements ICell {

  MtCell() {
  }

  // set the left cell
  public void setLeft(ICell left) {
    return;
  }

  // set the top cell
  public void setTop(ICell top) {
    return;
  }

  // set the right cell
  public void setRight(ICell right) {
    return;
  }

  // set the bottom cell
  public void setBottom(ICell bottom) {
    return;
  }

  public boolean shouldFlood(Color neighborColor, Color color) {
    return false;
  }

  public ArrayList<Cell> getNeighbors() {
    return new ArrayList<Cell>();
  }

  public boolean isEmptyCell() {
    return true;
  }
}

// Represents a single square of the game area
class Cell implements ICell {
  // In logical coordinates, with the origin at the top-left corner of the screen
  int x;
  int y;
  Color color;
  boolean flooded;
  boolean floodChecked = false;
  // the four adjacent cells to this one
  ICell left;
  ICell top;
  ICell right;
  ICell bottom;

  // constructor
  Cell(int x, int y, Color color) {
    this.x = x;
    this.y = y;
    this.color = color;
    this.flooded = false;
  }

  public boolean isEmptyCell() {
    return false;
  }

  // set the left cell
  public void setLeft(ICell left) {
    this.left = left;
  }

  // set the top cell
  public void setTop(ICell top) {
    this.top = top;
  }

  // set the right cell
  public void setRight(ICell right) {
    this.right = right;
  }

  // set the bottom cell
  public void setBottom(ICell bottom) {
    this.bottom = bottom;
  }

  public boolean shouldFlood(Color neighborColor, Color color) {
    return this.color == color || this.color == neighborColor;
  }

  public ArrayList<Cell> getNeighbors() {
    ArrayList<Cell> neighbors = new ArrayList<Cell>();
    if (!this.left.isEmptyCell()) {
      neighbors.add((Cell) this.left);
    }
    if (!this.top.isEmptyCell()) {
      neighbors.add((Cell) this.top);
    }
    if (!this.right.isEmptyCell()) {
      neighbors.add((Cell) this.right);
    }
    if (!this.bottom.isEmptyCell()) {
      neighbors.add((Cell) this.bottom);
    }
    return neighbors;
  }
}

class FloodItWorld extends World {
  // All the cells of the game
  ArrayList<Cell> board;

  // list of different colors (Max 8 different colors)
  ArrayList<Color> colors = new ArrayList<Color>(Arrays.asList(Color.red, Color.green,
      Color.blue, Color.yellow, Color.orange, Color.pink, Color.cyan, Color.magenta));

  // list of colors for this particular game
  ArrayList<Color> gameColors = new ArrayList<Color>();

  // random number generator
  Random rand;

  // number of colors
  int numColors;

  // size of the board
  int size;

  // number of attempts
  int attempts = 0;

  // maximum number of attempts
  int maxAttempts;

  // game over?
  boolean gameOver = false;

  // set the size of the scene
  int sceneSize = 1000;

  // set the size of the board
  int boardSize = 750;

  // set the size of each cell
  int cellSize;

  boolean allFlooded = false;

  Color clickedColor;

  ArrayList<ICell> toFlood = new ArrayList<ICell>();

  ArrayList<ICell> visited = new ArrayList<ICell>();

  boolean animating = false;


  // constructor
  FloodItWorld(int size, int numColors) {
    // check that the number of colors is between 3 and 8
    if (numColors < 3 || numColors > 8) {
      throw new IllegalArgumentException("You must have between 3 and 8 colors (inclusive)");
    } else if (size < 2 || size > 26) {
      throw new IllegalArgumentException("You must have between 2 and 26 cells (inclusive)");
    }

    // initialize board as an arraylist of size arraylists
    this.board = new ArrayList<Cell>();

    // record the size and number of colors
    this.size = size;
    this.numColors = numColors;

    // set the size of each cell
    this.cellSize = this.boardSize / this.size;

    // set the maximum number of attempts to a value
    // proportional to the size of the board and the number of colors
    this.maxAttempts = this.size + ((this.numColors - 3) * (this.size / 3));

    // initialize the random number generator
    this.rand = new Random();

    // randomly select the colors for this game
    for (int i = 0; i < numColors; i++) {
      int randIndex = this.rand.nextInt(this.colors.size());
      this.gameColors.add(this.colors.get(randIndex));
      this.colors.remove(randIndex);
    }

    // initialize each cell in the board
    for (int row = 0; row < size; row++) {
      for (int col = 0; col < size; col++) {
        Cell c = new Cell((col * this.cellSize) + ((this.cellSize / 2) + 125),
            (row * this.cellSize) + ((this.cellSize / 2) + 125),
            this.gameColors.get(this.rand.nextInt(this.numColors)));
        this.board.add(c);
      }
    }

    // set the left, top, right, and bottom cells for each cell
    for (Cell c : this.board) {
      int col = (c.x - ((this.cellSize / 2) + 125)) / this.cellSize;
      int row = (c.y - ((this.cellSize / 2) + 125)) / this.cellSize;
      // set the left cell
      if (col == 0) {
        c.setLeft(new MtCell());
      } else {
        c.setLeft(this.board.get((row * size) + (col - 1)));
      }
      // set the top cell
      if (row == 0) {
        c.setTop(new MtCell());
      } else {
        c.setTop(this.board.get(((row - 1) * size) + col));
      }
      // set the right cell
      if (col == this.size - 1) {
        c.setRight(new MtCell());
      } else {
        c.setRight(this.board.get((row * this.size) + (col + 1)));
      }
      // set the bottom cell
      if (row == this.size - 1) {
        c.setBottom(new MtCell());
      } else {
        c.setBottom(this.board.get(((row + 1) * this.size) + col));
      }
    }

    // flood the first cell
    this.board.get(0).flooded = true;

    // set the clicked color
    this.clickedColor = this.board.get(0).color;

    this.toFlood.add(this.board.get(0));
  }

  // reset the game
  public void reset() {
    // initialize board as an arraylist of size arraylists
    this.board = new ArrayList<Cell>();

    // reset colors
    this.colors = new ArrayList<Color>(Arrays.asList(Color.red, Color.green,
        Color.blue, Color.yellow, Color.orange, Color.pink, Color.cyan, Color.magenta));

    // reset game colors
    this.gameColors = new ArrayList<Color>();

    // reset the number of attempts
    this.attempts = 0;

    // initialize the random number generator
    this.rand = new Random();

    // randomly select the colors for this game
    for (int i = 0; i < numColors; i++) {
      int randIndex = this.rand.nextInt(this.colors.size());
      this.gameColors.add(this.colors.get(randIndex));
      this.colors.remove(randIndex);
    }

    // initialize each cell in the board
    for (int row = 0; row < size; row++) {
      for (int col = 0; col < size; col++) {
        Cell c = new Cell((col * this.cellSize) + ((this.cellSize / 2) + 125),
            (row * this.cellSize) + ((this.cellSize / 2) + 125),
            this.gameColors.get(this.rand.nextInt(this.numColors)));
        this.board.add(c);
      }
    }

    // set the left, top, right, and bottom cells for each cell
    for (Cell c : this.board) {
      int col = (c.x - ((this.cellSize / 2) + 125)) / this.cellSize;
      int row = (c.y - ((this.cellSize / 2) + 125)) / this.cellSize;
      // set the left cell
      if (col == 0) {
        c.setLeft(new MtCell());
      } else {
        c.setLeft(this.board.get((row * size) + (col - 1)));
      }
      // set the top cell
      if (row == 0) {
        c.setTop(new MtCell());
      } else {
        c.setTop(this.board.get(((row - 1) * size) + col));
      }
      // set the right cell
      if (col == this.size - 1) {
        c.setRight(new MtCell());
      } else {
        c.setRight(this.board.get((row * this.size) + (col + 1)));
      }
      // set the bottom cell
      if (row == this.size - 1) {
        c.setBottom(new MtCell());
      } else {
        c.setBottom(this.board.get(((row + 1) * this.size) + col));
      }
    }

    // flood the first cell
    this.board.get(0).flooded = true;

    this.toFlood = new ArrayList<ICell>(Arrays.asList(this.board.get(0)));
    this.visited = new ArrayList<ICell>();

    this.gameOver = false;
    this.allFlooded = false;
  }

  public void isGameOver() {
    // check if the game is over (all cells are flooded)
    if (this.attempts > this.maxAttempts) {
      this.gameOver = true;
      return;
    }
    for (Cell c : this.board) {
      if (!c.flooded) {
        return;
      }
    }
    this.allFlooded = true;
    this.gameOver = true;
  }

  public WorldScene makeScene() {
    // if the game is over, return the last scene
    if (this.gameOver) {
      return this.lastScene();
    }

    // create scene
    WorldScene scene = new WorldScene(this.sceneSize, this.sceneSize);

    // draw black border around the board
    scene.placeImageXY(new RectangleImage(this.sceneSize - 235, this.sceneSize - 235,
            OutlineMode.SOLID, Color.black),
        this.board.get(this.size / 2).x - (this.cellSize / 2),
        this.board.get((this.size / 2) * this.size).y - (this.cellSize / 2));

    // draw the board
    for (Cell c : this.board) {
      scene.placeImageXY(new RectangleImage(this.cellSize, this.cellSize,
          OutlineMode.SOLID, c.color), c.x, c.y);
    }

    // create "Flood-It" title
    scene.placeImageXY(new TextImage("Flood-It", 35, Color.black),
        this.sceneSize / 2, 25);

    // include game details, including size and number of colors
    scene.placeImageXY(new TextImage("Size: " + this.size,
        20, Color.black), this.sceneSize / 2, 60);
    scene.placeImageXY(new TextImage("Colors: " + this.numColors,
        20, Color.black), this.sceneSize / 2, 85);

    // include attempts counter in format "0/25" underneath board
    scene.placeImageXY(new TextImage(this.attempts + "/" + this.maxAttempts,
        20, Color.black), 800, 60);

    // include game instructions at the bottom of the screen
    scene.placeImageXY(new TextImage("Click cells. Fill the board with a single color.",
        20, Color.black), this.sceneSize / 2, 900);

    // return the scene
    return scene;
  }

  public WorldScene lastScene() {
    // make the last scene
    WorldScene scene = new WorldScene(1000, 1000);

    // create "Flood-It" title
    scene.placeImageXY(new TextImage("Flood-It", 35, Color.black),
        500, 25);

    // include game details, including size and number of colors
    scene.placeImageXY(new TextImage("Size: " + this.size,
        20, Color.black), 500, 60);

    scene.placeImageXY(new TextImage("Colors: " + this.numColors,
        20, Color.black), 500, 85);

    // if the player won, display "You Win!"
    if (this.allFlooded) {
      scene.placeImageXY(
          new TextImage("You Win,", 35, Color.black), 500, 450);
      scene.placeImageXY(
          new TextImage("Congratulations!", 35, Color.black), 500, 500);
      scene.placeImageXY(
          new TextImage("You Used " + this.attempts + " of " + this.maxAttempts + " Attempts!",
              35, Color.black), 500, 550);
    } else {
      // if the player lost, display "You Lose!"
      scene.placeImageXY(new TextImage("Sorry,", 35, Color.black),
          500, 450);
      scene.placeImageXY(new TextImage("You Lose!", 35, Color.black),
          500, 500);
      scene.placeImageXY(new TextImage("You Ran Out of Attempts!", 35, Color.black),
          500, 550);
    }

    // return the scene
    return scene;
  }

  public void onTick() {
    // if the game is over, do nothing
    this.isGameOver();
    if (this.gameOver) {
      return;
    }
    // if waterfall animation is over, reset flood states
    if (this.toFlood.isEmpty()) {
      this.visited = new ArrayList<ICell>();
      this.toFlood.add(this.board.get(0));
      this.animating = false;
    }
    // flood all cells for this round and set up for next round
    ArrayList<ICell> nextToFlood = new ArrayList<ICell>();
    ArrayList<ICell> nextVisited = new ArrayList<ICell>();
    // for every cell in the board
    for (Cell c : this.board) {
      // if the cell is in the list of cells to flood
      if (this.toFlood.contains(c)) {
        // add to the list of visited cells
        nextVisited.add(c);
        // get the color of the cell's neighbor before it is changed
        Color neighborColor = c.color;
        // change the color
        c.color = this.clickedColor;
        // add the cell's neighbors to the list of cells to flood
        // if they haven't been visited and should be flooded
        for (Cell n : c.getNeighbors()) {
          if (!this.visited.contains(n) && n.shouldFlood(neighborColor, this.clickedColor)) {
            n.flooded = true;
            nextToFlood.add(n);
          }
        }

      }
    }
    // update the list of cells to flood
    this.toFlood = nextToFlood;
    // update the list of visited cells
    this.visited.addAll(nextVisited);
  }

  public void onMousePressed(Posn pos) {
    System.out.println(pos.x + ", " + pos.y);
    if (this.animating) {
      return;
    }
    // check if the click is within the board
    if (pos.x > 125 && pos.x < 875 && pos.y > 125 && pos.y < 875) {


      // get the cell that was clicked with a margin of half the cell size
      Cell clicked = this.board.get((pos.y - 125) / this.cellSize * this.size
          + (pos.x - 125) / this.cellSize);

      // check if the cell is flooded
      if (!clicked.flooded) {
        // increment the number of attempts
        this.attempts++;
        // store the color of the clicked cell
        this.clickedColor = clicked.color;
        this.animating = true;
      }
    }
  }

  public void onKeyEvent(String key) {
    // check if the key pressed is "r"
    if (key.equals("r")) {
      // reset the game
      this.reset();
    }
  }
}

class ExamplesFloodIt {

  // TEST FOR THE FLOODIT WORLD CLASS

  // test displaying the game
  void testDisplayGame(Tester t) {
    new FloodItWorld(6, 3).bigBang(1000, 1000, 0.1);
  }

  // create five different worlds
  FloodItWorld world1;
  FloodItWorld world2;
  FloodItWorld world3;
  FloodItWorld world4;
  FloodItWorld world5;
  FloodItWorld world6;
  FloodItWorld world7;


  // initialize the data
  void initWorldData() {
    // initialize the worlds
    this.world1 = new FloodItWorld(2, 3);
    this.world2 = new FloodItWorld(6, 3);
    this.world3 = new FloodItWorld(10, 4);
    this.world4 = new FloodItWorld(14, 5);
    this.world5 = new FloodItWorld(18, 6);
    this.world6 = new FloodItWorld(22, 7);
    this.world7 = new FloodItWorld(26, 8);
  }

  // test for illegal arguments
  void testIllegalArguments(Tester t) {
    t.checkConstructorException(
        new IllegalArgumentException("You must have between 2 and 26 cells (inclusive)"),
        "FloodItWorld", 1, 3);
    t.checkConstructorException(
        new IllegalArgumentException("You must have between 2 and 26 cells (inclusive)"),
        "FloodItWorld", 27, 3);
    t.checkConstructorException(
        new IllegalArgumentException("You must have between 3 and 8 colors (inclusive)"),
        "FloodItWorld", 4, 1);
    t.checkConstructorException(
        new IllegalArgumentException("You must have between 3 and 8 colors (inclusive)"),
        "FloodItWorld", 4, 9);
  }

  // test the size of the board
  void testBoardSize(Tester t) {
    // initialize the data
    this.initWorldData();

    // test the size of the board
    t.checkExpect(this.world1.board.size(), 4);
    t.checkExpect(this.world2.board.size(), 36);
    t.checkExpect(this.world3.board.size(), 100);
    t.checkExpect(this.world4.board.size(), 196);
    t.checkExpect(this.world5.board.size(), 324);
    t.checkExpect(this.world6.board.size(), 484);
    t.checkExpect(this.world7.board.size(), 676);
  }

  // test the number of colors on the board
  void testBoardColors(Tester t) {
    // initialize the data
    this.initWorldData();

    // test the number of colors on the board
    t.checkExpect(this.world1.gameColors.size(), this.world1.numColors);
    t.checkExpect(this.world2.gameColors.size(), this.world2.numColors);
    t.checkExpect(this.world3.gameColors.size(), this.world3.numColors);
    t.checkExpect(this.world4.gameColors.size(), this.world4.numColors);
    t.checkExpect(this.world5.gameColors.size(), this.world5.numColors);
    t.checkExpect(this.world6.gameColors.size(), this.world6.numColors);
    t.checkExpect(this.world7.gameColors.size(), this.world7.numColors);
  }

  // test the top left cell is flooded
  void testTopLeftCell(Tester t) {
    // initialize the data
    this.initWorldData();

    // test the top left cell is flooded
    t.checkExpect(this.world1.board.get(0).flooded, true);
    t.checkExpect(this.world2.board.get(0).flooded, true);
    t.checkExpect(this.world3.board.get(0).flooded, true);
    t.checkExpect(this.world4.board.get(0).flooded, true);
    t.checkExpect(this.world5.board.get(0).flooded, true);
    t.checkExpect(this.world6.board.get(0).flooded, true);
    t.checkExpect(this.world7.board.get(0).flooded, true);
  }

  // test the reset method
  void testReset(Tester t) {
    // initialize the data
    this.initWorldData();

    // test the reset method
    // World 1
    // reset the world
    this.world1.reset();
    // test the reset method
    t.checkExpect(world1.colors.size(), 5);
    t.checkExpect(world1.gameColors.size(), 3);
    t.checkExpect(world1.attempts, 0);
    t.checkExpect(world1.board.get(0).flooded, true);
    t.checkExpect(world1.toFlood, new ArrayList<ICell>(Arrays.asList(world1.board.get(0))));
    t.checkExpect(world1.visited, new ArrayList<ICell>());
    t.checkExpect(world1.gameOver, false);
    t.checkExpect(world1.allFlooded, false);

    // World 2
    // reset the world
    this.world2.reset();
    // test the reset method
    t.checkExpect(world2.colors.size(), 5);
    t.checkExpect(world2.gameColors.size(), 3);
    t.checkExpect(world2.attempts, 0);
    t.checkExpect(world2.board.get(0).flooded, true);
    t.checkExpect(world2.toFlood, new ArrayList<ICell>(Arrays.asList(world2.board.get(0))));
    t.checkExpect(world2.visited, new ArrayList<ICell>());
    t.checkExpect(world2.gameOver, false);
    t.checkExpect(world2.allFlooded, false);

    // World 3
    // reset the world
    this.world3.reset();
    // test the reset method
    t.checkExpect(world3.colors.size(), 4);
    t.checkExpect(world3.gameColors.size(), 4);
    t.checkExpect(world3.attempts, 0);
    t.checkExpect(world3.board.get(0).flooded, true);
    t.checkExpect(world3.toFlood, new ArrayList<ICell>(Arrays.asList(world3.board.get(0))));
    t.checkExpect(world3.visited, new ArrayList<ICell>());
    t.checkExpect(world3.gameOver, false);
    t.checkExpect(world3.allFlooded, false);

    // World 4
    // reset the world
    this.world4.reset();
    // test the reset method
    t.checkExpect(world4.colors.size(), 3);
    t.checkExpect(world4.gameColors.size(), 5);
    t.checkExpect(world4.attempts, 0);
    t.checkExpect(world4.board.get(0).flooded, true);
    t.checkExpect(world4.toFlood, new ArrayList<ICell>(Arrays.asList(world4.board.get(0))));
    t.checkExpect(world4.visited, new ArrayList<ICell>());
    t.checkExpect(world4.gameOver, false);
    t.checkExpect(world4.allFlooded, false);

    // World 5
    // reset the world
    this.world5.reset();
    // test the reset method
    t.checkExpect(world5.colors.size(), 2);
    t.checkExpect(world5.gameColors.size(), 6);
    t.checkExpect(world5.attempts, 0);
    t.checkExpect(world5.board.get(0).flooded, true);
    t.checkExpect(world5.toFlood, new ArrayList<ICell>(Arrays.asList(world5.board.get(0))));
    t.checkExpect(world5.visited, new ArrayList<ICell>());
    t.checkExpect(world5.gameOver, false);
    t.checkExpect(world5.allFlooded, false);

    // World 6
    // reset the world
    this.world6.reset();
    // test the reset method
    t.checkExpect(world6.colors.size(), 1);
    t.checkExpect(world6.gameColors.size(), 7);
    t.checkExpect(world6.attempts, 0);
    t.checkExpect(world6.board.get(0).flooded, true);
    t.checkExpect(world6.toFlood, new ArrayList<ICell>(Arrays.asList(world6.board.get(0))));
    t.checkExpect(world6.visited, new ArrayList<ICell>());
    t.checkExpect(world6.gameOver, false);
    t.checkExpect(world6.allFlooded, false);

    // World 7
    // reset the world
    this.world7.reset();
    // test the reset method
    t.checkExpect(world7.colors.size(), 0);
    t.checkExpect(world7.gameColors.size(), 8);
    t.checkExpect(world7.attempts, 0);
    t.checkExpect(world7.board.get(0).flooded, true);
    t.checkExpect(world7.toFlood, new ArrayList<ICell>(Arrays.asList(world7.board.get(0))));
    t.checkExpect(world7.visited, new ArrayList<ICell>());
    t.checkExpect(world7.gameOver, false);
    t.checkExpect(world7.allFlooded, false);
  }

  // test the isGameOver method
  void testIsGameOver(Tester t) {
    // initialize the data
    this.initWorldData();

    // test the isGameOver method

    // World 1
    // test the isGameOver method
    world1.isGameOver();
    t.checkExpect(world1.gameOver, false);

    // World 2
    // test the isGameOver method
    world2.isGameOver();
    t.checkExpect(world2.gameOver, false);

    // World 3
    // test the isGameOver method
    for (Cell c : world3.board) {
      c.flooded = true;
    }
    world3.isGameOver();
    t.checkExpect(world3.gameOver, true);

    // World 4
    // test the isGameOver method
    for (Cell c : world4.board) {
      c.flooded = true;
    }
    world4.isGameOver();
    t.checkExpect(world4.gameOver, true);

    // World 5
    // test the isGameOver method
    world5.attempts = world5.maxAttempts + 1;
    world5.isGameOver();
    t.checkExpect(world5.gameOver, true);

    // World 6
    // test the isGameOver method
    world6.attempts = world6.maxAttempts + 1;
    world6.isGameOver();
    t.checkExpect(world6.gameOver, true);

    // World 7
    // test the isGameOver method
    world7.isGameOver();
    t.checkExpect(world7.gameOver, false);
  }

  // test the makeScene method
  void testMakeScene(Tester t) {
    // initialize the data
    this.initWorldData();

    // test the makeScene method
    // World 1
    // create expected world 1 scene
    WorldScene world1Scene = new WorldScene(1000, 1000);
    world1Scene.placeImageXY(new RectangleImage(765, 765,
            OutlineMode.SOLID, Color.black),
        world1.board.get(this.world1.size / 2).x - (world1.cellSize / 2),
        world1.board.get((world1.size / 2) * world1.size).y - (world1.cellSize / 2));
    for (Cell c : this.world1.board) {
      world1Scene.placeImageXY(new RectangleImage(world1.cellSize, world1.cellSize,
          OutlineMode.SOLID, c.color), c.x, c.y);
    }
    world1Scene.placeImageXY(new TextImage("Flood-It", 35, Color.black), 500, 25);
    world1Scene.placeImageXY(new TextImage("Size: 2", 20, Color.black), 500, 60);
    world1Scene.placeImageXY(new TextImage("Colors: 3", 20, Color.black), 500, 85);
    world1Scene.placeImageXY(new TextImage("0/" + world1.maxAttempts, 20, Color.black), 800, 60);
    world1Scene.placeImageXY(new TextImage("Click cells. Fill the board with a single color.",
        20, Color.black), 500, 900);
    t.checkExpect(this.world1.makeScene(), world1Scene);

    // World 2
    // create expected world 2 scene
    WorldScene world2Scene = new WorldScene(1000, 1000);
    world2Scene.placeImageXY(new RectangleImage(765, 765,
            OutlineMode.SOLID, Color.black),
        world2.board.get(this.world2.size / 2).x - (world2.cellSize / 2),
        world2.board.get((world2.size / 2) * world2.size).y - (world2.cellSize / 2));
    for (Cell c : this.world2.board) {
      world2Scene.placeImageXY(new RectangleImage(world2.cellSize, world2.cellSize,
          OutlineMode.SOLID, c.color), c.x, c.y);
    }
    world2Scene.placeImageXY(new TextImage("Flood-It", 35, Color.black), 500, 25);
    world2Scene.placeImageXY(new TextImage("Size: 6", 20, Color.black), 500, 60);
    world2Scene.placeImageXY(new TextImage("Colors: 3", 20, Color.black), 500, 85);
    world2Scene.placeImageXY(new TextImage("0/" + world2.maxAttempts, 20, Color.black), 800, 60);
    world2Scene.placeImageXY(new TextImage("Click cells. Fill the board with a single color.",
        20, Color.black), 500, 900);
    t.checkExpect(this.world2.makeScene(), world2Scene);

    // World 3
    // create expected world 3 scene
    WorldScene world3Scene = new WorldScene(1000, 1000);
    world3Scene.placeImageXY(new RectangleImage(765, 765,
            OutlineMode.SOLID, Color.black),
        world3.board.get(this.world3.size / 2).x - (world3.cellSize / 2),
        world3.board.get((world3.size / 2) * world3.size).y - (world3.cellSize / 2));
    for (Cell c : this.world3.board) {
      world3Scene.placeImageXY(new RectangleImage(world3.cellSize, world3.cellSize,
          OutlineMode.SOLID, c.color), c.x, c.y);
    }
    world3Scene.placeImageXY(new TextImage("Flood-It", 35, Color.black), 500, 25);
    world3Scene.placeImageXY(new TextImage("Size: 10", 20, Color.black), 500, 60);
    world3Scene.placeImageXY(new TextImage("Colors: 4", 20, Color.black), 500, 85);
    world3Scene.placeImageXY(new TextImage("0/" + world3.maxAttempts, 20, Color.black), 800, 60);
    world3Scene.placeImageXY(new TextImage("Click cells. Fill the board with a single color.",
        20, Color.black), 500, 900);
    t.checkExpect(this.world3.makeScene(), world3Scene);

    // World 4
    // create expected world 4 scene
    WorldScene world4Scene = new WorldScene(1000, 1000);
    world4Scene.placeImageXY(new RectangleImage(765, 765,
            OutlineMode.SOLID, Color.black),
        world4.board.get(this.world4.size / 2).x - (world4.cellSize / 2),
        world4.board.get((world4.size / 2) * world4.size).y - (world4.cellSize / 2));
    for (Cell c : this.world4.board) {
      world4Scene.placeImageXY(new RectangleImage(world4.cellSize, world4.cellSize,
          OutlineMode.SOLID, c.color), c.x, c.y);
    }
    world4Scene.placeImageXY(new TextImage("Flood-It", 35, Color.black), 500, 25);
    world4Scene.placeImageXY(new TextImage("Size: 14", 20, Color.black), 500, 60);
    world4Scene.placeImageXY(new TextImage("Colors: 5", 20, Color.black), 500, 85);
    world4Scene.placeImageXY(new TextImage("0/" + world4.maxAttempts, 20, Color.black), 800, 60);
    world4Scene.placeImageXY(new TextImage("Click cells. Fill the board with a single color.",
        20, Color.black), 500, 900);
    t.checkExpect(this.world4.makeScene(), world4Scene);

    // World 5
    // create expected world 5 scene
    WorldScene world5Scene = new WorldScene(1000, 1000);
    world5Scene.placeImageXY(new RectangleImage(765, 765,
            OutlineMode.SOLID, Color.black),
        world5.board.get(this.world5.size / 2).x - (world5.cellSize / 2),
        world5.board.get((world5.size / 2) * world5.size).y - (world5.cellSize / 2));
    for (Cell c : this.world5.board) {
      world5Scene.placeImageXY(new RectangleImage(world5.cellSize, world5.cellSize,
          OutlineMode.SOLID, c.color), c.x, c.y);
    }
    world5Scene.placeImageXY(new TextImage("Flood-It", 35, Color.black), 500, 25);
    world5Scene.placeImageXY(new TextImage("Size: 18", 20, Color.black), 500, 60);
    world5Scene.placeImageXY(new TextImage("Colors: 6", 20, Color.black), 500, 85);
    world5Scene.placeImageXY(new TextImage("0/" + world5.maxAttempts, 20, Color.black), 800, 60);
    world5Scene.placeImageXY(new TextImage("Click cells. Fill the board with a single color.",
        20, Color.black), 500, 900);
    t.checkExpect(this.world5.makeScene(), world5Scene);

    // World 6
    // create expected world 6 scene
    WorldScene world6Scene = new WorldScene(1000, 1000);
    world6Scene.placeImageXY(new RectangleImage(765, 765,
            OutlineMode.SOLID, Color.black),
        world6.board.get(this.world6.size / 2).x - (world6.cellSize / 2),
        world6.board.get((world6.size / 2) * world6.size).y - (world6.cellSize / 2));
    for (Cell c : this.world6.board) {
      world6Scene.placeImageXY(new RectangleImage(world6.cellSize, world6.cellSize,
          OutlineMode.SOLID, c.color), c.x, c.y);
    }
    world6Scene.placeImageXY(new TextImage("Flood-It", 35, Color.black), 500, 25);
    world6Scene.placeImageXY(new TextImage("Size: 22", 20, Color.black), 500, 60);
    world6Scene.placeImageXY(new TextImage("Colors: 7", 20, Color.black), 500, 85);
    world6Scene.placeImageXY(new TextImage("0/" + world6.maxAttempts, 20, Color.black), 800, 60);
    world6Scene.placeImageXY(new TextImage("Click cells. Fill the board with a single color.",
        20, Color.black), 500, 900);
    t.checkExpect(this.world6.makeScene(), world6Scene);

    // World 7
    // create expected world 7 scene
    WorldScene world7Scene = new WorldScene(1000, 1000);
    world7Scene.placeImageXY(new RectangleImage(765, 765,
            OutlineMode.SOLID, Color.black),
        world7.board.get(this.world7.size / 2).x - (world7.cellSize / 2),
        world7.board.get((world7.size / 2) * world7.size).y - (world7.cellSize / 2));
    for (Cell c : this.world7.board) {
      world7Scene.placeImageXY(new RectangleImage(world7.cellSize, world7.cellSize,
          OutlineMode.SOLID, c.color), c.x, c.y);
    }
    world7Scene.placeImageXY(new TextImage("Flood-It", 35, Color.black), 500, 25);
    world7Scene.placeImageXY(new TextImage("Size: 26", 20, Color.black), 500, 60);
    world7Scene.placeImageXY(new TextImage("Colors: 8", 20, Color.black), 500, 85);
    world7Scene.placeImageXY(new TextImage("0/" + world7.maxAttempts, 20, Color.black), 800, 60);
    world7Scene.placeImageXY(new TextImage("Click cells. Fill the board with a single color.",
        20, Color.black), 500, 900);
    t.checkExpect(this.world7.makeScene(), world7Scene);
  }

  // test the lastScene method
  void testLastScene(Tester t) {
    // initialize world examples
    this.initWorldData();

    // World 1
    world1.allFlooded = true;
    WorldScene scene1 = new WorldScene(1000, 1000);
    scene1.placeImageXY(new TextImage("Flood-It", 35, Color.black),
        500, 25);
    scene1.placeImageXY(new TextImage("Size: " + this.world1.size,
        20, Color.black), 500, 60);
    scene1.placeImageXY(new TextImage("Colors: " + this.world1.numColors,
        20, Color.black), 500, 85);
    scene1.placeImageXY(
        new TextImage("You Win,", 35, Color.black), 500, 450);
    scene1.placeImageXY(
        new TextImage("Congratulations!", 35, Color.black), 500, 500);
    scene1.placeImageXY(
        new TextImage("You Used " + this.world1.attempts + " of " +
            this.world1.maxAttempts + " Attempts!",
            35, Color.black), 500, 550);
    t.checkExpect(this.world1.lastScene(), scene1);

    // World 2
    world2.allFlooded = false;
    WorldScene scene2 = new WorldScene(1000, 1000);
    scene2.placeImageXY(new TextImage("Flood-It", 35, Color.black),
        500, 25);
    scene2.placeImageXY(new TextImage("Size: " + this.world2.size,
        20, Color.black), 500, 60);
    scene2.placeImageXY(new TextImage("Colors: " + this.world2.numColors,
        20, Color.black), 500, 85);
    scene2.placeImageXY(new TextImage("Sorry,", 35, Color.black),
        500, 450);
    scene2.placeImageXY(new TextImage("You Lose!", 35, Color.black),
        500, 500);
    scene2.placeImageXY(new TextImage("You Ran Out of Attempts!", 35, Color.black),
        500, 550);
    t.checkExpect(this.world2.lastScene(), scene2);

    // World 3
    world3.allFlooded = true;
    WorldScene scene3 = new WorldScene(1000, 1000);
    scene3.placeImageXY(new TextImage("Flood-It", 35, Color.black),
        500, 25);
    scene3.placeImageXY(new TextImage("Size: " + this.world3.size,
        20, Color.black), 500, 60);
    scene3.placeImageXY(new TextImage("Colors: " + this.world3.numColors,
        20, Color.black), 500, 85);
    scene3.placeImageXY(
        new TextImage("You Win,", 35, Color.black), 500, 450);
    scene3.placeImageXY(
        new TextImage("Congratulations!", 35, Color.black), 500, 500);
    scene3.placeImageXY(
        new TextImage("You Used " + this.world3.attempts + " of " +
            this.world3.maxAttempts + " Attempts!",
            35, Color.black), 500, 550);
    t.checkExpect(this.world3.lastScene(), scene3);

    // World 4
    world4.allFlooded = false;
    WorldScene scene4 = new WorldScene(1000, 1000);
    scene4.placeImageXY(new TextImage("Flood-It", 35, Color.black),
        500, 25);
    scene4.placeImageXY(new TextImage("Size: " + this.world4.size,
        20, Color.black), 500, 60);
    scene4.placeImageXY(new TextImage("Colors: " + this.world4.numColors,
        20, Color.black), 500, 85);
    scene4.placeImageXY(new TextImage("Sorry,", 35, Color.black),
        500, 450);
    scene4.placeImageXY(new TextImage("You Lose!", 35, Color.black),
        500, 500);
    scene4.placeImageXY(new TextImage("You Ran Out of Attempts!", 35, Color.black),
        500, 550);
    t.checkExpect(this.world4.lastScene(), scene4);

    // World 5
    world5.allFlooded = true;
    WorldScene scene5 = new WorldScene(1000, 1000);
    scene5.placeImageXY(new TextImage("Flood-It", 35, Color.black),
        500, 25);
    scene5.placeImageXY(new TextImage("Size: " + this.world5.size,
        20, Color.black), 500, 60);
    scene5.placeImageXY(new TextImage("Colors: " + this.world5.numColors,
        20, Color.black), 500, 85);
    scene5.placeImageXY(
        new TextImage("You Win,", 35, Color.black), 500, 450);
    scene5.placeImageXY(
        new TextImage("Congratulations!", 35, Color.black), 500, 500);
    scene5.placeImageXY(
        new TextImage("You Used " + this.world5.attempts + " of " +
            this.world5.maxAttempts + " Attempts!",
            35, Color.black), 500, 550);
    t.checkExpect(this.world5.lastScene(), scene5);

    // World 6
    world6.allFlooded = false;
    WorldScene scene6 = new WorldScene(1000, 1000);
    scene6.placeImageXY(new TextImage("Flood-It", 35, Color.black),
        500, 25);
    scene6.placeImageXY(new TextImage("Size: " + this.world6.size,
        20, Color.black), 500, 60);
    scene6.placeImageXY(new TextImage("Colors: " + this.world6.numColors,
        20, Color.black), 500, 85);
    scene6.placeImageXY(new TextImage("Sorry,", 35, Color.black),
        500, 450);
    scene6.placeImageXY(new TextImage("You Lose!", 35, Color.black),
        500, 500);
    scene6.placeImageXY(new TextImage("You Ran Out of Attempts!", 35, Color.black),
        500, 550);
    t.checkExpect(this.world6.lastScene(), scene6);

    // World 7
    world7.allFlooded = true;
    WorldScene scene7 = new WorldScene(1000, 1000);
    scene7.placeImageXY(new TextImage("Flood-It", 35, Color.black),
        500, 25);
    scene7.placeImageXY(new TextImage("Size: " + this.world7.size,
        20, Color.black), 500, 60);
    scene7.placeImageXY(new TextImage("Colors: " + this.world7.numColors,
        20, Color.black), 500, 85);
    scene7.placeImageXY(
        new TextImage("You Win,", 35, Color.black), 500, 450);
    scene7.placeImageXY(
        new TextImage("Congratulations!", 35, Color.black), 500, 500);
    scene7.placeImageXY(
        new TextImage("You Used " + this.world7.attempts + " of " +
            this.world7.maxAttempts + " Attempts!",
            35, Color.black), 500, 550);
    t.checkExpect(this.world7.lastScene(), scene7);

  }


  // test the onMouseClicked method
  void testOnMousePressed(Tester t) {
    // initialize world examples
    this.initWorldData();

    int prevAttempts = this.world1.attempts;
    this.world1.onMousePressed(new Posn(400, 400));
    t.checkExpect(this.world1.attempts, prevAttempts);

    prevAttempts = this.world2.attempts;
    this.world2.onMousePressed(new Posn(600, 600));
    t.checkExpect(this.world2.attempts, prevAttempts + 1);

    prevAttempts = this.world3.attempts;
    this.world3.onMousePressed(new Posn(600, 600));
    t.checkExpect(this.world3.attempts, prevAttempts + 1);

    prevAttempts = this.world4.attempts;
    this.world4.onMousePressed(new Posn(600, 600));
    t.checkExpect(this.world4.attempts, prevAttempts + 1);

    prevAttempts = this.world5.attempts;
    this.world5.onMousePressed(new Posn(400, 400));
    t.checkExpect(this.world5.attempts, prevAttempts + 1);

    prevAttempts = this.world6.attempts;
    this.world6.onMousePressed(new Posn(600, 600));
    t.checkExpect(this.world6.attempts, prevAttempts + 1);

    prevAttempts = this.world7.attempts;
    this.world7.onMousePressed(new Posn(600, 600));
    t.checkExpect(this.world7.attempts, prevAttempts + 1);
  }


  // test the onKeyEvent method
  void testOnKeyEvent(Tester t) {
    // test that the world is reset when the user presses the "r" key
    // initialize the data
    this.initWorldData();

    // test the reset method
    // World 1
    // reset the world
    this.world1.onKeyEvent("r");
    // test the reset method
    t.checkExpect(world1.colors.size(), 5);
    t.checkExpect(world1.gameColors.size(), 3);
    t.checkExpect(world1.attempts, 0);
    t.checkExpect(world1.board.get(0).flooded, true);
    t.checkExpect(world1.toFlood,
        new ArrayList<ICell>(Arrays.asList(world1.board.get(0))));
    t.checkExpect(world1.visited, new ArrayList<ICell>());
    t.checkExpect(world1.gameOver, false);
    t.checkExpect(world1.allFlooded, false);

    // World 2
    // reset the world
    this.world2.onKeyEvent("r");
    // test the reset method
    t.checkExpect(world2.colors.size(), 5);
    t.checkExpect(world2.gameColors.size(), 3);
    t.checkExpect(world2.attempts, 0);
    t.checkExpect(world2.board.get(0).flooded, true);
    t.checkExpect(world2.toFlood,
        new ArrayList<ICell>(Arrays.asList(world2.board.get(0))));
    t.checkExpect(world2.visited, new ArrayList<ICell>());
    t.checkExpect(world2.gameOver, false);
    t.checkExpect(world2.allFlooded, false);

    // World 3
    // reset the world
    this.world3.onKeyEvent("r");
    // test the reset method
    t.checkExpect(world3.colors.size(), 4);
    t.checkExpect(world3.gameColors.size(), 4);
    t.checkExpect(world3.attempts, 0);
    t.checkExpect(world3.board.get(0).flooded, true);
    t.checkExpect(world3.toFlood, new ArrayList<ICell>(Arrays.asList(world3.board.get(0))));
    t.checkExpect(world3.visited, new ArrayList<ICell>());
    t.checkExpect(world3.gameOver, false);
    t.checkExpect(world3.allFlooded, false);

    // World 4
    // reset the world
    this.world4.onKeyEvent("r");
    // test the reset method
    t.checkExpect(world4.colors.size(), 3);
    t.checkExpect(world4.gameColors.size(), 5);
    t.checkExpect(world4.attempts, 0);
    t.checkExpect(world4.board.get(0).flooded, true);
    t.checkExpect(world4.toFlood, new ArrayList<ICell>(Arrays.asList(world4.board.get(0))));
    t.checkExpect(world4.visited, new ArrayList<ICell>());
    t.checkExpect(world4.gameOver, false);
    t.checkExpect(world4.allFlooded, false);

    // World 5
    // reset the world
    this.world5.onKeyEvent("r");
    // test the reset method
    t.checkExpect(world5.colors.size(), 2);
    t.checkExpect(world5.gameColors.size(), 6);
    t.checkExpect(world5.attempts, 0);
    t.checkExpect(world5.board.get(0).flooded, true);
    t.checkExpect(world5.toFlood, new ArrayList<ICell>(Arrays.asList(world5.board.get(0))));
    t.checkExpect(world5.visited, new ArrayList<ICell>());
    t.checkExpect(world5.gameOver, false);
    t.checkExpect(world5.allFlooded, false);

    // World 6
    // reset the world
    this.world6.onKeyEvent("r");
    // test the reset method
    t.checkExpect(world6.colors.size(), 1);
    t.checkExpect(world6.gameColors.size(), 7);
    t.checkExpect(world6.attempts, 0);
    t.checkExpect(world6.board.get(0).flooded, true);
    t.checkExpect(world6.toFlood, new ArrayList<ICell>(Arrays.asList(world6.board.get(0))));
    t.checkExpect(world6.visited, new ArrayList<ICell>());
    t.checkExpect(world6.gameOver, false);
    t.checkExpect(world6.allFlooded, false);

    // World 7
    // reset the world
    this.world7.onKeyEvent("r");
    // test the reset method
    t.checkExpect(world7.colors.size(), 0);
    t.checkExpect(world7.gameColors.size(), 8);
    t.checkExpect(world7.attempts, 0);
    t.checkExpect(world7.board.get(0).flooded, true);
    t.checkExpect(world7.toFlood, new ArrayList<ICell>(Arrays.asList(world7.board.get(0))));
    t.checkExpect(world7.visited, new ArrayList<ICell>());
    t.checkExpect(world7.gameOver, false);
    t.checkExpect(world7.allFlooded, false);

  }

  //test the layout of the board in each world
  //(cells have the correct top, bottom, left, and right neighbors)
  void testLayout(Tester t) {
    // initialize world examples
    this.initWorldData();

    // test that neighbors are correct
    // use world 7 as an example since it has the most cells
    // and therefore the largest variety of neighbors
    for (Cell c : this.world7.board) {
      int col = (c.x - ((world7.cellSize / 2) + 125)) / world7.cellSize;
      int row = (c.y - ((world7.cellSize / 2) + 125)) / world7.cellSize;
      // set the left cell
      if (col == 0) {
        t.checkExpect(c.left, new MtCell());
      } else {
        t.checkExpect(c.left, this.world7.board.get((row * world7.size) + (col - 1)));
      }
      // set the top cell
      if (row == 0) {
        t.checkExpect(c.top, new MtCell());
      } else {
        t.checkExpect(c.top, this.world7.board.get(((row - 1) * world7.size) + col));
      }
      // set the right cell
      if (col == this.world7.size - 1) {
        t.checkExpect(c.right, new MtCell());
      } else {
        t.checkExpect(c.right, this.world7.board.get((row * this.world7.size) + (col + 1)));
      }
      // set the bottom cell
      if (row == this.world7.size - 1) {
        t.checkExpect(c.bottom, new MtCell());
      } else {
        t.checkExpect(c.bottom, this.world7.board.get(((row + 1) * this.world7.size) + col));
      }
    }
  }

  // TEST METHODS IN CELL CLASS
  // cell examples
  Cell cell1;
  Cell cell2;
  Cell cell3;
  Cell cell4;
  Cell cell5;
  Cell cell6;
  Cell cell7;
  Cell cell8;
  Cell cell9;
  Cell cell10;

  // initialize cell examples
  void initCellData() {
    this.cell1 = new Cell(0, 0, Color.red);
    this.cell2 = new Cell(1, 1, Color.blue);
    this.cell3 = new Cell(2, 2, Color.green);
    this.cell4 = new Cell(3, 3, Color.yellow);
    this.cell5 = new Cell(4, 4, Color.orange);
    this.cell6 = new Cell(5, 5, Color.pink);
    this.cell7 = new Cell(6, 6, Color.magenta);
    this.cell8 = new Cell(7, 7, Color.cyan);
    this.cell9 = new Cell(8, 8, Color.red);
    this.cell10 = new Cell(9, 9, Color.blue);
  }

  // test the constructor
  void testCellConstructor(Tester t) {
    // initialize cell examples
    this.initCellData();

    // test cell constructor
    t.checkExpect(this.cell1.x, 0);
    t.checkExpect(this.cell1.y, 0);
    t.checkExpect(this.cell1.color, Color.red);
    t.checkExpect(this.cell1.flooded, false);
    t.checkExpect(this.cell2.x, 1);
    t.checkExpect(this.cell2.y, 1);
    t.checkExpect(this.cell2.color, Color.blue);
    t.checkExpect(this.cell2.flooded, false);
    t.checkExpect(this.cell3.x, 2);
    t.checkExpect(this.cell3.y, 2);
    t.checkExpect(this.cell3.color, Color.green);
    t.checkExpect(this.cell3.flooded, false);
    t.checkExpect(this.cell4.x, 3);
    t.checkExpect(this.cell4.y, 3);
    t.checkExpect(this.cell4.color, Color.yellow);
    t.checkExpect(this.cell4.flooded, false);
    t.checkExpect(this.cell5.x, 4);
    t.checkExpect(this.cell5.y, 4);
    t.checkExpect(this.cell5.color, Color.orange);
    t.checkExpect(this.cell5.flooded, false);
    t.checkExpect(this.cell6.x, 5);
    t.checkExpect(this.cell6.y, 5);
    t.checkExpect(this.cell6.color, Color.pink);
    t.checkExpect(this.cell6.flooded, false);
    t.checkExpect(this.cell7.x, 6);
    t.checkExpect(this.cell7.y, 6);
    t.checkExpect(this.cell7.color, Color.magenta);
    t.checkExpect(this.cell7.flooded, false);
    t.checkExpect(this.cell8.x, 7);
    t.checkExpect(this.cell8.y, 7);
    t.checkExpect(this.cell8.color, Color.cyan);
    t.checkExpect(this.cell8.flooded, false);
    t.checkExpect(this.cell9.x, 8);
    t.checkExpect(this.cell9.y, 8);
    t.checkExpect(this.cell9.color, Color.red);
    t.checkExpect(this.cell9.flooded, false);
    t.checkExpect(this.cell10.x, 9);
    t.checkExpect(this.cell10.y, 9);
    t.checkExpect(this.cell10.color, Color.blue);
    t.checkExpect(this.cell10.flooded, false);
  }

  // test setLeft method
  void testSetLeft(Tester t) {
    // examples are just for testing purposes and are not accurate to the game board layout

    // initialize cell examples
    this.initCellData();

    // test setLeft method
    this.cell1.setLeft(null);
    t.checkExpect(this.cell1.left, null);
    this.cell2.setLeft(this.cell1);
    t.checkExpect(this.cell2.left, this.cell1);
    this.cell3.setLeft(this.cell2);
    t.checkExpect(this.cell3.left, this.cell2);
    this.cell4.setLeft(this.cell3);
    t.checkExpect(this.cell4.left, this.cell3);
    this.cell5.setLeft(this.cell4);
    t.checkExpect(this.cell5.left, this.cell4);
    this.cell6.setLeft(this.cell5);
    t.checkExpect(this.cell6.left, this.cell5);
    this.cell7.setLeft(this.cell6);
    t.checkExpect(this.cell7.left, this.cell6);
    this.cell8.setLeft(this.cell7);
    t.checkExpect(this.cell8.left, this.cell7);
    this.cell9.setLeft(this.cell8);
    t.checkExpect(this.cell9.left, this.cell8);
    this.cell10.setLeft(this.cell9);
    t.checkExpect(this.cell10.left, this.cell9);
  }

  // test setTop method
  void testSetTop(Tester t) {
    // examples are just for testing purposes and are not accurate to the game board layout

    // initialize cell examples
    this.initCellData();

    // test setTop method
    this.cell1.setTop(null);
    t.checkExpect(this.cell1.top, null);
    this.cell2.setTop(this.cell1);
    t.checkExpect(this.cell2.top, this.cell1);
    this.cell3.setTop(this.cell2);
    t.checkExpect(this.cell3.top, this.cell2);
    this.cell4.setTop(this.cell3);
    t.checkExpect(this.cell4.top, this.cell3);
    this.cell5.setTop(this.cell4);
    t.checkExpect(this.cell5.top, this.cell4);
    this.cell6.setTop(this.cell5);
    t.checkExpect(this.cell6.top, this.cell5);
    this.cell7.setTop(this.cell6);
    t.checkExpect(this.cell7.top, this.cell6);
    this.cell8.setTop(this.cell7);
    t.checkExpect(this.cell8.top, this.cell7);
    this.cell9.setTop(this.cell8);
    t.checkExpect(this.cell9.top, this.cell8);
    this.cell10.setTop(this.cell9);
    t.checkExpect(this.cell10.top, this.cell9);
  }

  // test setRight method
  void testSetRight(Tester t) {
    // examples are just for testing purposes and are not accurate to the game board layout

    // initialize cell examples
    this.initCellData();

    // test setRight method
    this.cell1.setRight(this.cell2);
    t.checkExpect(this.cell1.right, this.cell2);
    this.cell2.setRight(this.cell3);
    t.checkExpect(this.cell2.right, this.cell3);
    this.cell3.setRight(this.cell4);
    t.checkExpect(this.cell3.right, this.cell4);
    this.cell4.setRight(this.cell5);
    t.checkExpect(this.cell4.right, this.cell5);
    this.cell5.setRight(this.cell6);
    t.checkExpect(this.cell5.right, this.cell6);
    this.cell6.setRight(this.cell7);
    t.checkExpect(this.cell6.right, this.cell7);
    this.cell7.setRight(this.cell8);
    t.checkExpect(this.cell7.right, this.cell8);
    this.cell8.setRight(this.cell9);
    t.checkExpect(this.cell8.right, this.cell9);
    this.cell9.setRight(this.cell10);
    t.checkExpect(this.cell9.right, this.cell10);
    this.cell10.setRight(new MtCell());
    t.checkExpect(this.cell10.right, new MtCell());
  }

  // test setBottom method
  void testSetBottom(Tester t) {
    // examples are just for testing purposes and are not accurate to the game board layout

    // initialize cell examples
    this.initCellData();

    // test setBottom method
    this.cell1.setBottom(this.cell2);
    t.checkExpect(this.cell1.bottom, this.cell2);
    this.cell2.setBottom(this.cell3);
    t.checkExpect(this.cell2.bottom, this.cell3);
    this.cell3.setBottom(this.cell4);
    t.checkExpect(this.cell3.bottom, this.cell4);
    this.cell4.setBottom(this.cell5);
    t.checkExpect(this.cell4.bottom, this.cell5);
    this.cell5.setBottom(this.cell6);
    t.checkExpect(this.cell5.bottom, this.cell6);
    this.cell6.setBottom(this.cell7);
    t.checkExpect(this.cell6.bottom, this.cell7);
    this.cell7.setBottom(this.cell8);
    t.checkExpect(this.cell7.bottom, this.cell8);
    this.cell8.setBottom(this.cell9);
    t.checkExpect(this.cell8.bottom, this.cell9);
    this.cell9.setBottom(this.cell10);
    t.checkExpect(this.cell9.bottom, this.cell10);
    this.cell10.setBottom(null);
    t.checkExpect(this.cell10.bottom, null);
  }

  // test shouldFlood method
  void testShouldFlood(Tester t) {
    // examples are just for testing purposes and are not accurate to the game board layout

    // initialize cell examples
    this.initCellData();

    // test shouldFlood method
    t.checkExpect(this.cell1.shouldFlood(Color.RED, Color.BLUE), true);
    t.checkExpect(this.cell2.shouldFlood(Color.RED, Color.BLUE), true);
    t.checkExpect(this.cell3.shouldFlood(Color.RED, Color.BLUE), false);
    t.checkExpect(this.cell4.shouldFlood(Color.RED, Color.BLUE), false);
    t.checkExpect(this.cell5.shouldFlood(Color.RED, Color.BLUE), false);
    t.checkExpect(this.cell6.shouldFlood(Color.RED, Color.BLUE), false);
    t.checkExpect(this.cell7.shouldFlood(Color.RED, Color.BLUE), false);
    t.checkExpect(this.cell8.shouldFlood(Color.RED, Color.BLUE), false);
    t.checkExpect(this.cell9.shouldFlood(Color.RED, Color.BLUE), true);
    t.checkExpect(this.cell10.shouldFlood(Color.RED, Color.BLUE), true);
  }

  // testGetNeighbors method
  void testGetNeighbors(Tester t) {
    // examples are just for testing purposes and are not accurate to the game board layout

    // initialize cell examples
    this.initCellData();

    // test getNeighbors method
    this.cell1.setBottom(this.cell2);
    this.cell1.setRight(this.cell3);
    this.cell1.setTop(new MtCell());
    this.cell1.setLeft(new MtCell());
    t.checkExpect(this.cell1.getNeighbors(),
        new ArrayList<Cell>(Arrays.asList(this.cell3, this.cell2)));

    this.cell2.setBottom(this.cell4);
    this.cell2.setRight(this.cell5);
    this.cell2.setTop(this.cell1);
    this.cell2.setLeft(new MtCell());
    t.checkExpect(this.cell2.getNeighbors(),
        new ArrayList<Cell>(Arrays.asList(this.cell1, this.cell5, this.cell4)));

    this.cell3.setBottom(this.cell5);
    this.cell3.setRight(this.cell6);
    this.cell3.setTop(new MtCell());
    this.cell3.setLeft(this.cell1);
    t.checkExpect(this.cell3.getNeighbors(),
        new ArrayList<Cell>(Arrays.asList(this.cell1, this.cell6, this.cell5)));

    this.cell4.setBottom(this.cell7);
    this.cell4.setRight(this.cell8);
    this.cell4.setTop(this.cell2);
    this.cell4.setLeft(new MtCell());
    t.checkExpect(this.cell4.getNeighbors(),
        new ArrayList<Cell>(Arrays.asList(this.cell2, this.cell8, this.cell7)));

    this.cell5.setBottom(this.cell8);
    this.cell5.setRight(this.cell9);
    this.cell5.setTop(this.cell3);
    this.cell5.setLeft(this.cell2);
    t.checkExpect(this.cell5.getNeighbors(),
        new ArrayList<Cell>(Arrays.asList(this.cell2, this.cell3, this.cell9, this.cell8)));

    this.cell6.setBottom(this.cell9);
    this.cell6.setRight(this.cell10);
    this.cell6.setTop(new MtCell());
    this.cell6.setLeft(this.cell3);
    t.checkExpect(this.cell6.getNeighbors(),
        new ArrayList<Cell>(Arrays.asList(this.cell3, this.cell10, this.cell9)));

    this.cell7.setBottom(new MtCell());
    this.cell7.setRight(this.cell9);
    this.cell7.setTop(this.cell4);
    this.cell7.setLeft(new MtCell());
    t.checkExpect(this.cell7.getNeighbors(),
        new ArrayList<Cell>(Arrays.asList(this.cell4, this.cell9)));

    this.cell8.setBottom(this.cell10);
    this.cell8.setRight(new MtCell());
    this.cell8.setTop(this.cell5);
    this.cell8.setLeft(this.cell4);
    t.checkExpect(this.cell8.getNeighbors(),
        new ArrayList<Cell>(Arrays.asList(this.cell4, this.cell5, this.cell10)));

    this.cell9.setBottom(new MtCell());
    this.cell9.setRight(new MtCell());
    this.cell9.setTop(this.cell6);
    this.cell9.setLeft(this.cell5);
    t.checkExpect(this.cell9.getNeighbors(),
        new ArrayList<Cell>(Arrays.asList(this.cell5, this.cell6)));

    this.cell10.setBottom(new MtCell());
    this.cell10.setRight(new MtCell());
    this.cell10.setTop(this.cell8);
    this.cell10.setLeft(this.cell6);
    t.checkExpect(this.cell10.getNeighbors(),
        new ArrayList<Cell>(Arrays.asList(this.cell6, this.cell8)));
  }

  // test isEmptyCell method
  void testIsEmptyCell(Tester t) {
    // examples are just for testing purposes and are not accurate to the game board layout

    // initialize cell examples
    this.initCellData();

    // test isEmptyCell method
    t.checkExpect(this.cell1.isEmptyCell(), false);
    t.checkExpect(this.cell2.isEmptyCell(), false);
    t.checkExpect(this.cell3.isEmptyCell(), false);
    t.checkExpect(this.cell4.isEmptyCell(), false);
    t.checkExpect(this.cell5.isEmptyCell(), false);
    t.checkExpect(this.cell6.isEmptyCell(), false);
    t.checkExpect(this.cell7.isEmptyCell(), false);
    t.checkExpect(this.cell8.isEmptyCell(), false);
    t.checkExpect(this.cell9.isEmptyCell(), false);
    t.checkExpect(this.cell10.isEmptyCell(), false);
    t.checkExpect(new MtCell().isEmptyCell(), true);
  }

  // test the onTick method
  void testOnTick(Tester t) {
    // initialize world examples
    this.initWorldData();

    // world 1
    this.world1.clickedColor = Color.CYAN;
    this.world1.onTick();
    t.checkExpect(this.world1.board.get(0).color, Color.CYAN);

    // world 2
    this.world2.clickedColor = Color.GREEN;
    this.world2.onTick();
    t.checkExpect(this.world2.board.get(0).color, Color.GREEN);


    // world 3
    this.world3.clickedColor = Color.RED;
    this.world3.onTick();
    t.checkExpect(this.world3.board.get(0).color, Color.RED);

    // world 4
    this.world4.clickedColor = Color.BLUE;
    this.world4.onTick();
    t.checkExpect(this.world4.board.get(0).color, Color.BLUE);

    // world 5
    this.world5.clickedColor = Color.YELLOW;
    this.world5.onTick();
    t.checkExpect(this.world5.board.get(0).color, Color.YELLOW);

    // world 6
    this.world6.clickedColor = Color.MAGENTA;
    this.world6.onTick();
    t.checkExpect(this.world6.board.get(0).color, Color.MAGENTA);

    // world 7
    this.world7.clickedColor = Color.ORANGE;
    this.world7.onTick();
    t.checkExpect(this.world7.board.get(0).color, Color.ORANGE);
  }
}
