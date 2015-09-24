// CarryDropSpace
package demo;

import uchicago.src.sim.space.Object2DGrid; 

/**
 * Space object for the CarryDropModel
 * 
 * Includes functions for placing agents into the space,
 * determining the amount of money at a given cell
 * or in the entire space, moving agents, removing agents,
 * etc.
 * 
 * @author John T. Murphy<br>
 * University of Arizona, Department of Anthropology<br>
 * Arizona State University, Center for Environmental Studies
 */
public class CarryDropSpace {
private Object2DGrid moneySpace;
private Object2DGrid agentSpace;

  /**
   * Constructor that takes as arguments to x,y dimensions
   * of the space to be created
   * @param xSize size of X dimension
   * @param ySize size of Y dimension
   */
  public CarryDropSpace(int xSize, int ySize){
    moneySpace = new Object2DGrid(xSize, ySize);
    agentSpace = new Object2DGrid(xSize, ySize);

    for(int i = 0; i < xSize; i++){
      for(int j = 0; j < ySize; j++){
        moneySpace.putObjectAt(i,j,new Integer(0));
      }
    }
  }

  /**
   * Randomly distributes an amount of money around
   * the landscape
   * @param money the (total) amount of money to be distributed
   */
  public void spreadMoney(int money){
    // Randomly place money in moneySpace
    for(int i = 0; i < money; i++){

      // Choose coordinates
      int x = (int)(Math.random()*(moneySpace.getSizeX()));
      int y = (int)(Math.random()*(moneySpace.getSizeY()));;

      // Get the value of the object at those coordinates
      int currentValue = getMoneyAt(x, y);
      // Replace the Integer object with another one with the new value
      moneySpace.putObjectAt(x,y,new Integer(currentValue + 1));
    }
  }

  /**
   * Get the amount of money currently stored at
   * the cell location specified
   * @param x X coordinate of the desired cell
   * @param y Y coordinate of the desired cell
   * @return amount of money stored at cell X,Y
   */
  public int getMoneyAt(int x, int y){
    int i;
    if(moneySpace.getObjectAt(x,y)!= null){
      i = ((Integer)moneySpace.getObjectAt(x,y)).intValue();
    }
    else{
      i = 0;
    }
    return i;
  }

  /**
   * Return a pointer to the agent at position x,y.
   * If no agent is found, returns null.
   * @param x X coordinate of the desired cell
   * @param y Y coordinate of the desired cell
   * @return a pointer to the agent at X,Y, if any.
   */
  public RabbitsGrassSimulationAgent getAgentAt(int x, int y){
    RabbitsGrassSimulationAgent retVal = null;
    if(agentSpace.getObjectAt(x, y) != null){
      retVal = (RabbitsGrassSimulationAgent)agentSpace.getObjectAt(x,y);
    }
    return retVal;
  }

  /**
   * Get the 'money space' object
   * @return the Object2DGrid object in which money is stored
   */
  public Object2DGrid getCurrentMoneySpace(){
    return moneySpace;
  }

  /**
   * Get the 'agent space' object
   * @return the Object2DGrid object in which agents are stored
   */
  public Object2DGrid getCurrentAgentSpace(){
    return agentSpace;
  }

  /**
   * Determine if a given cell is occupied
   * @param x X coordinate of the desired cell
   * @param y Y coordinate of the desired cell
   * @return True if there is an agent at X,Y, false otherwise
   */
  public boolean IsCellOccupied(int x, int y){
    boolean retVal = false;
    if(agentSpace.getObjectAt(x, y)!=null) retVal = true;
    return retVal;
  }

  /**
   * Add an agent to this space.
   * Will place the agent in an unoccupied space.
   * Note that this will attempt to place the agent
   * randomly, making 10*XSize*YYSize tries before
   * giving up.
   * @param agent The agent to be placed
   * @return True if the agent was successfully placed,
   * false if not
   */
  public boolean addAgent(RabbitsGrassSimulationAgent agent){
    boolean retVal = false;
    int count = 0;
    int countLimit = 10 * agentSpace.getSizeX() * agentSpace.getSizeY();

    while((retVal==false) && (count < countLimit)){
      int x = (int)(Math.random()*(agentSpace.getSizeX()));
      int y = (int)(Math.random()*(agentSpace.getSizeY()));
      if(IsCellOccupied(x,y) == false){
        agentSpace.putObjectAt(x,y,agent);
        agent.setXY(x,y);
        agent.setCarryDropSpace(this);
        retVal = true;
      }
    }

    return retVal;
  }

  /**
   * Removes the agent from the specified location.
   * @param x the X coordinate of the cell from which the agent is to be removed
   * @param y the Y coordinate of the cell from which the agent is to be removed
   */
  public void removeAgentAt(int x, int y){
    agentSpace.putObjectAt(x, y, null);
  }

  /**
   * Removes the money found at the specified location
   * @param x the X coordinate of the cell from which the money is to be removed
   * @param y the Y coordinate of the cell from which the money is to be removed
   * @return the amount of money collected from the cell
   */
  public int takeMoneyAt(int x, int y){
    int money = getMoneyAt(x, y);
    moneySpace.putObjectAt(x, y, new Integer(0));
    return money;
  }

  /**
   * Moves an agent from one location to another.
   * Note that this will not fail if there is no
   * agent at the original location; it will only
   * fail if there is already an agent at the new location.
   * (If there is no agent at the original location,
   * all that happens is that nulls get moved around).
   * @param x the X coordinate of the original location
   * @param y the Y coordinate of the original location
   * @param newX the X coordinate of the destination location
   * @param newY the Y coordinate of the destination location
   * @return true if the move was successful, false otherwise
   */
  public boolean moveAgentAt(int x, int y, int newX, int newY){
    boolean retVal = false;
    if(!IsCellOccupied(newX, newY)){
      RabbitsGrassSimulationAgent cda = (RabbitsGrassSimulationAgent)agentSpace.getObjectAt(x, y);
      removeAgentAt(x,y);
      cda.setXY(newX, newY);
      agentSpace.putObjectAt(newX, newY, cda);
      retVal = true;
    }
    return retVal;
  }

  /**
   * Return the total money found in the landscape
   * @return total money found in the landscape
   */
  public int getTotalMoney(){
    int totalMoney = 0;
    for(int i = 0; i < agentSpace.getSizeX(); i++){
      for(int j = 0; j < agentSpace.getSizeY(); j++){
        totalMoney += getMoneyAt(i,j);
      }
    }
    return totalMoney;
  }
}