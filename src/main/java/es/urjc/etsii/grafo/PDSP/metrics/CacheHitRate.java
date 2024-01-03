package es.urjc.etsii.grafo.PDSP.metrics;

import es.urjc.etsii.grafo.metrics.AbstractMetric;

public class CacheHitRate extends AbstractMetric {
    public CacheHitRate(long referenceNanoTime) {
        super(referenceNanoTime);
    }
}
