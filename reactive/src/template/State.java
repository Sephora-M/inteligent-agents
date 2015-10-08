package template;

import logist.topology.Topology.City;

public class State {
	
	private final City mSource;
	private final City mDestination;
	
	public State(City source, City destination) {
		mSource = source;
		mDestination = destination;
	}
	
	public City getSourceCity() {
		return mSource;
	}
	
	public City getDestinationCity() {
		return mDestination;
	}
	
	@Override
	public String toString() {
		return "State (from, to): (" + mSource + "," + mDestination + ")"; 
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof State)) {
			return false;
		} else if (obj == this) {
			return true;
		} else {
			State s = (State) obj;
			return mSource.equals(s.mSource) && mDestination.equals(s.mDestination);
		}
	}

}