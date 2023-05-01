package searchclient;

import java.util.ArrayList;

public class CBSNode implements Comparable<CBSNode> {

    public State state; // The state of the node
    private CBSNode parent;
    private int agentIndex; // The index of the agent that made a move to reach this node
    private CBSNode child; // The child node of this node
    private ArrayList<Constraints> constraints;

    public CBSNode(State state, CBSNode parent) {
        this.state = state;
        this.parent = parent;
    }

    public CBSNode(State state, CBSNode parent, int agentIndex, ArrayList<Constraints> constraints, CBSNode child) {
        this.state = state;
        this.parent = parent;
        this.agentIndex = agentIndex;
        this.constraints = constraints == null ? new ArrayList<>() : constraints;
        this.child = child;
    }

    public CBSNode(State state, CBSNode parent, int agentIndex, Constraints c, CBSNode child) {
        this.state = state;
        this.parent = parent;
        this.agentIndex = agentIndex;
        this.addConstraints(c);
        this.child = child;
    }

    public void addConstraints(Constraints c) {
        this.constraints.add(c);
    }

    public State getState() {
        return state;
    }

    public CBSNode getParent() {
        return parent;
    }

    public int getCost() {
        int cost = 0;
        CBSNode currentNode = this;
        while (currentNode.getParent() != null) {
            cost++;
            currentNode = currentNode.getParent();
        }
        return cost;
    }

    @Override
    public int compareTo(CBSNode other) {
        return Integer.compare(getCost(), other.getCost());
    }

}