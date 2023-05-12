package searchclient.CBS;

import java.util.ArrayList;

import searchclient.Action;
import searchclient.State;

public class PlanStep {

	public Action action;
	int locationX;
	int locationY;
	int timestamp;

	public PlanStep(Action action, int locationX, int locationY, int timestamp) {
		this.action = action;
		this.locationX = locationX;
		this.locationY = locationY;
		this.timestamp = timestamp;
	}
	
	public PlanStep(PlanStep copy) {
		this.action = copy.action;
		this.locationX = copy.locationX;
		this.locationY = copy.locationY;
		this.timestamp = copy.timestamp;
	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		s.append("action: " + action.toString() + "; ");
		s.append("locationX: " + locationX + "; ");
		s.append("locationY: " + locationY + "; ");
		s.append("timestamp: " + timestamp + "; ");
		return s.toString();
	}

	public static PlanStep[][] mergePlans(PlanStep[][] individualPlans) {
		int maxTimestamp = 0;
		int numAgents = individualPlans.length;

		for (PlanStep[] plan : individualPlans) {

			if (plan != null) {
				maxTimestamp = Math.max(maxTimestamp, plan[plan.length - 1].timestamp);
			}

		}

		PlanStep[][] mergedPlans = new PlanStep[maxTimestamp + 1][numAgents];
		for (int t = 0; t <= maxTimestamp; t++) {
			for (int agent = 0; agent < numAgents; agent++) {
				mergedPlans[t][agent] = new PlanStep(Action.NoOp, -1, -1, t);
			}
		}

		for (int agent = 0; agent < numAgents; agent++) {
			for (PlanStep step : individualPlans[agent]) {
				mergedPlans[step.timestamp][agent] = step;
			}
		}
		
//		for (int i = 0; i < mergedPlans.length; i++) {
//			for (int j = 0; j < mergedPlans[i].length; j++) {
//				System.err.print("[" + mergedPlans[i][j].toString() + "]" + " ");
//				
//			}
//			System.err.println();
//		}


		return mergedPlans;
	}
}
