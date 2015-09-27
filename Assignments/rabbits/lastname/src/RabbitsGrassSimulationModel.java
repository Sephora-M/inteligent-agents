package Assignments.rabbits.lastname.src;
import java.awt.Color;
import java.util.ArrayList;

import uchicago.src.sim.analysis.BinDataSource;
import uchicago.src.sim.analysis.DataSource;
import uchicago.src.sim.analysis.Sequence;
import uchicago.src.sim.engine.BasicAction;
import uchicago.src.sim.engine.Schedule;
import uchicago.src.sim.engine.SimInit;
import uchicago.src.sim.engine.SimModelImpl;
import uchicago.src.sim.event.SliderListener;
import uchicago.src.sim.gui.ColorMap;
import uchicago.src.sim.gui.DisplaySurface;
import uchicago.src.sim.gui.Object2DDisplay;
import uchicago.src.sim.gui.Value2DDisplay;
import uchicago.src.sim.util.SimUtilities;

/**
 * Class that implements the simulation model for the rabbits grass
 * simulation.  This is the first class which needs to be setup in
 * order to run Repast simulation. It manages the entire RePast
 * environment and the simulation.
 * 
 * Note that this class was highly inspired by the Agent-Based Modelling tutorial
 * by John T. Murphy from the University of Arizona
 */
public class RabbitsGrassSimulationModel extends SimModelImpl {		

	// Default Values
	private static final int NUMAGENTS = 100;
	private static final int WORLDXSIZE = 40;
	private static final int WORLDYSIZE = 40;
	private static final int GRASS_GROWTH_RATE = 10;
	private static final int ENERGY_LEVEL_TO_REPRODUCE = 50;
	private static final int INITIAL_ENERGY_LEVE= 10;
	private static final int MOVE_ENERGY_LOSS= 1;
	private static final int REPRODUCTION_ENERGY_LOSS = 10;
	private static final int ENERGY_GAIN = 2;

	private int numAgents = NUMAGENTS;
	private int worldXSize = WORLDXSIZE;
	private int worldYSize = WORLDYSIZE;
	private int GrassGrowthRate = GRASS_GROWTH_RATE;
	private int reproductionEnergy = ENERGY_LEVEL_TO_REPRODUCE;
	private int initEnergy = INITIAL_ENERGY_LEVE;
	private int moveLoss = MOVE_ENERGY_LOSS;
	private int reproductionLoss = REPRODUCTION_ENERGY_LOSS;
	private int energyGain = ENERGY_GAIN;

	private Schedule schedule;

	private RabbitsGrassSimulationSpace rgSpace;

	private ArrayList<RabbitsGrassSimulationAgent> agentList;

	private DisplaySurface displaySurf;


	//	  private OpenSequenceGraph amountOfMoneyInSpace;
	//	  private OpenHistogram agentWealthDistribution;

	class grassInSpace implements DataSource, Sequence {

		public Object execute() {
			return new Double(getSValue());
		}

		public double getSValue() {
			return (double)rgSpace.getTotalGrass();
		}
	}

	class agentEnergy implements BinDataSource{
		public double getBinValue(Object o) {
			RabbitsGrassSimulationAgent rga = (RabbitsGrassSimulationAgent)o;
			return (double)rga.getEnergy();
		}
	}

	/**
	 * Get a String that serves as the name of the model
	 * @return the name of the model
	 */
	public String getName(){
		return "Rabbits Grass Simulation";
	}

	/**
	 * Tear down any existing pieces of the model and
	 * prepare for a new run.
	 */
	public void setup(){
		System.out.println("Running setup");
		rgSpace = null;
		agentList = new ArrayList<RabbitsGrassSimulationAgent>();
		schedule = new Schedule(1);

		// Tear down Displays
		if (displaySurf != null){
			displaySurf.dispose();
		}
		displaySurf = null;

		//	    if (amountOfMoneyInSpace != null){
		//	      amountOfMoneyInSpace.dispose();
		//	    }
		//	    amountOfMoneyInSpace = null;
		//
		//	    if (agentWealthDistribution != null){
		//	      agentWealthDistribution.dispose();
		//	    }
		//	    agentWealthDistribution = null;
		//
		// Create Displays
		displaySurf = new DisplaySurface(this, "Rabbit Grass Simulation Model Window 1");
		//	    amountOfMoneyInSpace = new OpenSequenceGraph("Amount Of Money In Space",this);
		//	    agentWealthDistribution = new OpenHistogram("Agent Wealth", 8, 0);
		//
		// Register Displays
		registerDisplaySurface("Rabbit Grass Simulation Model Window 1", displaySurf);
		//	    this.registerMediaProducer("Plot", amountOfMoneyInSpace);

		modelManipulator.init();

		// Add a slider for the growth rate.
		modelManipulator.addSlider("GrassGrowthRate", 0, 100, 10,
				new SliderListener() {
			@Override
			public void execute() {
				GrassGrowthRate = value;
			}
		});

		// Add a slider for the birth threshold.
		modelManipulator.addSlider("Birth threshold", 0, 100, 10,
				new SliderListener() {
			@Override
			public void execute() {
				reproductionEnergy = value;
			}
		});
	}

