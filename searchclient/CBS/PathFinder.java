package searchclient.CBS;

import java.util.PriorityQueue;
import java.util.Stack;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import searchclient.Action;
import searchclient.Color;
import searchclient.State;
import searchclient.CBS.Constraint;

//Improve the conflict resolution strategy: When a conflict is detected, you're currently considering all the agents involved in the conflict and adding constraints to each of them. This may not be the most efficient way to resolve conflicts. Instead, you could implement more advanced conflict resolution strategies like prioritized planning, where agents are ordered by priority and conflicts are resolved sequentially. You can also experiment with different heuristics for choosing which agent to resolve the conflict.
// Improve the CBSNode comparison: In the compare method of the PathFinder class, you're comparing two CBSNodes only based on their total cost. You could also include other factors in the comparison, like the number of constraints or the depth of the search tree. This might help prioritize nodes that are more likely to lead to a successful conflict resolution.
// Implement a tie-breaking strategy: In case two nodes have the same cost, you can use a tie-breaking strategy to decide which node to expand next. You can consider factors like the number of conflicts, the depth of the search tree, or the number of constraints.
// Optimize the search: You can optimize the search process using techniques like ID-CBS (Increasing Cost Tree Search for Conflict-Based Search) that incrementally increases the cost bound for the search. This can help you find the optimal solution faster.
// Add delay calculation logic: You have commented out the code that calculates the delay and introduces a delay in the agent's plan. Implementing a method to calculate the delay and introducing it into the agent's plan could help resolve conflicts. You can uncomment the code and implement a suitable delay calculation logic, considering factors like agent distance, agent priorities, and available alternative paths.

public class PathFinder implements Comparator<CBSNode> {
	private State initialState;
	private HashMap<Color, List<Integer>> preprocessedData;
	private static int triedTimes = 0;
	private static final int MAX_DEBUG_TRIALS = 3;

	public PathFinder(State initialState,  HashMap<Color, List<Integer>> preprocessedData) {
		this.initialState = initialState;
		this.preprocessedData = preprocessedData;
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

			if (PathFinder.triedTimes >= PathFinder.MAX_DEBUG_TRIALS) {
				System.exit(0);
			}

			for (int agentIndex : c.agentIndexes) {
				CBSNode a = new CBSNode(p);
				a.constraints.add(new Constraint(agentIndex, c.locationX, c.locationY, c.timestamp));
				// a.solution = p.solution

				// System.err.println("CONSTRAINTS FOR: " + agentIndex + ". TIMESTAMP: " +
				// c.timestamp + ": ");
				// for (Constraint constr: a.constraints) {
				// System.err.print(constr.toString());
				// }
				// System.err.println();
				// a.findIndividualPlan(agentIndex, a.solution);
				a.solution = a.findPlan();
				System.err.println("plan found");

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
