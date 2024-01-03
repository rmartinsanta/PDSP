package es.urjc.etsii.grafo.PDSP;

import es.urjc.etsii.grafo.PDSP.experiments.PDSPReferenceResults;
import es.urjc.etsii.grafo.PDSP.model.PDSPInstance;
import es.urjc.etsii.grafo.PDSP.model.PDSPSolution;
import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.services.TimeLimitCalculator;
import es.urjc.etsii.grafo.util.TimeUtil;

import java.util.concurrent.TimeUnit;

public class PDSPTimeLimit extends TimeLimitCalculator<PDSPSolution, PDSPInstance> {
    private final PDSPReferenceResults referenceResults;

    public PDSPTimeLimit(PDSPReferenceResults referenceResults) {
        this.referenceResults = referenceResults;
    }

    @Override
    public long timeLimitInMillis(PDSPInstance instance, Algorithm<PDSPSolution, PDSPInstance> algorithm) {
        var refResult = this.referenceResults.getValueFor(instance.getId());
        if (Double.isNaN(refResult.getTimeInSeconds())) {
            return TimeUtil.convert(1, TimeUnit.HOURS, TimeUnit.MILLISECONDS);
        } else {
            return (long) (refResult.getTimeInSeconds() * 1000);
        }
    }
}
