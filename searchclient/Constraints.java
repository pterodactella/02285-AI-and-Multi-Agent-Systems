package searchclient;

public class Constraints {
   String agentID;
   Integer loc_x;
   Integer loc_y;
   Integer timestamp;

   Constraints(String agentID, int loc_x, int loc_y, Integer timestamp ){
      this.agentID=agentID;
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

}