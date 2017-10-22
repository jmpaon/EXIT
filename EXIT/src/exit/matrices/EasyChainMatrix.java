/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package exit.matrices;

/**
 *
 * @author juha
 */
public class EasyChainMatrix extends CrossImpactMatrix {
    
    public EasyChainMatrix(int varCount, String[] names, double[] values, boolean onlyIntegers) {
        super(varCount, names, values, onlyIntegers);
    }
    
    public EasyChainMatrix(CrossImpactMatrix cim) {
        super(cim);
    }
    
    
    
    
    
}
