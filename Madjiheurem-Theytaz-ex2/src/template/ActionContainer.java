package template;

import logist.topology.Topology.City;

/**
 * This class models an action. It is made of an {@link ActionType} and of a destination
 * {@link City}.
 *
 */
public class ActionContainer {
	
	public static enum ActionType {PICK, MOVE}
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