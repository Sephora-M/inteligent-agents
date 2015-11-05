package template;

import java.util.ArrayList;
import java.util.List;

import template.Action.ActionType;
import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskSet;

public class SLS {
	
	private List<Vehicle> mVehicles;
	private TaskSet mTasks;
	private List<VehiclePlan> mVehiclesPlans;
	private int nT;
	private int nV;
	
	public SLS(List<Vehicle> vehicles, TaskSet tasks, int _nT, int _nV) {
		mVehicles = vehicles; // Deep copy necessary?
		mTasks = TaskSet.copyOf(tasks);
		mVehiclesPlans = new ArrayList<VehiclePlan>();
		nT = _nT;
		nV = _nV;
	}
	
	public void selectInitialSolution() {
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
		for (Task task : mTasks) {
			vehiclePlan.addActionToVehiclePlan(new Action(ActionType.PICKUP, task));
			vehiclePlan.addActionToVehiclePlan(new Action(ActionType.DELIVERY, task));
		}
		
		CentralizedTemplate.nextTask[nT + biggestVehicleIndex] = (Action) CentralizedTemplate.nextTaskDomain[0];
		
		for (int i = 1; i < nT-1; i++) {
			CentralizedTemplate.nextTask[i-1] = (Action) CentralizedTemplate.nextTaskDomain[i];
		}
		CentralizedTemplate.nextTask[nT-1] = null;
	}
	
	public void stochLocalSearch() {
		selectInitialSolution();
	}

}