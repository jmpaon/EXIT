/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package exit.samplers;

import exit.matrices.CrossImpactMatrix;
import exit.matrices.EXITImpactMatrix;
import exit.matrices.ImpactChain;
import exit.samplers.Sampler;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;

/**
 * Objects of this class estimate the summed direct and indirect impacts 
 * in a cross-impact matrix
 * on the basis of a stratified sample.
 * This implementation is based on using the <tt>ImpactChain</tt> class.
 * A more performant implementation of <tt>Sampler</tt> is the 
 * <tt>QuickSampler</tt> class.
 * @author juha
 */
public class ImpactChainSampler extends Sampler {

    /**
     * Constructor.
     * @param matrix <tt>EXITImpactMatrix</tt> on which the sampling to be performed on.
     */
    public ImpactChainSampler(EXITImpactMatrix matrix) {
        super(matrix);
    }
    
    
    /**
     * Returns a <tt>CrossImpactMatrix</tt> that contains the <u>estimated</u> summed 
     * direct and indirect impacts between the variables present in <b>matrix</b>. 
     * The summed impacts are estimated based on a stratified sample.
     * @param sampleSize Size of the drawn sample of chains of a specific length; 
     * for each variable pair, the average impact of chains of length x is based on 
     * a sample of size <b>sampleSize</b>.
     * @return CrossImpactMatrix : matrix that contains summed direct and indirect impacts.
     */
    @Override
    public CrossImpactMatrix estimateSummedImpactMatrix(int sampleSize) {
        CrossImpactMatrix im = new CrossImpactMatrix(matrix.copy().flush());
        for(int impactor=1;impactor<=matrix.getVarCount();impactor++) {
            for(int impacted=1;impacted<=matrix.getVarCount();impacted++) {
                if (impactor != impacted) {
                    im.setValue(impactor, impacted, estimateSummedImpact(impactor, impacted, sampleSize));
                }
            }
        }
        return im;
    }
    

    /**
     * Estimates 
     * @param impactor Index of impactor variable
     * @param impacted Index of impacted variable
     * @param sampleSize Size of sample that the estimation is based on
     * @return double : estimate of summed impact 
     */
    double estimateSummedImpact(int impactor, int impacted, int sampleSize) {
        double summedImpact=0;
        
        for(int length=2;length<=matrix.getVarCount();length++) {
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
    double estimateSummedImpact(int impactor, int impacted, int chainLength, int sampleSize) {
        
        assert chainLength > 1 && chainLength <= matrix.getVarCount();

        double sampledMean = sampleMean(drawSample(impactor, impacted, chainLength, sampleSize));
        
        // Get the count of possible intermediary chains between impactor and impacted
        double chainCount = EXITImpactMatrix.chainCount_intermediary(matrix.getVarCount(), chainLength-2);
        
        return sampledMean * chainCount;
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
        assert length <= matrix.getVarCount(): "length is " + length;
        
        length -= 2;
        List<Integer> chainMembers = new ArrayList<>();
        List<Integer> l = intermediaryIndices(impactorIndex, impactedIndex);
        int i=0;
        Collections.shuffle(l);
        chainMembers.add(impactorIndex);
        while(length-- > 0) {chainMembers.add(l.get(i++));}
        chainMembers.add(impactedIndex);
        return new ImpactChain(matrix, chainMembers);
    }
    
    
}
