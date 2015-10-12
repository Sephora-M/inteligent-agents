package template;

import logist.plan.Action;
import logist.topology.Topology.City;

public class ActionStruct {
	
//	private final Action mAction;
	private final City mDestinationCity;
	private final boolean deliverTask;
//	private final boolean isNeighbor;
	
	public ActionStruct(/*Action bestAction,*/ City destinationCity, boolean pickup/*, boolean isNeighbor*/) {
//		mAction = bestAction;
		mDestinationCity = destinationCity;
		this.deliverTask=pickup;
//		this.isNeighbor= isNeighbor;
	}
	
//	public Action getAction() {
//		return mAction;
//	}
	
	public City getDestinationCity() {
		return mDestinationCity;
	}
	
	public boolean getPickup(){
		return deliverTask;
	}
}