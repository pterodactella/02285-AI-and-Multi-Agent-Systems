package searchclient;

import java.util.ArrayList;
import java.util.HashMap;

public abstract class Distances {
    private ArrayList<Agent> agents;
	private char[][] goals;
	private char[][] boxes;
	protected HashMap<Character, int[]> coordinates;
	public Distances(ArrayList<Agent> agents, char[][] goals, char[][] boxes) {
        this.agents = agents;
		this.goals = goals;
		this.boxes = boxes;
		
		parseCoordinates();
		
	}
	
	public abstract int calculate();
	public void parseCoordinates() {
		this.coordinates = new HashMap<>();

		
		for(int i=0; i<this.boxes.length; i++) {
			for(int j=0; j<this.boxes[i].length; j++) {
				if('A' <= this.boxes[i][j] && this.boxes[i][j] <= 'Z') {
				coordinates.put((char) this.boxes[i][j], new int[] {i,j, 0, 0});
				}
			}
		}

		for(int i=0; i<this.goals.length; i++) {
			for(int j=0; j<this.goals[i].length; j++) {
//				if ('0' <= this.goals[i][j] && this.goals[i][j] <= '9') {
//					coordinates.get(this.goals[i][j])[2] = i;
//					coordinates.get(this.goals[i][j])[3] = j;
				if('A' <= this.goals[i][j] && this.goals[i][j] <= 'Z') {
					coordinates.get(this.goals[i][j])[2] = i;
					coordinates.get(this.goals[i][j])[3] = j;
					
					
				}
			}
		}
		for (int i = 0; i < agents.size(); i++) {
			Agent agent = agents.get(i);
			coordinates.put(Character.forDigit(agent.id, 10), new int[]{agent.row, agent.col, 0, 0});
		}
		
		
	}
}


class ManhattanDistance extends Distances {
    public ManhattanDistance(ArrayList<Agent> agents, char[][] goals, char[][] boxes) {
        super(agents, goals, boxes);
    }

    @Override
    public int calculate() {
        int sum = 0;
        for (HashMap.Entry<Character, int[]> set : this.coordinates.entrySet()) {
            int[] location = set.getValue();
            sum += Math.abs(location[2] - location[0]) + Math.abs(location[3] - location[1]);
        }
        return sum;
    }
}

class EuclideanDistanceWithoutRoot extends Distances {
    public EuclideanDistanceWithoutRoot(ArrayList<Agent> agents, char[][] goals, char[][] boxes) {
        super(agents, goals, boxes);
    }

    @Override
    public int calculate() {
        int sum = 0;
        for (HashMap.Entry<Character, int[]> set : this.coordinates.entrySet()) {
            int[] location = set.getValue();
            sum += Math.pow(location[2] - location[0], 2) + Math.pow(location[3] - location[1], 2);
        }
        return sum;
    }
}