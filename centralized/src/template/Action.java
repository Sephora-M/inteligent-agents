package template;

import logist.simulation.Vehicle;
import logist.task.Task;

public class Action {

	public static enum ActionType {PICKUP, DELIVERY}
	
	private ActionType mActionType;
	private Task mTask;
	private Vehicle mVehicle;
	private int mTime;
	private int mActionIndex;

	public Action(ActionType actionType, Task task, Vehicle vehicle, int time, int actionIndex) {
		mActionType = actionType;
		mTask = task;
		mVehicle = vehicle;
		mTime = time;
		mActionIndex = actionIndex;
	}
	
	public Action(ActionType actionType, Task task, int actionIndex) {
		mActionType = actionType;
		mTask = task;
		mActionIndex = actionIndex;
	}
	
	public ActionType getType() {
		return mActionType;
	}
	
	public Task getTask() {
		return mTask;
	}
	
	public void setVehicle(Vehicle v) {
		mVehicle = v;
	}
	
	public void setTime(int time) {
		mTime = time;
	}
	
	public Vehicle getVehicle() {
		return mVehicle;
	}
	
	public int getTime() {
		return mTime;
	}
	
	public int getActionIndex() {
		return mActionIndex;
	}
	
	public void setActionIndex(int actionIndex) {
		mActionIndex = actionIndex;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (obj == null || obj.getClass() != getClass()) {
			return false;
		} else {
			Action a = (Action) obj;
			if ((mActionType != a.mActionType) || (mTask != a.mTask)) {
				return false;
			} else {
				return true;
			}
		}
	}
	
	@Override
    public int hashCode() {
        int result = mActionType == ActionType.DELIVERY ? 1 : 0;
        return 31 * result + mTask.hashCode();
    }
	
	@Override
	public String toString() {
		return mActionType.toString() + " at time " + mTime + " of " + mTask + " by the vehicle " + mVehicle;
	}
	
}