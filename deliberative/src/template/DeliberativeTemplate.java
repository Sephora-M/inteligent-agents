package template;

/* import table */
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import logist.simulation.Vehicle;
import logist.agent.Agent;
import logist.behavior.DeliberativeBehavior;
import logist.plan.Plan;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.task.TaskSet;
import logist.topology.Topology;
import logist.topology.Topology.City;

/**
 * An optimal planner for one vehicle.
 */
@SuppressWarnings("unused")
public class DeliberativeTemplate implements DeliberativeBehavior {

	enum Algorithm { BFS, ASTAR, DFS, NAIVE }
	
	/* Environment */
	Topology topology;
	TaskDistribution td;
	
	/* the properties of the agent */
	Agent agent;
	int capacity;
	boolean exceedFullCap;

	/* the planning class */
	Algorithm algorithm;
	
	@Override
	public void setup(Topology topology, TaskDistribution td, Agent agent) {
		this.topology = topology;
		this.td = td;
		this.agent = agent;
		
		// initialize the planner
		capacity = agent.vehicles().get(0).capacity();
		String algorithmName = agent.readProperty("algorithm", String.class, "ASTAR");
		
		// Throws IllegalArgumentException if algorithm is unknown
		algorithm = Algorithm.valueOf(algorithmName.toUpperCase());
		
		// ...
	}
	
	@Override
	public Plan plan(Vehicle vehicle, TaskSet tasks) {
		Plan plan;
		exceedFullCap = capacity < tasks.size();
		
		LinkedList<City> route;
		// Compute the plan with the selected algorithm.
		switch (algorithm) {
		case ASTAR:
			route = AStarRoute(vehicle, tasks);
			plan = generatePlan(route, vehicle, tasks);
			break;
		case BFS:
			route = BFSroute(vehicle, tasks);
			plan = generatePlan(route, vehicle, tasks);
			break;
		case DFS:
			route = DFSroute(vehicle, tasks);
			plan = generatePlan(route, vehicle, tasks);
			break;
		case NAIVE:
			plan = naivePlan(vehicle, tasks);
			break;
		default:
			throw new AssertionError("Unknown algorithm!");
		}		
		return plan;
	}
	
	private Plan naivePlan(Vehicle vehicle, TaskSet tasks) {
		long startingTime = System.currentTimeMillis();
		
		City current = vehicle.getCurrentCity();
		Plan plan = new Plan(current);
		Double cost = 0.0;
		for (Task task : tasks) {
			// move: current city => pickup location
			for (City city : current.pathTo(task.pickupCity)){
				plan.appendMove(city);
			}
			cost += current.distanceTo(task.pickupCity);
			plan.appendPickup(task);

			// move: pickup location => delivery location
			for (City city : task.path()){
				plan.appendMove(city);
			}
			cost += task.pathLength();
			
			plan.appendDelivery(task);

			// set current city
			current = task.deliveryCity;
		}
		System.out.println("Naive route cost = "+cost);
		System.out.println("Planing Time = "+-(startingTime-System.currentTimeMillis()) +"ms");

		return plan;
	}
	
	/**
	 * Generate a plan for a vehicle according to a given route
	 * @param route
	 * @param vehicle
	 * @param tasks
	 * @return
	 */
	private Plan generatePlan(LinkedList<City> route, Vehicle vehicle, TaskSet tasks){
		City currentCity = route.peek();
		Plan plan = new Plan(currentCity);
		TaskSet currentTasks = vehicle.getCurrentTasks();
		boolean first = true;
		
		while (!route.isEmpty()) {
			currentCity = route.poll();
			if (!first)
				plan.appendMove(currentCity);
			else 
				first = false;
			Task dropOff = taskToDropOffInCity(currentTasks,currentCity);
			while (dropOff != null){
				plan.appendDelivery(dropOff);
				currentTasks.remove(dropOff);
				dropOff = taskToDropOffInCity(currentTasks,currentCity);
			}
			Task pickUp = taskToPickUpInCity(tasks,currentCity);
			while (pickUp != null && !isFull(currentTasks)){
				plan.appendPickup(pickUp);
				tasks.remove(pickUp);
				currentTasks.add(pickUp);
				pickUp = taskToPickUpInCity(tasks,currentCity);
			}
			
		}
		
		return plan;
	}
	

