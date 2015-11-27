package template;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import template.Action.ActionType;
import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskSet;
import logist.topology.Topology.City;
import logist.plan.Plan;

public class SLS {
	
	private final static int MAX_ITER = 10000;
	private final double prob = 0.3;
	private static Action[] nextTask;
	private static Object[] nextTaskDomain;
	private List<Vehicle> mVehicles;
	private TaskSet mTasks;
	private int nT;
	private int nV;
	private int mNumberOfTasks;
	
	public SLS(List<Vehicle> vehicles, TaskSet tasks) {
		mVehicles = vehicles;
		mTasks = TaskSet.copyOf(tasks);
		mNumberOfTasks = tasks.size();
		nT = mNumberOfTasks * 2;  // Number of tasks (pickup and delivery => *2)
		nV = vehicles.size();  // Number of vehicles.
		nextTask = new Action[nT + nV];
		nextTaskDomain = new Object[nT + nV];

		int i = 0;
		for (Task task : tasks) {
			Action pickUp = new Action(ActionType.PICKUP, task, i);
			Action dropOff =  new Action(ActionType.DELIVERY, task, i+1);
			pickUp.setComplement(dropOff);
			dropOff.setComplement(pickUp);
			nextTaskDomain[i] = pickUp;
			nextTaskDomain[i+1] = dropOff;
			i = i + 2;
		}

		for (Vehicle v : vehicles) {
			nextTaskDomain[i] = v;
			i++;
		}
	}

	public void stochLocalSearch() {
		int iter = 0;
		selectInitialSolution();
		 
		do {
			double currentCost = computeTotalCost(nextTask);
			System.out.println("Current cost = "+ currentCost +" at iteration "+iter);
			List<Action[]> neighbors = chooseNeighbors(); 

			Action[] newNextTask = localChoice(neighbors);

			if (newNextTask != null){
				nextTask = newNextTask;
			}
			
			iter++;
		} while (iter<MAX_ITER);
		
		System.out.println("cost found = " +computeTotalCost(nextTask) +" after "+iter+ " iterations");
	}
	
	private Action[] localChoice(List<Action[]> neighbors) {
		if (Math.random() > prob){
			return nextTask;}
		else {
			Action[] newSol = selectRandomBestSol(neighbors);
			return newSol;
		}
	}
	
	private Action[] selectRandomBestSol(List<Action[]> neighbors){
		double minCost = Double.POSITIVE_INFINITY;
		List<Integer> indices = new ArrayList<Integer>();
		for (int i=0;i<neighbors.size();i++){
			double costI =computeTotalCost(neighbors.get(i)); 
			if ( costI< minCost){
				minCost = costI ;
				indices = new ArrayList<Integer>();
				indices.add(i);
			} else if ( costI == minCost){
				indices.add(i);
			}
		}
		List<Action[]> bestSols = new ArrayList<Action[]>();
		
		for(int i =0; i<indices.size();i++){
			bestSols.add(neighbors.get(indices.get(i)));
		}
		
		if (bestSols.size() == 1 ) 
			return bestSols.get(0);
		else if (bestSols.size()<1) {
			System.out.println("empty neighborhood!");
			return null;}
		else
			return bestSols.get((int) Math.random()*bestSols.size());
	}
	
	private List<Action[]> chooseNeighbors() {
		List<Action[]> neighbors = new ArrayList<Action[]>();
		
		int v = (int) (Math.random()*mVehicles.size());
		while (nextTask[vehicleIndex(mVehicles.get(v))] == null){
			v = (int) (Math.random()*mVehicles.size());
		}
		
		neighbors.addAll(findValidOrderChanges3(mVehicles.get(v)));
		neighbors.addAll(findValidVehicleChanges(mVehicles.get(v)));

		return neighbors;
	}
	
	/**
	 * Given a vehicle v, find all permutations of 2 actions that keep the solution valid
	 * @param v
	 * @return
	 */
	private List<Action[]> findValidOrderChanges(Vehicle v){
		List<Action[]> neighbors = new ArrayList<Action[]>();
		
		int length = 0;
		Action act = nextTask[vehicleIndex(v)];
		
		while (act !=null) {
			length++;
			act = nextTask[act.getActionIndex()];
		}
		
		Action[] newNextTask = null;
		for (int i=1;i<=length; i++){
			for (int j=i+1;j<=length; j++){
				newNextTask = changingTaskOrder(v, i,j);
				if (checkConstraints(newNextTask)){
					neighbors.add(newNextTask) ;
				}
			}
		}
		
		return neighbors;
	}
	
