package es.urjc.etsii.grafo.PDSP.metrics;

import es.urjc.etsii.grafo.metrics.AbstractMetric;

public class CacheEvictionCount extends AbstractMetric {

    protected CacheEvictionCount(long referenceNanoTime) {
        super(referenceNanoTime);
    }
}
