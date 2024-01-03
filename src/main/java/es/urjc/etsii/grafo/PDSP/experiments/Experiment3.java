package es.urjc.etsii.grafo.PDSP.experiments;

import es.urjc.etsii.grafo.PDSP.algorithm.*;
import es.urjc.etsii.grafo.PDSP.constructives.grasp.CSharpGRGrasp;
import es.urjc.etsii.grafo.PDSP.constructives.grasp.PDSPGRASPMove;
import es.urjc.etsii.grafo.PDSP.constructives.grasp.SmartPDSPListManager;
import es.urjc.etsii.grafo.PDSP.ls.RemoveNodesLS;
import es.urjc.etsii.grafo.PDSP.model.PDSPInstance;
import es.urjc.etsii.grafo.PDSP.model.PDSPSolution;
import es.urjc.etsii.grafo.PDSP.sc.*;
import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.experiment.AbstractExperiment;

import java.util.List;

public class Experiment3 extends AbstractExperiment<PDSPSolution, PDSPInstance> {

    @Override
    public List<Algorithm<PDSPSolution, PDSPInstance>> getAlgorithms() {

        var constructive = new CSharpGRGrasp(0.25, new SmartPDSPListManager(true), PDSPGRASPMove::getNUnmarkedNeighbors);
        var reconstructive = constructive;
        var ls = new RemoveNodesLS();
        //Parallel.initialize(8);
        return List.of(
                // Normal PR
                new GraspPROnly("PR", 100, 5, 5, new PR(reconstructive, ls), constructive, ls),

                // 6 versions of MPR
                new GraspPRv1("MPR1_v1", 100, 5, 5, new BMPR(reconstructive, ls), constructive, ls),
                new GraspPRv1("MPR2_v1", 100, 5, 5, new WMPR(reconstructive, ls), constructive, ls),
                new GraspPRv1("MPR3_v1", 100, 5, 5, new PMPR(reconstructive, ls), constructive, ls),

                new GraspPRv2("MPR1_v2", 100, 5, 5, new BMPR(reconstructive, ls), constructive, ls),
                new GraspPRv2("MPR2_v2", 100, 5, 5, new WMPR(reconstructive, ls), constructive, ls),
                new GraspPRv2("MPR3_v2", 100, 5, 5, new PMPR(reconstructive, ls), constructive, ls),

                new GraspPRv3("MPR1_v3", 100, 5, 5, new BMPR(reconstructive, ls), constructive, ls),
                new GraspPRv3("MPR2_v3", 100, 5, 5, new WMPR(reconstructive, ls), constructive, ls),
                new GraspPRv3("MPR3_v3", 100, 5, 5, new PMPR(reconstructive, ls), constructive, ls),

                new GraspPRv4("MPR1_v4", 100, 5, 5, new BMPRv4(reconstructive, ls), constructive, ls),
                new GraspPRv4("MPR2_v4", 100, 5, 5, new WMPRv4(reconstructive, ls), constructive, ls),
                new GraspPRv4("MPR3_v4", 100, 5, 5, new PMPRv4(reconstructive, ls), constructive, ls),

                new GraspPRv5("MPR1_v5", 100, 5, 5, new BMPRv5(reconstructive, ls), constructive, ls),
                new GraspPRv5("MPR2_v5", 100, 5, 5, new WMPRv5(reconstructive, ls), constructive, ls),
                new GraspPRv5("MPR3_v5", 100, 5, 5, new PMPRv5(reconstructive, ls), constructive, ls)
        );
    }
}
