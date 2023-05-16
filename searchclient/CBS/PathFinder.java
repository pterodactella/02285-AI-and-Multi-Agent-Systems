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
import searchclient.CBS.Conflicts.AgentAlongBoxConflict;
import searchclient.CBS.Conflicts.AgentInBoxConflict;
import searchclient.CBS.Conflicts.BoxConflict;
import searchclient.CBS.Conflicts.BoxOrderedConflict;
import searchclient.CBS.Conflicts.Conflict;
import searchclient.CBS.Conflicts.GenericConflict;
import searchclient.CBS.Conflicts.OrderedConflict;
import searchclient.CBS.Conflicts.SymmetryConflict;
import searchclient.CBS.Conflicts.SymmetryType;

public class PathFinder implements Comparator<CBSNode> {
	private State initialState;
	private static int triedTimes = 0;
	private static final int MAX_DEBUG_TRIALS = 4;

	public PathFinder(State initialState) {
		this.initialState = initialState;
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
			// find the first conflict in the plan
			GenericConflict c = p.findFirstConflict();

			if (c == null) {
				// for (PlanStep[] plan : p.solution) {
				// for (PlanStep step : plan) {
				// logger.log("[ " + step.toString() + " ]");
				// }
				// logger.log("");
				// }
				return p.solution;
			}

			// for (PlanStep[] plan : p.solution) {
			// for (PlanStep step : plan) {
			// logger.log("[ " + step.toString() + " ]");
			// }
			// logger.log("");
			// }

			// if (c instanceof Conflict) {
			// // logger.log("Conflict found: " + c.toString());
			// } else if (c instanceof OrderedConflict) {
			// // logger.log("OrderedConflict found: " + c.toString());
			// }

			PathFinder.triedTimes++;

			// if (PathFinder.triedTimes >= PathFinder.MAX_DEBUG_TRIALS) {
			// System.exit(0);
			// }

			// Determine the cardinality of the conflict
			boolean isCardinal = false;
			boolean isSemiCardinal = false;
			
			if (c instanceof Conflict) {

				int agent1Index = ((Conflict) c).agentIndexes[0];
				int agent2Index = ((Conflict) c).agentIndexes[1];

				// System.err.println("agent1 Index + " + agent1Index);

				CBSNode a = new CBSNode(p);
				a.constraints.add(new Constraint(agent1Index, ((Conflict) c).locationX,
						((Conflict) c).locationY,
						((Conflict) c).timestamp));
				// a.solution = a.findPlan();
				// // Recalculate only for one(agentIndex)
				// PlanStep[][] individualPlans = a.findPlan();
				// a.solution[agentIndex] = individualPlans[agentIndex];
				// System.err.println("a.solution + " + a.solution.toString());

				a.solution[agent1Index] = a.findIndividualPlan(agent1Index, p.solution);
				a.totalCost = a.sumCosts();
				// a.totalCost = a.sumCosts();
				// a.solution[agent2Index] = a.findIndividualPlan(agent2Index, p.solution);

				CBSNode b = new CBSNode(p);
				b.constraints.add(new Constraint(agent2Index, ((Conflict) c).locationX,
						((Conflict) c).locationY,
						((Conflict) c).timestamp));

				b.solution[agent2Index] = b.findIndividualPlan(agent2Index, p.solution);

				b.totalCost = b.sumCosts();

				isCardinal = isCardinalConflict(p, a, b);
				isSemiCardinal = isSemiCardinalConflict(p, a, b);
				if (isCardinal) {
					if (!open.contains(a) && !expanded.contains(a)) {
						open.add(a);
					}
				} else if (isSemiCardinal) {
					if (!open.contains(a) && !expanded.contains(a)) {
						open.add(a);
					}
				} else {
					if (!open.contains(a) && !expanded.contains(a)) {
						open.add(a);
					}
					if (!open.contains(b) && !expanded.contains(b)) {
						open.add(b);
					}
				}

				// if (!open.contains(a) && !expanded.contains(a)) {
				// open.add(a);
				// }
				// if (!open.contains(b) && !expanded.contains(b)) {
				// open.add(b);
				// }
			}
			// }
			else if (c instanceof OrderedConflict) {
				CBSNode a = new CBSNode(p);
				a.constraints.add(
						new Constraint(((OrderedConflict) c).followerIndex, ((OrderedConflict) c).forbiddenLocationX,
								((OrderedConflict) c).forbiddenLocationY, ((OrderedConflict) c).timestamp));

				a.solution = a.findPlan();
				a.totalCost = a.sumCosts();

				if (!open.contains(a) && !expanded.contains(a)) {
					open.add(a);
				}
			} else if (c instanceof AgentInBoxConflict) {
				CBSNode a = new CBSNode(p);
				a.constraints.add(
						new Constraint(((AgentInBoxConflict) c).agentIndex, ((AgentInBoxConflict) c).forbiddenLocationX,
								((AgentInBoxConflict) c).forbiddenLocationY, ((AgentInBoxConflict) c).timestamp));

				a.solution = a.findPlan();
				a.totalCost = a.sumCosts();
				if (!open.contains(a) && !expanded.contains(a)) {
					open.add(a);
				}

			} else if (c instanceof AgentAlongBoxConflict) {
				CBSNode a = new CBSNode(p);
				a.constraints.add(new Constraint(((AgentAlongBoxConflict) c).agentWithoutBoxIndex,
						((AgentAlongBoxConflict) c).agentWithoutBoxLocationX,
						((AgentAlongBoxConflict) c).agentWithoutBoxLocationY, ((AgentAlongBoxConflict) c).timestamp));

				a.solution = a.findPlan();
				a.totalCost = a.sumCosts();
				if (!open.contains(a) && !expanded.contains(a)) {
					open.add(a);
				}

				CBSNode b = new CBSNode(p);
				b.boxConstraints.add(new BoxConstraint(((AgentAlongBoxConflict) c).agentWithBoxIndex,
						((AgentAlongBoxConflict) c).agentWithoutBoxLocationX,
						((AgentAlongBoxConflict) c).agentWithoutBoxLocationY, ((AgentAlongBoxConflict) c).timestamp));

				b.solution = b.findPlan();
				b.totalCost = b.sumCosts();
				if (!open.contains(b) && !expanded.contains(b)) {
					open.add(b);
				}

			} else if (c instanceof BoxOrderedConflict) {
				CBSNode a = new CBSNode(p);
				a.boxConstraints.add(new BoxConstraint(((BoxOrderedConflict) c).followerWithBoxIndex,
						((BoxOrderedConflict) c).forbiddenLocationX, ((BoxOrderedConflict) c).forbiddenLocationY,
						((BoxOrderedConflict) c).timestamp));

				a.solution = a.findPlan();
				a.totalCost = a.sumCosts();
				if (!open.contains(a) && !expanded.contains(a)) {
					open.add(a);
				}
				if (PathFinder.triedTimes % 100 == 0) {
					printSearchStatus(expanded, open);
				}
			} else if (c instanceof BoxConflict) {

				for (int agentIndex : ((BoxConflict) c).agentIndexes) {
					CBSNode a = new CBSNode(p);
					a.boxConstraints.add(new BoxConstraint(agentIndex, ((BoxConflict) c).boxLocationX,
							((BoxConflict) c).boxLocationY, ((BoxConflict) c).timestamp));

					a.solution = a.findPlan();
					a.totalCost = a.sumCosts();

					if (!open.contains(a) && !expanded.contains(a)) {
						open.add(a);
					}
				}
			}
		}
		return null;

	}

	@Override
	public int compare(CBSNode n1, CBSNode n2) {
		return Integer.compare(n1.totalCost, n2.totalCost);
	}

	private boolean isCardinalConflict(CBSNode parent, CBSNode agent1Node, CBSNode agent2Node) {
		int costN = parent.totalCost;
		int costA = agent1Node.totalCost;
		int costB = agent2Node.totalCost;
		return costA > costN && costB > costN;
	}

	private boolean isSemiCardinalConflict(CBSNode parent, CBSNode agent1Node, CBSNode agent2Node) {
		int costN = parent.totalCost;
		int costA = agent1Node.totalCost;
		int costB = agent2Node.totalCost;
		return (costA > costN && costB == costN) || (costA == costN && costB > costN);
	}

	// private boolean isNonCardinalConflict(CBSNode parent, CBSNode agent1Node,
	// CBSNode agent2Node) {
	// int costN = parent.totalCost;
	// int costA = agent1Node.totalCost;
	// int costB = agent2Node.totalCost;
	// return costA == costN && costB == costN;
	// }

	private static long startTime = System.nanoTime();

	private static void printSearchStatus(HashSet<CBSNode> expanded, PriorityQueue<CBSNode> open) {
		String statusTemplate = "#Expanded: %,8d, #Frontier: %,8d, #Generated: %,8d, Time: %3.3f s\n%s\n";
		double elapsedTime = (System.nanoTime() - startTime) / 1_000_000_000d;
		System.err.format(statusTemplate, expanded.size(), open.size(), expanded.size() + open.size(), elapsedTime,
				Memory.stringRep());
	}

}
