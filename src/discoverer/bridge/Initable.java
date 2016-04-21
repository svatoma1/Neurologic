package discoverer.bridge;

import discoverer.LiftedDataset;

/**
 * Created by EL on 19.4.2016.
 */
public interface Initable {

    public LiftedDataset init(String[] args);
}
