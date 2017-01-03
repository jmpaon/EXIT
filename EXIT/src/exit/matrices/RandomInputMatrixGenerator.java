/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package exit.matrices;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * This class provides cross-impact matrices with random values as contents
 * for testing purposes.
 * @author juha
 */
public class RandomInputMatrixGenerator {
    
    /**
     * Returns a new <tt>CrossImpactMatrix</tt> filled with random values.
     * @param varCount The number of variables in the returned matrix
     * @param impactProbability The probability of a matrix entry to have an impact; range [0..1]
     * @param greatestImpact Greatest possible absolute impact that the random values can have
     * @return <tt>CrossImpactMatrix</tt> with random contents
     */
    public static CrossImpactMatrix generateCrossImpactMatrix(int varCount, double impactProbability, double greatestImpact) {
        assert varCount > 1;
        assert impactProbability >= 0 && impactProbability <= 1;
        assert greatestImpact > 0;
        CrossImpactMatrix matrix = new CrossImpactMatrix(varCount);
        
        for(int impactor=1;impactor<=matrix.varCount;impactor++)
            for(int impacted=1;impacted<=matrix.varCount;impacted++) {
                if(impactor != impacted && Math.random() >= impactProbability) {
                    double newValue = Math.random() * greatestImpact;
                    newValue = Math.random() > 0.5? newValue : -newValue;
                    matrix.setValue(impactor, impacted, newValue);
                }
            }
        
        return matrix;
    }

    /**
     * Returns a new <tt>CrossImpactMatrix</tt> where the matrix entry values 
     * are cycled: the first entry of the matrix that can have an impact value 
     * will have the first value of <b>cycledValues</b>, the second such entry 
     * will have the second value of <b>cycledValues</b> and so on.
     * This method serves the purpose of providing a test matrix with known (non-random)
     * contents.
     * @param varCount Number of variables in the returned matrix
     * @param cycledValues A set of values that are cycled as values are input into the matrix
     * @return <tt>CrossImpactMatrix</tt> for test purposes
     */
    public static CrossImpactMatrix generateCrossImpactMatrix(int varCount, Double... cycledValues) {
        CrossImpactMatrix matrix = new CrossImpactMatrix(varCount);
        List<Double> l = Arrays.asList(cycledValues);
        Iterator<Double> it = l.iterator();
        for(int row=1;row<=varCount;row++)
            for(int col=1;col<=varCount;col++) {
                if(row != col) {
                    if(!it.hasNext()) it = l.iterator();
                    matrix.setValue(row, col, it.next());
                }
            }
        return matrix;
    }
    
    
    /**
     * Returns a new <tt>EXITImpactMatrix</tt> with random content for testing purposes.
     * @param varCount Number of variables in the matrix
     * @param impactProbability The probability of a matrix entry to have an impact; range [0..1]
     * @param greatestImpact Greatest possible absolute impact that the random values can have
     * @param maxImpact The maximum allowed impact value of EXIT impact matrix
     * @return 
     */
    public static EXITImpactMatrix generateEXITImpactMatrix(int varCount, double impactProbability, double greatestImpact, double maxImpact) {
        assert greatestImpact <= maxImpact;
        return new EXITImpactMatrix(generateCrossImpactMatrix(varCount, impactProbability, greatestImpact), maxImpact);
    }
    
    
}
