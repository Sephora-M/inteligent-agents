package template;

import logist.plan.Action;
import logist.topology.Topology.City;

public class ActionStruct {
	
	private final Action mAction;
	private final City mDestinationCity;
	private final double mReward;
	
	public ActionStruct(Action bestAction, City destinationCity, double reward) {
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