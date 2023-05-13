package searchclient.CBS;

import java.util.PriorityQueue;
import java.util.Stack;

import searchclient.Color;
import searchclient.State;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

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
