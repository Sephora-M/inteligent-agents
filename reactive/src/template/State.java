package template;

import logist.topology.Topology.City;

/**
 * This class models a state. It is made of a source {@link City} and a destination
 * {@link City}. Furthermore, it also has the "TValue" and "Vvalue" values.
 * You can get the best action to go for that state with the {@link #getBestAction()} method.
 *
 */
public class State {
	
	private final City mCurrentCity;
	private final City mDestinationCity;
	private double Vvalue;
	private double Tvalue;
	private ActionContainer bestAction;

	public State(City currentCity, City destinationCity) {
		mCurrentCity = currentCity;
		mDestinationCity = destinationCity;
		Vvalue = 0;
		Tvalue = 0;
		bestAction = null;
	}
	
	/**
	 * @return The bestion action to do for that state.
	 */
	public ActionContainer getBestAction() {
		return bestAction;
	}

	public void setBestAction(ActionContainer bestAction) {
		this.bestAction = bestAction;
	}
	
	public double getTvalue() {
		return Tvalue;
	}

	public void setTvalue(double tvalue) {
		Tvalue = tvalue;
	}
	
	public double getVvalue() {
		return Vvalue;
	}

	public void setVvalue(double vvalue) {
		Vvalue = vvalue;
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