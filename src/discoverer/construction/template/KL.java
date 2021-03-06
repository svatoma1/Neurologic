package discoverer.construction.template;

import discoverer.construction.Element;
import discoverer.global.Global;
import java.io.Serializable;

/**
 * Kappa or Lambda node
 */
public abstract class KL extends Element implements Serializable {

    public double offset;
    public double dropout;
    public Global.activationSet activation;

    public KL() {
    }

    public KL(String name) {
        super(name);
        if (name.startsWith("final")) {
            dropout = -1;
        } else {
            dropout = Global.getDropout();
        }
    }
    /*
    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final KL other = (KL) obj;
        if (!this.name.equals(other.name)) {
            return false;
        }
        return true;
    }
     */
}
