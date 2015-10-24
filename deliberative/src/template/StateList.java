package template;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;

public class StateList {
	
	PriorityQueue<State> list;
		
	public StateList(ArrayList<State> list){
				

		Comparator<State> StateHvalComparator 
		= new Comparator<State>() {
			public int compare(State s1, State s2) {
				return s2.compareTo(s1);
//				return s1.compareTo(s2);
			}

		};
		
		this.list = new PriorityQueue<State>(10,StateHvalComparator);
		list.addAll(list);
	}
	
	public StateList(){
		Comparator<State> StateHvalComparator 
		= new Comparator<State>() {
			public int compare(State s1, State s2) {
				return s2.compareTo(s1);
//				return s1.compareTo(s2);
			}

		};
		
		this.list = new PriorityQueue<State>(10,StateHvalComparator);
	}
	
	public void add(State s){
		list.add(s);
	}
	
	public PriorityQueue<State> getList(){
		return list;
	}

	public State removeFirst() {
		// TODO Auto-generated method stub
		return list.poll();
	}

	public boolean addAll(ArrayList<State> sprime) {
		// TODO Auto-generated method stub
		return list.addAll(sprime);
	}

	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return list.isEmpty();
	}
	
	
	
}
