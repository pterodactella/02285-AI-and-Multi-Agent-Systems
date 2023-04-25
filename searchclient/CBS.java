package searchclient;
import java.util.ArrayList;
import java.util.PriorityQueue;

public class CBS {
    public static Action[][] search (State initialState, Frontier frontier) {
        
		// Create an agent object for all agents, make an array of them
		// The object has the cost, solution, constraints
		PriorityQueue<Agent> agents = new PriorityQueue<Agent>();

		ArrayList<Agent> checkedAgents = new ArrayList<>();

		for (int i = 0; i < initialState.agentCols.length; i++) {
			// System.err.println(State.agentColors[i].toString() +" i:"+ i);
			// System.out.println(agents.size());
			// Agent(String agentId, Color color, State initialState, Frontier frontier, int
			// agentIndex) {
			agents.add(new Agent(State.agentColors[i].toString() + i, State.agentColors[i], initialState, frontier, i));
			// System.out.println(agents.size());
			// System.out.println("agents.size()");
		}
		ArrayList<Constraints> conflictFreePaths = new ArrayList<>(64);

		// iterate over the array of agents
		while (!agents.isEmpty()) {
			// int minimumCost = agents.get(0).cost;
			// int index = 0;
			// for (int i = 0; i < agents.size(); i++) {
			// 	if (agents.get(i).cost < minimumCost) {
			// 		minimumCost = agents.get(i).cost;
			// 		index = i;
			// 	}
			// }
			// // validate the constraints of the agent[index] regarding the global constratins
			// Agent agentWithLowestCost = agents.get(index);

         Agent agentWithLowestCost = agents.peek();



			// loop through all constraints within
			for (int agentConstI = 0; agentConstI < agentWithLowestCost.constraints.length; agentConstI++) {
				// Compare wth global constraints
				for (int globalConstJ = 0; globalConstJ < conflictFreePaths.size(); globalConstJ++) {
					if (conflictFreePaths.get(globalConstJ).isConflicting(agentWithLowestCost.constraints[agentConstI])) {
						// this is a conflict

						Agent newAgent1 = new Agent(agentWithLowestCost.agentId, agentWithLowestCost.agentColor,
								initialState, frontier, agentWithLowestCost.agentIndex,
								conflictFreePaths.get(globalConstJ));

                        agents.add(newAgent1);
                        
						// Constraints conflict = agents.get(index).constraint
						// We need to add the constrain to one of the child and calculate a new path
						//

					} else {
						// this is not a conflict
						conflictFreePaths.add(agentWithLowestCost.constraints[agentConstI]);
					}

				}
			}

			checkedAgents.add(agentWithLowestCost);
			agents.poll();

		}

		return checkedAgents.get(0).solution;
    }
}




// final PriorityQueue<CBSNode> open = new PriorityQueue<>(Comparator.comparingInt(CBSNode::getCost));
// // Create an agent object for all agents, make an array of them
// // The object has the cost, solution, constraints
// CBSNode root = new CBSNode(initialState);
// open.add(root);

// while (!open.isEmpty()) {
//   CBSNode currentNode = open.poll();

//   if (initialState.isGoalState()) {
// 	return currentNode.extractSolution();
//   }

//   Conflict conflict = currentNode.findFirstConflict();
//   if (conflict == null) {
// 	continue;
//   }

//   for (Agent agent : conflict.getInvolvedAgents()) {
// 	CBSNode childNode = new CBSNode(currentNode, agent.agentIndex, conflict.row, conflict.col, conflict.time);
// 	if (childNode.isSolvable()) {
// 	  open.add(childNode);
// 	}
//   }
// }
// return null;