package Assignments.rabbits.lastname.src;

import java.awt.Color;

import uchicago.src.sim.gui.Drawable;
import uchicago.src.sim.gui.SimGraphics;
import uchicago.src.sim.space.Object2DGrid;

/**
 * Class that implements the simulation agent for the rabbits grass simulation.
 * Note that this class was highly inspired by the Agent-Based Modelling tutorial
 * by John T. Murphy from the University of Arizona
 */
public class RabbitsGrassSimulationAgent implements Drawable {

    private int x;
    private int y;
    private int vX;
    private int vY;
    private int energy;
    private int moveLoss;

    private static int IDNumber = 0;
    private int ID;
    private RabbitsGrassSimulationSpace rgSpace;

    /**
     * Constructor that takes the initial energy level
     * @param initEnergy
     */
    public RabbitsGrassSimulationAgent(int initEnergy, int moveLoss) {
        x = -1;
        y = -1;
        energy = initEnergy;
        this.moveLoss = moveLoss;
        setVxVy();
        IDNumber++;
        ID = IDNumber;
    }

    /**
     * Draw this agent to the RePast graphics
     * object.
     * @param G the graphics object to which this agent
     * will be drawn
     */
    public void draw(SimGraphics G) {
        if (energy > 1) {
            G.drawFastRoundRect(Color.white);
        } else {
            G.drawFastRoundRect(Color.blue);
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
    public void setXY(int newX, int newY) {
        x = newX;
        y = newY;
    }

    /**
     * Get this agent's X position
     * @return the agent's X position
     */
    public int getX() {
        return x;
    }

    /**
     * Get this agent's Y position
     * @return the agent's Y position
     */
    public int getY() {
        return y;
    }

    /**
     * Set this agent's pointer to the space object
     * in which it resides.
     * @param rgs The space object into which the agent is
     * being placed
     */
    public void setRabbitsGrassSimulationSpace(RabbitsGrassSimulationSpace rgs) {
        rgSpace = rgs;
    }


    /**
     * A basic 'step' for this agent- the actions it
     * takes when it is the agent's 'turn' in the simulation
     */
    public void step() {
        int newX = x + vX;
        int newY = y + vY;

        // the grid is a torus 
        Object2DGrid grid = rgSpace.getCurrentAgentSpace();
        newX = (newX + grid.getSizeX()) % grid.getSizeX();
        newY = (newY + grid.getSizeY()) % grid.getSizeY();
        //    System.out.println("energy before = "+energy);
        if(tryMove(newX, newY)) {
            energy += rgSpace.eatGrassAt(x, y);
        }
        
        setVxVy();
        energy -= moveLoss ;
        //System.out.println("energy after = "+energy);
    }

    /**
     * Set this agent's velocity in the X and Y directon
     * Actually chooses a new velocity randomly; velocity
     * will be one of the 8 possible variations where
     * X and Y are -1, 0, or 1 and Y but both are not zero 
     */
    private void setVxVy() {
        vX = 0;
        vY = 0;
        
        if (Math.random() < 0.5) {
        	while (vX == 0) {
        		vX = (int) Math.floor(Math.random() * 3) - 1;
        	}
        } else {
        	while (vY == 0) {
        		vY = (int) Math.floor(Math.random() * 3) - 1;
        	}
        }
    }

    /**
     * Attempt a move to a new location.
     * @param newX the intended destination's X coordinate
     * @param newY the intended destination's Y coordinate
     * @return true if the move was successfully completed,
     * false otherwise
     */
    private boolean tryMove(int newX, int newY) {
        return rgSpace.moveRabbitAt(x, y, newX, newY);
    }

    /**
     * Prints a report on this agent's status variables to
     * the System output
     */
    public void report() {
        System.out.println(getID() + 
                " at " + 
                x + ", " + y + 
                " has " + 
                getEnergy() + " unit of energy." ); //+ 
        //        " and " + 
        //        getStepsToLive() + " steps to live.");
    }

    public String getID() {
        return "A-" + ID;
    }

    public int getEnergy() {
        return energy;
    }

    public void setEnergy(int newEnergy) {
        this.energy = newEnergy;
    }

    //      /**
    //       * Get the number of steps this rabbit has remaining
    //       * in its 'stepsToLive' variable.
    //       * @return the number of steps until this rabbit dies
    //       */
    //      public int getStepsToLive(){
    //        return stepsToLive;
    //      }
}