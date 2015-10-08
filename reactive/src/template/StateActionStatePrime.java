package template;

public class StateActionStatePrime {
	
	private final State mState;
	private final ActionContainer mAction;
	private final State mStatePrime;
	
	public StateActionStatePrime(State s, ActionContainer a, State sPrime) {
		mState = s;
		mAction = a;
		mStatePrime = sPrime;
	}
	
	public State getState() {
		return mState;
	}
	
	public ActionContainer getAction() {
		return mAction;
	}
	
	public State getStatePrime() {
		return mStatePrime;
	}

}