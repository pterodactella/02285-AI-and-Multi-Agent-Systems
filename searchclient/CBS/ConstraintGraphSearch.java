package searchclient.CBS;

import java.util.HashSet;

import searchclient.Action;
import searchclient.Frontier;
import searchclient.Memory;
import searchclient.State;

public class ConstraintGraphSearch {

	public static PlanStep[] search(CBSNode cbsNode, ConstraintFrontier frontier, int agent) {

//		int iterations = 0;

		frontier.add(new ConstraintState(cbsNode.state, agent, cbsNode.constraints, cbsNode.boxConstraints, 0));
//            HashSet<ConstraintState> expanded = GlobalExpandsHashSet.getInstance().getSet();
		HashSet<ConstraintState> expanded = new HashSet<>();

		while (true) {
			if (frontier.isEmpty()) {
				return null;
			}
			ConstraintState s = frontier.pop();
			if (s.isGoalState()) {
				return s.extractPlan();
			}
			expanded.add(s);

			for (ConstraintState t : s.getExpandedStates()) {
				if (!frontier.contains(t) && !expanded.contains(t)) {
					frontier.add(t);
				}

			}

			// Print a status message every 10000 iteration
//                if (++iterations % 10000 == 0) {
//                    printSearchStatus(expanded, frontier);
//                }

			// Your code here... Don't forget to print out the stats when a solution has
			// been found (see above)
		}
	}

	private static long startTime = System.nanoTime();

	private static void printSearchStatus(HashSet<ConstraintState> expanded, ConstraintFrontier frontier) {
		String statusTemplate = "#Expanded: %,8d, #Frontier: %,8d, #Generated: %,8d, Time: %3.3f s\n%s\n";
		double elapsedTime = (System.nanoTime() - startTime) / 1_000_000_000d;
		System.err.format(statusTemplate, expanded.size(), frontier.size(), expanded.size() + frontier.size(),
				elapsedTime, Memory.stringRep());
	}
}
