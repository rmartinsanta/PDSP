package es.urjc.etsii.grafo.PDSP.model;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.util.collections.BitSet;

import java.util.*;


/**
 * Represents a PDSP instance, analyzing and extracting several metrics that may be useful both during solving
 * and then during the experiment results analysis
 */
public class PDSPInstance extends Instance {

    /**
     * Graph representation mapping each node to its adjacent nodes
     */
    private final Map<Integer, BitSet> graph;
    /**
     * Set of leafs in the graph. A leaf is a node with only one adjacent node.
     */
    private final BitSet leafs;

    /**
     * Set of super critical nodes in the graph.
     * A node is critical when it must be in the solution in order for it to be feasible.
     * A node is considered critical when it has two or more adjacent leafs.
     */
    private final BitSet criticalNodes;

    /**
     * Because leafs can never be part of the solution, and critical nodes must always be part of the solution,
     * there is a limited set of nodes that can be added and removed from it.
     * Therefore, the set of nodes that can be added at any point of time becomes configurableNodes - alreadyAddedNodes,
     * and the set of nodes that can be removed is configurableNodes INTERSECT alreadyAddedNodes
     */
    private final BitSet configurableNodes;

    /**
     * Reference solution that contains all critical nodes, used as a base solution
     */
    private final PDSPSolution referenceSolution;

    /**
     * New nodes marked for each configurable node if starting from the reference solution
     */
    private final Map<Integer, BitSet> newNodesMarked;

    public PDSPInstance(String name, Map<Integer, Set<Integer>> graph){
        super(name);
        this.graph = new HashMap<>();
        this.leafs = new BitSet(graph.size());
        this.criticalNodes = new BitSet(graph.size());
        for(var e: graph.entrySet()){
            this.graph.put(e.getKey(), new BitSet(graph.size(), e.getValue()));
        }

        if(this.graph.size() <= 2){ // Must have at least three elements in the graph
            throw new IllegalArgumentException(String.format("The number of nodes in instance %s is %s, should have at least 3 elements", name, graph.size()));
        }

        classifyNodes();
        this.configurableNodes = calculateConfigurableNodes();
        this.referenceSolution = buildReferenceSolution();
        this.newNodesMarked = calculateNodeMarks();
        this.setProperty("nNodes", this.graph.size());
        this.setProperty("leafNodes", this.leafs.size());
        this.setProperty("criticalNodes", this.criticalNodes.size());
        this.setProperty("configurableNodes", this.configurableNodes.size());
        this.setProperty("unmarkedNodes", this.referenceSolution.getUnmarked().size());
        this.setProperty("density", calculateDensity());
    }

    private Map<Integer, BitSet> calculateNodeMarks() {
        var result = new HashMap<Integer, BitSet>();
        for(int n: configurableNodes){
            var copy = this.referenceSolution.cloneSolution();
            copy.add(n);
            var newNodes = BitSet.difference(referenceSolution.getUnmarked(), copy.getUnmarked());
            result.put(n, newNodes);
        }
        return result;
    }

    private double calculateDensity() {
        int nNodes = this.graph.size();
        int maxEdges = nNodes * (nNodes - 1) / 2;
        int realEdges = 0;
        for(var l: this.graph.values()){
            realEdges += l.size();
        }

        return realEdges / (double) maxEdges;
    }

    private void classifyNodes() {
        int[] leafsPerNode = new int[this.nNodes()];
        for(var e: this.graph.entrySet()){
            var neighs = e.getValue();
            if(neighs.size() == 1){
                this.leafs.add(e.getKey());
                int neighbor = neighs.iterator().next(); // Size 1 --> Single neighbor
                leafsPerNode[neighbor]++;                // neighboor has a leaf
            }
        }
        for (int i = 0; i < leafsPerNode.length; i++) {
            if(leafsPerNode[i] >= 2){
                this.criticalNodes.add(i);
            }
        }
    }

    @Override
    public int compareTo(Instance other) {
        var instance = (PDSPInstance) other;
        return Comparator.comparing(PDSPInstance::nNodes).compare(this, instance);
    }

    public int nNodes(){
        return this.graph.size();
    }

    public Map<Integer, BitSet> graph() {
        return graph;
    }

    public Set<Integer> nodes(){
        return this.graph.keySet();
    }

    public boolean isLeaf(int nodeId) {
        return this.leafs.get(nodeId);
    }

    public boolean isCritical(int nodeId) {
        return this.criticalNodes.get(nodeId);
    }

    public BitSet configurableNodes(){
        return this.configurableNodes;
    }

    public boolean isConfigurable(int nodeId){
        return this.configurableNodes.get(nodeId);
    }

    /**
     * How many nodes can we pick among
     * @return
     */
    public BitSet calculateConfigurableNodes(){
        var configurableNodes = new BitSet(this.graph.size());
        configurableNodes.add(0, this.graph.size());
        for(var n: this.leafs){
            configurableNodes.remove(n);
        }
        for(var n: this.criticalNodes){
            configurableNodes.remove(n);
        }
        return configurableNodes;
    }

    public PDSPSolution buildReferenceSolution(){
        var solution = new PDSPSolution(this, true);
        for(var n: criticalNodes){
            solution.add(n);
        }
        return solution;
    }

    public PDSPSolution referenceSolution(){
        return this.referenceSolution;
    }

    public String cardinality(){
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < this.graph.size(); i++) {
            sb.append(i).append('\t').append(graph.get(i).size()).append('\n');
        }
        return sb.toString();
    }
    public String observadality(){
        int nNodes = this.graph.size();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < nNodes; i++) {
            PDSPSolution solution = new PDSPSolution(this);
            solution.add(i);
            int observes = nNodes - solution.getUnmarked().size();
            sb.append(i).append('\t').append(observes).append('\n');
        }
        return sb.toString();
    }

    public BitSet getNewNodesMarked(int nodeId){ // new nodes marked when picking the given nodeId
        return this.newNodesMarked.get(nodeId);
    }
}
