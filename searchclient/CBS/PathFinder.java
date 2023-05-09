package searchclient.CBS;

import java.util.PriorityQueue;
import java.util.Stack;
import java.util.Comparator;

import searchclient.Action;
import searchclient.State;
import searchclient.CBS.Constraint;

public class PathFinder implements Comparator<CBSNode> {
	private State initialState;

	public PathFinder(State initialState) {
		this.initialState = initialState;
	}

	public PlanStep[][] solveCBS() {
		CBSNode root = new CBSNode(this.initialState);
		root.solution = root.findPlan();
		root.cost = root.sumCosts();
		//  use priority queue
		PriorityQueue<CBSNode> open = new PriorityQueue<>(new Comparator<CBSNode>() {
			@Override
			public int compare(CBSNode n1, CBSNode n2) {
				return Integer.compare(n1.cost, n2.cost);
			}
		});
		open.add(root);

		while (!open.isEmpty()) {
			CBSNode p = open.poll();
			Conflict c = p.findFirstConflict();

			if (c == null) {
				return p.solution;
			}

			for (int agentIndex : c.agentIndexes) {
				CBSNode a = new CBSNode(this.initialState);
				a.constraints.add(new Constraint(agentIndex, c.locationX, c.locationY, c.timestamp));
				// a.solution = p.solution
				a.solution = new PlanStep[p.solution.length][];
				for (int i = 0; i < p.solution.length; i++)
					a.solution[i] = p.solution[i].clone();

					
				// Recalculate only for one(agentIndex)
				PlanStep[][] individualPlans = a.findPlan();
				a.solution[agentIndex] = individualPlans[agentIndex];
				a.cost = a.sumCosts();

				// use a number instead of infinity
				a.cost = Integer.MAX_VALUE;
				open.add(a);
				
			}
			
		}

		return null;
	}

	@Override
	public int compare(CBSNode n1, CBSNode n2) {
		return Integer.compare(n1.cost, n2.cost);
	}

}
