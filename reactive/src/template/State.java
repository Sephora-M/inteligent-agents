package template;

import java.util.List;

import logist.topology.Topology.City;

public class State {
	
	public static enum ActionType {PICK, MOVE}
	private final City mCurrentCity;
	private final City mDestinationCity;
	private double value;
	private ActionStruct bestAction;
//	private List<ActionStruct> actions;
	
	public ActionStruct getBestAction() {
		return bestAction;
	}

	public void setBestAction(ActionStruct bestAction) {
		this.bestAction = bestAction;
	}

	public State(City currentCity, City destinationCity) {
		mCurrentCity = currentCity;
		mDestinationCity = destinationCity;
		value = 0.0;
	}
	
	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}

	public City getSourceCity() {
		return mCurrentCity;
	}
	
	public City getDestinationCity() {
		return mDestinationCity;
	}
	
	@Override
	public String toString() {
		return "State (from, to): (" + mCurrentCity + "," + mDestinationCity + ")"; 
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof State)) {
			return false;
		} else if (obj == this) {
			return true;
		} else {
			State s = (State) obj;
			return mCurrentCity.equals(s.mCurrentCity) && mDestinationCity.equals(s.mDestinationCity);
		}
	}

}