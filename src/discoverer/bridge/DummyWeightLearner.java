package discoverer.bridge;

import discoverer.LiftedDataset;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * Created by EL on 19.4.2016.
 */
public class DummyWeightLearner implements WeightLearner {
    @Override
    public LiftedDataset backprop(LiftedDataset groundSamples, double[] weights, String backPropType) {
        throw new NotImplementedException();
    }
}
