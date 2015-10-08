package template;

public class StateActionPair<Left, Right> {
	
	private final Left mState;
	private final Right mAction;
	
	public StateActionPair(Left s, Right a) {
		mState = s;
		mAction = a;
	}

}
