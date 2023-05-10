package searchclient.CBS;

import java.util.HashSet;

// import searchclient.Action;
import searchclient.Color;
// import searchclient.Frontier;
import searchclient.Memory;
import searchclient.State;
import java.util.ArrayList;


public class ConstraintGraphSearch {

    public static PlanStep[] search(CBSNode cbsNode, ConstraintFrontier frontier, int agent)
    {
            State specificState = createSpecificState(cbsNode, frontier, agent);

            int iterations = 0;

            frontier.add(new ConstraintState(cbsNode.state, agent, cbsNode.constraints, 0)); // Original version
            // frontier.add(new ConstraintState(specificState, agent, cbsNode.constraints, 0));  // New version
            HashSet<ConstraintState> expanded = new HashSet<>();

            while (true) {
            	if(frontier.isEmpty()) {
            		return null;
            	}
            	ConstraintState s = frontier.pop();
            	if(s.isGoalState()) {
            		return s.extractPlan();
            	}
            	expanded.add(s);
            	
            	for (ConstraintState t : s.getExpandedStates()) {
            		if(!frontier.contains(t) && !expanded.contains(t)) {
            			frontier.add(t);
            		}
					
				}

                //Print a status message every 10000 iteration
                if (++iterations % 10000 == 0) {
                    printSearchStatus(expanded, frontier);
                }

                //Your code here... Don't forget to print out the stats when a solution has been found (see above)
            }
    }

    public static State createSpecificState(CBSNode cbsNode, ConstraintFrontier frontier, int agent){

        // Create a new state with only the appropriately colored agents
        // Find the color of the agent  
        Color agentColor = State.agentColors[agent];
        System.out.println("Agent color: " + agentColor);
        // Find everything from that color
        ArrayList<Integer> sameColoredAgents = new ArrayList<>(State.agentColors.length);
        // agents
        for (int i = 0; i < State.agentColors.length; i++) {
            if (State.agentColors[i] == agentColor) {
                sameColoredAgents.add(i);
            }
        }
        // boxes
        ArrayList<Integer> sameColoredBoxes = new ArrayList<>(State.boxColors.length);
        for (int i = 0; i < State.boxColors.length; i++) {
            if (State.boxColors[i] == agentColor) {
                sameColoredBoxes.add(i);
            }
        }

        System.out.println("================================ Index of agent: " + agent +",  Color of agent: " + agentColor+ ", Same colored agents: " + sameColoredAgents + ",  Same colored boxes: " + sameColoredBoxes);

        // TODO: Initialize these variables
        int[] newAgentRows;
        int[] newAgentCols;
        Color[] newAgentColors;
        boolean[][] newWalls;
        char[][] newBoxes;
        Color[] newBoxColors;
        char[][] newGoals;

        // TODO: create new state, for this new spcificState class is rqeuired WITHOUT THE STATIC fields!
        // Create a new state with only the appropriately colored elements
        // State newState = new State(newAgentRows, newAgentCols, newAgentColors, newWalls, newBoxes, newBoxColors, newGoals);
        State newState = cbsNode.state;

        return newState;
    }



	private static long startTime = System.nanoTime();

    private static void printSearchStatus(HashSet<ConstraintState> expanded, ConstraintFrontier frontier)
    {
        String statusTemplate = "#Expanded: %,8d, #Frontier: %,8d, #Generated: %,8d, Time: %3.3f s\n%s\n";
        double elapsedTime = (System.nanoTime() - startTime) / 1_000_000_000d;
        System.err.format(statusTemplate, expanded.size(), frontier.size(), expanded.size() + frontier.size(),
                          elapsedTime, Memory.stringRep());
    }

    



}
