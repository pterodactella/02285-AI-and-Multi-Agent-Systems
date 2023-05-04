package searchclient;

// import java.util.ArrayList;
// import java.util.Arrays;
import java.util.HashSet;

public class GraphSearch {

  public static Action[][] search(CBSNode initialNode, int agentID, Frontier frontier) {
    int iterations = 0;
    HashSet<State> expanded = new HashSet<>();

    frontier.add(initialNode);

    while (true) {
      if (frontier.isEmpty()) {
        return null;
      }

      CBSNode node = frontier.pop();
      // System.err.println("CBSNode" + node);

      if (node.getState().isGoalState()) {
        System.err.println("getState().extractPlan().get(0)" + node.getState().extractPlan().get(agentID));
        return node.getState().extractPlan().get(agentID);
      }

      expanded.add(node.getState());

      for (State child : node.getState().getExpandedStates()) {
        if (!expanded.contains(child)) {
          frontier.add(new CBSNode(child));
          expanded.add(child);
        }
      }

      // Print a status message every 10000 iteration
      if (++iterations % 10000 == 0) {
        printSearchStatus(expanded, frontier);
      }
    }
    
  }

  private static long startTime = System.nanoTime();

  private static void printSearchStatus(HashSet<State> expanded, Frontier frontier) {
    String statusTemplate = "#Expanded: %,8d, #Frontier: %,8d, #Generated: %,8d, Time: %3.3f s\n%s\n";
    double elapsedTime = (System.nanoTime() - startTime) / 1_000_000_000d;
    System.err.format(statusTemplate, expanded.size(), frontier.size(), expanded.size() + frontier.size(),
        elapsedTime, Memory.stringRep());
  }

}