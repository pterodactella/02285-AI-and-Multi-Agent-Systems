package searchclient;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import searchclient.Agent;
import java.lang.Object;
//Consider only one agent from one color at a time

public class CBS {
  public static void ConflictBasedSearch(Agent[] agents) {
    Agent[] conflictFreeAgents;


    for (int i =0; i < agents.length; i++) {
      if (conflictFreeAgents.length < 1) {
        conflictFreeAgents[i] = new Agent("a"+ i, null, null, 0);
      }
      
      
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

  
}
