/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package exit.procedures;

import exit.matrices.CrossImpactMatrix;

/**
 *
 * @author jmpaon
 */
public class StratifiedSamplingResult extends EXITresult {
    
    public final CrossImpactMatrix resultMatrix;
    
    public StratifiedSamplingResult(CrossImpactMatrix resultMatrix) {
        super();
        this.resultMatrix = resultMatrix;
    }

    @Override
    public void print() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
