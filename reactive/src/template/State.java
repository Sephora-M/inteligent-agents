package template;

import java.util.List;

import logist.topology.Topology.City;

public class State {
	
	public static enum ActionType {PICK, MOVE}
	private final City mCurrentCity;
	private final City mDestinationCity;
	private List<ActionContainer> mActions;
	
	public State(City currentCity, City destinationCity) {
		mCurrentCity = currentCity;
		mDestinationCity = destinationCity;
		
		computePossibleActions();
	}
	
	public City getSourceCity() {
		return mCurrentCity;
	}
	
	public City getDestinationCity() {
		return mDestinationCity;
	}
	
	public List<ActionContainer> getPossibleAction() {
		return mActions;
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
	
	private void computePossibleActions() {
		//TODO
	}
 
}