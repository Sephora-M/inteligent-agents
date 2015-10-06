// CarryDropAgent
package demo;

import java.awt.Color;

import uchicago.src.sim.gui.Drawable;
import uchicago.src.sim.gui.SimGraphics;
import uchicago.src.sim.space.Object2DGrid;

/**
 * Agent for the CarryDrop model.
 * 
 * The agent includes basic internal variables, methods
 * for modifying an agent's position and direction,
 * ID variables, etc.
 * 
 * @author John T. Murphy<br>
 * University of Arizona, Department of Anthropology<br>
 * Arizona State University, Center for Environmental Studies
 */
public class RabbitsGrassSimulationAgent implements Drawable{
  private int x;
  private int y;
  private int vX;
  private int vY;
  private int money;
  private int stepsToLive;
  private static int IDNumber = 0;
  private int ID;
  private CarryDropSpace cdSpace;

  /**
   * Constructor that takes the ranges of permissible life spans
   * @param minLifeSpan Shortest possible life span
   * @param maxLifeSpan Longest possible life span
   */
  public RabbitsGrassSimulationAgent(int minLifeSpan, int maxLifeSpan){
    x = -1;
    y = -1;
    money = 0;
    setVxVy();
    stepsToLive = 
        (int)((Math.random() * (maxLifeSpan - minLifeSpan)) + minLifeSpan);
    IDNumber++;
    ID = IDNumber;
  }

  /**
   * Set this agent's velocity in the X and Y directon
   * Actually chooses a new velocity randomly; velocity
   * will be one of the 8 possible variations where
   * X and Y are -1, 0, or 1 and Y but both are not zero 
   */
  private void setVxVy(){
    vX = 0;
    vY = 0;
    while((vX == 0) && ( vY == 0)){
      vX = (int)Math.floor(Math.random() * 3) - 1;
      vY = (int)Math.floor(Math.random() * 3) - 1;
    }
  }

  /**
   * Set a new X and Y position for the agent.
   * Note that this affects only the agent's internal
   * assessment of its own location, and thus should
   * be called only after the Space object has confirmed
   * that this location is acceptable.
   * @param newX
   * @param newY
   */
  public void setXY(int newX, int newY){
    x = newX;
    y = newY;
  }

  /**
   * Set this agent's pointer to the space object
   * in which it resides.
   * @param cds The space object into which the agent is
   * being placed
   */
  public void setCarryDropSpace(CarryDropSpace cds){
    cdSpace = cds;
  }

  /**
   * Get this agent's internal unique ID
   * @return a String representing the unique ID for this agent;
   * this will be in the form "A-101"
   */
  public String getID(){
    return "A-" + ID;
  }

  /**
   * Get the amount of money held by this agent
   * @return the amount of money this agent has
   */
  public int getMoney(){
    return money;
  }
  
  /**
   * Get the number of steps this agent has remaining
   * in its 'stepsToLive' variable.
   * @return the number of steps until this agent dies
   */
  public int getStepsToLive(){
    return stepsToLive;
  }

  /**
   * Prints a report on this agent's status variables to
   * the System output
   */
  public void report(){
    System.out.println(getID() + 
                       " at " + 
                       x + ", " + y + 
                       " has " + 
                       getMoney() + " dollars" + 
                       " and " + 
                       getStepsToLive() + " steps to live.");
  }

  /**
   * Get this agent's X position
   * @return the agent's X position
   */
  public int getX(){
    return x;
  }

  /**
   * Get this agent's Y position
   * @return the agent's Y position
   */
  public int getY(){
    return y;
  }

  /**
   * Draw this agent to the RePast graphics
   * object.
   * @param G the graphics object to which this agent
   * will be drawn
   */
  public void draw(SimGraphics G){
    if(stepsToLive > 10)
      G.drawFastRoundRect(Color.green);
    else
      G.drawFastRoundRect(Color.blue);
  }

  /**
   * A basic 'step' for this agent- the actions it
   * takes when it is the agent's 'turn' in the simulation
   */
  public void step(){
    int newX = x + vX;
    int newY = y + vY;

    Object2DGrid grid = cdSpace.getCurrentAgentSpace();
    newX = (newX + grid.getSizeX()) % grid.getSizeX();
    newY = (newY + grid.getSizeY()) % grid.getSizeY();

    if(tryMove(newX, newY)){
      money += cdSpace.takeMoneyAt(x, y);
    }
    else{
      RabbitsGrassSimulationAgent cda = cdSpace.getAgentAt(newX, newY);
      if (cda!= null){
        if(money > 0){
          cda.receiveMoney(1);
          money--;
        }
      }
      setVxVy();
    }
    stepsToLive--;
  }

  /**
   * Attempt a move to a new location.
   * @param newX the intended destination's X coordinate
   * @param newY the intended destination's Y coordinate
   * @return true if the move was successfully completed,
   * false otherwise
   */
  private boolean tryMove(int newX, int newY){
    return cdSpace.moveAgentAt(x, y, newX, newY);
  }

  /**
   * Receive an amount of money and put it in the agent's
   * holdings.
   * @param amount the amount of money received
   */
  public void receiveMoney(int amount){
    money += amount;
  }
}