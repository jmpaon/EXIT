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
public class EXITresult {
    public final EXITinput input;
    public final CrossImpactMatrix resultMatrix;

    public EXITresult(EXITinput input, CrossImpactMatrix resultMatrix) {
        this.input = input;
        this.resultMatrix = resultMatrix;
    }
    
    
    
}
