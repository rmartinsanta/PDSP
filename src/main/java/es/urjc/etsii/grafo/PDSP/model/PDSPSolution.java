package es.urjc.etsii.grafo.PDSP.model;

import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.util.collections.BitSet;

import java.util.*;

public class PDSPSolution extends Solution<PDSPSolution, PDSPInstance> {

    private final Node[] nodes;
    private final BitSet chosen;
    private BitSet unmarked;

    /**
     * Initialize solution from instance
     *
     * @param instance
     */
    public PDSPSolution(PDSPInstance instance, boolean forceEmpty) {
        super(instance);
        nodes = new Node[instance.nNodes()];
        if (forceEmpty) {
            for (var e : instance.graph().entrySet()) {
                int id = e.getKey();
                nodes[id] = new Node(id, e.getValue());
            }
            // All nodes start being unmarked, and no nodes are chosen
            this.unmarked = new BitSet(nodes.length);
            this.unmarked.add(0, nodes.length);
            this.chosen = new BitSet(nodes.length);
        } else {
            var refSol = instance.referenceSolution();
            for (var n : refSol.nodes) {
                nodes[n.id] = new Node(n);
            }
            this.unmarked = new BitSet(refSol.unmarked);
            this.chosen = new BitSet(refSol.chosen);
        }
    }

    /**
     * Initialize solution from instance
     *
     * @param instance
     */
    public PDSPSolution(PDSPInstance instance) {
        this(instance, false);
    }

    /**
     * Clone constructor
     *
     * @param s Solution to clone
     */
    public PDSPSolution(PDSPSolution s) {
        super(s);
        this.nodes = new Node[s.nodes.length];
        for (int i = 0; i < s.nodes.length; i++) {
            this.nodes[i] = s.nodes[i].copy();
        }
        this.chosen = new BitSet(s.chosen);
        this.unmarked = new BitSet(s.unmarked);
    }


    @Override
    public PDSPSolution cloneSolution() {
        // You do not need to modify this method
        // Call clone constructor
        return new PDSPSolution(this);
    }

    @Override
    protected boolean _isBetterThan(PDSPSolution other) {
        return this.chosen.size() < other.chosen.size();
    }

    /**
     * Get the current solution score.
     * The difference between this method and recalculateScore is that
     * this result can be a property of the solution, or cached,
     * it does not have to be calculated each time this method is called
     *
     * @return current solution score as double
     */
    @Override
    public double getScore() {
        return this.chosen.size();
    }

    /**
     * Recalculate solution score from scratch, using the problem objective function.
     * The difference between this method and getScore is that we must recalculate the score from scratch,
     * without using any cache/shortcuts.
     * This method will be used to validate the correct behaviour of the getScore() method, and to help catch
     * bugs or mistakes when changing incremental score calculation.
     *
     * @return current solution score as double
     */
    @Override
    public double recalculateScore() {
        validateSolutionState();
        return this.chosen.size();
    }

    public BitSet getChosen() {
        return chosen;
    }

    public BitSet getChosenCopy() {
        return new BitSet(this.chosen);
    }

    public BitSet getUnmarked() {
        return unmarked;
    }

    public BitSet getObservedNodes(){
        return BitSet.not(unmarked);
    }

    /**
     * Remove the given node from the solution. The resulting solution MAY NOT BE FEASIBLE
     *
     * @param nodeId node to remove
     */
    public void removeNode(int nodeId) {
        this.chosen.remove(nodeId);
        recalculateMarkedNodes(); // TODO: Potential improvement, instead of recalculating marks from scratch
        notifyUpdate();
    }

    /**
     * Are we covering all nodes?
     *
     * @return true if all nodes are covered according
     * to the problem propagation rules, false otherwise
     */
    public boolean isCovered() {
        return this.unmarked.isEmpty();
    }

    private void validateSolutionState() {
        var copy = this.cloneSolution();
        copy.recalculateMarkedNodes();

        assert this.chosen.equals(copy.chosen);
        assert this.unmarked.equals(copy.unmarked);
    }

    public void resetMarkedNodes() {
        var refSol = getInstance().referenceSolution();
        for (var n : refSol.nodes) {
            nodes[n.id] = new Node(n);
        }

        this.unmarked = new BitSet(refSol.unmarked);
    }

    /**
     * Unmark all nodes and run propagation algorithm again
     */
    public void recalculateMarkedNodes() {
        resetMarkedNodes();
        var instance = getInstance();

        var propagators = new HashSet<Integer>();
        for (var node : this.chosen) {
            if (!instance.isConfigurable(node)) {
                continue;
            }
            this.unmarked.remove(node);
            unmarkNeighbors(node); // TODO optimize?
            propagators.addAll(this.nodes[node].neighbors);
        }
        propagateMarksFrom(new ArrayDeque<>(propagators));
    }

