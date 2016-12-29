/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package exit;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;

/**
 *
 * @author juha
 */
public class ImpactChainSampler extends Sampler {

    /**
     * 
     * @param matrix 
     */
    public ImpactChainSampler(EXITImpactMatrix matrix) {
        super(matrix);
    }
    
    /**
     * WORKS
     * @param sampleSize
     * @return 
     */
    @Override
    public CrossImpactMatrix estimateSummedImpacts(int sampleSize) {
        CrossImpactMatrix im = new CrossImpactMatrix(matrix.copy().flush());
        for(int impactor=1;impactor<=matrix.varCount;impactor++) {
            for(int impacted=1;impacted<=matrix.varCount;impacted++) {
                if (impactor != impacted) {
                    im.setValue(impactor, impacted, estimateSummedImpact(impactor, impacted, sampleSize));
                }
            }
        }
        return im;
    }
    

    public double estimateSummedImpact(int impactor, int impacted, int sampleSize) {
        double summedImpact=0;
        for(int length=2;length<=matrix.varCount;length++) {
            summedImpact += estimateSummedImpact(impactor, impacted, length, sampleSize);
        }
        return summedImpact;
    }
    
    /**
     * Estimates the average relative impact of chains of total length <b>chainLength</b>
     * where the index of impactor variable is <b>impactor</b> and
     * index of impacted variable is <b>impacted</b>
     * based on a sample of size <b>sampleSize</b>.
     * @param impactor Index of impactor variable 
     * @param impacted Index of impacted variable
     * @param chainLength Total length of chains sampled
     * @param sampleSize Size of the sample
     * @return double: estimated impact 
     */
    public double estimateSummedImpact(int impactor, int impacted, int chainLength, int sampleSize) {
        
        assert chainLength > 1 && chainLength <= matrix.varCount;
        
        // 
        double sampledMean = sampleMean(drawSample(impactor, impacted, chainLength, sampleSize));
        
        // Get the count of possible intermediary chains between impactor and impacted
        double chainCount = EXITImpactMatrix.chainCount_intermediary(matrix.varCount, chainLength-2);
        return sampledMean * chainCount;
    }
    
    
    /**
     * Calculates a sample mean from a sample of <tt>ImpactChain<tt>s.
     * Sample mean is the sum of relative impacts of the impact chains 
     * in the sample divided by the sample size.
     * in a <tt>List</tt>.
     * @param sample <tt>List</tt> of <tt>ImpactChain</tt>s. 
     * @return double: sample mean
     */
    double sampleMean(List<ImpactChain> sample) {
        double sum=0;
        for(ImpactChain i : sample) {
            sum += i.impact();
        }
        return sum / sample.size();
    }
    
    
    /**
     * Draws a sample of size <b>count</b> of impact chains 
     * with defined <b>impactorIndex</b> and <b>impactedIndex</b> 
     * and total length (including impactor and impacted variables) equal to <b>length</b>.
     * @param impactorIndex Impactor index of the chains to be sampled
     * @param impactedIndex Impacted index of the chains to be sampled
     * @param length Total length of chains in the sample
     * @param count Number of chains in the sample
     * @return List&lt;ImpactChain&gt;
     */
    List<ImpactChain> drawSample(int impactorIndex, int impactedIndex, int length, int count) {
        List<ImpactChain> sample = new LinkedList<ImpactChain>();
        while(count-- > 0) {
            sample.add(randomChain(impactorIndex, impactedIndex, length));
        }
        return sample;
    }
    
    /**
     * Returns an impact chain where 
     * variable with index <b>impactorIndex</b> is the impactor,
     * variable with index <b>impactedIndex</b> is the impacted,
     * and length is <b>length</b>.
     * The number of randomly picked variables in the chain 
     * will therefore be <u><b>length</b>-2</u>.
     * @param impactorIndex Index of impactor variable of the returned chain
     * @param impactedIndex Index of impacted variable of the returned chain 
     * @param length Total length of the returned chain
     * @return Impact chain with randomly picked <u>intermediary</u> variables and defined <u>impactor</u> and <u>impacted</u> variables.
     */
    ImpactChain randomChain(int impactorIndex, int impactedIndex, int length) {
        assert indexIsValid(impactorIndex);
        assert indexIsValid(impactedIndex);
        assert length > 1 : "Chain length > 1 required; length is " + length;
        assert length <= matrix.varCount: "length is " + length;
        
        length -= 2;
        List<Integer> chainMembers = new ArrayList<>();
        List<Integer> l = availableIndices(impactorIndex, impactedIndex);
        int i=0;
        Collections.shuffle(l);
        chainMembers.add(impactorIndex);
        while(length-- > 0) {chainMembers.add(l.get(i++));}
        chainMembers.add(impactedIndex);
        return new ImpactChain(matrix, chainMembers);
    }
    
    
}
