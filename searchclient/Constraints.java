package searchclient;

public class Constraints {
   int agent1;
   int agent2;
   int loc_x;
   int loc_y;
   Integer timestamp;
   Action action1;
   Action action2;

   Constraints(int agentID, int loc_x, int loc_y, Integer timestamp ){
      this.agent1=agentID;
      this.loc_x=loc_x;
      this.loc_y=loc_y;
      this.timestamp=timestamp;
   }

   public boolean isConflicting(Constraints o) {
       if (this == o) return true;
       if (o == null || getClass() != o.getClass()) return false;
       Constraints that = (Constraints) o;
       
       return timestamp == that.timestamp && loc_y == that.loc_y  && loc_x == that.loc_x;
   }
   public boolean isViolated(int destCol, int destRow, int timestamp) {
    // Check if the constraint is being violated
    if (destCol == loc_x && destRow == loc_y && timestamp == this.timestamp) {
        // Agent is occupying the cell, so the constraint is being violated
        return true;
    }

    // Constraint is not being violated
    return false;
}

}