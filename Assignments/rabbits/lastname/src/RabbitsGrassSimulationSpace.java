package Assignments.rabbits.lastname.src;


import uchicago.src.sim.space.Object2DGrid;

/**
 * Class that implements the simulation space of the rabbits grass simulation.
 * 
 * Note that this class was highly inspired by the Agent-Based Modelling tutorial
 * by John T. Murphy from the University of Arizona
 *  
 */

public class RabbitsGrassSimulationSpace {
	private Object2DGrid grassSpace;
	private Object2DGrid agentSpace;
	
	public RabbitsGrassSimulationSpace(int xSize, int ySize) {
	    grassSpace = new Object2DGrid(xSize, ySize);
	    agentSpace = new Object2DGrid(xSize, ySize);

	    for(int i = 0; i < xSize; i++){
	      for(int j = 0; j < ySize; j++){
	        grassSpace.putObjectAt(i,j,new Integer(0));
	      }
	    }	
	    
	}
	
	  /**
	   * Randomly distributes grass around
	   * the landscape
	   * @param grass the (total) amount of grass to be distributed
	   */
	  public void growGrass(int grass, int energyGain){
	   
	    for(int i = 0; i < grass; i++){

	      int x = (int)(Math.random()*(grassSpace.getSizeX()));
	      int y = (int)(Math.random()*(grassSpace.getSizeY()));;

	      int currentValue = getGrassAt(x, y);
	      grassSpace.putObjectAt(x,y,new Integer(currentValue + energyGain));
	    }
	  }
	  
	  public RabbitsGrassSimulationAgent getRabbitAt(int x, int y){
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
	  public Object2DGrid getCurrentGrassSpace(){
	    return grassSpace;
	  }

	  /**
	   * Get the 'agent space' object
	   * @return the Object2DGrid object in which agents are stored
	   */
	  public Object2DGrid getCurrentAgentSpace(){
	    return agentSpace;
	  }
	  
	  /**
	   * Get the amount of grass currently stored at
	   * the cell location specified
	   * @param x X coordinate of the desired cell
	   * @param y Y coordinate of the desired cell
	   * @return amount of money stored at cell X,Y
	   */
	  public int getGrassAt(int x, int y){
	    int i;
	    if(grassSpace.getObjectAt(x,y)!= null){
	      i = ((Integer)grassSpace.getObjectAt(x,y)).intValue();
	    }
	    else{
	      i = 0;
	    }
	    return i;
	  }
	  
	  public int eatGrassAt(int x, int y){
		    int grass = getGrassAt(x, y);
		    grassSpace.putObjectAt(x, y, new Integer(0));
		    return grass;
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
	  public boolean moveRabbitAt(int x, int y, int newX, int newY){
	    boolean retVal = false;
	    if(!IsCellOccupied(newX, newY)){
	      RabbitsGrassSimulationAgent rga = (RabbitsGrassSimulationAgent)agentSpace.getObjectAt(x, y);
	      removeRabbitAt(x,y);
	      rga.setXY(newX, newY);
	      agentSpace.putObjectAt(newX, newY, rga);
	      retVal = true;
	    }
	    return retVal;
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
	        agent.setRabbitsGrassSimulationSpace(this);
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
	  public void removeRabbitAt(int x, int y){
	    agentSpace.putObjectAt(x, y, null);
	  }
	  
	  public int getTotalGrass(){
		    int totalMoney = 0;
		    for(int i = 0; i < agentSpace.getSizeX(); i++){
		      for(int j = 0; j < agentSpace.getSizeY(); j++){
		        totalMoney += getGrassAt(i,j);
		      }
		    }
		    return totalMoney;
		  }
	  

}
