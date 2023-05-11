package searchclient.CBS;

import java.util.PriorityQueue;
import java.util.Stack;
import java.util.Comparator;

import searchclient.Action;
import searchclient.State;
import searchclient.CBS.Constraint;

//Improve the conflict resolution strategy: When a conflict is detected, you're currently considering all the agents involved in the conflict and adding constraints to each of them. This may not be the most efficient way to resolve conflicts. Instead, you could implement more advanced conflict resolution strategies like prioritized planning, where agents are ordered by priority and conflicts are resolved sequentially. You can also experiment with different heuristics for choosing which agent to resolve the conflict.
// Improve the CBSNode comparison: In the compare method of the PathFinder class, you're comparing two CBSNodes only based on their total cost. You could also include other factors in the comparison, like the number of constraints or the depth of the search tree. This might help prioritize nodes that are more likely to lead to a successful conflict resolution.
// Implement a tie-breaking strategy: In case two nodes have the same cost, you can use a tie-breaking strategy to decide which node to expand next. You can consider factors like the number of conflicts, the depth of the search tree, or the number of constraints.
// Optimize the search: You can optimize the search process using techniques like ID-CBS (Increasing Cost Tree Search for Conflict-Based Search) that incrementally increases the cost bound for the search. This can help you find the optimal solution faster.
// Add delay calculation logic: You have commented out the code that calculates the delay and introduces a delay in the agent's plan. Implementing a method to calculate the delay and introducing it into the agent's plan could help resolve conflicts. You can uncomment the code and implement a suitable delay calculation logic, considering factors like agent distance, agent priorities, and available alternative paths.

public class PathFinder implements Comparator<CBSNode> {
	private State initialState;
	private static int triedTimes = 0;
	private static final int MAX_DEBUG_TRIALS = 3;

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
			int[] delayAndAgentToDelay = calculateDelay(c, p.solution);
			if (delayAndAgentToDelay[1] > 0) {
					p.solution = introduceDelay(p.solution, delayAndAgentToDelay[0], delayAndAgentToDelay[1]);
					p.totalCost = p.sumCosts();
					printDelayedPlans(p.solution);

				}
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


	private int[] calculateDelay(Conflict conflict, PlanStep[][] individualPlans) {
		int delay = 0;
		int agentIndex1 = conflict.agentIndexes[0];
		int agentIndex2 = conflict.agentIndexes[1];
	
		// Determine the agent with lower priority 
		int agentToDelay = agentIndex1 > agentIndex2 ? agentIndex1 : agentIndex2;
		int otherAgent = agentIndex1 < agentIndex2 ? agentIndex1 : agentIndex2;
	
		// Calculate the delay as the difference between the plan lengths of the agents
		delay = individualPlans[otherAgent].length - individualPlans[agentToDelay].length;
	
		return new int[]{agentToDelay, delay};
	}
	private PlanStep[][] introduceDelay(PlanStep[][] plans, int agentToDelay, int delay) {
		// Introduce a delay for the agent with lower priority by extending its plan with NoOps
		PlanStep[][] delayedPlans = new PlanStep[plans.length][];
		for (int i = 0; i < plans.length; i++) {
			if (i == agentToDelay) {
				int numSteps = plans[i].length + delay;
				delayedPlans[i] = new PlanStep[numSteps];
				System.arraycopy(plans[i], 0, delayedPlans[i], 0, plans[i].length);
				for (int j = plans[i].length; j < numSteps; j++) {
					delayedPlans[i][j] = new PlanStep(Action.NoOp, plans[i][plans[i].length - 1].locationX,
							plans[i][plans[i].length - 1].locationY, j);
				}
			} else {
				delayedPlans[i] = plans[i];
			}
		}
		return delayedPlans;
	}


	private void printDelayedPlans(PlanStep[][] delayedPlans) {
		System.err.println("Delayeeeeeeeeeeeddddddd plans:");
		for (int i = 0; i < delayedPlans.length; i++) {
			System.err.print("Agent " + i + ": ");
			for (int j = 0; j < delayedPlans[i].length; j++) {
				System.err.print(delayedPlans[i][j].toString() + ", ");
			}
			System.err.println();
		}
	}
	

}