	private List<Action[]> findValidOrderChanges2(Vehicle v){
		List<Action[]> neighbors = new ArrayList<Action[]>();

		int length = 0;
		Action act = nextTask[vehicleIndex(v)];

		while (act !=null){
			length++;
			act = nextTask[act.getActionIndex()];
		}

		Action[] newNextTask = null;
		Action[] tempNeighbors = null;
		int i=0, j=0;
		while(i==j) {
			i = (int) (Math.random()*(length+1));
			j = (int) (Math.random()*(length+1));
		}
		if (i>j) {
			int temp = i;
			i =j;
			j = temp;
		}

		newNextTask = changingTaskOrder(v, i,j);
		if (checkConstraints(newNextTask)){
			neighbors.add(newNextTask) ;

			for (int k=1;k<=length; k++){
				for (int l=k+1;l<=length; l++){
					if (i!=k && l!=j && newNextTask != null ){
						tempNeighbors = changingTaskOrder2(newNextTask,v,k,l);
						if (checkConstraints(tempNeighbors)){
							neighbors.add(tempNeighbors) ;
						}
					}
				}
			}
		}
		
		return neighbors;
	}
	

private List<Action[]> findValidOrderChanges3(Vehicle v){
		
	List<Action[]> neighbors = new ArrayList<Action[]>();
	
	int length = 0;
	Action act = nextTask[vehicleIndex(v)];
	
	while (act !=null){
		length++;
		act = nextTask[act.getActionIndex()];
	} ;
	
	Action[] newNextTask = null;
	Action[] tempNeighbors = null;
	for (int i=1;i<=length; i++){
		for (int j=i+1;j<=length; j++){

			newNextTask = changingTaskOrder(v, i,j);
			if (checkConstraints(newNextTask) && Math.random()<0.05){
				neighbors.add(newNextTask) ;

					for (int k=1;k<=length; k++){
						for (int l=k+1;l<=length; l++){
							if (i!=k && l!=j && newNextTask != null ){
								tempNeighbors = changingTaskOrder2(newNextTask,v,k,l);
								if (checkConstraints(tempNeighbors)){
									neighbors.add(tempNeighbors) ;
								}
							}
						}
					}
				}
			}
		}
		
		return neighbors;
	}
	
	/**
	 * Given a vehicle v, finds all valid solution that give a task of v to any other vehicle
	 * @param v
	 * @return
	 */
	private List<Action[]> findValidVehicleChanges(Vehicle v){
		List<Action[]> neighbors = new ArrayList<Action[]>();
		
		List<Action[]> newNextTasks = null;
		for (Vehicle v2: mVehicles){
			if (v2.id() != v.id()){
				newNextTasks = allChangingVehicle(v,v2); 
				if(newNextTasks == null) System.out.println("no changing possible here");
				else {
					for(Action[] newNextTask : newNextTasks){
					if (checkConstraints(newNextTask)){
						neighbors.add(newNextTask);
						}
					}
				}
			}
		}
		
		if(neighbors.isEmpty()) System.out.println("Empty neighbors subset");
		
		return neighbors;
	}
	
	private List<Action[]> allChangingVehicle(Vehicle v1, Vehicle v2){
		List<Action[]> neighbors = new ArrayList<Action[]>();
		
		if (nextTask[vehicleIndex(v1)] == null){
			System.out.println("no changing possible here");
			return null;
		}
		
		Action[] A1 = new Action[nT+nV];
		for(int i=0; i<A1.length;i++){
			if (nextTask[i] != null)
				A1[i] = nextTask[i].clone();
			else A1[i] = null;
		}
		
		Action act = A1[vehicleIndex(v1)];
		Action[] newNextTask = null;
		do{
			if(act.getType().equals(Action.ActionType.PICKUP)){
				newNextTask = changingVehicle(v1, v2,act);
				if (checkConstraints(newNextTask)){
					neighbors.add(newNextTask) ;
				}
			}
			act = A1[act.getActionIndex()];
		} while (act !=null);
		
		return neighbors;
	}
	
