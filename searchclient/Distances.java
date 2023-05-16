package searchclient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

//This class deals with the distances that are used for push and pull boxes, be aware there is 2 type of Distance class that we uploaded.
public abstract class Distances {
	private int[] agentRows;
	private int[] agentCols;
	private char[][] goals;
	private char[][] boxes;
	protected HashMap<Character, ArrayList<int[]>> coordinates;
	protected HashMap<Character, int[]> agentCoordinates;

	public Distances(int[] agentRows, int[] agentCols, char[][] goals, char[][] boxes) {
		this.agentRows = agentRows.clone();
		this.agentCols = agentCols.clone();
		this.goals = new char[goals.length][];
		for (int i = 0; i < goals.length; i++) {
			this.goals[i] = goals[i].clone();
		}
		this.boxes = new char[boxes.length][];
		for (int i = 0; i < boxes.length; i++) {
			this.boxes[i] = boxes[i].clone();
		}
		
		this.agentCoordinates = new HashMap<>();
		for(int i = 0; i < this.agentRows.length; i++) {
			this.agentCoordinates.put((char)('0' + i), new int[] { this.agentRows[i], this.agentCols[i], 0, 0 });
		}
		
//		this.goals = goals;
//		this.boxes = boxes;

		parseCoordinates();

//		for (HashMap.Entry<Character, int[]> set :
//            this.coordinates.entrySet()) {
//			System.out.println(set.getKey() + " = "
//                    + Arrays.toString(set.getValue()));
//       }
	}

	public abstract int calculate();

	public void parseCoordinates() {
		this.coordinates = new HashMap<>();
        for (int i = 0; i < boxes.length; i++) {
            for (int j = 0; j < boxes[i].length; j++) {
                if ('A' <= boxes[i][j] && boxes[i][j] <= 'Z') {
                    char boxChar = boxes[i][j];
                    if (!coordinates.containsKey(boxChar)) {
                        coordinates.put(boxChar, new ArrayList<>());
                    }
                    coordinates.get(boxChar).add(new int[]{i, j, -1, -1});
                }
            }
        }

        for (int i = 0; i < goals.length; i++) {
        	
            for (int j = 0; j < goals[i].length; j++) {
//                System.err.print(Character.toString(goals[i][j]));

                if ('A' <= goals[i][j] && goals[i][j] <= 'Z') {
                    char goalChar = goals[i][j];
                    if (coordinates.containsKey(goalChar)) {
                        ArrayList<int[]> boxCoordinates = coordinates.get(goalChar);
                        for (int[] arr : boxCoordinates) {
                            if (arr[2] == -1) {
                                arr[2] = i;
                                arr[3] = j;
                                break; 
                            }
                        }
                    }
                } else if('0' <= goals[i][j] && goals[i][j] <= '9'){
                    this.agentCoordinates.get(goals[i][j])[2] = i;
                    this.agentCoordinates.get(goals[i][j])[3] = j;
                }
            }

	}
        

        
	}
}

class EuclideanDistanceWithoutRoot extends Distances {
	public EuclideanDistanceWithoutRoot(int[] agentRows, int[] agentCols, char[][] goals, char[][] boxes) {
		super(agentRows, agentCols, goals, boxes);
	}

	@Override
	public int calculate() {
		int sum = 0;
		for (HashMap.Entry<Character, ArrayList<int[]>> set : this.coordinates.entrySet()) {
			for (int[] location : set.getValue()) {
				sum += Math.pow(location[2] - location[0], 2) + Math.pow(location[3] - location[1], 2);
			}
		}
		return sum;
	}
}