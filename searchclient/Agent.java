package searchclient;

import java.util.ArrayList;

public class Agent {
    public int id;
    public int row;
    public int col;
    public Color color;
    public ArrayList<CBSNode> path;

    public Agent(int id, int row, int col, Color color) {
        this.id = id;
        this.row = row;
        this.col = col;
        this.color = color;
        this.path = new ArrayList<CBSNode>();
    }

    public Agent copy() {
        Agent copy = new Agent(this.id, this.row, this.col, this.color);
        copy.path = new ArrayList<CBSNode>(this.path);
        return copy;
    }

    public ArrayList<Constraints> resolveConflictsWith(Agent other) {
        ArrayList<Constraints> constraints = new ArrayList<>();
        if (this.color == other.color) {
            // Agents have the same color and can't occupy the same cell at the same time
            if (this.row == other.row && this.col == other.col) {
                Constraints constraint1 = new Constraints(this.id, this.row + 1, this.col, null);
                Constraints constraint2 = new Constraints(this.id, this.row - 1, this.col, null);
                Constraints constraint3 = new Constraints(this.id, this.row, this.col + 1, null);
                Constraints constraint4 = new Constraints(this.id, this.row, this.col - 1, null);
                Constraints constraint5 = new Constraints(other.id, other.row + 1, other.col, null);
                Constraints constraint6 = new Constraints(other.id, other.row - 1, other.col, null);
                Constraints constraint7 = new Constraints(other.id, other.row, other.col + 1, null);
                Constraints constraint8 = new Constraints(other.id, other.row, other.col - 1, null);
                constraints.add(constraint1);
                constraints.add(constraint2);
                constraints.add(constraint3);
                constraints.add(constraint4);
                constraints.add(constraint5);
                constraints.add(constraint6);
                constraints.add(constraint7);
                constraints.add(constraint8);
            }
        } else {
            // Agents have different colors and can't be at the same cell at the same time
            if (this.row == other.row && this.col == other.col) {
                Constraints constraint1 = new Constraints(this.id, this.row, this.col, null);
                Constraints constraint2 = new Constraints(other.id, other.row, other.col, null);
                constraints.add(constraint1);
                constraints.add(constraint2);
            }
    
            // Agents have different colors and can't swap places at the same time
            if (this.row == other.row + 1 && this.col == other.col && other.row == this.row + 1) {
                Constraints constraint = new Constraints(this.id, this.row + 1, this.col, null);
                constraints.add(constraint);
            } else if (this.row == other.row - 1 && this.col == other.col && other.row == this.row - 1) {
                Constraints constraint = new Constraints(this.id, this.row - 1, this.col, null);
                constraints.add(constraint);
            } else if (this.row == other.row && this.col == other.col + 1 && other.col == this.col - 1) {
                Constraints constraint = new Constraints(this.id, this.row, this.col + 1, null);
                constraints.add(constraint);
            } else if (this.row == other.row && this.col == other.col - 1 && other.col == this.col + 1) {
                Constraints constraint = new Constraints(this.id, this.row, this.col - 1, null);
                constraints.add(constraint);
            }
        }
        return constraints;
    }
    
    
    
}
