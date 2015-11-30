package template;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import logist.agent.Agent;
import logist.behavior.AuctionBehavior;
import logist.plan.Plan;
import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.task.TaskSet;
import logist.topology.Topology;
import logist.topology.Topology.City;

/**
 * An agent that uses the previous bidding result make a 
 * new bid (predict adversary lowest bids by averaging over past prices)
 *
 */
public class PercievedPriceAgent implements AuctionBehavior{

	private final static long TIMEOUT_BID = logist.LogistPlatform.getSettings().get(logist.LogistSettings.TimeoutKey.BID);
	private final static long TIMEOUT_PLAN = logist.LogistPlatform.getSettings().get(logist.LogistSettings.TimeoutKey.PLAN);
	private final static double MIN_BID =1.0;
	private Topology topology;
	private TaskDistribution distribution;
	private Agent agent;
	private Random random;
	private Vehicle vehicle;
	private City currentCity;
	
	private double mCurrentCost = 0.0;
	private double mNewCost = 0.0;
	private long mReward = 0;
//	private SLS mSolver;
	private List<Vehicle> mVehicles;
	private Task[] mTasks;
	private ArrayList<Task> mLostTasks = new ArrayList<Task>();
	private Task[] mTasksWithNewTask;
	private int round = 0;
	
	private double d =1.0; // average over past lost bids of of b_i/b, where b is our bid and b_i is the bid of the winner
	private double dWin = 1.0; // ratio b_i/b for the last win
	private int mCountLoss = 0;
	private boolean mWonLast = true;
	private double mPredictV = MIN_BID;
//	private SLS mPredictSol;
	private double mPredictCurrentCost =0.0;
	private double mPredictNewCost=0.0;

	@Override
	public void setup(Topology topology, TaskDistribution distribution,
			Agent agent) {

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
		int advId = Math.abs(agent.id()-1);
		d = (mCountLoss * d)+(bids[advId]/mPredictV);
		mCountLoss ++;
		d /= mCountLoss;
		
		if (winner == agent.id()) {
			mWonLast = true;
			System.out.println("Smart agent (" + agent.id() + ") wins!");
			System.out.println("New cost of Smart agent (" + agent.id() + ") is " + mNewCost);
			mTasks = copy(mTasksWithNewTask);  // Add the win task to our tasks.
			mCurrentCost = mNewCost;  // Update the cost of our plan.
			mReward += bids[winner];  // Add the task reward.
			
			double looser = Double.POSITIVE_INFINITY;
			for(int i =0; i<bids.length;i++){
				if (i!=winner && looser>bids[i]){
					looser = bids[i];
				}
			}
			
			dWin = looser/bids[winner] ; // the ration btw us and the closest winner
			
		} else {
			mWonLast = false;
			mLostTasks.add(previous);
			System.out.println("Smart agent (" + agent.id() + ") loses!");
			System.out.println("Current cost of Smart agent (" + agent.id() + ") is " + mCurrentCost);
			mPredictCurrentCost = mPredictNewCost; 
			
			
		}
		System.out.println("Smart agent (" + agent.id() + ") has " + mTasks.length + " tasks!");
		System.out.println("Smart agent (" + agent.id() + ") current reward : " + mReward);
	}
	
	@Override
	public Long askPrice(Task task) {
		long time_start = System.currentTimeMillis();
		round++;
		double bid = 0.0;
	
		System.out.println("-- AskPrice --");
		mTasksWithNewTask = new Task[mTasks.length+1];
		for (int i = 0; i < mTasks.length; i++) {
			mTasksWithNewTask[i] = mTasks[i];
		}
		mTasksWithNewTask[mTasks.length] = task;

		SLS mSolver = new SLS(mVehicles, mTasksWithNewTask);
		mSolver.stochLocalSearch((long) (0.05 * (double) TIMEOUT_PLAN));
		mNewCost = mSolver.getCost();
		System.out.println("Smart agent (" + agent.id() + ") has found a route with cost " + mNewCost);

		double v = mNewCost - mCurrentCost;
		
		
		// predicting what the adversary should bid if it is truthful
		Task[] lost = new Task[mLostTasks.size()+1];
		
		for (int i =0; i<mLostTasks.size();i++){
			lost[i] = mLostTasks.get(i);
		}
		lost[mLostTasks.size()] = task;

		SLS mPredictSol = new SLS(mVehicles, lost);
		mPredictSol.stochLocalSearch((long)(0.02* (double) TIMEOUT_PLAN));
		mPredictNewCost = mPredictSol.getCost();

		mPredictV = mPredictNewCost - mPredictCurrentCost;
		
		if (mPredictV<0.0){
			mPredictV = MIN_BID;
		}
		
		if (v<0.0){
			v = MIN_BID;
		}
		
		// if we won last time, check if we can afford to bid a little more than the valuation price
		if (mWonLast){
			bid = Math.max( mPredictV*d*0.9, v); // highest between 90% the predition of the adversary and our valuation
		} else {
			
			if (mReward >= (v- mPredictV*d*0.95) || round <= 3){ // if we have enough reward to cover for the risk or if still at the beginning of the game
			bid = mPredictV*d*0.95; // if we lost, we bid 95% of the predicted best bid (which is v*d) if we can afford it
			} else {
				bid = v;
			}
		}
		
	
		
		System.out.println("Smart agent (" + agent.id() + ") bids " + (long) Math.round(bid));
        
        System.out.println("Planing Time = "+-(time_start-System.currentTimeMillis()) +"ms");

		return (long) Math.round(bid);
	}

	@Override
	public List<Plan> plan(List<Vehicle> vehicles, TaskSet tasks) {
		System.out.println("-- Plan of agent (" + agent.id() + ") --");
		System.out.println("Number of tasks of agent (" + agent.id() + ") is " + tasks.size());
		double gain = mReward - mCurrentCost;
		System.out.println("Gain of agent (" + agent.id() + ") is " + gain);
		
		SLS mSolver = new SLS(vehicles, tasks);
        mSolver.stochLocalSearch((long) (0.1 * (double) TIMEOUT_PLAN));
        
        List<Plan> plans = mSolver.generatePlans();
        while (plans.size() < vehicles.size())
			plans.add(Plan.EMPTY);

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
