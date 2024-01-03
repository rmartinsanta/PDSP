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

public class WMPR extends AbstractMPR {

    public WMPR(Reconstructive<PDSPSolution, PDSPInstance> repair, Improver<PDSPSolution, PDSPInstance> improver) {
        super(repair, improver);
    }

    public WMPR(CSharpGRGrasp constructive, RemoveNodesLS ls, double evaluationProbability) {
        super(constructive, ls, evaluationProbability);
    }

    @Override
    public Set<PDSPSolution> generateNewSet(PDSPSolution[] originSolutions, PDSPSolution[] guidingSolutions) {
        var generatedSolutions = new HashSet<PDSPSolution>();
        var frequencies = freq(guidingSolutions, true);

        for (var solution : originSolutions) {
            if (TimeControl.isTimeUp()) break;
            var list = walk(solution, frequencies);
            list = repairAndLS(list);
            generatedSolutions.addAll(list);
        }

        return generatedSolutions;
    }
}
