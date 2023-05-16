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
	MovingBox movingBox;

	public PlanStep(Action action, int locationX, int locationY, int timestamp, int originalX, int originalY,
			MovingBox movingBox) {
		this.action = action;
		this.locationX = locationX;
		this.locationY = locationY;
		this.timestamp = timestamp;
		this.originalX = originalX;
		this.originalY = originalY;
		this.movingBox = movingBox;
	}

	public PlanStep(PlanStep copy) {
		this.action = copy.action;
		this.locationX = copy.locationX;
		this.locationY = copy.locationY;
		this.timestamp = copy.timestamp;
		this.originalX = copy.originalX;
		this.originalY = copy.originalY;
		
		if (copy.movingBox == null) {
			this.movingBox = null;
		} else {
			this.movingBox = new MovingBox(copy.movingBox.prevX, copy.movingBox.prevY, copy.movingBox.currX, copy.movingBox.currY);
		}
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
		if(movingBox == null) {
			s.append("movingBox: null;");
			
		}else {
			s.append("\n movingBox: \n ");
			s.append(movingBox.toString());
		}
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
				mergedPlans[t][agent] = new PlanStep(Action.NoOp, -1, -1, t, -1, -1, null);
			}
		}

		for (int agent = 0; agent < numAgents; agent++) {
			PlanStep lastStep = null;
			for (PlanStep step : individualPlans[agent]) {
				mergedPlans[step.timestamp][agent] = step;
				lastStep = step;
			}

			if (lastStep != null) {
				int[] lastPositions = new int[] { lastStep.locationX, lastStep.locationY, lastStep.originalX,
						lastStep.originalY };

				for (int trailing = lastStep.timestamp; trailing <= maxTimestamp; trailing++) {
					mergedPlans[trailing][agent].locationX = lastPositions[0];
					mergedPlans[trailing][agent].locationY = lastPositions[1];
					mergedPlans[trailing][agent].originalX = lastPositions[2];
					mergedPlans[trailing][agent].originalY = lastPositions[3];
					if (lastStep.movingBox != null) {
						mergedPlans[trailing][agent].movingBox = new MovingBox(lastStep.movingBox);
					}
				}
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

	@Override
	public int hashCode() {
		final int prime = 111;
		int result = 1;
		result = result * prime + this.action.hashCode();
		result = result * prime + this.locationX;
		result = result * prime + this.locationY;
		result = result * prime + this.timestamp;
		result = result * prime + this.originalX;
		result = result * prime + this.originalY;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		PlanStep other = (PlanStep) obj;
		return this.action.equals(other.action) && this.locationX == other.locationX
				&& this.locationY == other.locationY && this.originalX == other.originalX
				&& this.originalY == other.originalY && this.timestamp == other.timestamp;
	}
}

class MovingBox {
	int prevX;
	int prevY;
	int currX;
	int currY;

	public MovingBox(int prevX, int prevY, int currX, int currY) {
		this.prevX = prevX;
		this.prevY = prevY;
		this.currX = currX;
		this.currY = currY;
	}

	public MovingBox(MovingBox copy) {
		this.prevX = copy.prevX;
		this.prevY = copy.prevY;
		this.currX = copy.currX;
		this.currY = copy.currY;
	}
	
	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		s.append("prevX: " + prevX + "; ");
		s.append("prevY: " + prevY + "; ");
		s.append("currX: " + currX + "; ");
		s.append("currY: " + currY + "; ");
		return s.toString();
	}

}
