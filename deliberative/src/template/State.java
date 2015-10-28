package template;

import java.util.Collection;
import java.util.LinkedList;

import logist.task.Task;
import logist.task.TaskSet;
import logist.topology.Topology.City;

public class State implements Comparable<State>{
	
	private final City mCurrentCity;
	private TaskSet toDeliver ;
	
	private boolean isFull; // true if the vehicle has reached its capacity
	private boolean exceedFull;
	private double heuristic;
	private TaskSet remainingTasks;
	public double g;
	
	public LinkedList<City> route;
	public double routeLength = 0.0;

	public TaskSet getToDeliver() {
		return toDeliver;
	}
	
	public TaskSet getRemainingTasks() {
		return remainingTasks;
	}
	
	public State(City currentCity, TaskSet remainingTasks, TaskSet toDeliver, boolean isFull, boolean exceedFullCap) {
		mCurrentCity = currentCity;
		this.remainingTasks = remainingTasks;
		this.toDeliver = toDeliver;
		this.isFull = isFull;
		this.exceedFull = exceedFullCap;
		heuristic = computeHval();
		route = new LinkedList<City>();
	}
	
	public void removeRemainingTask(Task t) {
		remainingTasks.remove(t);
	}
	
	public void addToRoute(City city) {
		if (!route.isEmpty())
			routeLength += route.getLast().distanceTo(city);
		route.add(city);
	}
	
	public void addAllToRoute(Collection<City> cities, double l) {
		routeLength += l;
		route.addAll(cities);
	}
	
	public void upDateToDeliverTask(TaskSet t) {
		toDeliver = t;
	}
	
	public boolean isGoal() {
		return remainingTasks.isEmpty() && toDeliver.isEmpty();
	}
	
	public double getHeuristic() {
		return heuristic;
	}
	
	public boolean getIsFull() {
		return isFull;
	}
	
	/*
	 * The heuristic is the following:
	 * If there's no task to pick up in the environment, set hVal to be the sum of the shortest paths
	 * from the current city to the destination of tasks being currently carried.
	 * 
	 * If there're still some tasks to pick up, the hVal is the sum of the shortest paths from the current city
	 * the pickup city of the remaining tasks.
	 * 
	 * The intuition is that we do not want to move too far away from cities with a task to pick up or drop off.
	 */
	private double computeHval(){
		double hVal = 0.0;
		

			for (Task task: remainingTasks) {
				hVal += mCurrentCity.distanceTo(task.pickupCity);
			}
			if (exceedFull) {
				for (Task task: toDeliver) {
					hVal += mCurrentCity.distanceTo(task.deliveryCity); 
				}
			}
		return hVal;
	}
	
	
	public String toStringRemainingTasks() {
		String s = " ";
		for (Task task: remainingTasks){
			s += "(" + task.pickupCity + ", " + task.deliveryCity + ")";
		}
		return s;
	}
	
	public String toStringToDeliverTasks() {
		String s = " ";
		for (Task task: toDeliver){
			s += "(" + task.pickupCity + ", " + task.deliveryCity + ")";
		}
		return s;
	}
	
	public City getCurrentCity() {
		return mCurrentCity;
	}
	
	public double f() {
		return heuristic + g;
	}
	
	@Override
	public String toString() {
		return "State : (" + mCurrentCity + "," + remainingTasks.size() + " tasks remaining, " + ")"; 
	}

	/**
	 * compare the states wrt their f(n) = g(n)+h(n) value
	 */
	@Override
	public int compareTo(State s) {
		double diff = s.f() - f();
		if (diff < 0)
			return -1;
		else if (diff > 0) 
			return 1;
		return 0;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof State)) {
			return false;
		} else if (obj == this) {
			return true;
		} else {
			State s = (State) obj;
			return mCurrentCity.equals(s.mCurrentCity) && remainingTasks.equals(s.remainingTasks) && toDeliver.equals(s.toDeliver);
		}	
	}
}