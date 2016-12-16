/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package exit;

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
    
    
    public Ordering MICMACordering(Orientation orientation) {
        throw new UnsupportedOperationException();
    }
    
    
    
    
    
    /**
     * Returns a info table about the MICMAC rankings of the variables.
     * @param orientation [byInfluence|byDependence]
     * @return <code>VarInfoTable</code> containing initial and MICMAC rankings of the matrix variables
     * @see MicmacMatrix#altMICMAC(exit.MicmacMatrix.Orientation) alternative implementation
     */
    @Deprecated
    public VarInfoTable<String> MICMACranking(CrossImpactMatrix.Orientation orientation) {
        
        Ordering initialOrdering = new Ordering(this, orientation);
        
        /* Place initial ordering to the iterated ordering variable */
        Ordering iterOrdering = new Ordering(this, orientation);
        
        /* Place the first power matrix in the iterated matrix variable */
        MicmacMatrix iterMatrix = this.power();
        
        int iter = 0;
        
        /* 
        System.out.println("MICMAC calculation, " + orientation.toString());
        System.out.println("MICMAC iteration 0 (Initial matrix)");
        System.out.println(this);
        System.out.println(this.getOrdering(orientation));
        */
        
        while(true) {
            
            /*
            System.out.println("MICMAC iteration " + ++iter);
            System.out.println(iterMatrix);
            System.out.println(iterMatrix.getOrdering(orientation));
            */
            
            /* Stop MICMAC iteration if the previous ordering is equal to current power matrix' ordering:
            This means that the ordering has become stable and the MICMAC end condition has been reached. */
            if(iterOrdering.equals(iterMatrix.getOrdering(orientation))) break;
            
            /* Save this iteration's ordering */
            iterOrdering = new Ordering(iterMatrix, orientation);
            
            /* Iterate to the next power matrix */
            iterMatrix = iterMatrix.power();
            
            
        }
        
        VarInfoTable<String> rankings = new VarInfoTable<>(String.format("Initial ranking and MICMAC ranking of variables %s", orientation.toString()), Arrays.asList("Initial", "MICMAC"));
        
        Collections.sort(initialOrdering.ordering, initialOrdering.getVariableIndexComparator());
        Collections.sort(iterOrdering.ordering, iterOrdering.getVariableIndexComparator());
        
        for(int i = 0; i < initialOrdering.ordering.size(); i++) {
            String varName = this.getNamePrint(i+1);
            String initPos   = String.valueOf(initialOrdering.ordering.get(i).position);
            String micmacPos = String.valueOf(iterOrdering.ordering.get(i).position);
            rankings.put(varName, Arrays.asList(initPos, micmacPos));
        }
        
        return rankings;
        
    }
    
    /**
     * Multiplies the matrix by itself (resulting in power matrix).
     * This functionality is provided to implement the MICMAC method
     * inside EXIT for comparisons.
     * @return Squared matrix
     */
    public MicmacMatrix power() {
        MicmacMatrix powerMatrix = new MicmacMatrix(this);
        for (int row = 1; row <= varCount; row++) {
            for (int col = 1; col <= varCount; col++) {
                powerMatrix.setValue(row, col, this.multiplyEntries(row, col));
            }
        }
        return powerMatrix;
    }
    
    
    /**
     * 
     * @param orientation
     * @return 
     */
    public MicmacMatrix iteratedPowerMatrix(Orientation orientation) {
        Ordering initialOrdering = new Ordering(this, orientation);
        Ordering powerOrdering = new Ordering(this.power(), orientation);
        
        MicmacMatrix powerMatrix = new MicmacMatrix(this);
        
        /**
         * Square the matrix and derive a new ordering from that 
         * as long as the ordering changes.
         * Iteration is stopped when the ordering isn't different from
         * the ordering of the previous power matrix.
         */
        while(!initialOrdering.equals(powerOrdering)) {
            initialOrdering = powerMatrix.getOrdering(orientation);
            powerMatrix     = powerMatrix.power();            
            powerOrdering   = powerMatrix.getOrdering(orientation);
        }
        
        return powerMatrix;
    }
    
    
    
    /**
     * Returns the ranking of a variable in an ordering of the variables
     * in the matrix by row or column sum.
     * <b>orientation</b> determines whether row or column sum is used.
     * For example, if the variable has 2 variables that have a 
     * higher absolute row/column sum, the sumRanking for that variable will be
     * 3 (2+1). The variable with the highest row/column sum will have ranking 1.
     * @param varIndex Index of the variable for which the sumRanking is calculated
     * @param orientation <i>[byInfluence|byDependence]</i> 
     * if <i>byInfluence</i>, row sums are used;
     * if <i>byDependence</i>, column sums are used;
     * @return the ranking of the variable with index <b>varIndex</b>
     */
    public int sumRanking(int varIndex, Orientation orientation) {
        if(varIndex < 1 || varIndex > this.varCount) 
            throw new IndexOutOfBoundsException(String.format("varIndex %d is out of bounds (varCount %d)", varIndex, this.varCount));
        
        double sum = orientation == Orientation.byInfluence ? rowSum(varIndex, true) : columnSum(varIndex, true);
        int greaterCount=0;
        
        for(int i=1 ; i<=this.varCount ; i++) {
            double comparedSum = orientation == Orientation.byInfluence ? rowSum(i, true) : columnSum(i, true);
            if(comparedSum > sum) greaterCount++;
        }
        
        return greaterCount+1;
        
    }
    
    
    /**
     * Returns the initial ranking
     * and the MICMAC ranking
     * of the variables in this matrix
     * in a <code>VarInfoTable</code>.
     * Variable with rank 1 has the highest score
     * by influence/dependency, rank 2 has the second highest score etc.
     * @param orientation <i>[byInfluence|byDependence]</i> is the ranking based on row or column sums?
     * Summing absolute row values gives a score indicating influence in the cross-impact system,
     * summing absolute column values gives a score indicating dependency.
     * @return Initial and MICMAC rankings of the matrix variables
     * @see MicmacMatrix#MICMACranking(exit.MicmacMatrix.Orientation) Alternative implementation
     */
    public VarInfoTable altMICMAC(Orientation orientation) {
        AltOrdering ord = new AltOrdering(this, orientation);
        MicmacMatrix powerMatrix = this.power();
        
        /**
         * Square the matrix and derive a new ordering from that 
         * as long as the ordering changes.
         * Iteration is stopped when the ordering isn't different from
         * the ordering of the previous power matrix.
         */
        while(! ord.equals(powerMatrix.getAltOrdering(orientation))  ) {
            System.out.println(powerMatrix);
            System.out.println(ord);
            System.out.println(ord.isUnambiguous());
            
            ord = powerMatrix.getAltOrdering(orientation);
            powerMatrix = powerMatrix.power();
        }
        
        VarInfoTable<Integer> rankings = new VarInfoTable<>(
                String.format("Initial ranking and MICMAC ranking of variables %s (alternative method):", orientation), 
                Arrays.asList("Initial","MICMAC")
        );
        
        for(int i = 0; i < ord.positions.length; i++) {
            String varname = this.getNameShort(i+1);
            int micmacpos = ord.positions[i];
            int initpos = this.getAltOrdering(orientation).positions[i];
            
            rankings.put(varname, Arrays.asList(initpos, micmacpos));
            
        }
        return rankings;
    }

    
    
    /**
     * Sums the products of each entry in <b>row</b> and corresponding entry in <b>col</b>.
     * Used in {@link MicmacMatrix#power()} calculation.
     * @param row Index of row
     * @param col Index of col
     * @return Sum of pairwise products of entries in row <b>row</b> and column <b>col</b>.
     */
    private double multiplyEntries(int row, int col) {
        double result=0;
        for (int i = 1; i <= varCount; i++) {
            result += getValue(row, i) * getValue(i, col);
        }
        return result;
    }
    
    
}
