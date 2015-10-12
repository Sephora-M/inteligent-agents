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

	private double pPickup;
	private Topology mTopology;
	private TaskDistribution mTaskDistribution;
	private int mCostVehiclePerKm;

	private List<State> mStates;
	private Map<State, Double> mRewardReachedFromState;  // Called 'V' in the course
	private Map<StateActionPair, Double> mRewardMap; // Called 'R' in the course
	private Map<StateActionStatePrime, Double> mTMap; // Called 'T' in the course
	private Map<State, ActionContainer> bestActionForState;

	@Override
	public void setup(Topology topology, TaskDistribution td, Agent agent) {
		Double discount = agent.readProperty("discount-factor", Double.class,
				0.95);
		pPickup = discount;
		mTopology = topology;
		mTaskDistribution = td;
		mCostVehiclePerKm = agent.vehicles().get(0).costPerKm();

		mStates = createAllStates();
		mRewardMap = createRMap();
		mRewardReachedFromState = createVMap();
		mTMap = createTMap();

		reinforcementLearning();
	}

	@Override
	public Action act(Vehicle vehicle, Task availableTask) {
		Action action;

		/*Random random = new Random();
		if (availableTask == null || random.nextDouble() > pPickup) {
			City currentCity = vehicle.getCurrentCity();
			action = new Move(currentCity.randomNeighbor(random));
		} else {
			action = new Pickup(availableTask);
		}*/

		final State s = new State(vehicle.getCurrentCity(), availableTask.deliveryCity);
		final ActionContainer bestAction = bestActionForState.get(s);

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

	// Create the "V" map. This maps a State with the reward that may be reached from this state.
	private Map<State, Double> createVMap() {
		mRewardReachedFromState = new HashMap<State, Double>();
		for (State s : mStates) {
			mRewardReachedFromState.put(s, 0.0);
		}
		return mRewardReachedFromState;
	}

	// Create the "T" map.
	private Map<StateActionStatePrime, Double> createTMap() {
		mTMap = new HashMap<StateActionStatePrime, Double>();
		for (State s : mStates) {
			for (ActionContainer action : possibleActionsFromState(s)) {
				City destinationCity = action.getDestinationCity();
				double p1 = 1.0;
				for (State sPrime : mStates) {
					if (sPrime.getDestinationCity() != null && sPrime.getSourceCity().equals(destinationCity)) {
						final double p = mTaskDistribution.probability(sPrime.getSourceCity(), sPrime.getDestinationCity());
						p1 -= p;
						mTMap.put(new StateActionStatePrime(s, action, sPrime), p);
					} else {
						mTMap.put(new StateActionStatePrime(s, action, sPrime), 0.0);
					}
				}
				mTMap.put(new StateActionStatePrime(s, action, new State(destinationCity, null)), p1);
			}
		}
		return mTMap;
	}
	
	private List<ActionContainer> possibleActionsFromState(State s) {
		ArrayList<ActionContainer> actions = new ArrayList<ActionContainer>();

		// The agent may move to each one of its neighbors.
		City sourceCity = s.getSourceCity();
		for (City to : mTopology.cities()) {
			if (!(to.equals(sourceCity))) {
				if (sourceCity.hasNeighbor(to)) {
					actions.add(new ActionContainer(ActionType.MOVE, to));
				}
			}
		}

		// The agent may also do a PICK_UP action.
		if (s.getDestinationCity() != null) {
			actions.add(new ActionContainer(ActionType.PICKUP, s.getDestinationCity()));
		}

		return actions;
	}

	private void reinforcementLearning() {
		double sumDiffBetweenTwoIterations = 0.0;
		double epsilon = 0.01;

		do {
			for (State s : mStates) {
				for (ActionContainer action : possibleActionsFromState(s)) {
					// TODO Implement the algorithm.
				}
			}
		} while(sumDiffBetweenTwoIterations > epsilon);
	}
}
