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

		
		for(int i=0; i<boxes.length; i++) {
			for(int j=0; j<boxes[i].length; j++) {
				if('A' <= boxes[i][j] && boxes[i][j] <= 'Z') {
				coordinates.put((char) boxes[i][j], new int[] {i,j, 0, 0});
				}
			}
		}

		for(int i=0; i<goals.length; i++) {
			for(int j=0; j<goals[i].length; j++) {
//				if ('0' <= goals[i][j] && goals[i][j] <= '9') {
//					coordinates.get(goals[i][j])[2] = i;
//					coordinates.get(goals[i][j])[3] = j;
				if('A' <= goals[i][j] && goals[i][j] <= 'Z') {
					coordinates.get(goals[i][j])[2] = i;
					coordinates.get(goals[i][j])[3] = j;
					
					
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