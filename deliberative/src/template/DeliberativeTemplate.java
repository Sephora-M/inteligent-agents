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

	enum Algorithm { BFS, ASTAR }
	
	/* Environment */
	Topology topology;
	TaskDistribution td;
	
	/* the properties of the agent */
	Agent agent;
	int capacity;

	/* the planning class */
	Algorithm algorithm;
	
	@Override
	public void setup(Topology topology, TaskDistribution td, Agent agent) {
		this.topology = topology;
		this.td = td;
		this.agent = agent;
		
		// initialize the planner
		int capacity = agent.vehicles().get(0).capacity();
		String algorithmName = agent.readProperty("algorithm", String.class, "ASTAR");
		
		// Throws IllegalArgumentException if algorithm is unknown
		algorithm = Algorithm.valueOf(algorithmName.toUpperCase());
		
		// ...
	}
	
	
	@Override
	public Plan plan(Vehicle vehicle, TaskSet tasks) {
		Plan plan;
		
		 
		// Compute the plan with the selected algorithm.
		switch (algorithm) {
		case ASTAR:
			// ...
			plan = aStarPlan(vehicle, tasks);
			break;
		case BFS:
			// ...
			plan = aStarPlan(vehicle, tasks);
			break;
		default:
			throw new AssertionError("Unknown algorithm!");
		}		
		return plan;
	}
	
	private Plan naivePlan(Vehicle vehicle, TaskSet tasks) {
		City current = vehicle.getCurrentCity();
		Plan plan = new Plan(current);

		for (Task task : tasks) {
			// move: current city => pickup location
			for (City city : current.pathTo(task.pickupCity))
				plan.appendMove(city);

			plan.appendPickup(task);

			// move: pickup location => delivery location
			for (City city : task.path())
				plan.appendMove(city);

			plan.appendDelivery(task);

			// set current city
			current = task.deliveryCity;
			 
		}
		return plan;
	}
	
	private Plan aStarPlan(Vehicle vehicle, TaskSet tasks){
		City current = vehicle.getCurrentCity();
		Plan plan = new Plan(current);
		TaskSet remainingTasks = TaskSet.copyOf(tasks);
		TaskSet currentTasks = vehicle.getCurrentTasks();
		System.out.println("init # of tasks ="+remainingTasks.size());
		
		
		
		boolean isFull = isFull(vehicle); 
		State initNode = new State(current, TaskSet.copyOf(remainingTasks), TaskSet.copyOf(currentTasks), isFull); 
		
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
				
			if(!initState){
//				plan.appendMove(n.getCurrentCity());
				System.out.println("move to node = "+n.getCurrentCity().name );
			}else
				initState = false;
//			}
			
			System.out.println("remains "+n.toStringRemainingTasks());
			System.out.println("to deliver "+n.toStringToDeliverTasks());
			
			Task pickUp = taskToPickUpInCity(remainingTasks,n.getCurrentCity());
			Task dropOff = taskToDropOffInCity(currentTasks,n.getCurrentCity());
			if (dropOff != null){
//				plan.appendDelivery(dropOff);
				currentTasks.remove(dropOff);
				System.out.println("left to drop off =" + currentTasks.size());
			}
			if (pickUp != null){
//				plan.appendPickup(pickUp);
				remainingTasks.remove(pickUp);
				currentTasks.add(pickUp);
				System.out.println("left to pickup =" + remainingTasks.size());
			}
			if (n.isGoal()){
				System.out.println("GOAL");
				break;
			}
			StateList S = new StateList(); // the sorted list of successors
			StateList Sprime = new StateList();
			System.out.println("successors of "+n.getCurrentCity().name);
			for (City neighbor : n.getCurrentCity().neighbors()){
				State succ = new State(neighbor, TaskSet.copyOf(remainingTasks), TaskSet.copyOf(currentTasks), isFull);
				if(!C.contains(succ)){
					S.add(succ);
					Sprime.add(succ);
				}
				
				
				//				System.out.println(succ.getCurrentCity().name+" "+succ.getHeuristic());
			}
			
			for (State suc: S.getList()){
				System.out.println(suc.getCurrentCity().name +" remaing " + suc.toStringRemainingTasks());
				System.out.println("to del " +suc.toStringToDeliverTasks());
			}
			int sizeS = Sprime.getList().size();
			for(int i=0; i<sizeS;i++){
				State s = Sprime.getList().poll();
//				System.out.println("i = "+i+" "+s.getCurrentCity().name+" "+s.getHeuristic());
//				Q.addLast(s);
				Q.addFirst(s);
			}
//			Q.addAll(S.getList());
			parent = n;
			if(Q.isEmpty()){
				System.out.println("exit because Q empty!");
			}
		} while (!Q.isEmpty());
		
		
		return naivePlan( vehicle,  tasks);
	}
	
	private Task taskToPickUpInCity(TaskSet tasks, City city){
		for (Task task : tasks){
//			System.out.println("task in "+task.pickupCity);
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
	
	private boolean isFull(Vehicle vehicle){
		return  (capacity - vehicle.getCurrentTasks().weightSum())<3;//3 being the constant value of a task weight
	}

	@Override
	public void planCancelled(TaskSet carriedTasks) {
		
		if (!carriedTasks.isEmpty()) {
			// This cannot happen for this simple agent, but typically
			// you will need to consider the carriedTasks when the next
			// plan is computed.
		}
	}
}
