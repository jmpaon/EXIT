/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package exit.procedures;

import exit.matrices.CrossImpactMatrix;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author jmpaon
 */
public abstract class EXITresult {
    public final EXITinput input;
    public final CrossImpactMatrix resultMatrix;
    public final List<String> printables;

    public EXITresult(EXITinput input, CrossImpactMatrix resultMatrix) {
        this.input = input;
        this.resultMatrix = resultMatrix;
        this.printables = new LinkedList<>();
    }
    
    public abstract void print();
        
    
    
    
    
}
