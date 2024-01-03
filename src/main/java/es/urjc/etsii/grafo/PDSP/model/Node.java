package es.urjc.etsii.grafo.PDSP.model;

import es.urjc.etsii.grafo.util.collections.BitSet;

import java.util.Objects;
import java.util.Set;

public class Node {
    final int id;
    final BitSet neighbors;
    BitSet unmarkedNeighbors;
    BitSet markedNeighbors;

    public Node(int id, BitSet neighbors) {
        int nNodes = neighbors.getCapacity();
        this.id = id;
        this.neighbors = new BitSet(neighbors);
        this.unmarkedNeighbors = new BitSet(nNodes, neighbors);
        this.markedNeighbors = new BitSet(nNodes);
    }

    public Node(Node n){
        this.id = n.id;
        this.neighbors = n.neighbors; // neighbors are immutable
        this.unmarkedNeighbors = new BitSet(n.unmarkedNeighbors);
        this.markedNeighbors = new BitSet(n.markedNeighbors);
    }

    public Node copy(){
        return new Node(this);
    }

    public boolean canPropagate(){
        // A node can recursively propagate
        // when the number of unmarked neighborhoods is either 0 or 1
        return unmarkedNeighbors.size() == 1;
    }

    public int propagationTarget(){
        assert this.unmarkedNeighbors.size() == 1;
        return unmarkedNeighbors.iterator().next();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node node = (Node) o;
        return id == node.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.valueOf(id);
    }

    public Set<Integer> neighbors() {
        return neighbors;
    }

    public Set<Integer> unmarked() {
        return unmarkedNeighbors;
    }

    public Set<Integer> marked() {
        return markedNeighbors;
    }
}
