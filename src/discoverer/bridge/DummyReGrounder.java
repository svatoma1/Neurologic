package discoverer.bridge;

import discoverer.LiftedDataset;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.List;

/**
 * Created by EL on 19.4.2016.
 */
public class DummyReGrounder implements ReGrounder {

    @Override
    public LiftedDataset reGroundMe(LiftedDataset previousRound, List<String> templateExtension) {
        throw new NotImplementedException();
    }
}
