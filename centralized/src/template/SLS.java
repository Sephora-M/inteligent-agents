package template;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import template.Action.ActionType;
import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskSet;

public class SLS {

	private static Action[] nextTask;
	private static Object[] nextTaskDomain;
	private List<Vehicle> mVehicles;
	private TaskSet mTasks;
	//private List<VehiclePlan> mVehiclesPlans;
	private int nT;
	private int nV;
	private int mNumberOfTasks;

	public SLS(List<Vehicle> vehicles, TaskSet tasks) {
		mVehicles = vehicles; // Deep copy necessary?
		mTasks = TaskSet.copyOf(tasks);
		//mVehiclesPlans = new ArrayList<VehiclePlan>();
		mNumberOfTasks = tasks.size();
		nT = mNumberOfTasks * 2;  // Number of tasks (pickup and delivery => *2)
		nV = vehicles.size();  // Number of vehicles.
		nextTask = new Action[nT + nV];
		nextTaskDomain = new Object[nT + nV];

		int i = 0;
		for (Task task : tasks) {
			nextTaskDomain[i] = new Action(ActionType.PICKUP, task, i);
			nextTaskDomain[i+1] = new Action(ActionType.DELIVERY, task, i+1);
			i = i + 2;
		}

		for (Vehicle v : vehicles) {
			nextTaskDomain[i] = v;
			i++;
		}
	}

	public void stochLocalSearch() {
		selectInitialSolution();
		System.out.println(checkConstraint());
	}

	private void selectInitialSolution() {
		// Find the biggest vehicle.
		Vehicle biggestVehicle = mVehicles.get(0);
		int biggestVehicleIndexOffset = 0;
		for (int i = 1; i < mVehicles.size(); i++) {
			Vehicle v = mVehicles.get(i);
			//mVehiclesPlans.add(new VehiclePlan(v, null));
			if (v.capacity() > biggestVehicle.capacity()) {
				biggestVehicle = v;
				biggestVehicleIndexOffset = i;
			}
		}
		
		/*VehiclePlan vehiclePlan = mVehiclesPlans.get(biggestVehicleIndex);
		int time = 1;
		for (Task task : mTasks) {
			vehiclePlan.addActionToVehiclePlan(new Action(ActionType.PICKUP, task, biggestVehicle, time));
			vehiclePlan.addActionToVehiclePlan(new Action(ActionType.DELIVERY, task, biggestVehicle, time+1));
			time = time + 2;
		}*/

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
	}

	private boolean checkConstraint() {
		return checkConstraint1() && checkConstraint2() && checkConstraint3()
				&& checkConstraint4() && checkConstraint5() && checkConstraint6()
				&& checkConstraint7() && checkConstraint8() && checkConstraint9();
	}

	// nextTask(t) = t: the task delivered after some task t cannot be the same task
	private boolean checkConstraint1() {
		for (int i = 0; i < nT; i++) {
			if (nextTask[i] == nextTaskDomain[i]) {
				return false;
			}
		}
		return true;
	}

	// nextTask(vk) = tj ⇒ time(tj) = 1
	private boolean checkConstraint2() {
		for (int i = nT; i < nextTask.length; i++) {
			if (nextTask[i] != null) {
				if (nextTask[i].getTime() != 1) {
					return false;
				}
			}
		}
		return true;
	}

	// nextTask(ti) = tj ⇒ time(tj) = time(ti) + 1
	private boolean checkConstraint3() {
		for (int i = 0; i < nT; i++) {
			if (nextTask[i] != null && nextTaskDomain[i] != null) {
				if (nextTask[i].getTime() != (((Action) nextTaskDomain[i]).getTime() + 1)) {
					return false;
				}
			}
		}
		return true;
	}

	// nextTask(vk) = tj ⇒ vehicle(tj) = vk
	private boolean checkConstraint4() {
		for (int i = nT; i < nextTask.length; i++) {
			if (nextTask[i] != null) {
				if (nextTask[i].getVehicle() != ((Vehicle) nextTaskDomain[i])) {
					return false;
				}
			}
		}
		return true;
	}

	// nextTask(ti) = tj ⇒ vehicle(tj) = vehicle(ti)
	private boolean checkConstraint5() {
		for (int i = 0; i < nT; i++) {
			if (nextTask[i] != null && nextTaskDomain[i] != null) {
				if (nextTask[i].getVehicle() != ((Action) nextTaskDomain[i]).getVehicle()) {
					return false;
				}
			}
		}
		return true;
	}

	// all tasks must be delivered: the set of values of the variables in the
	// nextTask array must be equal to the set of tasks T plus NV times the value NULL.
	private boolean checkConstraint6() {
		int nullCounter = 0; // Counts the number of null actions. At the end, must be equal to nV.
		Set<Task> notNullPickUpTasks = new HashSet<Task>();
		Set<Task> notNullDeliveryTasks = new HashSet<Task>();
		
		// For each action,
		for (int i = 0; i < nextTask.length; i++) {
			Action taskAction = nextTask[i];
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

	// the capacity of a vehicle cannot be exceeded: if load(ti) > capacity(vk) ⇒ vehicle(ti) != vk
	private boolean checkConstraint7() {
		// TODO
		return true;
	}

	// Checks that each vehicle actions "list" contains at most one pickUp and delivery actions for each task.
	// Furthermore, for each task, the pickUp action has to be present in the actions "list" before the delivery.
	private boolean checkConstraint8() {
		// For each vehicle, 
		for (int i = nT; i < nextTask.length; i++) {
			// we have an array of size the number of tasks (initialized at 0 everywhere).
			int[] checkSum = new int[mNumberOfTasks];
			// We look at all the actions of the vehicle starting with the first one,
			Action action = nextTask[i];
			// until we find a "null" action meaning we saw all the actions of that vehicle.
			while (action != null) {
				int taskIndex = action.getTask().id; // Index of the task (from 0 to numberOfTasks)
				int currentValue = checkSum[taskIndex]; // Current value of the checkSum array for that task
				// If we have a pickup action, we increment that value, otherwise we decrement it
				checkSum[taskIndex] = (action.getType() == ActionType.PICKUP) ? currentValue+1 : currentValue-1;
				// If that value is less than 0, it means we have a delivery action before pickUp action for the current task.
				// If that value is more than 1, it means we have two times a pickUp action for the same task.
				if (checkSum[taskIndex] < 0 || checkSum[taskIndex] > 1) {
					// so, for both cases, we return false as this is not possible!
					return false;
				}
				// We go to the next action.
				action = nextTask[action.getActionIndex()];
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

	private boolean checkConstraint9() {
		// TODO
		return true;
	}

}