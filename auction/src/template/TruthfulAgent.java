package template;

//the list of imports
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import logist.Measures;
import logist.behavior.AuctionBehavior;
import logist.agent.Agent;
import logist.simulation.Vehicle;
import logist.plan.Plan;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.task.TaskSet;
import logist.topology.Topology;
import logist.topology.Topology.City;

/**
 * A very simple auction agent that truthfully bids at the valuation price.
 * 
 */
@SuppressWarnings("unused")
public class TruthfulAgent implements AuctionBehavior {

	private final static long TIMEOUT_BID = logist.LogistPlatform.getSettings().get(logist.LogistSettings.TimeoutKey.BID);
	private final static long TIMEOUT_PLAN = logist.LogistPlatform.getSettings().get(logist.LogistSettings.TimeoutKey.PLAN);
	private final static double MIN_BID =0.0;
	private Topology topology;
	private TaskDistribution distribution;
	private Agent agent;
	private Random random;
	private Vehicle vehicle;
	private City currentCity;
	
	private double mCurrentCost = 0.0;
	private double mNewCost = 0.0;
	private long mReward = 0;
	private SLS mSolver;
	private List<Vehicle> mVehicles;
	private Task[] mTasks;
	private Task[] mTasksWithNewTask;
	
	private double tempCost = 0.0;
	private SLS tempSol = null;

	@Override
	public void setup(Topology topology, TaskDistribution distribution, Agent agent) {
		this.topology = topology;
		this.distribution = distribution;
		this.agent = agent;
		this.vehicle = agent.vehicles().get(0);
		this.currentCity = vehicle.homeCity();

		long seed = -9019554669489983951L * currentCity.hashCode() * agent.id();
		this.random = new Random(seed);
		
		this.mVehicles = agent.vehicles();
		this.mTasks = new Task[0];
		this.mTasksWithNewTask = new Task[0];
	}

	@Override
	public void auctionResult(Task previous, int winner, Long[] bids) {
		System.out.println("-- auction results --");
		if (winner == agent.id()) {
			System.out.println("Truthful agent (" + agent.id() + ") wins!");
			System.out.println("New cost of Truthful agent (" + agent.id() + ") is " + mNewCost);
			mTasks = copy(mTasksWithNewTask);  // Add the win task to our tasks.
			mCurrentCost = mNewCost;  // Update the cost of our plan.
			mReward += bids[winner];  // Add the task reward.
			tempSol = mSolver; // if we won, we want to remember the solution
		} else {
			System.out.println("Truthful agent (" + agent.id() + ") loses!");
			System.out.println("Current cost of Truthful agent (" + agent.id() + ") is " + mCurrentCost);
		}
		System.out.println("Truthful agent (" + agent.id() + ") has " + mTasks.length + " tasks!");
		System.out.println("Truthful agent (" + agent.id() + ") current reward : " + mReward);
	}
	
	@Override
	public Long askPrice(Task task) {
		long time_start = System.currentTimeMillis();
		double bid = 0.0;
	
		System.out.println("-- AskPrice --");
		mTasksWithNewTask = new Task[mTasks.length+1];
		for (int i = 0; i < mTasks.length; i++) {
			mTasksWithNewTask[i] = mTasks[i];
		}
		mTasksWithNewTask[mTasks.length] = task;

		mSolver = new SLS(mVehicles, mTasksWithNewTask);
		mSolver.stochLocalSearch((long) (0.1 * (double) TIMEOUT_PLAN));
		mNewCost = mSolver.getCost();
		System.out.println("Truthful agent (" + agent.id() + ") has found a route with cost " + mNewCost);
		
		double marginalCost = mNewCost - mCurrentCost;
		
<<<<<<< Updated upstream
		if (marginalCost < 0.0) {
=======
		marginalCost *= 1.1;
		
		if (marginalCost<0.0){
>>>>>>> Stashed changes
			marginalCost = MIN_BID;
		}
		bid = marginalCost;
		
		System.out.println("Truthful agent (" + agent.id() + ") bids " + (long) Math.round(bid));
        
        System.out.println("Planing Time = "+-(time_start-System.currentTimeMillis()) +"ms");

		return (long) Math.round(bid);
	}

	@Override
	public List<Plan> plan(List<Vehicle> vehicles, TaskSet tasks) {
		List<Plan> plans;
		
		System.out.println("-- Plan of agent (" + agent.id() + ") --");
		System.out.println("Number of tasks of agent (" + agent.id() + ") is " + tasks.size());
		double gain = mReward - mCurrentCost;
		System.out.println("Gain of agent (" + agent.id() + ") is " + gain);
		
		mSolver = new SLS(vehicles, tasks);
        mSolver.stochLocalSearch((long) (0.3 * (double) TIMEOUT_PLAN));
        
        if (tempSol != null) {
        	System.out.println("Current cost of agent (" + agent.id() + ") is " + tempSol.getCost());
        	System.out.println("number of tasks = " + tempSol.getNumberOfTasks());
        }
        System.out.println("Recomputed final cost of agent (" + agent.id() + ") is " + mSolver.getCost());
        System.out.println("number of tasks = " + mSolver.getNumberOfTasks());
        
        if (tempSol != null) {
        	System.out.println(tempSol.generatePlans());
        }
        
        System.out.println(mSolver.generatePlans());

<<<<<<< Updated upstream
        if (tempSol != null && mSolver.getCost() > tempSol.getCost()){
        	plans = tempSol.generatePlans();
        } else {
=======
//        if (tempSol != null && mSolver.getCost() > tempSol.getCost()){
//        	plans = tempSol.generatePlans();
//        }
//        else {
>>>>>>> Stashed changes
        	plans = mSolver.generatePlans();
//        }
        
        while (plans.size() < vehicles.size()) {
        	plans.add(Plan.EMPTY);
        }
        
		return plans;
	}
	
	private Task[] copy(Task[] from) {
		Task[] to = new Task[from.length];
		for (int i = 0; i < from.length; i++) {
			to[i] = from[i];
		}
		return to;
	}
}