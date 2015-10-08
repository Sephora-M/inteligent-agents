package template;

import logist.plan.Action;
import logist.topology.Topology.City;

public class ActionStruct {
	
	private final Action mAction;
	private final City mDestinationCity;
	
	public ActionStruct(Action bestAction, City destinationCity) {
		mAction = bestAction;
		mDestinationCity = destinationCity;
	}
	
	public Action getAction() {
		return mAction;
	}
	
	public City getDestinationCity() {
		return mDestinationCity;
	}
	
}