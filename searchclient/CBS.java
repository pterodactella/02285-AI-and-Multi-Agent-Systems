package searchclient;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.PriorityQueue;

public class CBS {

	public static Action[][] search(State initialState, Frontier frontier) {
		int iterations = 0;

		// Create the root node of the CBS search tree
		CBSNode root = new CBSNode(initialState, null);

		// Initialize the frontier with the root node
		frontier.add(root);

		// Initialize the set of already-explored nodes
		HashSet<CBSNode> closed = new HashSet<>();

		while (!frontier.isEmpty()) {
			// Pop the lowest-cost node from the frontier
			CBSNode node = (CBSNode) frontier.pop();

			if (node.getState().isGoalState()) {
				return node.getState().extractPlan();
			}

			// Generate child nodes by resolving conflicts between agents' paths
			ArrayList<CBSNode> children = new ArrayList<>();
			ArrayList<Agent> agents = node.getState().agents;

			for (int i = 0; i < agents.size(); i++) {
				Agent agent = agents.get(i);
				ArrayList<Constraints> conflictResolutionNodes = new ArrayList<>();

				// Check for conflicts with other agents
				for (int j = i + 1; j < agents.size(); j++) {
					Agent otherAgent = agents.get(j);
					ArrayList<Constraints> nodes = agent.resolveConflictsWith(otherAgent);
					conflictResolutionNodes.addAll(nodes);
				}

				// Add the child nodes to the list of children
				if (conflictResolutionNodes.isEmpty()) {
					children.add(new CBSNode(node.getState(), node));
				} else {
					for (Constraints childConstraints : conflictResolutionNodes) {
						// Create a new state based on the current state and the child constraints
						State childState = new State(node.getState(), childConstraints); // create a new state object
						childState.globalConstraints = new ArrayList<>(node.getState().globalConstraints); 
						childState.globalConstraints.add(childConstraints); // add the child constraints to the new list

						// Add the child node to the list of children
						CBSNode childNode = new CBSNode(childState, node, i, childConstraints, null);
						children.add(childNode);
						System.out.println("Child Node: " + childNode.toString());
					}
				}
			}

			// Add child nodes to the frontier if they have not been explored before
			for (CBSNode child : children) {
				if (!closed.contains(child)) {
					frontier.add(child);
					System.out.println("Added to Frontier: " + child);
				}
			}

			// Add the current node to the closed set
			closed.add(node);
			System.out.println("Closed Node: " + node);
			for (CBSNode child : children) {
				System.out.println("Child node: " + child);
			}

			// Print a status message every 10000 iteration
			if (++iterations % 10000 == 0) {
				printSearchStatus(closed, frontier);
			}
		}

		// No solution was found
		return null;
	}

	private static long startTime = System.nanoTime();

	private static void printSearchStatus(HashSet<CBSNode> expanded, Frontier frontier) {
		String statusTemplate = "#CBS, #Expanded: %,8d, #Frontier: %,8d, #Generated: %,8d, Time: %3.3f s\n%s\n";
		double elapsedTime = (System.nanoTime() - startTime) / 1_000_000_000d;
		System.err.format(statusTemplate, expanded.size(), frontier.size(), expanded.size() + frontier.size(),
				elapsedTime, Memory.stringRep());
	}
}
