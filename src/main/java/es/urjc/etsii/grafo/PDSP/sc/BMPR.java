package es.urjc.etsii.grafo.PDSP.sc;

import es.urjc.etsii.grafo.PDSP.constructives.grasp.CSharpGRGrasp;
import es.urjc.etsii.grafo.PDSP.ls.RemoveNodesLS;
import es.urjc.etsii.grafo.PDSP.model.PDSPInstance;
import es.urjc.etsii.grafo.PDSP.model.PDSPSolution;
import es.urjc.etsii.grafo.create.Reconstructive;
import es.urjc.etsii.grafo.improve.Improver;
import es.urjc.etsii.grafo.util.TimeControl;

import java.util.HashSet;
import java.util.Set;

public class BMPR extends AbstractMPR {

    public BMPR(Reconstructive<PDSPSolution, PDSPInstance> repair, Improver<PDSPSolution, PDSPInstance> improver) {
        super(repair, improver);
    }

    public BMPR(CSharpGRGrasp constructive, RemoveNodesLS ls, double evaluationProbability) {
        super(constructive, ls, evaluationProbability);
    }

    @Override
    public Set<PDSPSolution> generateNewSet(PDSPSolution[] originSolutions, PDSPSolution[] guidingSolutions) {
        var newSet = new HashSet<PDSPSolution>();
        var frequencies = freq(guidingSolutions, false);

        for (var solution : originSolutions) {
            if (TimeControl.isTimeUp()) break;
            var list = walk(solution, frequencies);
            list = repairAndLS(list);
            newSet.addAll(list);
        }

        return newSet;
    }
}
