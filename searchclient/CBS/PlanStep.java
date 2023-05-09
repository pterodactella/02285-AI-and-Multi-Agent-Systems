package searchclient.CBS;

import java.util.ArrayList;
import java.util.Arrays;

import searchclient.Action;
import searchclient.State;

public class PlanStep {

	public Action action;
	public int locationX;
	public int locationY;
	public int timestamp;

	public PlanStep(Action action, int locationX, int locationY, int timestamp) {
		this.action = action;
		this.locationX = locationX;
		this.locationY = locationY;
		this.timestamp = timestamp;
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
			for (PlanStep step : plan) {
				maxTimestamp = Math.max(maxTimestamp, step.timestamp);
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
		
		return mergedPlans;
	}
	

}