    private void propagateMarksFrom(ArrayDeque<Integer> propagators) {
        /* When a new node is marked, there are two propagation cases:
                - When there is a line or a structure like A --> B --> C
                  B can now be marked, and later C
                                                                D
                                                                |
                - When there is a star like structure           B
                  If both B and D are marked, and we mark     /   \
                  A, C can be propagated later               A     C
                  neighborsRecheck marks all neighbors that must be rechecked for propagation at the end
             */

        Set<Integer> neighborsRecheck;
        do {
            neighborsRecheck = new HashSet<>();
            while (!propagators.isEmpty()) {
                int currentNodeId = propagators.remove();
                this.unmarked.remove(currentNodeId);
                unmarkNeighbors(currentNodeId);

                var node = this.nodes[currentNodeId];
                // Neighbors must be rechecked because one that had two unmarked may now have only 1 and keep propagating
                neighborsRecheck.addAll(node.markedNeighbors);
                if (node.canPropagate()) {
                    // propagation target
                    int target = node.propagationTarget();
                    propagators.add(target);
                }
            }
            for (int n : neighborsRecheck) {
                var node = this.nodes[n];
                if (node.canPropagate()) {
                    propagators.add(node.propagationTarget());
                }
            }
        } while (!propagators.isEmpty());

    }

    private void unmarkNeighbors(int id) {
        for (var neighId : this.nodes[id].neighbors) {
            var neigh = this.nodes[neighId];
            neigh.unmarkedNeighbors.remove(id);
            neigh.markedNeighbors.add(id);
        }
    }

    /**
     * Pick and include node n in solution
     *
     * @param n
     */
    public void add(int n) {
        this.unmarked.remove(n);
        this.chosen.add(n);
        unmarkNeighbors(n);
        var propagators = new ArrayDeque<>(this.nodes[n].neighbors);
        propagateMarksFrom(propagators);
        notifyUpdate();
    }

    /**
     * Check how many nodes would be observed if this node were to be added to the solution
     *
     * @param n node to check
     */
    public int testAdd(int n) {
        if(this.chosen.contains(n)) throw new IllegalArgumentException("Node " + n + " is already in the solution");
        var removedFromUnmarked = new HashSet<Integer>();
        removedFromUnmarked.add(n);
        var propagators = new ArrayDeque<>(this.nodes[n].neighbors);
        Set<Integer> neighborsRecheck;
        do {
            neighborsRecheck = new HashSet<>();
            while (!propagators.isEmpty()) {
                int currentNodeId = propagators.remove();
                removedFromUnmarked.add(currentNodeId);
                var node = this.nodes[currentNodeId];
                // Neighbors must be rechecked because one that had two unmarked may now have only 1 and keep propagating
                for(var neigh: node.neighbors){
                    if(node.markedNeighbors.get(neigh) || removedFromUnmarked.contains(neigh)){
                        neighborsRecheck.add(neigh); // A marked node is any that was already marked or has already been removed from unmarked becouse it gets marked
                    }
                }

                var set = new HashSet<>(node.neighbors);
                set.removeAll(removedFromUnmarked);
                if (set.size() == 1) {
                    // propagation target
                    int target = set.iterator().next();
                    propagators.add(target);
                }
            }
            for (int neigh : neighborsRecheck) {
                var node = this.nodes[neigh];
                var set = new HashSet<>(node.neighbors);
                set.removeAll(removedFromUnmarked);
                if (set.size() == 1) {
                    propagators.add(set.iterator().next());
                }
            }
        } while (!propagators.isEmpty());
        return removedFromUnmarked.size();
    }

    public Node node(int id) {
        return this.nodes[id];
    }

    /**
     * Check if a node can be removed
     *
     * @param node node to check
     * @return true if the node can be removed,
     * false if the solution does not contain the given node or the node is locked in place
     */
    public boolean removable(int node) {
        return this.chosen.contains(node) && this.getInstance().isConfigurable(node);
    }

    /**
     * Check if a node can be added
     *
     * @param node node to check
     * @return true if the node is not excluded and it is not already in the solution, false otherwise
     */
    public boolean addable(int node) {
        return !this.chosen.contains(node) && this.getInstance().isConfigurable(node);
    }

    /**
     * Get all nodes that can be added to the solution
     *
     * @return set with all nodes not already in the solution and not excluded
     */
    public BitSet getAddableNodes() {
        return BitSet.difference(this.getInstance().configurableNodes(), this.chosen);
    }

    /**
     * Get all nodes that can be potentially removed from the solution
     *
     * @return set with all nodes in the solution that have not been locked
     */
    public BitSet getRemovableNodes() {
        return BitSet.intersection(this.getInstance().configurableNodes(), this.chosen);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PDSPSolution solution = (PDSPSolution) o;
        return Objects.equals(chosen, solution.chosen);
    }

    @Override
    public int hashCode() {
        return Objects.hash(chosen);
    }

    /**
     * Generate a string representation of this solution. Used when printing progress to console,
     * show as minimal info as possible
     *
     * @return Small string representing the current solution (Example: id + score)
     */
    @Override
    public String toString() {
        return this.chosen.size() + " -> " + this.chosen;
    }
}
