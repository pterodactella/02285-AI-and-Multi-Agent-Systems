package searchclient;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import searchclient.Agent;
import java.lang.Object;
//Consider only one agent from one color at a time

public class CBS {
  public static void ConflictBasedSearch(State initialState, Frontier frontier) {

    // Create an agent object for all agents, make an array of them
    // The object has the cost, solution, constraints
    ArrayList<Agent> agents = new ArrayList<>();
    for (int i = 0; i< initialState.agentCols.length; i ++) {
      agents.add(new Agent(initialState.agentColors[i].toString() + i , initialState, frontier));
    }
    ArrayList<Constraints> globalConstraints = new ArrayList<>();

    // iterate over the array of agents
    while (!agents.isEmpty()) {
      int minimumCost = agents.get(0).cost;
      int index = 0;
      for (int i = 0; i < agents.size(); i++) {
          if(agents.get(i).cost < minimumCost) {
            minimumCost = agents.get(i).cost;
            index = i;
          }
      }
      // validate the constraints of the agent[index] regarding the global constratins
      // Agent agentWithLowestCost = agents.get(index);
      
      for (int i = 0; i < agents.get(index).constraints.length; i++) {
        // Compare wth global constraints
        for (int j = 0; j < globalConstraints.size(); j++) {
          if ( globalConstraints.get(j).isConflicting(agents.get(index).constraints[i])  ) {
            // this is a conflict


          }

          else {
            // this is not a conflict
            globalConstraints.add(agents.get(index).constraints[i]);
          }

        }
      }

      
      // Finished checking
      agents.remove(index);


      
      
    }


    
      // start checking the agents based on their cost
      // Validate the solutions until there is no conflict




    
    }
    

    // while 
    // Root.constraints = ∅
    // Root.solution = find individual paths by the low level()
    // Root.cost = SIC(Root.solution)
    // insert Root to OPEN
    // while OPEN not empty do
    // P ← best node from OPEN // lowest solution cost
    // Validate the paths in P until a conflict occurs.
    // if P has no conflict then
    // return P .solution // P is goal
    // C ← first conflict (ai , a j , v, t) in P
    // if shouldMerge(ai , a j ) // Optional, MA-CBS only then
    // a{i, j} = merge(ai , a j , v, t)
    // Update P .constraints(external constraints).
    // Update P .solution by invoking low level(a{i, j} )
    // Update P .cost
    // if P .cost < ∞ // A solution was found then
    // Insert P to OPEN
    // continue // go back to the while statement
    // foreach agent ai in C do
    // A ← new node
    // A.constraints ← P .constraints + (ai , v, t)
    // A.solution ← P .solution
    // Update A.solution by invoking low level(ai )
    // A.cost = SIC(A.solution)
    // if A.cost < ∞ // A solution was found then
    // Insert A to OPEN

  }

  
