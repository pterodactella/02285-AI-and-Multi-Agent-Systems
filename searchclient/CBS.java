package searchclient;

import java.util.ArrayList;
import java.util.HashSet;

public class CBS {

    public static Action[][] search(State initialState, Frontier frontier) {
        int iterations = 0;

        // Create the root node of the CBS search tree
        CBSNode root = new CBSNode(initialState);

        // Initialize the frontier with the root node
        frontier.add(root);

        // Initialize the set of already-explored nodes
        HashSet<CBSNode> closed = new HashSet<>();

        while (!frontier.isEmpty()) {
            // Pop the lowest-cost node from the frontier
            CBSNode node = frontier.pop();
            if (node.getState().isGoalState()) {
                return node.getState().extractPlan();
            }

            // Generate child nodes by resolving conflicts
            ArrayList<CBSNode> children = new ArrayList<>();

            boolean conflictExists = true;

            while (conflictExists) {
                conflictExists = false;
                ArrayList<Constraints> constraints = new ArrayList<Constraints>();

                // Check for conflicts
                for (int i = 0; i < node.getState().getAgents().size(); i++) {
                    for (int j = i + 1; j < node.getState().getAgents().size(); j++) {
                        if (node.getState().getAgents().get(i).hasConflictWith(node.getState().getAgents().get(j))) {
                            // Resolve conflicts using MACBS
                            conflictExists = true;
                            ArrayList<Constraints> subConstraints = node.getState().getAgents().get(i)
                                    .resolveConflictsWith(node.getState().getAgents().get(j),
                                            node.getState().getAgentTimestamps());

                            if (subConstraints == null) {
                                // No solution was found, return failure
                                return null;
                            }
                            System.err.println("subConstraints" + subConstraints);

                            constraints.addAll(subConstraints);
                        }
                    }
                }

                // Create new states for each set of constraints
                ArrayList<State> childStates = new ArrayList<>();
                for (Constraints c : constraints) {
                    // Create a new state with the updated constraints
                    State childState = new State(node.getState(), c); // Clone the parent state
                    childStates.add(childState);
                }

                // Create new child nodes for each child state
                for (State childState : childStates) {
                    CBSNode childNode = new CBSNode(childState);
                    children.add(childNode);
                }
            }

            // Add child nodes to the frontier if they have not been explored before
            for (CBSNode child : children) {
                if (!closed.contains(child)) {
                    frontier.add(child);
                }
            }

            // Add the current node to the closed set
            closed.add(node);

            // Print a status message every 10000 iteration
            if (++iterations % 10000 == 0) {
                printSearchStatus(closed, frontier);
            }

        }
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