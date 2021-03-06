/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package discoverer.grounding.network.groundNetwork;

import discoverer.construction.template.KL;
import discoverer.global.Global;
import java.io.Serializable;

/**
 *
 * @author Gusta
 */
public class GroundNeuron implements Serializable {

    public String name;
    public Global.activationSet activation;

    public boolean dropMe = false;
    public double outputValue;
    public double sumedInputs;
    //
    public int groundParentsCount;
    public int groundParentsChecked;
    public double groundParentDerivativeAccumulated;

    void invalidateValue() {
        outputValue = 0.00000000;
        groundParentsChecked = 0;
        groundParentDerivativeAccumulated = 0;
    }
    
    @Override
    public String toString() {
        return name;
    }
    
}
