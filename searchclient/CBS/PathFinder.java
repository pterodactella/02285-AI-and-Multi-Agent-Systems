package searchclient.CBS;

import java.util.PriorityQueue;
import java.util.Stack;
import java.util.Comparator;

public class PathFinder implements Comparator<CBSNode> {
import searchclient.Action;
import searchclient.State;
import searchclient.CBS.Constraint;

public class PathFinder implements Comparator<CBSNode> {
	private State initialState;
	private static int triedTimes = 0;
	private static final int MAX_DEBUG_TRIALS = 3;

	public PathFinder(State initialState) {
		this.initialState = initialState;
		this.preprocessedData = preprocessedData;
	}

	public PlanStep[][] solveCBS() {
		CBSNode root = new CBSNode(this.initialState);
		root.solution = root.findPlan();
		root.totalCost = root.sumCosts();

		// TODO: Replace with priority qyueyue
		PriorityQueue<CBSNode> open = new PriorityQueue<>(this);
		open.add(root);

		while (!open.isEmpty()) {
			System.err.println("#########################################");
			CBSNode p = open.poll();
			GenericConflict c = p.findFirstConflict();

			if (c == null) {
				return p.solution;
			}

			if (c instanceof Conflict) {
				System.err.println("Conflict found: " + c.toString());
			} else if (c instanceof OrderedConflict) {
				System.err.println("OrderedConflict found: " + c.toString());
			}

			PathFinder.triedTimes++;

//			if (PathFinder.triedTimes >= PathFinder.MAX_DEBUG_TRIALS) {
//				System.exit(0);
//			}

			if (c instanceof Conflict) {

				for (int agentIndex : ((Conflict) c).agentIndexes) {
					CBSNode a = new CBSNode(p);
					a.constraints.add(new Constraint(agentIndex, ((Conflict) c).locationX, ((Conflict) c).locationY,
							((Conflict) c).timestamp));
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
			} else if (c instanceof OrderedConflict) {
				CBSNode a = new CBSNode(p);
				a.constraints.add(
						new Constraint(((OrderedConflict) c).followerIndex, ((OrderedConflict) c).forbiddenLocationX,
								((OrderedConflict) c).forbiddenLocationY, ((OrderedConflict) c).timestamp));
				// a.solution = p.solution

//			System.err.println("CONSTRAINTS FOR: " + agentIndex + ". TIMESTAMP: " + c.timestamp + ": ");
//			for (Constraint constr: a.constraints) {
//				System.err.print(constr.toString());
//			}
//			System.err.println();
//			a.findIndividualPlan(agentIndex, a.solution);
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
