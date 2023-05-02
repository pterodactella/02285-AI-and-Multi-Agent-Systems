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
    
        System.err.println("Frontier: " +frontier);
        while (!frontier.isEmpty()) {
            // Pop the lowest-cost node from the frontier
            CBSNode node = frontier.pop();
            // System.err.println("Node: " +node);
            // System.err.println("node.getState().isGoalState(): " + node.getState().isGoalState());
            if (node.getState().isGoalState()) {
                return node.getState().extractPlan();
            }
    
            // Generate child nodes by resolving conflicts between agents' paths
            ArrayList<CBSNode> children = new ArrayList<>();
            ArrayList<Agent> agents = node.getState().getAgents();
    
            for (int i = 0; i < agents.size(); i++) {
                Agent agent = agents.get(i);
                ArrayList<Constraints> conflictResolutionNodes = new ArrayList<>();
    
                // Check for conflicts with other agents
                for (int j = i + 1; j < agents.size(); j++) {
                    Agent otherAgent = agents.get(j);
                    ArrayList<Constraints> nodes = agent.resolveConflictsWith(otherAgent,
                            node.getState().getAgentTimestamps());
                    conflictResolutionNodes.addAll(nodes);
                }
    
                if (conflictResolutionNodes.isEmpty()) {
                    // No conflicts, add a child node without constraints
                    for (State expandedState : node.getState().getExpandedStates()) {
                        CBSNode childNode = new CBSNode(expandedState, node);
                        children.add(childNode);
                    }
                } else {
                    for (Constraints childConstraints : conflictResolutionNodes) {
                        // Create a new state based on the current state and the child constraints
                        State childState = new State(node.getState(), childConstraints);
    
                        // Create a new CBSNode object for each of the expanded states and add them to
                        // the children list
    
                        // Add the child node to the list of children
                        CBSNode childNode = new CBSNode(childState, node);
                        children.add(childNode);
                    }
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