	/**
	 * Takes the action at time (pickup and deliver actions) from v1 and give it to v2
	 * @param v1
	 * @param v2
	 * @return
	 */
	private Action[] changingVehicle(Vehicle v1, Vehicle v2, Action a){
		if (nextTask[vehicleIndex(v1)] == null){
			System.out.println("no changing possible here");
			return null;
		}
		
		Action[] A1 = new Action[nT+nV];
		for(int i=0; i<A1.length;i++){
			if (nextTask[i] != null)
				A1[i] = nextTask[i].clone();
			else A1[i] = null;
		}

		Action t1 = a; // t1 is the pickup part of the task we give to v2
		Action t2 = t1.getComplement().clone(); // t2 is the deliver part of the task we give to v2
		Action nextT1= A1[t1.getActionIndex()];
		// check that the next action of v1 isn't complement of the action we are giving to v2
		boolean t1t2 = false; // true if t2 follows t2
		if (nextT1.equals(t2)){
			nextT1=A1[t2.getActionIndex()];
			t1t2 = true;
		}
		
		Action nextT2= A1[t2.getActionIndex()];
		if(!t1t2){
			Action prevT2=null;
			if(prevTask(nextTask,t2) instanceof Action)
				prevT2= (Action)prevTask(nextTask,t2);

			A1[prevT2.getActionIndex()] = nextT2;
		}
		
		Action prevT1=null;
		if(prevTask(nextTask,t1) instanceof Action){
			prevT1= (Action)prevTask(nextTask,t1);
			A1[prevT1.getActionIndex()] = nextT1;
		} else A1[vehicleIndex(v1)] = nextT1;
		
		A1[t1.getActionIndex()] = t2;
		A1[t2.getActionIndex()] = A1[vehicleIndex(v2)];
		A1[vehicleIndex(v2)]=t1;
		
		t2.setVehicle(v2);
		t1.setVehicle(v2);
		Action actionV1 =  A1[vehicleIndex(v1)];
		
		int time =1;
		while (actionV1 != null){
			actionV1.setTime(time);
			time++;
			actionV1 = A1[actionV1.getActionIndex()];
		}
		time = 1;
		Action actionV2 =  A1[vehicleIndex(v2)];
		while (actionV2 != null){
			actionV2.setTime(time);
			time++;
			actionV2 = A1[actionV2.getActionIndex()];
		}
		return A1;
	}
	
	private Action[] changingTaskOrder(Vehicle v, int idxA1, int idxA2){
		if (nextTask[vehicleIndex(v)] == null)
			return null;
		
		Action[] A1 = new Action[nT+nV];
		for(int i=0; i<A1.length;i++){
			if (nextTask[i] != null)
				A1[i] = nextTask[i].clone();
			else A1[i] = null;
		}
		
		Action next = A1[vehicleIndex(v)];
		Action first = null;
		Action second = null;
		while (next != null){
			if (next.getTime() == idxA1){
				first = next;
			}
			else if  (next.getTime() == idxA2){
				second = next;
			}
			next = A1[next.getActionIndex()];
		}
		
		if (first == null || second == null) return null; // degenerate case where idxA1 or idxA2 are not valid times
		
		Action prevFirst = null;
		for (int i=0; i<A1.length;i++){
			if (A1[i] != null && A1[i].equals(first)){
				if(nextTaskDomain[i] instanceof Action)
					prevFirst = (Action) nextTaskDomain[i];
				break;
			}
		}
		
		Action prevSecond = null;
		for (int i=0; i<A1.length;i++){
			if (A1[i] != null && A1[i].equals(second)){
				if(nextTaskDomain[i] instanceof Action)
					prevSecond = (Action) nextTaskDomain[i];
				break;
			}
		}
		
		Action nextFirst = A1[first.getActionIndex()];
		
		Action nextSecond = A1[second.getActionIndex()];
		
		if (nextFirst.equals(second)){
			if(prevFirst !=null)
				A1[prevFirst.getActionIndex()] = second;
			else A1[vehicleIndex(v)] = second;
			
			A1[second.getActionIndex()] = first;
			A1[first.getActionIndex()] = nextSecond;
			
			int time = 1;
			Action actionV =  A1[vehicleIndex(v)];
			while (actionV != null){
				actionV.setTime(time);
				time++;
				actionV = A1[actionV.getActionIndex()];
			}
		} else {
			if(prevFirst !=null)
				A1[prevFirst.getActionIndex()] = second;
			else A1[vehicleIndex(v)] = second;
			A1[second.getActionIndex()] = nextFirst;
			A1[prevSecond.getActionIndex()] = first;
			A1[first.getActionIndex()] = nextSecond;
			
			int time = 1;
			Action actionV =  A1[vehicleIndex(v)];
			while (actionV != null){
				actionV.setTime(time);
				time++;
				actionV = A1[actionV.getActionIndex()];
			}
		}
		return A1;
	}
	
