package template;

import logist.plan.Action;
import logist.topology.Topology.City;

public class ActionContainer {
	
	private final Action mAction;
	private final City mDestinationCity;
	private double mReward;
	
	public ActionContainer(Action bestAction, City destinationCity, double reward) {
		mAction = bestAction;
		mDestinationCity = destinationCity;
		mReward = reward;
	}
	
	public Action getAction() {
		return mAction;
	}
	
	public City getDestinationCity() {
		return mDestinationCity;
	}
	
	public double getReward() {
		return mReward;
	}
	
}