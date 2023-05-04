package searchclient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CBSNode {

    public State state; // The state of the node
    public CBSNode parent;
    public Agent agent;

    public CBSNode(State state) {
        this.state = state;
        this.parent = null;

    }

    public CBSNode(State state, CBSNode parent) {
        this.state = state;
        this.parent = parent;

    }

    public CBSNode(State state, Agent agent) {
        this.state = state;
        this.agent = agent;

    }

    public State getState() {
        return state;
    }

    public CBSNode getParent() {
        return parent;
    }

    public void setParent(CBSNode parent) {
        this.parent = parent;
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
        CBSNode other = (CBSNode) obj;
        if (this.parent != other.parent) {
            return false;
        }
        return state.equals(other.state);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((parent == null) ? 0 : parent.hashCode());
        result = prime * result + ((state == null) ? 0 : state.hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("State: ").append(state.toString()).append("\n");
        sb.append("Parent: ");
        if (parent == null) {
            sb.append("null");
        } else {
            sb.append(parent.getState().toString());
        }
        sb.append("\n");
        return sb.toString();
    }

}
