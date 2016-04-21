package discoverer.bridge;

import discoverer.LiftedDataset;
import discoverer.Main;
import discoverer.global.FileToStringList;
import discoverer.global.Global;
import discoverer.global.Glogger;
import discoverer.global.Settings;
import org.apache.commons.cli.CommandLine;

/**
 * Created by EL on 19.4.2016.
 */
public class DummyInitable implements Initable {

    @Override
    public LiftedDataset init(String[] args) {
        CommandLine cmd = Main.parseArguments(args);
        if (cmd == null) {
            throw new IllegalStateException("No input arguments.");
        }

        Main.setParameters(cmd);

        //---------------------loading all input files
        //get examples from file
        String dataset = cmd.getOptionValue("e");
        Settings.setDataset(dataset);
        String[] exs = null;
        if (Global.multiLine) {
            exs = FileToStringList.convertMultiline(dataset, Main.maxReadline);
        } else {
            exs = FileToStringList.convert(dataset, Main.maxReadline);
        }

        if (exs.length == 0) {
            Glogger.err("no examples");
            throw new IllegalStateException("No examples");
        }

        //separate test set?
        String[] test = null;
        String tt = cmd.getOptionValue("test");
        if (tt != null) {
            Settings.setTestSet(tt);
            test = FileToStringList.convert(tt, Main.maxReadline);
        }

        //get rules one by one from a file
        String rls = cmd.getOptionValue("r");
        Settings.setRules(rls);
        String[] rules = FileToStringList.convert(rls, Main.maxReadline);
        if (rules.length == 0) {
            Glogger.err("no rules");
        }

        //we want sigmoid at the output, not identity (for proper error measurement)

        //System.out.println("commented right now: if (Global.getKappaActivation() == Global.activationSet.id) {...}");
        if (Global.getKappaActivation() == Global.activationSet.id) {
            if (rules[rules.length - 1].startsWith("0.") || rules[rules.length - 1].startsWith("1.")) {  //does it end with Kappa line?
                rules = Main.addFinalLambda(rules);  //a hack to end with lambda
            }
        }

        //pretrained template with some lifted literals in common (will be mapped onto new template)
        String pretrained = cmd.getOptionValue("t");
        Settings.setPretrained(pretrained);
        String[] pretrainedRules = FileToStringList.convert(pretrained, Main.maxReadline);
        if (pretrainedRules != null) {
            Glogger.out("pretrained= " + pretrained + " of length: " + pretrainedRules.length);
        }
        LiftedDataset groundedDataset = Main.createDataset(test, exs, rules, pretrainedRules);
        return groundedDataset;
    }
}