	private Action[] changingTaskOrder2(Action[] tempSol,Vehicle v, int idxA1, int idxA2){
		if (tempSol[vehicleIndex(v)] == null)
			return null;
		
		Action[] A1 = new Action[nT+nV];
		for(int i=0; i<A1.length;i++){
			if (tempSol[i] != null)
				A1[i] = tempSol[i].clone();
			else A1[i] = null;
		}
		
		Action next = A1[vehicleIndex(v)];
		Action first = null;
		Action second = null;
		while (next != null){
			if (next.getTime() == idxA1){
				first = next;
			}
			else if  (next.getTime() == idxA2){
				second = next;
			}
			next = A1[next.getActionIndex()];
		}
		
		if (first == null || second == null) return null; // degenerate case where idxA1 or idxA2 are not valid times
		
		Action prevFirst = null;
		for (int i=0; i<A1.length;i++){
			if (A1[i] != null && A1[i].equals(first)){
				if(nextTaskDomain[i] instanceof Action)
					prevFirst = (Action) nextTaskDomain[i];
				break;
			}
		}
		
		Action prevSecond = null;
		for (int i=0; i<A1.length;i++){
			if (A1[i] != null && A1[i].equals(second)){
				if(nextTaskDomain[i] instanceof Action)
					prevSecond = (Action) nextTaskDomain[i];
				break;
			}
		}
		
		Action nextFirst = A1[first.getActionIndex()];
		
		Action nextSecond = A1[second.getActionIndex()];
		
		if (nextFirst.equals(second)){
			if(prevFirst !=null)
				A1[prevFirst.getActionIndex()] = second;
			else A1[vehicleIndex(v)] = second;
			
			A1[second.getActionIndex()] = first;
			A1[first.getActionIndex()] = nextSecond;
			
			int time = 1;
			Action actionV =  A1[vehicleIndex(v)];
			while (actionV != null){
				actionV.setTime(time);
				time++;
				actionV = A1[actionV.getActionIndex()];
			}
		} else {
			if(prevFirst !=null)
				A1[prevFirst.getActionIndex()] = second;
			else A1[vehicleIndex(v)] = second;
			A1[second.getActionIndex()] = nextFirst;
			A1[prevSecond.getActionIndex()] = first;
			A1[first.getActionIndex()] = nextSecond;
			
			int time = 1;
			Action actionV =  A1[vehicleIndex(v)];
			while (actionV != null){
				actionV.setTime(time);
				time++;
				actionV = A1[actionV.getActionIndex()];
			}
		}
		return A1;
	}

	private void selectInitialSolution() {
		// Find the biggest vehicle.
		Vehicle biggestVehicle = mVehicles.get(0);
		int biggestVehicleIndexOffset = 0;
		for (int i = 1; i < mVehicles.size(); i++) {
			Vehicle v = mVehicles.get(i);
			if (v.capacity() > biggestVehicle.capacity()) {
				biggestVehicle = v;
				biggestVehicleIndexOffset = i;
			}
		}
	
		// Give all the tasks to that vehicle.
		nextTask[nT + biggestVehicleIndexOffset] = (Action) nextTaskDomain[0];
		nextTask[nT + biggestVehicleIndexOffset].setVehicle(biggestVehicle);
		nextTask[nT + biggestVehicleIndexOffset].setTime(1);

		for (int i = 1; i < nT; i++) {
			nextTask[i-1] = (Action) nextTaskDomain[i];
			nextTask[i-1].setVehicle(biggestVehicle);
			nextTask[i-1].setTime(i+1);
		}
		nextTask[nT-1] = null;
		System.out.println("selected init sol");
	}
	
	private boolean checkConstraints(Action[] solution) {
		if (solution==null) return false;
		return checkConstraint1(solution) && checkConstraint2(solution) && checkConstraint3(solution)
				&& checkConstraint4(solution) && checkConstraint5(solution) && checkConstraint6(solution)
				&& checkConstraint7(solution);
	}

	// nextTask(t) = t: the task delivered after some task t cannot be the same task
	private boolean checkConstraint1(Action[] solution) {
		for (int i = 0; i < nT; i++) {
			if (solution[i] == nextTaskDomain[i]) {
				return false;
			}
		}
		return true;
	}

	// nextTask(vk) = tj ⇒ time(tj) = 1
	private boolean checkConstraint2(Action[] solution) {
		for (int i = nT; i < solution.length; i++) {
			if (solution[i] != null) {
				if (solution[i].getTime() != 1) {
					return false;
				}
			}
		}
		return true;
	}

