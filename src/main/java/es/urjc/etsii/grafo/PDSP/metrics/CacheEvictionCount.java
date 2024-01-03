package es.urjc.etsii.grafo.PDSP.metrics;

import es.urjc.etsii.grafo.metrics.AbstractMetric;

public class CacheEvictionCount extends AbstractMetric {

    public CacheEvictionCount(long referenceNanoTime) {
        super(referenceNanoTime);
    }
}