	/**
	 * Initialize the model by building the separate elements that make
	 * up the model
	 */
	public void begin(){
		buildModel();
		buildSchedule();
		buildDisplay();

		displaySurf.display();
		//	    amountOfMoneyInSpace.display();
		//	    agentWealthDistribution.display();
	}

	/**
	 * Initialize the basic model by creating the space
	 * and populating it with money and agents.
	 */
	public void buildModel(){
		System.out.println("Running BuildModel");
		rgSpace = new RabbitsGrassSimulationSpace(worldXSize, worldYSize);
		rgSpace.growGrass(GrassGrowthRate, energyGain);

		for(int i = 0; i < numAgents; i++){
			addNewAgent();
		}
		for(int i = 0; i < agentList.size(); i++){
			RabbitsGrassSimulationAgent cda = (RabbitsGrassSimulationAgent)agentList.get(i);
			cda.report();
		}
	}

	/**
	 * Create the schedule object(s) that will be executed
	 * during the running of the model
	 */
	public void buildSchedule(){
		System.out.println("Running BuildSchedule");

		class RabbitsGrassSimulationStep extends BasicAction {
			public void execute() {
				SimUtilities.shuffle(agentList);
				for(int i =0; i < agentList.size(); i++){
					RabbitsGrassSimulationAgent rga = (RabbitsGrassSimulationAgent)agentList.get(i);
					rga.step();
				}
				reapDeadAgents();
				reproduceAgents();
				rgSpace.growGrass(GrassGrowthRate, energyGain); // grow grass at each time steps according to the growth rate

				displaySurf.updateDisplay();       }
		}

		schedule.scheduleActionBeginning(0, new RabbitsGrassSimulationStep());

		class RabbitsGrassSimulationCountLiving extends BasicAction {
			public void execute(){
				countLivingAgents();
			}
		}

		schedule.scheduleActionAtInterval(10, new RabbitsGrassSimulationCountLiving());

		//	    class RabbitsGrassSimulationUpdateGrassInSpace extends BasicAction {
		//	      public void execute(){
		//	        amountOfMoneyInSpace.step();
		//	      }
		//	    }
		//
		//	    schedule.scheduleActionAtInterval(10, new RabbitsGrassSimulationUpdateGrassInSpace());
		//
		//	    class RabbitsGrassSimulationUpdateAgentEnergy extends BasicAction {
		//	      public void execute(){
		//	        agentWealthDistribution.step();
		//	      }
		//	    }
		//
		//	    schedule.scheduleActionAtInterval(10, new RabbitsGrassSimulationUpdateAgentEnergy());
	}

	/**
	 * Build the display elements for this model.
	 */
	public void buildDisplay(){
		System.out.println("Running BuildDisplay");

		ColorMap map = new ColorMap();

		for(int i = 1; i<16; i++){
			map.mapColor(i, Color.green);
		}
		map.mapColor(0, Color.darkGray);

		Value2DDisplay displayMoney = 
				new Value2DDisplay(rgSpace.getCurrentGrassSpace(), map);

		Object2DDisplay displayAgents = new Object2DDisplay(rgSpace.getCurrentAgentSpace());
		displayAgents.setObjectList(agentList);

		displaySurf.addDisplayableProbeable(displayMoney, "Grass");
		displaySurf.addDisplayableProbeable(displayAgents, "Agents");

		//	    amountOfMoneyInSpace.addSequence("Money In Space", new grassInSpace());
		//	    agentWealthDistribution.createHistogramItem("Agent Wealth",agentList,new agentMoney());

	}

	/**
	 * Add a new agent to this model's agent list and agent space
	 */
	private void addNewAgent(){
		RabbitsGrassSimulationAgent a = new RabbitsGrassSimulationAgent(initEnergy, moveLoss);
		agentList.add(a);
		rgSpace.addAgent(a);
	}