	// nextTask(ti) = tj ⇒ time(tj) = time(ti) + 1
	private boolean checkConstraint3(Action[] solution) {
		for (int i = nT; i < solution.length; i++) {
			Action currentAction = solution[i];
			if (currentAction != null) {
				Action nextAction = solution[currentAction.getActionIndex()];
				while (currentAction != null && nextAction != null) {
					if (currentAction.getTime() != nextAction.getTime() - 1) {
						return false;
					}
					currentAction = nextAction;
					nextAction = solution[currentAction.getActionIndex()];
				}
			}
		}
		return true;
	}

	// nextTask(vk) = tj ⇒ vehicle(tj) = vk
	private boolean checkConstraint4(Action[] solution) {
		for (int i = nT; i < solution.length; i++) {
			if (solution[i] != null) {
				if (solution[i].getVehicle() != ((Vehicle) nextTaskDomain[i])) {
					return false;
				}
			}
		}
		return true;
	}

	// nextTask(ti) = tj ⇒ vehicle(tj) = vehicle(ti)
	private boolean checkConstraint5(Action[] solution) {
		for (int i = 0; i < nT; i++) {
			Action currentAction = solution[i];
			if (currentAction != null) {
				Action nextAction = solution[currentAction.getActionIndex()];
				if (nextAction != null) {
					if (currentAction.getVehicle() != nextAction.getVehicle()) {
						return false;
					}
				}
			}
		}
		return true;
	}

	// all tasks must be delivered: the set of values of the variables in the
	// nextTask array must be equal to the set of tasks T plus NV times the value NULL.
	private boolean checkConstraint6(Action[] solution) {
		int nullCounter = 0; // Counts the number of null actions. At the end, must be equal to nV.
		Set<Task> notNullPickUpTasks = new HashSet<Task>();
		Set<Task> notNullDeliveryTasks = new HashSet<Task>();
		
		// For each action,
		for (int i = 0; i < solution.length; i++) {
			Action taskAction = solution[i];
			// if it is null, we increment the null counter,
			if (taskAction == null) {
				nullCounter++;
			// otherwise, we add it in the corresponding (based on its type) set.
			} else {
				Task task = taskAction.getTask();
				if (taskAction.getType() == ActionType.PICKUP) {
					// If the action is of "PickUp" type and does not already exist in the
					// PickUp actions set, we add it in this set,
 					if (!notNullPickUpTasks.contains(task)) {
						notNullPickUpTasks.add(task);
					// otherwise, we return false,
					// because it means this task exists at least two times in "nextTask".
					} else {
						return false;
					}
 				// If the action is of "Delivery" type and does not already exist in the
				// Delivery actions set, we add it in this set,
				} else if (!notNullDeliveryTasks.contains(task)) {
					notNullDeliveryTasks.add(task);
				// otherwise, we return false,
				// because it means this task exists at least two times in "nextTask".
				} else {
					return false;
				}
			}
		}
		// We must have "nV" null elements in "nextTask". If this is not the case, we return false,
		if (nullCounter != nV) {
			return false;
		// otherwise, we check that each task is contained in the "pickUp" or in the "delivery" set.
		} else {
			for (Task t : mTasks) {
				if (!notNullPickUpTasks.contains(t) || !notNullDeliveryTasks.contains(t)) {
					return false;
				}
			}
		}
		return true;
	}

