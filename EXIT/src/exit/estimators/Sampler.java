/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package exit.estimators;

import exit.matrices.CrossImpactMatrix;
import exit.matrices.EXITImpactMatrix;
import exit.matrices.ImpactChain;
import java.io.PrintStream;
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
    
    
    /**
     * The <tt>EXITImpactMatrix</tt> this sampler is linked to and 
     * whose summed impact matrix is returned by the 
     * <tt>estimateSummedImpactMatrix</tt> method.
     */
    public final EXITImpactMatrix matrix;
    public final PrintStream reportingStream;
    
    
    /**
     * Constructor
     * @param matrix <tt>EXITImpactMatrix</tt>
     */
    public Sampler(EXITImpactMatrix matrix) {
        this(matrix, null);
    }
    
    
    public Sampler(EXITImpactMatrix matrix, PrintStream reportingStream) {
        assert matrix != null;
        this.matrix = matrix;
        this.reportingStream = reportingStream;        
    }

    /**
     * Prints <b>text</b> to a new line in <b>reportingStream</b>.
     * @param text 
     */
    protected void report(String text) {
        assert text != null;
        if(reportingStream == null) return;
        reportingStream.println(text);
    }
    
    protected void reportf(String text, Object... objs) {
        assert text != null;
        if(reportingStream == null) return;
        reportingStream.printf(text, objs);
    }
    
    
    /**
     * Returns a summed impact matrix 
     * describing the sum of direct and indirect impacts
     * in the cross-impact system the sampler is attached to.
     * @param sampleSize
     * @return CrossImpactMatrix : summed direct and indirect impact matrix
     */
    public abstract CrossImpactMatrix estimateSummedImpactMatrix(int sampleSize);
    
    
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
     * Computes a summed impact matrix from the direct impact matrix <b>matrix</b>
     * by computing the relative impact of all possible impact chains.
     * This process is very slow for big (9+ variables) cross-impact models 
     * and {@link Sampler#computeAll()} method 
     * should be used mainly for testing the different pruning or sampling based 
     * estimation strategies for the summed impacts.
     * @return CrossImpactMatrix : summed direct and indirect impacts in <b>matrix</b>
     */
    public CrossImpactMatrix computeAll() {
        
        CrossImpactMatrix summedImpactMatrix = new CrossImpactMatrix(matrix.copy().flush());
        
        report("Computing all summed impacts in input matrix");
        for(int impactor=1; impactor<=summedImpactMatrix.getVarCount(); impactor++) {
            for(int impacted=1;impacted<=summedImpactMatrix.getVarCount();impacted++) {
                for(int length=2;length<=summedImpactMatrix.getVarCount();length++) {
                    if(impactor != impacted) {
                        report(String.format("Computing impact of %s on %s", matrix.getNameShort(impactor), matrix.getNameShort(impacted)));
                        double currentValue = summedImpactMatrix.getValue(impactor, impacted);
                        double addedValue   = computeAll(impactor,impacted, length);
                        summedImpactMatrix.setValue(impactor, impacted, currentValue + addedValue );
                    }
                        
                }
            }
        }
        return summedImpactMatrix;
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
     * @param impactorIndex Index of impactor variable
     * @param impactedIndex Index of impacted variable
     * @param length Length of the chains that are computed 
     * @return double : summed relative impacts of chains meeting the criteria
     */
    protected double computeAll(int impactorIndex, int impactedIndex, int length) {
        List<Integer> usedIndices = new ArrayList<>();
        usedIndices.add(impactorIndex);
        usedIndices.add(impactedIndex);
        return computeAll(impactorIndex, impactedIndex, length, usedIndices);
    }
    
    
    /**
     * Returns the relative impact of chain if chain is of length <b>length</b> 
     * or recursively calls <tt>computeAll</tt> to expand chain by one of the 
     * available indices.
     * @param impactorIndex Index of impactor variable in the computed chains
     * @param impactedIndex Index of impacted variable in the computed chains
     * @param length Length of the computed chains
     * @param usedIndices Indices of variables in <b>matrix</b> that are present in the chain that is being built by the method
     * @return double : sum of relative impacts of meeting the criteria
     */
    private double computeAll(int impactorIndex, int impactedIndex, int length, List<Integer> usedIndices) {
        
        assert usedIndices != null;
        
        List<Integer> available = availableIndices(usedIndices);
        if( available.isEmpty() || (usedIndices.size() >= length) ) {
            return new ImpactChain(matrix, usedIndices).impact();
        } else {
            double impactSum = 0;
            for(Integer i : available) {
                List<Integer> used = new ArrayList<>(usedIndices);
                used.add(used.size()-1, i);
                impactSum += Sampler.this.computeAll(impactorIndex, impactedIndex, length, used);
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
    protected List<Integer> randomIndices(int impactorIndex, int impactedIndex, int totalLength) {
        assert matrix.isIndexValid(impactorIndex) && matrix.isIndexValid(impactedIndex);
        assert totalLength > 1 && totalLength <= matrix.getVarCount();
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
     * @return List&lt;Integer&gt; : possible intermediary indices between impactor and impacted 
     */
    protected List<Integer> intermediaryIndices(int impactorIndex, int impactedIndex) {
        assert matrix.isIndexValid(impactorIndex) && matrix.isIndexValid(impactedIndex);
        List<Integer> indices = new ArrayList<>();
        for(int i=1;i<=matrix.getVarCount();i++) {
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
        for(int i=1;i<=matrix.getVarCount();i++) {if(!usedIndices.contains(i)) available.add(i);}
        return available;
    }
    
    
}
