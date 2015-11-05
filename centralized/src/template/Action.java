package template;

import logist.simulation.Vehicle;
import logist.task.Task;

public class Action {

	public static enum ActionType {PICKUP, DELIVERY}
	
	private ActionType mActionType;
	private Task mTask;
	private Vehicle mVehicle;
	private int mTime;

	public Action(ActionType actionType, Task task, Vehicle vehicle, int time) {
		mActionType = actionType;
		mTask = task;
		mVehicle = vehicle;
		mTime = time;
	}
	
	public Action(ActionType actionType, Task task) {
		mActionType = actionType;
		mTask = task;
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
	
}