	// Checks that each vehicle actions "list" contains at most one pickUp and delivery actions for each task.
	// Furthermore, for each task, the pickUp action has to be present in the actions "list" before the delivery.
	// It also checks that for each vehicle, at any time, the sum of the carried tasks is not bigger than the capacity.
	// The capacity of a vehicle cannot be exceeded: if load(ti) > capacity(vk) ⇒ vehicle(ti) != vk
	private boolean checkConstraint7(Action[] solution) {
		// For each vehicle, 
		for (int i = nT; i < solution.length; i++) {
			// we fetch the vehicle capacity.
			Vehicle v = (Vehicle) nextTaskDomain[i];
			int vehicleCapacity = v.capacity();
			int vehicleCurrentLoad = 0;
			// We create an array of size the number of tasks (initialized at 0 everywhere).
			int[] checkSum = new int[mNumberOfTasks];
			// We look at all the actions of the vehicle starting with the first one,
			Action action = solution[i];
			// until we find a "null" action meaning we saw all the actions of that vehicle.
			while (action != null) {
				Task vehicleTask = action.getTask();
				int taskIndex = vehicleTask.id; // Index of the task (from 0 to numberOfTasks)
				int currentValue = checkSum[taskIndex]; // Current value of the checkSum array for that task
				// If we have a pickup action, we increment that value, otherwise we decrement it
				checkSum[taskIndex] = (action.getType() == ActionType.PICKUP) ? currentValue+1 : currentValue-1;
				// If that value is less than 0, it means we have a delivery action before pickUp action for the current task.
				// If that value is more than 1, it means we have two times a pickUp action for the same task.
				if (checkSum[taskIndex] < 0 || checkSum[taskIndex] > 1) {
					// so, for both cases, we return false as this is not possible!
					return false;
				}
				
				// Add or remove the task weight from the vehicle current load.
				if (action.getType() == ActionType.PICKUP) {
					vehicleCurrentLoad += vehicleTask.weight;
				} else {
					vehicleCurrentLoad -= vehicleTask.weight;
				}
				// Check that the vehicle capacity is not violated.
				if (vehicleCurrentLoad > vehicleCapacity) {
					return false;
				}
				// We go to the next action.
				action = solution[action.getActionIndex()];
			}
			// Finally, we test that each value of the checkSum array is equal to 0 (means same number of pickUp and delivery).
			for (int sum : checkSum) {
				if (sum != 0) {
					return false;
				}
			}
		}
		return true;
	}
	
	private int vehicleIndex(Vehicle v){
		for (int i = nT; i<nextTaskDomain.length;i++){
			if (nextTaskDomain[i] instanceof Vehicle){
				Vehicle v1  = (Vehicle) nextTaskDomain[i];
				if (v1.id() == v.id())
					return i;
			}	
		}
		return -1;
	}
	
	// returns the task that preceeds a
	private Object prevTask(Action[] solution, Action a){
		int indexA = -1;
		for (int i=0; i<solution.length;i++){
			if (solution[i] !=null ){
				if (solution[i].equals(a)){
					indexA = i;
					break;}
			}
		}
		if (indexA>-1) return nextTaskDomain[indexA];
		return null;
	}
	
	private double computeTotalCost(Action[] solution){
		double cost = 0.0;
		for (int i = nT; i<solution.length; i++){
			 Vehicle v = (Vehicle) nextTaskDomain[i];
			 City current = v.homeCity();
			 Action actionV = solution[i];
			 while ( actionV != null){
				 // move to next action's city
				 cost += v.costPerKm()*current.distanceTo(actionV.getCity());
				 current = actionV.getCity();
				 actionV = solution[actionV.getActionIndex()];
			 }
		 }
		return cost;
	}
	
	public List<Plan> generatePlans(){
		 List<Plan> plans = new ArrayList<Plan>();
		 
		 for (int i = nT; i<nextTask.length; i++){
			 Vehicle v = (Vehicle) nextTaskDomain[i];
			 System.out.println(v.name());
			 City current = v.getCurrentCity();
			 Plan p = new Plan(current);
			 Action actionV = nextTask[i];
			 while ( actionV != null){
				 // move to next action's city
				 for (City city : current.pathTo(actionV.getCity())){
						p.appendMove(city);
					}
				 current = actionV.getCity();
				// pickup or deliver task
				 switch (actionV.getType()){
				 	case PICKUP:
				 		p.appendPickup(actionV.getTask());
				 		System.out.println("adding pickup in "+actionV.getCity().name + " "+ actionV);
				 		break;
				 	case DELIVERY:
				 		p.appendDelivery(actionV.getTask());
				 		System.out.println("adding deliver in "+actionV.getCity().name + " "+ actionV);
				 		break;
				 }
				 actionV = nextTask[actionV.getActionIndex()];
			 }
			 plans.add(p);
		 }
		 return plans;
	}
	
	public void printSolution(Action[] solution){
		 for (int i = nT; i<solution.length; i++){
			 Vehicle v = (Vehicle) nextTaskDomain[i];
			 System.out.println("Vehicle "+ v.id());
			 Action actionV = solution[i];
			 while ( actionV != null){
				 // move to next action's city
				 switch (actionV.getType()){
				 	case PICKUP:
				 		System.out.println("adding pickup in "+actionV.getCity().name + " "+ actionV);
				 		break;
				 	case DELIVERY:
				 		System.out.println("adding deliver in "+actionV.getCity().name + " "+ actionV);
				 		break;
				 }
				 actionV = solution[actionV.getActionIndex()];
			 }
		 }
	}
}