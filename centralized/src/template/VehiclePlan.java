package template;

import java.util.ArrayList;
import java.util.List;

import logist.simulation.Vehicle;

public class VehiclePlan {

	private Vehicle mVehicle;
	private int mCapacity;
	private List<Action> mActions;

	public VehiclePlan(Vehicle vehicle, List<Action> actions) {
		mVehicle = vehicle;
		mCapacity = vehicle.capacity();
		mActions = (actions == null) ? new ArrayList<Action>() : actions;
	}

	public boolean addActionToVehiclePlan(Action action) {
		boolean added = false;
		switch (action.getType()) {
		case PICKUP:
			if (action.getTask().weight > mCapacity) {
				added = false;
			} else {
				mActions.add(action);
				mCapacity -= action.getTask().weight;
				added = true;
			}
			break;
		case DELIVERY:
			mActions.add(action);
			mCapacity += action.getTask().weight;
			break;
		default:
			break;
		}
		return added;
	}

	public List<Action> getVehicleAction() {
		// Deep copy ?
		return mActions;
	}
}