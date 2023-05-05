package searchclient.CBS;

import java.util.Stack;

import searchclient.Action;
import searchclient.State;
import searchclient.CBS.Constraint;

public class PathFinder {
	private State initialState;
	
	public PathFinder(State initialState) {
		this.initialState = initialState;
	}
	
	public Action[][] solveCBS() {
		CBSNode root = new CBSNode(this.initialState);
		root.solution = root.findPlan();
		root.cost = root.sumCosts();
		
		//TODO: Replace with priority qyueyue
		Stack<CBSNode> open = new Stack<>();
		open.add(root);
		
		
		while(!open.empty()) {
			CBSNode p = open.pop();
			Conflict c = p.findFirstConflict();
			
			
			if(c == null) {
				return p.solution;
			}
			
			 for (int agentIndex : c.agentIndexes) {
				 CBSNode a = new CBSNode(this.initialState);
				 a.constraints.add(new Constraint(agentIndex,c.locationX,c.locationY,c.timestamp));
				 //a.solution = p.solution
				 a.solution = new Action[p.solution.length][];
				 for(int i = 0; i < p.solution.length; i++)
					    a.solution[i] = p.solution[i].clone();
				 //TODO: Recalculate only for one(agentIndex)
				 a.solution = a.findPlan();
				 a.cost = a.sumCosts();
				 
				 //TODO: use a number instead of infinity
				 open.add(a);
			 }
		}
		
		return null;
	}

}
