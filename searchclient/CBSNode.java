package searchclient;

import java.util.ArrayList;

public class CBSNode implements Comparable<CBSNode> {

    private State state; // The state of the node
    private CBSNode parent;
    private ArrayList<MetaAgent> metaAgents; // The meta-agents of the node
    private int cost; // The cost of the node

    public CBSNode(State state) {
        this.state = state;
        this.parent = null;
        this.metaAgents = null;
        this.cost = 0;
    }

    public CBSNode(State state, CBSNode parent) {
        this.state = state;
        this.parent = parent;

    }

    public State getState() {
        return state;
    }

    public CBSNode getParent() {
        return parent;
    }

    public void setParent(CBSNode parent) {
        this.parent= parent;
    }

    public ArrayList<MetaAgent> getMetaAgents() {
        return metaAgents;
    }

    public int getCost() {
        return cost;
    }

    @Override
    public int compareTo(CBSNode other) {
        return Integer.compare(getCost(), other.getCost());
    }

}
