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
    
    
    /**
     * Calculates a sample mean from a sample of <tt>ImpactChain</tt>s.
     * Sample mean is the sum of relative impacts of the impact chains
     * in the sample divided by the sample size.
     * in a <tt>List</tt>.
     * @param sample <tt>List</tt> of <tt>ImpactChain</tt>s.
     * @return double: sample mean
     */
    public static double sampleMean(List<ImpactChain> sample) {
        double sum = 0;
        for (ImpactChain i : sample) {
            sum += i.impact();
        }
        return sum / sample.size();
    }
    
    
    /**
     * Computes the summed impact of all possible impact chains 
     * starting from variable with index <b>impactorIndex</b>
     * and ending to variable with index <b>impactedIndex</b>
     * of length <b>length</b>.<br/>
     * 
     * This method can be used in the stratified sampling based strategy of
     * computing the total impact: 
     * for sets of short chains, it is faster to sum all possible chains 
     * of that length
     * than to accurately sample with replacement.
     * 
     * @param impactorIndex
     * @param impactedIndex
     * @param length
     * @return 
     */
    protected double calculateImpactOfAll(int impactorIndex, int impactedIndex, int length) {
        List<Integer> usedIndices = new ArrayList<>();
        usedIndices.add(impactorIndex);
        usedIndices.add(impactedIndex);
        return calculateImpactOfAll(impactorIndex, impactedIndex, length, usedIndices);
    }
    
    private double calculateImpactOfAll(int impactorIndex, int impactedIndex, int length, List<Integer> usedIndices) {
        
        assert usedIndices != null;
        
        List<Integer> available = availableIndices(usedIndices);
        if( available.isEmpty() || (usedIndices.size() >= length) ) {
            return new ImpactChain(matrix, usedIndices).impact();
        } else {
            double impactSum = 0;
            for(Integer i : available) {
                List<Integer> used = new ArrayList<>(usedIndices);
                used.add(used.size()-1, i);
                impactSum += calculateImpactOfAll(impactorIndex, impactedIndex, length, used);
            }
            return impactSum;
        }
    }
    
    
    
    /**
     * Returns a list of integers that are the indices of variables in an impact chain
     * that starts with <b>impactorIndex</b>, ends with <b>impactedIndex</b>, 
     * the intermediary indices (indices between impactor and impacted) are picked
     * randomly without replacement from the variables of the <b>matrix</b>.
     * @param impactorIndex Index of impactor variable of the chain
     * @param impactedIndex Index of impacted variable of the chain
     * @param totalLength Total length of the chain
     * @return List of integers representing impact chain variable indices
     */
    protected List<Integer> randomChainIndices(int impactorIndex, int impactedIndex, int totalLength) {
        assert indexIsValid(impactorIndex) && indexIsValid(impactedIndex);
        assert totalLength > 1 && totalLength <= matrix.varCount;
        List<Integer> indices = new ArrayList<>();
        List<Integer> available = intermediaryIndices(impactorIndex, impactedIndex);
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
    protected List<Integer> intermediaryIndices(int impactorIndex, int impactedIndex) {
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
     * Returns the variable indices that are available in <b>matrix</b>
     * but are not present in <b>usedIndices</b>.
     * @param usedIndices A list of used variable indices
     * @return List of integers representing variable indices
     */
    protected List<Integer> availableIndices(List<Integer> usedIndices) {
        List<Integer> available = new ArrayList<>();
        if(usedIndices == null) usedIndices = new ArrayList<>();
        for(int i=1;i<=matrix.varCount;i++) {if(!usedIndices.contains(i)) available.add(i);}
        return available;
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
