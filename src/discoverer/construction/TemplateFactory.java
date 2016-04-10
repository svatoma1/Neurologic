package discoverer.construction;

import discoverer.construction.network.rules.LambdaRule;
import discoverer.construction.network.rules.KappaRule;
import discoverer.construction.network.rules.SubL;
import discoverer.construction.network.rules.SubK;
import discoverer.construction.Variable;
import discoverer.construction.ConstantFactory;
import discoverer.construction.Parser;
import discoverer.construction.template.KL;
import discoverer.construction.template.Kappa;
import discoverer.construction.template.KappaFactory;
import discoverer.construction.template.Lambda;
import discoverer.construction.template.LambdaFactory;
import discoverer.construction.template.LiftedTemplate;
import discoverer.construction.template.MolecularTemplate;
import discoverer.global.Global;
import java.util.ArrayList;
import java.util.List;

/**
 * Factory for whole network
 */
public class TemplateFactory {

    private KappaFactory kFactory = new KappaFactory();
    private LambdaFactory lFactory = new LambdaFactory();
    private VariableFactory vFactory = new VariableFactory();

    private List<KappaRule> kappaRules = new ArrayList<KappaRule>();

    public TemplateFactory() {
        ConstantFactory.clearConstantFactory();
    }

    public List<KappaRule> getKappaRules() {
        return kappaRules;
    }

    /**
     * creates network from rules, returns last row KL node as output
     * (kappa-lambda superclass)
     *
     * @param rules
     * @return
     */
    public LiftedTemplate construct(String[] rules) {
        KL kl = null;

        if (Global.isCheckback()) {
            for (int i = 0; i < 42; i++) {
                Global.getRandomDouble();   //to synchronize initizalization of new template with lambda elements
            }
        }

        for (int x = 0; x < rules.length; x++) {
            String[][] tokens = Parser.parseRule(rules[x]);

            boolean isLambdaLine = tokens[0][0].isEmpty();

            //the self handling of each K/L rule, adding it to the base
            kl = isLambdaLine ? handleLambdaLine(tokens, rules[x]) : handleKappaLine(tokens, rules[x]);

            vFactory.clear();
        }

        kl.dropout = -1;    //do never drop the last element of the network!! (otherwise there is no network :))

        if (Global.isKappaAdaptiveOffset()) {
            for (Kappa kappa : kFactory.getKappas()) {
                kappa.initOffset();
            }
        }
        //setup network
        LiftedTemplate network;
        if (Global.molecularTemplates) {
            network = new MolecularTemplate(kl);  //a wrapper for the last KL literal
        } else {
            network = new LiftedTemplate(kl);
        }
        return network;
    }

    private Variable constructTerm(String s) {
        boolean isVariable = s.matches("^[A-Z].*");

        return isVariable ? vFactory.construct(s) : ConstantFactory.construct(s);
    }

    /**
     * takes a lambda row token string (conjunction with no weights) and
     * <p>
     * adds lambda node to lambda-Factory
     * <p>
     * creates grounded Lambda node sl
     * <p>
     * adds every head variable as Terminal to variable-Factory
     * <p>
     * creates LambdaRule with sl as head<p>
     * <p>
     * creates Kappa node and its grounding for every literal in body<p>
     * for each variable in the literal adds corresponding Terminal's as
     * variable's to the Kappa grounding<p>
     * <p>
     * adds every body literal with all its variables to the rule sets current
     * rule to this Lambda (head) node (has just this one)
     *
     * @param tokens
     * @return
     */
    private Lambda handleLambdaLine(String[][] tokens, String original) {
        Lambda l = lFactory.construct(tokens[1][0]);
        SubL sl = new SubL(l, true);
        for (int i = 1; i < tokens[1].length; i++) {
            Variable v = vFactory.construct(tokens[1][i]);
            sl.addVariable(v);
        }
        LambdaRule lr = new LambdaRule();
        lr.addHead(sl);

        for (int i = 2; i < tokens.length; i++) {
            Kappa k = kFactory.construct(tokens[i][0]);
            SubK sk = new SubK(k, false);
            for (int j = 1; j < tokens[i].length; j++) {
                Variable t = constructTerm(tokens[i][j]);
                sk.addVariable(t);
            }
            lr.addBodyEl(sk);
        }

        l.setRule(lr);
        lr.originalName = original;
        return l;
    }

    /**
     * takes a Kappa row token string (disjunction with weights) and
     * <p>
     * adds Kappa node to kappa-Factory
     * <p>
     * creates grounded Kappa node sk
     * <p>
     * adds every head variable as Terminal to variable-Factory
     * <p>
     * creates KappaRule with sk as head<p>
     * <p>
     * adds KappaRule to list kappaRules
     * <p>
     * creates Lambda node and its grounding for every literal in body<p>
     * for each variable in the literal adds corresponding Terminal's as
     * variable's to the Lambda grounding<p>
     * <p>
     * sets the last body literal (there should be only one for Kappa rule line)
     * with its variables to the rule adds current rule to this Kappa (head)
     * node (may have more)
     *
     * @param tokens
     * @return
     */
    private Kappa handleKappaLine(String[][] tokens, String original) {
        Double w = Double.parseDouble(tokens[0][0]);
        Kappa k = kFactory.construct(tokens[1][0]);
        SubK sk = new SubK(k, true);
        for (int i = 1; i < tokens[1].length; i++) {
            Variable v = vFactory.construct(tokens[1][i]);
            sk.addVariable(v);
        }
        KappaRule kr = new KappaRule(w);
        kr.setHead(sk);
        kappaRules.add(kr);

        for (int i = 2; i < tokens.length; i++) {
            Lambda l = lFactory.construct(tokens[i][0]);
            SubL sl = new SubL(l, false);
            for (int j = 1; j < tokens[i].length; j++) {
                Variable t = constructTerm(tokens[i][j]);
                sl.addVariable(t);
            }
            kr.setBody(sl);
        }

        k.addRule(kr);
        kr.originalName = original;
        return k;
    }

    public void printWeights() {
        System.out.println("-----------------offsets-------------");
        int i = 0;
        for (Kappa kappa : kFactory.getKappas()) {
            System.out.println(i++ + " -> " + kappa.offset);
        }
        System.out.println("----------------ruleweights--------------");
        i = 0;
        for (KappaRule kappaRule : kappaRules) {
            System.out.println(i++ + " -> " + kappaRule.getWeight());
        }

    }

    /**
     * mergeElements saved network with a new one - replace some with pretrained
     * weights
     *
     * @param network
     * @param savedNet
     * @return
     */
    public KL merge(KL network, KL savedNet) {
        return null;
    }
}