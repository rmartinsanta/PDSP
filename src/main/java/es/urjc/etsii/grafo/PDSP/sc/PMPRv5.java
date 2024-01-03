package es.urjc.etsii.grafo.PDSP.sc;

import es.urjc.etsii.grafo.PDSP.model.PDSPInstance;
import es.urjc.etsii.grafo.PDSP.model.PDSPSolution;
import es.urjc.etsii.grafo.create.Reconstructive;
import es.urjc.etsii.grafo.improve.Improver;
import es.urjc.etsii.grafo.util.CollectionUtil;
import es.urjc.etsii.grafo.util.TimeControl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PMPRv5 extends PMPR {

    public PMPRv5(Reconstructive<PDSPSolution, PDSPInstance> repair, Improver<PDSPSolution, PDSPInstance> improver) {
        super(repair, improver);
    }

    @Override
    public Set<PDSPSolution> generateNewSet(PDSPSolution[] originSolutions, PDSPSolution[] guidingSolutions) {
        var newSet = new HashSet<PDSPSolution>();

        // for each solution, remove from set, add to list, shuffle, and pick in groups of 3
        var all = new HashSet<>(List.of(guidingSolutions));
        for(var s: guidingSolutions){
            all.remove(s);
            var randomizedGuides = new ArrayList<>(all.stream().toList());
            CollectionUtil.shuffle(randomizedGuides); // randomize order
            //if(randomizedGuides.size() % 3 != 0) throw new IllegalStateException(randomizedGuides.size() + " is not multiple of 3");
            for (int i = 2; i < randomizedGuides.size(); i++) {
                if (TimeControl.isTimeUp()) break;
                PDSPSolution[] realGuides = new PDSPSolution[]{
                        randomizedGuides.get(i-2),
                        randomizedGuides.get(i-1),
                        randomizedGuides.get(i  ),
                };
                var frequencies = freq(realGuides, false);
                var list = _walk(s, guidingSolutions, frequencies);
                list = repairAndLS(list);
                newSet.addAll(list);
            }
            all.add(s);
        }

        return newSet;
    }
}
