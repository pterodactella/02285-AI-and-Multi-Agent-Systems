package searchclient;

public class Constraints {
   int agent1;
   int agent2;
   Integer loc_x;
   Integer loc_y;
   Integer timestamp;
   Action action1;
   Action action2;

   Constraints(int agentID, int loc_x, int loc_y, Integer timestamp ){
      this.agent1=agentID;
      this.loc_x=loc_x;
      this.loc_y=loc_y;
      this.timestamp=timestamp;
   }

   Constraints(int a1, int a2, Action act1, Action act2) {
      this.agent1 = a1;
      this.agent2 = a2;
      this.action1 = act1;
      this.action2 = act2;
   }


   public boolean isConflicting(Constraints o) {
       if (this == o) return true;
       if (o == null || getClass() != o.getClass()) return false;
       Constraints that = (Constraints) o;
       
       return timestamp == that.timestamp && loc_y == that.loc_y  && loc_x == that.loc_x;
   }
   public boolean isViolated(State state) {
    // Check if the constraint is being violated
    if (state.agentRows[timestamp] == loc_x && state.agentCols[timestamp] == loc_y) {
        // Agent is occupying the cell, so the constraint is being violated
        return true;
    }

    // Constraint is not being violated
    return false;
}

}