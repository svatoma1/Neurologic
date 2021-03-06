package extras;

import discoverer.construction.template.Kappa;
import discoverer.construction.network.rules.KappaRule;
import discoverer.construction.template.Lambda;
import discoverer.construction.network.rules.SubK;
import discoverer.construction.example.Example;
import discoverer.learning.functions.Activations;
import java.util.WeakHashMap;

/**
 * Experimental!
 * Estimate the upperbound more precisely
 */
public class Estimator {
    private static Example example;
    private static WeakHashMap<Object, Double> cache = new WeakHashMap<Object, Double>();

    public static double estimate(Kappa k, Example e) {
        if (e != example) {
            example = e;
            cache.clear();
        }

        return estimate(k);
    }

    private static double estimate(Kappa k) {
        Double ret = cache.get(k);
        if (ret != null) return ret;

        if (k.isElement())
            return example.containsLiteral(k) ? 1.0 : 0.0;

        double est = k.getOffset();
        for (KappaRule kr: k.getRules())
            est += estimate(kr.getBody().getParent()) * kr.getWeight();

//        est = Activations.kappaActivation(est);
        cache.put(k, est);
        return est;
    }

    private static double estimate(Lambda l) {
        Double ret = cache.get(l);
        if (ret != null) return ret;

        double est = l.getOffset();
        for (SubK sk: l.getRule().getBody())
            est += estimate(sk.getParent());

//        est = Activations.lambdaActivation(est);
        cache.put(l, est);
        return est;
    }
}
