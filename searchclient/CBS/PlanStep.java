package searchclient.CBS;

import java.util.ArrayList;

import searchclient.Action;
import searchclient.State;

public class PlanStep {

	public Action action;
	int locationX;
	int locationY;
	int timestamp;
	int originalX;
	int originalY;

	public PlanStep(Action action, int locationX, int locationY, int timestamp, int originalX, int originalY) {
		this.action = action;
		this.locationX = locationX;
		this.locationY = locationY;
		this.timestamp = timestamp;
		this.originalX = originalX;
		this.originalY = originalY;
	}
	
	public PlanStep(PlanStep copy) {
		this.action = copy.action;
		this.locationX = copy.locationX;
		this.locationY = copy.locationY;
		this.timestamp = copy.timestamp;
		this.originalX = copy.originalX;
		this.originalY = copy.originalY;
	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		s.append("action: " + action.toString() + "; ");
		s.append("locationX: " + locationX + "; ");
		s.append("locationY: " + locationY + "; ");
		s.append("timestamp: " + timestamp + "; ");
		s.append("originalX: " + originalX + "; ");
		s.append("originalY: " + originalY + "; ");
		return s.toString();
	}

	public static PlanStep[][] mergePlans(PlanStep[][] individualPlans) {
		int maxTimestamp = 0;
		int numAgents = individualPlans.length;

		for (PlanStep[] plan : individualPlans) {
//			for (PlanStep step : plan) {
			if (plan != null) {
				maxTimestamp = Math.max(maxTimestamp, plan[plan.length - 1].timestamp);
			}
//			}
		}

		PlanStep[][] mergedPlans = new PlanStep[maxTimestamp + 1][numAgents];
		for (int t = 0; t <= maxTimestamp; t++) {
			for (int agent = 0; agent < numAgents; agent++) {
				mergedPlans[t][agent] = new PlanStep(Action.NoOp, -1, -1, t, -1, -1);
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
