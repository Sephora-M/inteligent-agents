package template;

import logist.task.Task;

public class Action {

	public static enum ActionType {PICKUP, DELIVERY}
	
	private ActionType mActionType;
	private Task mTask;

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
	
}