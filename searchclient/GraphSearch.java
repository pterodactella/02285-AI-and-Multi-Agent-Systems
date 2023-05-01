package searchclient;

// import java.util.ArrayList;
// import java.util.Arrays;
import java.util.HashSet;

public class GraphSearch {

  public static Action[][] search(State initialState, Frontier frontier) {
    int iterations = 0;

    CBSNode initialNode = new CBSNode(initialState, null);
    frontier.add(initialNode);

    HashSet<CBSNode> expanded = new HashSet<>();

    while (true) {
      if (frontier.isEmpty()) {
        return null;
      }

      CBSNode node = frontier.pop();

      if (node.getState().isGoalState()) {
        return node.getState().extractPlan();
      }
      expanded.add(node);

      for (State t : node.getState().getExpandedStates()) {
        CBSNode childNode = new CBSNode(t, node);
        if (!expanded.contains(childNode) && !frontier.contains(childNode)) {
          frontier.add(childNode);
        } else if (frontier.contains(childNode)) {
          // If the state is already in the frontier, check if the new path has a lower
          // cost
          CBSNode existing = frontier.getNode(childNode);
          if (existing != null && childNode.getState().g() < existing.getState().g()) {
            frontier.remove(existing);
            frontier.add(childNode);
          }
        } else if (expanded.contains(childNode)) {
          // If the state is already expanded, check if the new path has a lower cost
          CBSNode existing = expanded.stream().filter(n -> n.equals(childNode)).findFirst().orElse(null);
          if (existing != null && childNode.getState().g() < existing.getState().g()) {
            expanded.remove(existing);
            frontier.add(childNode);
          }
        }
      }
      // Print a status message every 10000 iteration
      if (++iterations % 10000 == 0) {
        printSearchStatus(expanded, frontier);
      }
    }
  }

  private static long startTime = System.nanoTime();

  private static void printSearchStatus(HashSet<CBSNode> expanded, Frontier frontier) {
    String statusTemplate = "#CBS, #Expanded: %,8d, #Frontier: %,8d, #Generated: %,8d, Time: %3.3f s\n%s\n";
    double elapsedTime = (System.nanoTime() - startTime) / 1_000_000_000d;
    System.err.format(statusTemplate, expanded.size(), frontier.size(), expanded.size() + frontier.size(),
        elapsedTime, Memory.stringRep());
  }

}
