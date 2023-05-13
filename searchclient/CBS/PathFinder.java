package searchclient.CBS;

import java.util.PriorityQueue;
import java.util.Stack;
import java.util.Comparator;
import java.util.HashSet;

import searchclient.Action;
import searchclient.Logger;
import searchclient.Memory;
import searchclient.State;
import searchclient.CBS.Constraint;
import searchclient.CBS.CBSNode.Conflict;;

public class PathFinder implements Comparator<CBSNode> {
	private State initialState;
	private static int triedTimes = 0;
	private static final int MAX_DEBUG_TRIALS = 4;
	private InitialState initialStateForStorage;

	public PathFinder(State initialState) {
		this.initialState = initialState;
		this.initialStateForStorage =	new InitialState(initialState.agentRows, initialState.agentCols, initialState.agentColors, initialState.walls, initialState.boxes, initialState.boxColors, initialState.goals);
	}

	public PlanStep[][] solveCBS() {
		CBSNode root = new CBSNode(this.initialState);
		root.solution = root.findPlan();
		root.totalCost = root.sumCosts();

		// TODO: Replace with priority qyueyue
		PriorityQueue<CBSNode> open = new PriorityQueue<>(this);
		HashSet<CBSNode> expanded = new HashSet<>();

		open.add(root);
		Logger logger = Logger.getInstance();

		while (!open.isEmpty()) {
			logger.log("#########################################");
			CBSNode p = open.poll();
			expanded.add(p);
			GenericConflict c = p.findFirstConflict();

			if (c == null) {
//				for (PlanStep[] plan : p.solution) {
//					for (PlanStep step : plan) {
//						logger.log("[ " + step.toString() + " ]");
//					}
//					logger.log("");
//				}
				return p.solution;
			}

//			for (PlanStep[] plan : p.solution) {
//				for (PlanStep step : plan) {
//					logger.log("[ " + step.toString() + " ]");
//				}
//				logger.log("");
//			}

			if (c instanceof Conflict) {
//				logger.log("Conflict found: " + c.toString());
			} else if (c instanceof OrderedConflict) {
//				logger.log("OrderedConflict found: " + c.toString());
			}

			PathFinder.triedTimes++;

//			if (PathFinder.triedTimes >= PathFinder.MAX_DEBUG_TRIALS) {
//				System.exit(0);
//			}

			if (c instanceof Conflict) {

				for (int agentIndex : ((Conflict) c).agentIndexes) {
					CBSNode a = new CBSNode(p);
					a.constraints.add(new Constraint(agentIndex, ((Conflict) c).locationX, ((Conflict) c).locationY,
							((Conflict) c).timestamp));

					a.solution = a.findPlan();
					a.totalCost = a.sumCosts();

					// TODO: use a number instead of infinity
					if (!open.contains(a) && !expanded.contains(a)) {
						open.add(a);
					}
				}
			} else if (c instanceof OrderedConflict) {
				CBSNode a = new CBSNode(p);
				a.constraints.add(
						new Constraint(((OrderedConflict) c).followerIndex, ((OrderedConflict) c).forbiddenLocationX,
								((OrderedConflict) c).forbiddenLocationY, ((OrderedConflict) c).timestamp));

				a.solution = a.findPlan();
				a.totalCost = a.sumCosts();

				// TODO: use a number instead of infinity
				if (!open.contains(a) && !expanded.contains(a)) {
					open.add(a);
				}
			}
			 if (PathFinder.triedTimes % 100 == 0) {
                 printSearchStatus(expanded, open);
             }
		}

		return null;
	}

	@Override
	public int compare(CBSNode n1, CBSNode n2) {
		return Integer.compare(n1.totalCost, n2.totalCost);
	}

	private static long startTime = System.nanoTime();
	private static void printSearchStatus(HashSet<CBSNode> expanded, PriorityQueue<CBSNode> open) {
		String statusTemplate = "#Expanded: %,8d, #Frontier: %,8d, #Generated: %,8d, Time: %3.3f s\n%s\n";
		double elapsedTime = (System.nanoTime() - startTime) / 1_000_000_000d;
		System.err.format(statusTemplate, expanded.size(), open.size(), expanded.size() + open.size(),
				elapsedTime, Memory.stringRep());
	}

}
