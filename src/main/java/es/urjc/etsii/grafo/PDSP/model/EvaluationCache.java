package es.urjc.etsii.grafo.PDSP.model;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import es.urjc.etsii.grafo.PDSP.metrics.CacheEvictionCount;
import es.urjc.etsii.grafo.PDSP.metrics.CacheHitRate;
import es.urjc.etsii.grafo.PDSP.metrics.CacheRequests;
import es.urjc.etsii.grafo.PDSP.sc.AbstractMPR;
import es.urjc.etsii.grafo.metrics.Metrics;
import es.urjc.etsii.grafo.util.collections.BitSet;


public class EvaluationCache {
    Cache<BitSet, Double> cache;
    AbstractMPR mpr;

    public EvaluationCache(AbstractMPR mpr){
        this.mpr = mpr;
        this.cache = Caffeine
                .newBuilder()
                .recordStats()
                .maximumWeight(cacheSize())
                .weigher((BitSet k,Double v) -> k.size() / 64 + 1)
                .build();
    }

    public static long cacheSize(){
        long cacheBytes = Runtime.getRuntime().maxMemory() / 32; // Half ram for cache
        // Peso es numero de longs por set. Un long son 8 bytes.
        return cacheBytes / 8;
    }

    public double score(BitSet nodes, PDSPInstance instance){
        return this.cache.get(nodes, k -> evaluator(mpr, instance, k).getScore());
    }

    public void invalidate(){
        this.cache.invalidateAll();
    }

    public static PDSPSolution evaluator(AbstractMPR mpr, PDSPInstance instance, BitSet nodeSet){
        var s = new PDSPSolution(instance);
        // TODO optimize multiadd?
        for(int n: nodeSet){
            if(!instance.isCritical(n)){
                s.add(n);
            }
        }
        s = mpr.repairAndLS(s);
        assert s.isCovered();
        return s;
    }

    public void reportMetrics(){
        var stats = cache.stats();
        Metrics.add(CacheHitRate.class, stats.hitRate());
        Metrics.add(CacheEvictionCount.class, stats.evictionCount());
        Metrics.add(CacheRequests.class, stats.requestCount());
    }

    public double getHitRate(){
        var stats = cache.stats();
        return stats.hitRate();
    }
}
