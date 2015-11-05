package template;

import java.util.ArrayList;
import java.util.List;

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
		return checkConstraint1() && checkConstraint2() && checkConstraint3() && checkConstraint4()
				&& checkConstraint5() && checkConstraint6() && checkConstraint7() && checkConstraint8();
	}
	
	private boolean checkConstraint1() {
		for (int i = 0; i < nT; i++) {
			if (nextTask[i] == nextTaskDomain[i]) {
				return false;
			}
		}
		return true;
	}
	
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
	
	private boolean checkConstraint3() {
		return false;
	}
	
	private boolean checkConstraint4() {
		return false;
	}
	
	private boolean checkConstraint5() {
		return false;
	}
	
	private boolean checkConstraint6() {
		return false;
	}
	
	private boolean checkConstraint7() {
		return false;
	}
	
	private boolean checkConstraint8() {
		return false;
	}

}