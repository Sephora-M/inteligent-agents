package template;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

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

	private Random random;
	private double pPickup;
	private List<State> states;
	private Topology topology;
	private TaskDistribution mTaskDistribution;
	private Map<State, Double> vRewardsReachedFromState;
	private Map<State, ActionContainer> bestActionForState;
	private Map<StateActionPair<State, ActionContainer>, Double> mRewardMap;

	@Override
	public void setup(Topology topology, TaskDistribution td, Agent agent) {
		// Reads the discount factor from the agents.xml file.
		// If the property is not present it defaults to 0.95
		Double discount = agent.readProperty("discount-factor", Double.class,
				0.95);

		this.random = new Random();
		this.pPickup = discount;
		this.topology = topology;
		mTaskDistribution = td;
		
		init();

		reinforcementLearning();
	}

	@Override
	public Action act(Vehicle vehicle, Task availableTask) {
		Action action;

		/*if (availableTask == null || random.nextDouble() > pPickup) {
			City currentCity = vehicle.getCurrentCity();
			action = new Move(currentCity.randomNeighbor(random));
		} else {
			action = new Pickup(availableTask);
		}*/

		final State s = new State(vehicle.getCurrentCity(), availableTask.deliveryCity);
		final ActionContainer bestAction = bestActionForState.get(s);
		
		if (availableTask == null || bestAction.getAction() instanceof Move) {
			action = new Move(bestAction.getDestinationCity());
		} else {
			action = new Pickup(availableTask);
		}

		return action;
	}

	private void init() {
		// Create a states array
		states = new ArrayList<State>();
		for (City source : topology.cities()) {
			states.add(new State(source, null));
			for (City dest : topology.cities()) {
				if (!source.equals(dest)) {
					states.add(new State(source, dest));
				}
			}
		}

		// Init the "V" map. This maps a State with the reward that may be reached from this state.
		vRewardsReachedFromState = new HashMap<State, Double>();
		for (State s : states) {
			vRewardsReachedFromState.put(s, 0.0);
		}
		
		// Create the reward map
		mRewardMap = new HashMap<StateActionPair<State, ActionContainer>, Double>();
		for (State s : states) {
			for (ActionContainer a : possibleActionsFromState(s)) {
				final double reward;
				if (a.getAction() instanceof Pickup) {
					reward = mTaskDistribution.reward(s.getSourceCity(), s.getDestinationCity());
				} else {
					reward = 0;
				}
				// TODO Remove cost of the travel!
				mRewardMap.put(new StateActionPair<State, ActionContainer>(s, a), reward);
			}
		}
	}

	private ActionContainer lookForBestAction(City currentVehicleCity, City deliveryCity) {
		// TODO(Look up in a precomputed table what is the best action with that "currentCity" and "deliveryCity")
		return null;
	}
	
	private List<ActionContainer> possibleActionsFromState(State s) {
		ArrayList<ActionContainer> actions = new ArrayList<ActionContainer>();
		
		for (City to : topology.cities()) {
			if (!(to.equals(s.getDestinationCity()))) {
				if (s.getSourceCity().hasNeighbor(to)) {
					//actions.add(new ActionContainer(new Move(to), to));
				}
			}
		}
		
		return actions;
	}

	private void reinforcementLearning() {
		double sumDiffBetweenTwoIterations = 0.0;
		double epsilon = 0.01;
		
		do {
			for (State s : states) {
				for (ActionContainer action : possibleActionsFromState(s)) {
					
				}
			}
		} while(sumDiffBetweenTwoIterations > epsilon);
	}
}
