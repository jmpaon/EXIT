/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package exit.matrices;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;


/**
 * <code>MicmacMatrix</code> class provides an implementation of 
 * Michel Godet's MICMAC analysis.
 * In MICMAC, the cross-impact matrix variables are ordered by their
 * influence or dependency, calculated as the row or column sums.
 * The variable with the highest sum becomes the first variable 
 * in the ordering.
 * The direct impact matrix gives an initial ordering of the variables;
 * the matrix is then squared iteratively, with the stopping condition
 * that if the ordering of the variables does not change after squaring the matrix,
 * iteration can be stopped and the final ordering is the MICMAC classification.
 * 
 * The implementation of MICMAC is provided to enable comparisons with
 * MICMAC and EXIT methods.
 * 
 * @author jmpaon
 */
public class MicmacMatrix extends CrossImpactMatrix {
    

    
    public MicmacMatrix(SquareMatrix matrix) {
        super(matrix.varCount, matrix.names.clone(), matrix.values.clone(), matrix.allValuesAreIntegers());
    }
    
    public MicmacMatrix(int varCount, String[] names, double[] values, boolean onlyIntegers) {
        super(varCount, names, values, onlyIntegers);
    }
    
    public MicmacMatrix(int varCount, String[] names, boolean onlyIntegers) {
        super(varCount, names, onlyIntegers);
    }
    
    
    /**
     * Returns the MICMAC ordering
     * @param orientation MICMAC ordering by influence or dependence?
     * @return Ordering
     */
    public Ordering MICMACordering(Orientation orientation) {
        return MICMACiteration(orientation).getOrdering(orientation);
    }
    
    

    
    
    /**
     * Squares the MICMAC matrix iteratively 
     * and compares the ordering of the power matrix to the previous matrix
     * until the ordering of variables no longer changes.
     * This stable ordering is the MICMAC ordering.
     * In MICMAC analysis the original ordering of the variables 
     * is compared to the MICMAC ordering, to show how the
     * ranking of variables by influence or dependency changes 
     * as indirect or higher-order interactions are taken into account.
     * @param orientation Are variables ranked by <b>influence</b> or <b>dependence</b>?
     * @return The iteratively squared matrix with the MICMAC-stable variable ordering
     */
    public MicmacMatrix MICMACiteration(Orientation orientation) {
        
        Ordering currentOrdering = new Ordering(this, orientation);
        Ordering powerOrdering = new Ordering(new MicmacMatrix(this.power()), orientation);
        
        MicmacMatrix powerMatrix = new MicmacMatrix(this);
        
        /**
         * Square the matrix and derive a new ordering from that 
         * as long as the ordering changes.
         * Iteration is stopped when the ordering isn't different from
         * the ordering of the previous power matrix.
         */
        
            
        while(!currentOrdering.equals(powerOrdering)) {
            System.out.println("Current ordering " + currentOrdering.toString());
            System.out.println("Power ordering " + powerOrdering.toString());
            System.out.println(currentOrdering.equals(powerOrdering));
            
            currentOrdering = powerMatrix.getOrdering(orientation);
            powerMatrix     = new MicmacMatrix(powerMatrix.power());            
            powerOrdering   = powerMatrix.getOrdering(orientation);
            
            System.out.println(powerMatrix);
            
        }
        
        System.out.println("MEM");
        System.out.println("Current ordering " + currentOrdering.toString());
        System.out.println("Power ordering " + powerOrdering.toString());        
        System.out.println("cOrd eq pOrd: " + currentOrdering.equals(powerOrdering));
        
        
        return powerMatrix;
    }

    

    
    
}
