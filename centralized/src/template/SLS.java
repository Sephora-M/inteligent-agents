package template;

import java.util.ArrayList;
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
	private List<VehiclePlan> mVehiclesPlans;
	private int nT;
	private int nV;

	public SLS(List<Vehicle> vehicles, TaskSet tasks) {
		mVehicles = vehicles; // Deep copy necessary?
		mTasks = TaskSet.copyOf(tasks);
		mVehiclesPlans = new ArrayList<VehiclePlan>();
		nT = tasks.size() * 2;  // Number of tasks (pickup and delivery => *2)
		nV = vehicles.size();  // Number of vehicles.
		nextTask = new Action[nT + nV];
		nextTaskDomain = new Object[nT + nV];

		int i = 0;
		for (Task task : tasks) {
			nextTaskDomain[i] = new Action(ActionType.PICKUP, task);
			nextTaskDomain[i+1] = new Action(ActionType.DELIVERY, task);
			i = i + 2;
		}

		for (Vehicle v : vehicles) {
			nextTaskDomain[i] = v;
			i++;
		}
	}

	public void stochLocalSearch() {
		selectInitialSolution();
		
		// TODO Remove
		System.out.println("1 => " + checkConstraint1());
		System.out.println("2 => " + checkConstraint2());
		System.out.println("3 => " + checkConstraint3());
		System.out.println("4 => " + checkConstraint4());
		System.out.println("5 => " + checkConstraint5());
		System.out.println("6 => " + checkConstraint6());
		System.out.println("7 => " + checkConstraint7());
		System.out.println("8 => " + checkConstraint8());
		System.out.println("9 => " + checkConstraint9());
	}

	private void selectInitialSolution() {
		// Find the biggest vehicle.
		Vehicle biggestVehicle = mVehicles.get(0);
		int biggestVehicleIndex = 0;
		for (int i = 1; i < mVehicles.size(); i++) {
			Vehicle v = mVehicles.get(i);
			mVehiclesPlans.add(new VehiclePlan(v, null));
			if (v.capacity() > biggestVehicle.capacity()) {
				biggestVehicle = v;
				biggestVehicleIndex = i;
			}
		}

		// Give all the tasks to that vehicle.
		VehiclePlan vehiclePlan = mVehiclesPlans.get(biggestVehicleIndex);
		int time = 1;
		for (Task task : mTasks) {
			vehiclePlan.addActionToVehiclePlan(new Action(ActionType.PICKUP, task, biggestVehicle, time));
			vehiclePlan.addActionToVehiclePlan(new Action(ActionType.DELIVERY, task, biggestVehicle, time+1));
			time = time + 2;
		}

		nextTask[nT + biggestVehicleIndex] = (Action) nextTaskDomain[0];
		nextTask[nT + biggestVehicleIndex].setVehicle(biggestVehicle);
		nextTask[nT + biggestVehicleIndex].setTime(1);

		for (int i = 1; i < nT-1; i++) {
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
		int nullCounter = 0;
		Set<Action> notNullActions = new HashSet<Action>();
		for (int i = 0; i < nextTask.length; i++) {
			Action task = nextTask[i];
			if (task == null) {
				nullCounter++;
			} else {
				notNullActions.add(task);
			}
		}
		if (nullCounter != nV) {
			return false;
		} else {
			for (Task t : mTasks) {
				if (!notNullActions.contains(t)) {
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

	private boolean checkConstraint8() {
		// TODO
		return true;
	}

	private boolean checkConstraint9() {
		// TODO
		return true;
	}

}