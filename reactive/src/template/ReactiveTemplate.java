package template;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import logist.simulation.Vehicle;
import logist.agent.Agent;
import logist.behavior.ReactiveBehavior;
import logist.plan.Action;
import logist.plan.Action.Move;
import logist.plan.Action.Pickup;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.topology.Topology;
import logist.topology.Topology.City;

public class ReactiveTemplate implements ReactiveBehavior {

	public enum ActionType {PICKUP, MOVE}
	private double gamma;
	private Topology mTopology;
	private TaskDistribution mTaskDistribution;
	private int mCostVehiclePerKm;

	private List<State> mStates;
	private Map<StateActionPair, Double> mRewardMap; // Called 'R' in the course
	private Map<StateActionStatePrime, Double> mTMap; // Called 'T' in the course
	
	@Override
	public void setup(Topology topology, TaskDistribution td, Agent agent) {

		// Reads the discount factor from the agents.xml file.
		// If the property is not present it defaults to 0.95
		Double discount = agent.readProperty("discount-factor", Double.class,
				0.95);

		this.gamma = discount;
		this.mTopology = topology;
		this.mTaskDistribution = td;
		this.mCostVehiclePerKm = agent.vehicles().get(0).costPerKm();
		
		mStates = createAllStates();
		mRewardMap = createRMap();
		mTMap = createTMap();
		reinforcementLearning();
	}

	@Override
	public Action act(Vehicle vehicle, Task availableTask) {
		Action action;

//		if (availableTask == null || random.nextDouble() > pPickup) {
//			City currentCity = vehicle.getCurrentCity();
//			action = new Move(currentCity.randomNeighbor(random));
//		} else {
//			action = new Pickup(availableTask);
//		}
		
		final State s;
		if (availableTask != null)
			s = findState(vehicle.getCurrentCity(), availableTask.deliveryCity);
		else
			s = findState(vehicle.getCurrentCity(), null);
		
		final ActionContainer bestAction = s.getBestAction();

		if (availableTask == null || bestAction.getActionType().equals(ActionType.MOVE)) {
			action = new Move(bestAction.getDestinationCity());
		} else {
			action = new Pickup(availableTask);
		}
		return action;
	}

	private List<State> createAllStates() {
		mStates = new ArrayList<State>();
		for (City source : mTopology.cities()) {
			mStates.add(new State(source, null));
			for (City dest : mTopology.cities()) {
				if (!source.equals(dest)) {
					mStates.add(new State(source, dest));
				}
			}
		}
		return mStates;
	}

	// Create the reward map "R".
	private Map<StateActionPair, Double> createRMap() {
		mRewardMap = new HashMap<StateActionPair, Double>();
		for (State s : mStates) {
			final City sourceCity = s.getSourceCity();
			for (ActionContainer a : possibleActionsFromState(s)) {
				final City destinationCity = a.getDestinationCity();
				double reward;
				if (a.getActionType().equals(ActionType.PICKUP)) {
					reward = mTaskDistribution.reward(sourceCity, destinationCity);
				} else {
					reward = 0;
				}
				reward -= mCostVehiclePerKm * sourceCity.distanceTo(destinationCity);
				mRewardMap.put(new StateActionPair(s, a), reward);
				
			}
		}
		return mRewardMap;
	}

	// Create the "T" map.
	private Map<StateActionStatePrime, Double> createTMap() {
		mTMap = new HashMap<StateActionStatePrime, Double>();
		for (State s : mStates) {
			for (ActionContainer action : possibleActionsFromState(s)) {
				City destinationCity = action.getDestinationCity();
				for (State sPrime : mStates) {
					if (sPrime.getSourceCity().equals(destinationCity)) {
						// note that mTaskDistribution.probability(c, null) return the probability of having no task in city c
						final double p = mTaskDistribution.probability(sPrime.getSourceCity(), sPrime.getDestinationCity());
						sPrime.setTvalue(p);
					} else {
						mTMap.put(new StateActionStatePrime(s, action, sPrime), 0.0);
						sPrime.setTvalue(0.0);
					}
				}
			}
		}
		return mTMap;
	}
	
	
	private List<ActionContainer> possibleActionsFromState(State s) {
		ArrayList<ActionContainer> actions = new ArrayList<ActionContainer>();

		// The agent may move to each one of its neighbors.
		City sourceCity = s.getSourceCity();
		List<City> neighbors = new ArrayList<City>();
		neighbors.addAll(sourceCity.neighbors());
		for(City neighbor: neighbors){
			actions.add(new ActionContainer(ActionType.MOVE, neighbor));
		}

		// The agent may also do a PICK_UP action.
		if (s.getDestinationCity() != null) {
			actions.add(new ActionContainer(ActionType.PICKUP, s.getDestinationCity()));
		}

		return actions;
	}

	private void reinforcementLearning() {
		double sumDiffBetweenTwoIterations = 0.0;
		int maxIter = 10; // i'm using a small fixed number of iteration here 
						  // because I realized that the learning process was quite
						  // fast (converges after ~iteration)
		double epsilon = 0.01;

//		do {
		for (int t=0; t<maxIter;t++){
			// print out to observe the evolution of V(s) and bestAction(S)
//			for (State s : mStates){
//				if (s.getBestAction() != null)
//				System.out.print("[ "+s.getVvalue() +", "+s.getBestAction().getDestinationCity().name+"] ");
//				else
//					System.out.print("[ "+s.getVvalue() +", "+s.getBestAction()+"] ");
//			}
			System.out.println();
			for (State s : mStates) {
				City currentCity = s.getSourceCity();
				List<ActionContainer> actions = possibleActionsFromState(s);
				List<Double> Q = new ArrayList<Double>(actions.size());
				for (int i = 0; i<actions.size(); i++) {
					double Qval;
					double R;
					ActionContainer currentAction = actions.get(i);
					if (currentAction.getActionType().equals(ActionType.PICKUP)) {
						R = mTaskDistribution.reward(currentCity, currentAction.getDestinationCity());
					} else {
						R = 0;
					}
					R -= mCostVehiclePerKm * currentCity.distanceTo(currentAction.getDestinationCity());
					
					double sum = 0.0;
					
					for (State next_s : mStates){
						if (next_s.getSourceCity().id == actions.get(i).getDestinationCity().id) {
							sum += next_s.getTvalue()*next_s.getVvalue();
						}
					}
					
					Qval = R + sum*gamma;
					Q.add(i, Qval);
				}
				
				int Qmax = findMax(Q);
				s.setBestAction(actions.get(Qmax));
				s.setVvalue(Q.get(Qmax));
			}
			
		}
//		} while(sumDiffBetweenTwoIterations > epsilon);
	}
	
	private int findMax(List<Double> Q){
		Double max = Double.NEGATIVE_INFINITY;
		int best = -1;
		for (int i = 0; i<Q.size(); i++) {
			if (Q.get(i) > max){
				max = Q.get(i);
				best = i;
			}	
		}
		return best;
	}
	
	private State findState(City from, City to){
		if (from == null) {
			return null;
		}
		for (State state: mStates) {
			if (state.getSourceCity().id == from.id) {
				if (state.getDestinationCity() == null) {
					if (to == null) {
						return state;
					}
				} else {
					if (state.getDestinationCity().id == to.id) {
						return state;
					}
				}
			} 
		}
		return null;
	}
}