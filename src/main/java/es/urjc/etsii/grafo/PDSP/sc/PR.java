package es.urjc.etsii.grafo.PDSP.sc;

import es.urjc.etsii.grafo.PDSP.model.PDSPInstance;
import es.urjc.etsii.grafo.PDSP.model.PDSPSolution;
import es.urjc.etsii.grafo.create.Reconstructive;
import es.urjc.etsii.grafo.improve.Improver;
import es.urjc.etsii.grafo.util.TimeControl;
import es.urjc.etsii.grafo.util.collections.BitSet;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PR extends AbstractMPR {

    public PR(Reconstructive<PDSPSolution, PDSPInstance> repair, Improver<PDSPSolution, PDSPInstance> improver) {
        super(repair, improver);
    }

    @Override
    public Set<PDSPSolution> generateNewSet(PDSPSolution[] originSolutions, PDSPSolution[] guidingSolutions) {
        if(originSolutions.length != 0){
            throw new IllegalArgumentException("PR will run for each pair of guiding solutions, not using diverse solutions");
        }

        var generatedSolutions = new HashSet<PDSPSolution>();

        // For each pair of solutions without repeating A-->B, B-->A
        for (int i = 0; i < guidingSolutions.length-1; i++) {
            for (int j = i + 1; j < guidingSolutions.length; j++) {
                if (TimeControl.isTimeUp()) break;
                // Walk the path from solution I to J taking into account all intermediate solutions
                var list = _walk(guidingSolutions[i], guidingSolutions[j]);
                // Repair and run LS on all intermediate solutions
                list = repairAndLS(list);
                generatedSolutions.addAll(list);
            }
        }

        return generatedSolutions;
    }

    protected List<PDSPSolution> _walk(PDSPSolution a, PDSPSolution b) {
        var stats = this.stats.get();
        // Should never happen, just in case
        if(a.getInstance() != b.getInstance()){
            throw new IllegalArgumentException("Different instances for solutions in Path Relinking");
        }

        var generatedSolutions = new ArrayList<BitSet>();
        var nodesInA = a.getRemovableNodes();
        var nodesInB = b.getRemovableNodes();

        // We have to add the nodes that are in B that are not in A, and remove those in A not in B
        var nodesToAdd = BitSet.difference(nodesInB, nodesInA);
        var nodesToRemove = BitSet.difference(nodesInA, nodesInB);

        // Copy solution that is going to be modified during walk
        var current = a.cloneSolution();
        stats.nIteraciones().getAndIncrement();

        while(nodesToRemove.size() > nodesToAdd.size()){
            var nextRemovedNode = iteratorNext(nodesToRemove);
            nodesToRemove.remove(nextRemovedNode);
            current.removeNode(nextRemovedNode);
            generatedSolutions.add(current.getChosenCopy());
            stats.nSteps().getAndIncrement();
        }

        while(nodesToAdd.size() > nodesToRemove.size()){
            var nextAddedNode = iteratorNext(nodesToAdd);
            nodesToAdd.remove(nextAddedNode);
            current.add(nextAddedNode);
            generatedSolutions.add(current.getChosenCopy());
            stats.nSteps().getAndIncrement();
        }

        // Same size, start swapping
        while(!nodesToAdd.isEmpty()){
            // Swap first element in each set, until set is empty
            var nextAddedNode = iteratorNext(nodesToAdd);
            var nextRemovedNode = iteratorNext(nodesToRemove);
            nodesToAdd.remove(nextAddedNode);
            nodesToRemove.remove(nextRemovedNode);
            current.removeNode(nextRemovedNode);
            current.add(nextAddedNode);
            generatedSolutions.add(current.getChosenCopy());
            stats.nSteps().getAndIncrement();
        }

        // because sets have the same size before starting the last while loop,
        // verify they are emptied at the same time
        assert nodesToRemove.isEmpty();

        assert a.getInstance() == b.getInstance();
        return filterSolutions(a.getInstance(), generatedSolutions);
    }

    private int iteratorNext(BitSet nodesToRemove) {
        var iterator = nodesToRemove.iterator();
        if(!iterator.hasNext()){
            // If nodesToRemove size is greater than nodesToAdd, the iterator MUST have elements to return
            throw new IllegalStateException("Impossible");
        }
        return iterator.next();
    }

}
