package discoverer.structureLearning.tools;

import java.util.List;

/**
 * Created by EL on 20.4.2016.
 */
public class Tools {

    public static boolean hasConverged(List<Double> errors, Integer longTimeWindow, Integer shortTimeWindow, Double epsilonDifference) {
        if (longTimeWindow > errors.size()){
            return false;
        }
        return Math.abs(average(errors,longTimeWindow) - average(errors,shortTimeWindow)) < epsilonDifference;
    }

    public static double average(List<Double> list, Integer timeWindow) {
        if (timeWindow > list.size()){
            timeWindow = list.size();
        }
        return list.subList(list.size() - timeWindow,list.size()).stream().mapToDouble(d -> d).average().orElse(0);
    }

}
