package discoverer.bridge;

import discoverer.LiftedDataset;

/**
 * Created by EL on 19.4.2016.
 */
public interface WeightLearner {

    public LiftedDataset backprop(LiftedDataset groundSamples, double[] weights, String backPropType);

}
