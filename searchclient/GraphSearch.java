package searchclient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

public class GraphSearch {

  public static Action[][] search(State initialState, Frontier frontier, Constraints[] constraints) {
    int iterations = 0;
    int conflicts = 0;
    int step = 0;

    frontier.add(initialState);
    // PriorityQueue<>
    HashSet<State> expanded = new HashSet<>();


    while (true) {
      if (frontier.isEmpty()) {
        return null;
      }
      step++;
      State s = frontier.pop();

      if (s.isGoalState()) {
        return s.extractPlan();
      }
      expanded.add(s);

      for (State t : s.getExpandedStates()) {
        if (!expanded.contains(t)) {
          int h = calculateHeuristic(t, agents);

          frontier.add(t, s.g() + 1, h);
        } else {
          // If the state is already expanded, check if the new path has a lower cost
          State existing = getStateFromSet(expanded, t);
          if (existing != null && s.g() + 1 < existing.g()) {
            expanded.remove(existing);
            frontier.add(t, s.g() + 1, calculateHeuristic(t, agents));
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

  private static void printSearchStatus(HashSet<State> expanded, Frontier frontier) {
    String statusTemplate = "#Expanded: %,8d, #Frontier: %,8d, #Generated: %,8d, Time: %3.3f s\n%s\n";
    double elapsedTime = (System.nanoTime() - startTime) / 1_000_000_000d;
    System.err.format(statusTemplate, expanded.size(), frontier.size(), expanded.size() + frontier.size(),
        elapsedTime, Memory.stringRep());
  }

  private static State getStateFromSet(HashSet<State> set, State state) {
    for (State s : set) {
      if (s.equals(state)) {
        return s;
      }
    }
    return null;
  }

  private static int calculateHeuristic(State state, ArrayList<Agent> agents) {
    int h = 0;
    
  }

}
