// LE BIS

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
	private double gamma;
	private double pPickup;
	private List<State> states;
//	List<ActionStruct> allPickUpOrRefuseActions;
	private List<City> cities;
	private Topology topology;
	private TaskDistribution td;
	private Map<State, Double> vRewardsReachedFromState;
	private Map<State, ActionStruct> bestActionForState;
	private int costPerKm;

	@Override
	public void setup(Topology topology, TaskDistribution td, Agent agent) {

		// Reads the discount factor from the agents.xml file.
		// If the property is not present it defaults to 0.95
		Double discount = agent.readProperty("discount-factor", Double.class,
				0.95);

		this.random = new Random();
		this.pPickup = discount;
		this.gamma = discount;
		this.cities = topology.cities();
		costPerKm = agent.vehicles().get(0).costPerKm();
		this.td = td;
		
		init();
		reinforcementLearning();
	}

	@Override
	public Action act(Vehicle vehicle, Task availableTask) {
		Action action;

		if (availableTask == null || random.nextDouble() > pPickup) {
			City currentCity = vehicle.getCurrentCity();
			action = new Move(currentCity.randomNeighbor(random));
		} else {
			action = new Pickup(availableTask);
		}
		return action;
	}
////	@Override
//	public void setup(Topology topology, TaskDistribution td, Agent agent) {
//		// Reads the discount factor from the agents.xml file.
//		// If the property is not present it defaults to 0.95
//		Double discount = agent.readProperty("discount-factor", Double.class,
//				0.95);
//
//		this.random = new Random();
//		this.gamma = discount;
//		this.topology = topology;
//		this.cities = topology.cities();
//		this.td = td;
//		costPerKm = agent.vehicles().get(0).costPerKm();
//		init();
//
//		reinforcementLearning();
//	}
//
////	@Override
//	public Action act(Vehicle vehicle, Task availableTask) {
//		Action action;
//
//		if (availableTask == null || random.nextDouble() > gamma) {
//			City currentCity = vehicle.getCurrentCity();
//			action = new Move(currentCity.randomNeighbor(random));
//		} else {
//			action = new Pickup(availableTask);
//		}
//
////		ActionStruct bestAction = lookForBestAction(vehicle.getCurrentCity(), availableTask.deliveryCity);
////		
////		if (availableTask == null || bestAction.getAction() instanceof Move) {
////			action = new Move(bestAction.getDestinationCity());
////		} else {
////			action = new Pickup(availableTask);
////		}
//
//		return action;
//	}

	private void init() {
		// Create a states array
//		allPickUpOrRefuseActions = new ArrayList<ActionStruct>();
		states = new ArrayList<State>();
		for (City source : cities) {
			states.add(new State(source, null));
//			allPickUpOrRefuseActions.add(new ActionStruct(source,true, false));
//			allPickUpOrRefuseActions.add(new ActionStruct(source,false, false));
			for (City dest : cities) {
				if (!source.equals(dest)) {
					states.add(new State(source, dest));
				}
			}
		}
		
		// Create the pickup action array

		// Init the "V" map. This maps a State with the reward that may be reached from this state.
		vRewardsReachedFromState = new HashMap<State, Double>();
		for (State s : states) {
			vRewardsReachedFromState.put(s, 0.0);
		}
	}

	private ActionStruct lookForBestAction(City currentVehicleCity, City deliveryCity) {
		// TODO(Look up in a precomputed table what is the best action with that "currentCity" and "deliveryCity")
		return null;
	}
	
//	private List<ActionStruct> possibleActionsFromState(State s) {
//		ArrayList<ActionStruct> actions = new ArrayList<ActionStruct>();
//		
//		for (City to : topology.cities()) {
//			if (!(to.equals(s.getDestinationCity()))) {
//				if (s.getSourceCity().hasNeighbor(to)) {
//					actions.add(new ActionStruct(new Move(to), to));
//				}
//			}
//		}
//		
//		return actions;
//	}
	
	

	private void reinforcementLearning() {
		double sumDiffBetweenTwoIterations = 0.0;
		double epsilon = 0.01;
		
//		do {
//			for (State s : states) {
//				for (ActionStruct action : possibleActionsFromState(s)) {
//					
//				}
//			}
//		} while(sumDiffBetweenTwoIterations > epsilon);
		
		//state value V[s], here a state is a pair of cities, and a city's ID is its index in the list cities
		//such that V[i][j] = the state value of the state corresponding to the pair of cities (i,j)
		double[][] V = new double[cities.size()][cities.size()];
		double[][] preV = new double[cities.size()][cities.size()];
		
		// initialization of state value vector to zero
		for (int i=0; i< cities.size();i++){
			for (int j=0; j< cities.size();j++){
				V[i][j]=0.0;
			}
			
		}
	
		// value iteration to learn V
		do {
			for(State state : states){
				preV = V.clone();
				City current = state.getSourceCity();
				List<City> neighbors = current.neighbors();
				neighbors.add(state.getSourceCity()); 
//				Map<City,Double> Q = new HashMap<City,Double>();
//				double[] Q = new double[cities.size()];
				
				List<ActionStruct> actions = new ArrayList<ActionStruct>();
				if (!state.getDestinationCity().equals(null)){
					actions.add(new ActionStruct(state.getDestinationCity(),true)); //  pickup action
				}
				
				for(City destination : neighbors){
					actions.add(new ActionStruct(destination,false)); // move to destination
				}
				
				List<Double> Q = new ArrayList<Double>(actions.size());
				for(int i = 0; i<actions.size();i++){
					ActionStruct action = actions.get(i);
					City destination = action.getDestinationCity();
					Double QVal;
					double R;
					double sum = 0.0;
					if (action.getPickup()){
						R = td.reward(current, destination) - current.distanceTo(destination);
					}else {
						R =- current.distanceTo(destination);
					}
					
					for (City next_city : cities){
						if(next_city.id != destination.id){
							sum += td.probability(destination, next_city)*V[destination.id][next_city.id]; // states with a task from destination to any city
						}
					}
					sum += td.probability(destination, null)*V[destination.id][destination.id]; // the state where there's no task at destination
					
					QVal = R + gamma*sum;
					Q.add(i, QVal);
				}
				
				state.setBestAction(actions.get(findMax(Q)));
				state.setValue(Q.get(findMax(Q)));
				
				V[current.id][state.getSourceCity().id]=Q.get(findMax(Q));
				
			}
			
			double max = Double.NEGATIVE_INFINITY;
			for (int i=0; i< cities.size();i++){
				for (int j=0; j< cities.size();j++){
					double abs = Math.abs(V[i][j]-preV[i][j]);
					if (abs>max) max=abs;
				}
			}
			sumDiffBetweenTwoIterations = max;
			System.out.println(V.toString());
		} while(sumDiffBetweenTwoIterations > epsilon);
		// the policy corresponding to V, i.e. the action to be chosen to get the expected reward V(s)
		// e.g. in city i, best[i] = j, where j is either the index of one of i's neighbor

		
	}
	
	private int findMax(List<Double> Q){
		Double max = Double.NEGATIVE_INFINITY;
		int best = -1;
		for(int i =0; i<Q.size();i++){
			if (Q.get(i)>max){
				max = Q.get(i);
				best = i;
			}	
		}
		return best;
	}
	
//	private City findMax(Map<City,Double> Q){
//		Double max = Double.NEGATIVE_INFINITY;
//		City best = null;
//		for(City city : Q.keySet()){
//			if (Q.get(city)>max){
//				max = Q.get(city);
//				best = city;
//			}	
//		}
//		return best;
//	}
}
