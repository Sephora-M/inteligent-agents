package template;

import logist.topology.Topology.City;

public class ActionContainer {
	
	private final ReactiveTemplate.ActionType mActionType;
	private final City mDestinationCity;
	
	public ActionContainer(ReactiveTemplate.ActionType actionType, City destinationCity) {
		mActionType = actionType;
		mDestinationCity = destinationCity;
	}
	
	public ReactiveTemplate.ActionType getActionType() {
		return mActionType;
	}
	
	public City getDestinationCity() {
		return mDestinationCity;
	}
	
}