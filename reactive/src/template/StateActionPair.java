package reactive.src.template;

public class StateActionPair {
	
	private final State mState;
	private final ActionContainer mAction;
	
	public StateActionPair(State s, ActionContainer a) {
		mState = s;
		mAction = a;
	}
	
	public State getState() {
		return mState;
	}
	
	public ActionContainer getAction() {
		return mAction;
	}

}