	/**
	 * Collect any dead rabbits from the simulation
	 * @return a count of the agents that died
	 */
	private int reapDeadAgents(){
		int count = 0;
		for(int i = (agentList.size() - 1); i >= 0 ; i--){
			RabbitsGrassSimulationAgent rga = (RabbitsGrassSimulationAgent)agentList.get(i);
			if(rga.getEnergy() < 1){
				rgSpace.removeRabbitAt(rga.getX(), rga.getY());
				agentList.remove(i);
				count++;
			}
		}
		return count;
	}

	/**
	 * Make agents which have enough energy reproduce
	 * @return a count of the agents that reproduced
	 */
	private int reproduceAgents(){
		int count = 0;
		for(int i = (agentList.size() - 1); i >= 0 ; i--){
			RabbitsGrassSimulationAgent rga = (RabbitsGrassSimulationAgent)agentList.get(i);
			if(rga.getEnergy() >= reproductionEnergy){
				rga.setEnergy(rga.getEnergy()-reproductionLoss);
				addNewAgent();
				count++;
			}
		}
		return count;
	}

	/**
	 * Get a count of the living agents on the model's agent list.
	 * @return count of the living agents on the agent list
	 */
	private int countLivingAgents(){
		int livingAgents = 0;
		for(int i = 0; i < agentList.size(); i++){
			RabbitsGrassSimulationAgent rga = (RabbitsGrassSimulationAgent)agentList.get(i);
			if(rga.getEnergy() > 0) livingAgents++;
		}
		System.out.println("Number of living agents is: " + livingAgents);

		return livingAgents;
	}

	/**
	 * Returns the Schedule object for this model; for use
	 * internally by RePast
	 * @return the Schedule object for this model
	 */
	public Schedule getSchedule(){
		return schedule;
	}

	/**
	 * Get the string array that lists the initialization parameters
	 * for this model
	 * @return a String array that includes the names of all variables
	 * that can be modified by the RePast user interface
	 */
	public String[] getInitParam(){
		String[] initParams = { "NumAgents"};
		return initParams;
	}

	/**
	 * Get the parameter indicating the number of agents in this model
	 * @return the number of agents in the model
	 */
	public int getNumAgents(){
		return numAgents;
	}

	/**
	 * Set the parameter indicating the initial number of agents for this
	 * model.
	 * @param na new value for initial number of agents.
	 */
	public void setNumAgents(int na){
		numAgents = na;
	}

	/**
	 * Get the width of the space in the model
	 * @return the width of the space object in the model (X-dimension)
	 */
	public int getWorldXSize(){
		return worldXSize;
	}

	/**
	 * Set the parameter initializing the width of the space
	 * object in this model
	 * @param wxs the new size of the X-dimension of the model.
	 */
	public void setWorldXSize(int wxs){
		worldXSize = wxs;
	}

	/**
	 * Get the heighth of the space in the model
	 * @return the heighth of the space object in the model (Y-dimension)
	 */
	public int getWorldYSize(){
		return worldYSize;
	}

	/**
	 * Set the parameter initializing the heighth of the space
	 * object in this model
	 * @param wys the new size of the Y-dimension of the model.
	 */
	public void setWorldYSize(int wys){
		worldYSize = wys;
	}

	public int getBirthThreshold() {
		return reproductionEnergy;
	}

	public void setBirthThreshold(int reproductionEnergy) {
		this.reproductionEnergy = reproductionEnergy;
	}

	/**
	 * Get the value of the parameter initializing the total amount
	 * of money in this model
	 * @return the initial value for the total amount of money in the
	 * model
	 */
	public int getGrassGrowthRate() {
		return GrassGrowthRate;
	}

	public void setGrassGrowthRate(int growthRate) {
		this.GrassGrowthRate = growthRate;
	}

	/**
	 * Set the new value for the total amount of money to be used when
	 * initializing the simulation
	 * @param i the new value for the total amount of money
	 */
	public void setGrowthRate(int i) {
		GrassGrowthRate = i;
	}


	/**
	 * Main method for this model object; this runs the model.
	 * @param args Any string arguments to be passed to this model (currently none)
	 */
	public static void main(String[] args) {
		System.out.println("Rabbit skeleton");
		SimInit init = new SimInit();
		RabbitsGrassSimulationModel model = new RabbitsGrassSimulationModel();
		init.loadModel(model, "", false);
	}	
}