package searchclient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class CBS {

    public static HashMap<Integer, Action[][]> search(CBSNode initialState, Frontier frontier) {
        int iterations = 0;

        // Initialize the frontier with the root node
        frontier.add(initialState);
        // System.err.println("froniter empty" + frontier.isEmpty());

        // Initialize the set of already-explored nodes
        HashSet<CBSNode> closed = new HashSet<>();
        HashMap<Integer, Action[][]> agentPlans = new HashMap<Integer, Action[][]>();

        while (!frontier.isEmpty()) {

            // System.err.println("froniter not empty");

            // Pop the lowest-cost node from the frontier
            CBSNode node = frontier.pop();
            // System.err.println("CBSNode pop");
            // System.err.println(node);

            if (node.getState().isGoalState()) {
                // System.err.println("Goal state found");
                agentPlans = node.getState().extractPlan();
                // System.err.println("agentPlans" + agentPlans);
                return agentPlans;
            }

            // Check for conflicts
            boolean conflictExists = false;
            // System.err.println("conflict doesnt exist");

            for (int i = 0; i < node.getState().getAgents().size(); i++) {
                for (int j = i + 1; j < node.getState().getAgents().size(); j++) {
                    if (node.getState().getAgents().get(i).hasConflictWith(node.getState().getAgents().get(j))) {
                        conflictExists = true;
                        break;
                    }
                }
                if (conflictExists) {
                    // System.err.println("conflict exists");

                    break;
                }
            }
            ArrayList<CBSNode> children = new ArrayList<>();
            if (!conflictExists) {
                // No conflicts, search for each agent individually
                for (int i = 0; i < node.getState().agentRows.length; i++) {

                    if (!agentPlans.containsKey(i)) {
                        System.err.println("plan doesnt exist for agent " + i);

                        // The plan for this agent has not been computed yet
                        Action[][] agentPlan = GraphSearch.search(node, i, frontier);
                        System.err.println("agent plan" + agentPlan);
                        agentPlans.put(i, agentPlan);
                    }
                }
            } else {
                // Generate child nodes by resolving conflicts
                ArrayList<Constraints> constraints = new ArrayList<>();
                for (int i = 0; i < node.getState().getAgents().size(); i++) {
                    for (int j = i + 1; j < node.getState().getAgents().size(); j++) {
                        if (node.getState().getAgents().get(i).hasConflictWith(node.getState().getAgents().get(j))) {
                            // Resolve conflicts using MACBS
                            ArrayList<Constraints> subConstraints = node.getState().getAgents().get(i)
                                    .resolveConflictsWith(node.getState().getAgents().get(j),
                                            node.getState().getAgentTimestamps());

                            if (subConstraints == null) {
                                // No solution was found, return failure
                                return null;
                            }

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
                    CBSNode nodeChild = new CBSNode(childState);
                    children.add(nodeChild);
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

        }
        return agentPlans;
    }

    private static long startTime = System.nanoTime();

    private static void printSearchStatus(HashSet<CBSNode> expanded, Frontier frontier) {
        String statusTemplate = "#CBS, #Expanded: %,8d, #Frontier: %,8d, #Generated: %,8d, Time: %3.3f s\n%s\n";
        double elapsedTime = (System.nanoTime() - startTime) / 1_000_000_000d;
        System.err.format(statusTemplate, expanded.size(), frontier.size(), expanded.size() + frontier.size(),
                elapsedTime, Memory.stringRep());
    }
}