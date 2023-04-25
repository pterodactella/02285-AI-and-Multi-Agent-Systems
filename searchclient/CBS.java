package searchclient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.PriorityQueue;

public class CBS {
	public static Action[][] search(State initialState, Frontier frontier) {

		// Create an agent object for all agents, make an array of them
		// The object has the cost, solution, constraints
		PriorityQueue<Agent> agents = new PriorityQueue<Agent>();
		boolean solutionFound = false;
		ArrayList<Agent> checkedAgents = new ArrayList<Agent>();

		for (int i = 0; i < initialState.agentCols.length; i++) {
			agents.add(
					new Agent(State.agentColors[i].toString() + i, State.agentColors[i], initialState, frontier, i));
			// System.out.println(agents.size());
			// System.out.println("agents.size()");
		}
		System.err.println("Initial agent list: " + agents.toString());

		ArrayList<Constraints> conflictFreePaths = new ArrayList<>(64);
		// Map<int, Array<(int, int)> objects = new HashMap<String, Object>();

		// iterate over the array of agents
		while (!solutionFound) {
			// int minimumCost = agents.get(0).cost;

			Agent agentWithLowestCost = agents.peek();
			System.err.println("Current agent with lowest cost: " + agentWithLowestCost.toString());

			boolean allConstraintsConflictFree = true;
			// should check if the conflict is already covered by an existing constraint
			// before adding a new constraint
			// loop through all constraints within
			for (int agentConstI = 0; agentConstI < agentWithLowestCost.constraints.length; agentConstI++) {
				Constraints currentConstraint = agentWithLowestCost.constraints[agentConstI];
				boolean conflictFound = false;

				for (int globalConstJ = 0; globalConstJ < conflictFreePaths.size(); globalConstJ++) {
					Constraints globalConstraint = conflictFreePaths.get(globalConstJ);
					if (globalConstraint.isConflicting(currentConstraint)) {
						conflictFound = true;
						break;
					}
				}
				if (conflictFound) {
					System.out.println("Conflict found between: " + currentConstraint.toString() + " and "
							+ initialState.globalConstraints.toString());

					allConstraintsConflictFree = false;

					// This constraint conflicts with an existing constraint, so add the new
					// constraint
					Constraints[] newConstraints = Arrays.copyOf(agentWithLowestCost.constraints,
							agentWithLowestCost.constraints.length + 1);
					newConstraints[agentWithLowestCost.constraints.length] = currentConstraint;
					agents.remove(agentWithLowestCost);

					Agent newAgent1 = new Agent(agentWithLowestCost.agentId, agentWithLowestCost.agentColor,
							initialState, frontier, agentWithLowestCost.agentIndex, newConstraints);
					System.out.println("Creating new agent with updated constraints: " + newAgent1.toString());

					agents.add(newAgent1);
				}
			}

			if (allConstraintsConflictFree) {
				// All constraints for the current agent are conflict-free, so add them to the
				// list of conflict-free
				// paths
				System.out.println("All constraints conflict-free for agent: " + agentWithLowestCost.agentId);

				conflictFreePaths.addAll(Arrays.asList(agentWithLowestCost.constraints));

				solutionFound = agents.isEmpty(); // Set the flag to true if all agents have been checked
			}

			checkedAgents.add(agents.poll());
		}

		if (checkedAgents.isEmpty()) {
			// The initial state is already a goal state, so return its solution
			return GraphSearch.search(initialState, frontier);
		} else {
			System.out.println("Solution found for all agents. Checked agents: " + checkedAgents.toString());

			// Return the solution of the first agent that was checked
			return checkedAgents.get(0).solution;
		}
	}
}