package es.urjc.etsii.grafo.PDSP.experiments;

import es.urjc.etsii.grafo.PDSP.constructives.grasp.CSharpGRGrasp;
import es.urjc.etsii.grafo.PDSP.constructives.grasp.PDSPGRASPMove;
import es.urjc.etsii.grafo.PDSP.constructives.grasp.SmartPDSPListManager;
import es.urjc.etsii.grafo.PDSP.ls.RemoveNodesLS;
import es.urjc.etsii.grafo.PDSP.model.PDSPInstance;
import es.urjc.etsii.grafo.PDSP.model.PDSPSolution;
import es.urjc.etsii.grafo.PDSP.sc.*;
import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.algorithms.FMode;
import es.urjc.etsii.grafo.algorithms.scattersearch.ScatterSearch;
import es.urjc.etsii.grafo.experiment.AbstractExperiment;

import java.util.ArrayList;
import java.util.List;

public class Experiment4 extends AbstractExperiment<PDSPSolution, PDSPInstance> {
    @Override
    public List<Algorithm<PDSPSolution, PDSPInstance>> getAlgorithms() {
        var candidateList = new SmartPDSPListManager(true);
        var constructive = new CSharpGRGrasp(0.25, candidateList, PDSPGRASPMove::getNUnmarkedNeighbors);
        var constructiveDiversity = new CSharpGRGrasp(1, candidateList, PDSPGRASPMove::getNUnmarkedNeighbors);
        var ls = new RemoveNodesLS();
        var mprs = new AbstractMPR[]{
                new BMPRv4(constructive, ls, 1),
                new WMPRv4(constructive, ls, 1),
                new PMPRv4(constructive, ls, 1),
        };

        var list = new ArrayList<Algorithm<PDSPSolution, PDSPInstance>>();
        for (int i = 0; i < mprs.length; i++) {
            list.add(new ScatterSearch<>("SS" + i,
                    10, 10,
                    constructive, constructiveDiversity,
                    ls, mprs[i],
                    FMode.MINIMIZE, 10_000, // Big a high value for maxIterations because TimeControl is enabled
                    0.5, new PDSPDistance(), false
            ));
        }
        return list;
    }
}
