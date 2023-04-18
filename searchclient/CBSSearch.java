package searchclient;

import java.util.Comparator;
import java.util.PriorityQueue;

public class CBSSearch {
  public static Action[][] CBSsearch(State initialState, Frontier frontier) {
    final PriorityQueue<CBSNode> open = new PriorityQueue<>(Comparator.comparingInt(CBSNode::getCost));
    // Create an agent object for all agents, make an array of them
    // The object has the cost, solution, constraints
    CBSNode root = new CBSNode(initialState);
    open.add(root);

    while (!open.isEmpty()) {
      CBSNode currentNode = open.poll();

      if (initialState.isGoalState()) {
        return currentNode.extractSolution();
      }

      Conflict conflict = currentNode.findFirstConflict();
      if (conflict == null) {
        continue;
      }

      for (Agent agent : conflict.getInvolvedAgents()) {
        CBSNode childNode = new CBSNode(currentNode, agent.agentIndex, conflict.row, conflict.col, conflict.time);
        if (childNode.isSolvable()) {
          open.add(childNode);
        }
      }
    }
  return null;
  }
}