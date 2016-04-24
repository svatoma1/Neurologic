package discoverer.structureLearning;

import discoverer.structureLearning.algorithms.liftedCascadeCorrelation.LiftedCascadeCorrelation;
import discoverer.structureLearning.algorithms.liftedDynamicNodeCreation.LiftedDynamicNodeCreation;
import discoverer.structureLearning.algorithms.liftedTopGen.LiftedTopGen;
import org.apache.commons.cli.CommandLine;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * Created by EL on 19.4.2016.
 */
public class StructureLearningFactory {
    public static StructureLearnable create(String[] exs, String[] test, String[] rules, String[] pretrainedRules, CommandLine cmd, String[] args) {
        String alg = cmd.getOptionValue("sla");

        switch (alg) {
            case "DNC":
                return new LiftedDynamicNodeCreation(exs, test, rules, pretrainedRules, cmd, args);
            case "CasCor":
                return new LiftedCascadeCorrelation(exs, test, rules, pretrainedRules, cmd, args);
            case "SLF":
                throw new NotImplementedException();
            case "TopGen":
                return new LiftedTopGen(exs, test, rules, pretrainedRules, cmd, args);
            case "REGENT":
                throw new NotImplementedException();
            default:
                throw new IllegalStateException("I do not know option '" + alg + "'.");
        }
    }
}