	private LinkedList<City> DFSroute(Vehicle vehicle, TaskSet tasks) {
		long startingTime = System.currentTimeMillis();
		City current = vehicle.getCurrentCity();
		Plan plan = new Plan(current);
		TaskSet remainingTasks = TaskSet.copyOf(tasks);
		TaskSet currentTasks = vehicle.getCurrentTasks();
//		System.out.println("init # of tasks ="+remainingTasks.size());
		
		LinkedList<City> route = null;
		
		boolean isFull = isFull(currentTasks); 
		State initNode = new State(current, TaskSet.copyOf(remainingTasks), TaskSet.copyOf(currentTasks), isFull, exceedFullCap); 
		initNode.g = 0.0;
		initNode.addToRoute(current);
		
		System.out.println("remains "+initNode.toStringRemainingTasks());
		
		LinkedList<State> Q = new LinkedList<State>();
		LinkedList<State> C = new LinkedList<State>();
		Q.add(initNode);
		boolean initState = true;
		State parent = null;
		do {
			State n = Q.removeFirst();
			C.add(n);
			
			currentTasks = n.getToDeliver();
			remainingTasks = n.getRemainingTasks();
//			if (vehicle.getCurrentCity().id != n.getCurrentCity().id){
				
			
//			System.out.println("remains "+n.toStringRemainingTasks());
//			System.out.println("to deliver "+n.toStringToDeliverTasks());
			
			// pickUp/dropOff all the tasks in the current city if any
			Task dropOff = taskToDropOffInCity(currentTasks,n.getCurrentCity());
			while (dropOff != null){
//				plan.appendDelivery(dropOff);
				currentTasks.remove(dropOff);
				dropOff = taskToDropOffInCity(currentTasks,n.getCurrentCity());
			}
			Task pickUp = taskToPickUpInCity(remainingTasks,n.getCurrentCity());
			while (pickUp != null && !isFull(currentTasks)){
//				plan.appendPickup(pickUp);
				remainingTasks.remove(pickUp);
				currentTasks.add(pickUp);
				pickUp = taskToPickUpInCity(remainingTasks,n.getCurrentCity());
			}
			
			// check if a goal's been reached
			if (n.isGoal()){
				System.out.println("GOAL");
				System.out.println(n.route.toString());
				System.out.println("DFS route cost = "+n.routeLength);
				System.out.println("Planing Time = "+-(startingTime-System.currentTimeMillis()) +"ms");
				route = n.route;
				break;
			}
			
			// create the list of successors 
			ArrayList<State> Sprime = new ArrayList<State>();
			for (City neighbor : n.getCurrentCity().neighbors()){
				State succ = new State(neighbor, TaskSet.copyOf(remainingTasks), TaskSet.copyOf(currentTasks), isFull(currentTasks), exceedFullCap);
				succ.g = n.g + n.getCurrentCity().distanceTo(neighbor);
				succ.addAllToRoute(n.route, n.routeLength);
				succ.addToRoute(neighbor);
				if(!C.contains(succ)){ // cycle detection 
					Sprime.add(succ);
				}
			}
			
			int sizeS = Sprime.size();
			for(int i=0; i<sizeS;i++){
				State s = Sprime.get(i);
				Q.addFirst(s);
			}
			parent = n;
			if(Q.isEmpty()){
				System.out.println("exit because Q empty!");
			}
		} while (!Q.isEmpty());
		
		return route;
	}
	
	private LinkedList<City> BFSroute(Vehicle vehicle, TaskSet tasks){
		long startingTime = System.currentTimeMillis();
		City current = vehicle.getCurrentCity();
		Plan plan = new Plan(current);
		TaskSet remainingTasks = TaskSet.copyOf(tasks);
		TaskSet currentTasks = vehicle.getCurrentTasks();
		
		LinkedList<City> route = null;
		
		boolean isFull = isFull(currentTasks); 
		State initNode = new State(current, TaskSet.copyOf(remainingTasks), TaskSet.copyOf(currentTasks), isFull, exceedFullCap); 
		initNode.g = 0.0;
		initNode.addToRoute(current);
		
		LinkedList<State> Q = new LinkedList<State>();
		LinkedList<State> C = new LinkedList<State>();
		Q.add(initNode);
		boolean initState = true;
		State parent = null;
		do {
			State n = Q.removeFirst();
			C.add(n);
			
			currentTasks = n.getToDeliver();
			remainingTasks = n.getRemainingTasks();
				
			// pickUp/dropOff all the tasks in the current city if any
			Task dropOff = taskToDropOffInCity(currentTasks,n.getCurrentCity());
			while (dropOff != null){
				currentTasks.remove(dropOff);
				dropOff = taskToDropOffInCity(currentTasks,n.getCurrentCity());
			}
			Task pickUp = taskToPickUpInCity(remainingTasks,n.getCurrentCity());
			while (pickUp != null && !isFull(currentTasks)){
				remainingTasks.remove(pickUp);
				currentTasks.add(pickUp);
				pickUp = taskToPickUpInCity(remainingTasks,n.getCurrentCity());
			}
			
			// check if a goal's been reached
			if (n.isGoal()){
				System.out.println("GOAL");
				System.out.println(n.route.toString());

				System.out.println("BFS route cost "+n.routeLength);
				System.out.println("Planing Time = "+-(startingTime-System.currentTimeMillis()) +"ms");
				route = n.route;
				break;
			}
			
			// create the list of successors 
			ArrayList<State> Sprime = new ArrayList<State>();
			for (City neighbor : n.getCurrentCity().neighbors()){
				State succ = new State(neighbor, TaskSet.copyOf(remainingTasks), TaskSet.copyOf(currentTasks), isFull(currentTasks), exceedFullCap);
				succ.g = n.g + n.getCurrentCity().distanceTo(neighbor);
//				succ.route.addAll(n.route);
//				succ.route.add(neighbor);
				succ.addAllToRoute(n.route, n.routeLength);
				succ.addToRoute(neighbor);
				if(!C.contains(succ)){ // cycle detection 
					Sprime.add(succ);
				}
				//				System.out.println(succ.getCurrentCity().name+" "+succ.getHeuristic());
			}
			
			Q.addAll(Sprime);
			parent = n;
			if(Q.isEmpty()){
				System.out.println("exit because Q empty!");
			}
		} while (!Q.isEmpty());
		
		return route;
	}
	
