/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package exit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A <tt>Sampler</tt> estimates the total (summed direct and indirect impacts)
 * in a <tt>CrossImpactMatrix</tt> based on sample drawn from the possible 
 * impact chains of the matrix.
 * @author juha
 */
public abstract class Sampler {
    
    public final EXITImpactMatrix matrix;
    
    public Sampler(EXITImpactMatrix matrix) {
        assert matrix != null;
        this.matrix = matrix;
    }

    
    /**
     * Returns a summed impact matrix 
     * describing the sum of direct and indirect impacts
     * in the cross-impact system the sampler is attached to.
     * @param sampleSize
     * @return 
     */
    public abstract CrossImpactMatrix estimateSummedImpacts(int sampleSize);
    
    public abstract double estimateSummedImpact(int impactorIndex, int impactedIndex, int sampleSize);
    
    public abstract double estimateSummedImpact(int impactorIndex, int impactedIndex, int chainLength, int sampleSize);
    
    
    
    protected List<Integer> randomIntermediaryChainIndices(int impactorIndex, int impactedIndex, int totalLength) {
        assert indexIsValid(impactorIndex) && indexIsValid(impactedIndex);
        assert totalLength > 1 && totalLength <= matrix.varCount;
        List<Integer> indices = new ArrayList<>();
        List<Integer> available = availableIndices(impactorIndex, impactedIndex);
        int i=0;
        Collections.shuffle(available);
        indices.add(impactorIndex);
        while(totalLength-- > 2) indices.add(available.get(i++));
        indices.add(impactedIndex);
        return indices;
    }
    
    
    /**
     * Returns a list of possible indices of intermediary variables for chain generation for <b>matrix</b>.
     * @param impactorIndex Index of impactor variable of the chain
     * @param impactedIndex Index of impacted variable of the chain
     * @return 
     */
    protected List<Integer> availableIndices(int impactorIndex, int impactedIndex) {
        assert indexIsValid(impactorIndex) && indexIsValid(impactedIndex);
        List<Integer> indices = new ArrayList<>();
        for(int i=1;i<=matrix.varCount;i++) {
            if(i != impactorIndex && i != impactedIndex) {
                indices.add(i);
            }
        }
        return indices;
    }    
    
    /**
     * Tests whether a variable index is a valid index in matrix <b>matrix</b>.
     * @param index Index to be tested 
     * @return true if the index is a valid index in <b>matrix</b>, false otherwise.
     */
    protected boolean indexIsValid(int index) {
        return index > 0 && index <= matrix.varCount;
    }
    
}
