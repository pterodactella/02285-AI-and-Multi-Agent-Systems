package searchclient.CBS;
// import java.util.Stack;
import searchclient.Action;
import searchclient.State;
// import searchclient.CBS.Constraint;
import java.util.PriorityQueue;
import java.util.Comparator;

public class PathFinder {
	private State initialState;
	
	public PathFinder(State initialState) {
		this.initialState = initialState;
	}
	
	public Action[][] solveCBS() {
		CBSNode root = new CBSNode(this.initialState);
		root.solution = root.findPlan();
		root.cost = root.sumCosts();

		PriorityQueue<CBSNode> open = new PriorityQueue<>(new Comparator<CBSNode>() {
			@Override
			public int compare(CBSNode n1, CBSNode n2) {
				return Integer.compare(n1.cost, n2.cost);
			}
		});
		open.add(root);
		
		
		while(!open.isEmpty()) {
			CBSNode p = open.poll();
			Conflict c = p.findFirstConflict();
			
			
			if(c == null) {
				return p.solution;
			}
			
			 for (int agentIndex : c.agentIndexes) {
				 CBSNode a = new CBSNode(this.initialState);
				 a.constraints.add(new Constraint(agentIndex,c.locationX,c.locationY,c.timestamp));

				 a.solution = new Action[p.solution.length][];
				 for(int i = 0; i < p.solution.length; i++)
					    a.solution[i] = p.solution[i].clone();
						

				Action[][] individualPlans = a.findPlan();
				a.solution[agentIndex] = individualPlans[agentIndex];
				a.cost = a.sumCosts();
				 
				 a.cost = Integer.MAX_VALUE;
				 open.add(a);
			 }
		}
		
		return null;
	}

}