	private LinkedList<City> AStarRoute(Vehicle vehicle, TaskSet tasks){
		long startingTime = System.currentTimeMillis();
		City current = vehicle.getCurrentCity();
		Plan plan = new Plan(current);
		TaskSet remainingTasks = TaskSet.copyOf(tasks);
		TaskSet currentTasks = vehicle.getCurrentTasks();
		
		LinkedList<City> route = null;
		
		boolean isFull = isFull(currentTasks); 
		State initNode = new State(current, TaskSet.copyOf(remainingTasks), TaskSet.copyOf(currentTasks), isFull, exceedFullCap); 
		initNode.g = 0.0;
		initNode.addToRoute(current);
		
		StateList Q = new StateList();
		LinkedList<State> C = new LinkedList<State>();
		Q.add(initNode);
		boolean initState = true;
		State parent = null;
		do {
			State n = Q.removeFirst();
			C.add(n);
			
			currentTasks = n.getToDeliver();
			remainingTasks = n.getRemainingTasks();
				
//			if(!initState){
//				System.out.println("move to node = "+n.getCurrentCity().name );
//			}else
//				initState = false;
			
//			System.out.println("remains "+n.toStringRemainingTasks());
//			System.out.println("to deliver "+n.toStringToDeliverTasks());
			
			// pickUp/dropOff all the tasks in the current city if any
			Task dropOff = taskToDropOffInCity(currentTasks,n.getCurrentCity());
			while (dropOff != null){
				currentTasks.remove(dropOff);
//				System.out.println("left to drop off =" + currentTasks.size());
				dropOff = taskToDropOffInCity(currentTasks,n.getCurrentCity());
			}
			
			Task pickUp = taskToPickUpInCity(remainingTasks,n.getCurrentCity());
			while (pickUp != null && !isFull(currentTasks)){
				remainingTasks.remove(pickUp);
				currentTasks.add(pickUp);
//				System.out.println("left to pickup =" + remainingTasks.size());
				pickUp = taskToPickUpInCity(remainingTasks,n.getCurrentCity());
			}
			
			// check if a goal's been reached
			if (n.isGoal()){
				System.out.println("GOAL");
				System.out.println(n.route.toString());

				System.out.println("A* route cost " +n.routeLength);
				System.out.println("Planing Time = "+ -(startingTime-System.currentTimeMillis()) +"ms");
				route = n.route;
				break;
			}
			
			// create the list of successors 
			ArrayList<State> Sprime = new ArrayList<State>();
//			System.out.println("successors of "+n.getCurrentCity().name);
			for (City neighbor : n.getCurrentCity().neighbors()){
				State succ = new State(neighbor, TaskSet.copyOf(remainingTasks), TaskSet.copyOf(currentTasks), isFull(currentTasks), exceedFullCap);
				succ.g = n.g + n.getCurrentCity().distanceTo(neighbor);
				succ.addAllToRoute(n.route, n.routeLength);
				succ.addToRoute(neighbor);
				if(!C.contains(succ)){ // cycle detection 
					Sprime.add(succ);
				}
				//				System.out.println(succ.getCurrentCity().name+" "+succ.getHeuristic());
			}
			
			Q.addAll(Sprime);
			parent = n;
			if (Q.isEmpty()) {
				System.out.println("exit because Q empty!");
			}
		} while (!Q.isEmpty());
		
		return route;
	}
	
	private Task taskToPickUpInCity(TaskSet tasks, City city){
		for (Task task : tasks){
			if (city.id == task.pickupCity.id)
				return task;
		}
		return null;
	}
	
	private Task taskToDropOffInCity(TaskSet tasks, City city){
		for (Task task : tasks){
			if (city.id == task.deliveryCity.id)
				return task;
		}
		return null;
	}
	
	private boolean isFull(TaskSet currentTasks) {
		return (capacity - currentTasks.weightSum()) < 3;//3 being the constant value of a task weight
	}

	@Override
	public void planCancelled(TaskSet carriedTasks) {
		
		/*No need to implement this as the planning always takes into account the 
		 * agent's carried tasks 
		 * 
		 */
	}
}
