package searchclient.CBS;

import java.util.PriorityQueue;
// import java.util.Stack;
import java.util.Comparator;

// import searchclient.Action;
import searchclient.State;
// import searchclient.CBS.Constraint;

public class PathFinder implements Comparator<CBSNode> {
	private State initialState;
	private static int triedTimes = 0;
	private static final int MAX_DEBUG_TRIALS=3;

	public PathFinder(State initialState) {
		this.initialState = initialState;
	}

	public PlanStep[][] solveCBS() {
		CBSNode root = new CBSNode(this.initialState);
		root.solution = root.findPlan();
		root.totalCost = root.sumCosts();


		PriorityQueue<CBSNode> open = new PriorityQueue<>(this);
		open.add(root);

		while (!open.isEmpty()) {
			CBSNode p = open.poll();
			Conflict c = p.findFirstConflict();

			if (c == null) {
				return p.solution;
			}
			
			System.err.println("Conflict found: " + c.toString());
			PathFinder.triedTimes++;
			System.err.println("#########################################");
			
			//			if (PathFinder.triedTimes >= PathFinder.MAX_DEBUG_TRIALS) {
			//				System.exit(0);
			//			}

			for (int agentIndex : c.agentIndexes) {
				CBSNode a = new CBSNode(p);
				a.constraints.add(new Constraint(agentIndex, c.locationX, c.locationY, c.timestamp));
				// a.solution = p.solution
				
				//				System.err.println("CONSTRAINTS FOR: " + agentIndex + ". TIMESTAMP: " + c.timestamp + ": ");
				//				for (Constraint constr: a.constraints) {
				//					System.err.print(constr.toString());
				//				}
				//				System.err.println();
				//				a.findIndividualPlan(agentIndex, a.solution);
				a.solution = a.findPlan();
				a.totalCost = a.sumCosts();

				// TODO: use a number instead of infinity
				open.add(a);
			}
		}

		return null;
	}

	@Override
	public int compare(CBSNode n1, CBSNode n2) {
		return Integer.compare(n1.totalCost, n2.totalCost);
	}

}
