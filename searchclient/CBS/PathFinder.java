package searchclient.CBS;

import java.util.PriorityQueue;
import java.util.Stack;
import java.util.ArrayList;
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

	public PathFinder(State initialState, HashMap<Color, List<Integer>> preprocessedData) {
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
			List<Integer> prioritizedAgents = prioritizeAgents();
			CBSNode chosenNode = null;
			int minSteps = Integer.MAX_VALUE;
			for (int agentIndex : c.agentIndexes) {
				int prioritizedAgentIndex = prioritizedAgents.get(agentIndex);
				System.err.println("!!!!Prioritized agent " + prioritizedAgentIndex);
				CBSNode a = new CBSNode(p);
				a.constraints.add(new Constraint(prioritizedAgentIndex, c.locationX, c.locationY, c.timestamp));
				a.solution = a.findPlan();
				System.err.println("Plan found");

				if (a.solution != null && a.solution.length < minSteps) {
					chosenNode = a;
					minSteps = a.solution.length;
				}
			}

			if (chosenNode != null) {
				chosenNode.totalCost = chosenNode.sumCosts();
				open.add(chosenNode);
			}
		}
		return null;
	}

	@Override
	public int compare(CBSNode n1, CBSNode n2) {
		return Integer.compare(n1.totalCost, n2.totalCost);
	}

	private List<Integer> prioritizeAgents() {
		// Create a list of agent indexes
		List<Integer> agentIndexes = new ArrayList<>();
		for (int i = 0; i < initialState.agentRows.length; i++) {
			agentIndexes.add(i);
		}

		// Sort the agent indexes based on the preprocessed data
		agentIndexes.sort((a, b) -> {
			Color aColor = initialState.agentColors[a];
			Color bColor = initialState.agentColors[b];
			List<Integer> aDistances = preprocessedData.get(aColor);
			List<Integer> bDistances = preprocessedData.get(bColor);

			// Calculate the total distances of other agents of the same color
			int aSumOthers = aDistances.stream().filter(dist -> dist != aDistances.get(a)).mapToInt(Integer::intValue)
					.sum();
			int bSumOthers = bDistances.stream().filter(dist -> dist != bDistances.get(b)).mapToInt(Integer::intValue)
					.sum();

			return Integer.compare(aSumOthers, bSumOthers);
		});

		return agentIndexes;
	}

}
