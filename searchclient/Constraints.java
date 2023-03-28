package searchclient;

public class Constraints {
   String agentID;
   Integer loc_x;
   Integer loc_y;
   Integer timestamp;

   Constraints(String agentID, Agent agent, Integer timestamp ){
      this.agentID=agentID;
      this.loc_x=agent.agentCols[0];
      this.loc_y=agent.agentRows[0];
      this.timestamp=timestamp;
   }

}