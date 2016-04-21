package discoverer.bridge;

import discoverer.LiftedDataset;

import java.util.List;

/**
 * Created by EL on 19.4.2016.
 */
public interface ReGrounder {

    public LiftedDataset reGroundMe(LiftedDataset previousRound, List<String> templateExtension);

}
