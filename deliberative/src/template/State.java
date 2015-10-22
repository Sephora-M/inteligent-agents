package template;

import java.util.List;

import logist.task.Task;
import logist.task.TaskSet;
import logist.topology.Topology.City;

public class State {
	
	public static enum ActionType {PICK, MOVE}
	private final City mCurrentCity;
	private TaskSet toDeliver ;
	private TaskSet remainingTasks;
	private boolean isFull;


	public State(City currentCity, TaskSet remainingTasks) {
		mCurrentCity = currentCity;
		this.remainingTasks = remainingTasks;
		
	}
	
	public void removeTask(Task t){
		remainingTasks.remove(t);
	}
	
	public boolean isGoal(){
		return remainingTasks.isEmpty();
	}
	

	
	@Override
	public String toString() {
		return "State : (" + mCurrentCity + "," + remainingTasks.size() + "tasks remaining)"; 
	}
	
}