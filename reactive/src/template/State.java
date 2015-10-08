package template;

import logist.topology.Topology.City;

public class State {
	
	public static enum ActionType {PICK, MOVE}
	private final City mCurrentCity;
	private final City mDestinationCity;
	private List<ActionStruct> actions;
	
	public State(City currentCity, City destinationCity) {
		mCurrentCity = currentCity;
		mDestinationCity = destinationCity;
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