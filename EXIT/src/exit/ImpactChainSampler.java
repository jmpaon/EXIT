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
public class ImpactChainSampler {
    
    public final EXITImpactMatrix matrix;
    
    
    public ImpactChainSampler(EXITImpactMatrix matrix) {
        this.matrix = matrix;
    }
    
    
    

    
    
    public CrossImpactMatrix testSampling(int sampleSize) {
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
    

    double estimateSummedImpact(int impactor, int impacted, int sampleSize) {
        double summedImpact=0;
        for(int length=2;length<=matrix.varCount-2;length++) {
            double sampledMean = sampleMean(sampleChains(impactor, impacted, length, sampleSize));
            double chainCount = EXITImpactMatrix.approximateChainCountBetweenTwo(matrix.varCount, length);
            summedImpact += sampledMean * chainCount;
        }
        return summedImpact;
    }
    
    double estimateImpactOfChains(int impactor, int impacted, int length, int sampleSize) {
        double sampledMean = sampleMean(sampleChains(impactor, impacted, length, sampleSize));
        double chainCount = EXITImpactMatrix.approximateChainCountBetweenTwo(matrix.varCount, length);
        return 999; // FIXME
    }
    
    
    double sampleMean(List<ImpactChain> sample) {
        double sum=0;
        for(ImpactChain i : sample) {
            sum += i.impact();
        }
        return sum / sample.size();
    }
    
    
    List<ImpactChain> sampleChains(int impactorIndex, int impactedIndex, int length, int count) {
        List<ImpactChain> sample = new LinkedList<ImpactChain>();
        while(count-- > 0) {
            sample.add(randomChain(impactorIndex, impactedIndex, length));
        }
        return sample;
    }
    
    ImpactChain randomChain(int impactorIndex, int impactedIndex, int length) {
        assert indexIsValid(impactorIndex);
        assert indexIsValid(impactedIndex);
        assert length > 1;
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
    
    
    /**
     * Returns a list of possible indices of intermediary variables for chain generation for <b>matrix</b>.
     * @param impactorIndex Index of impactor variable of the chain
     * @param impactedIndex Index of impacted variable of the chain
     * @return 
     */
    private List<Integer> availableIndices(int impactorIndex, int impactedIndex) {
        assert indexIsValid(impactorIndex) && indexIsValid(impactedIndex);
        List<Integer> indices = new ArrayList<>();
        for(int i=1;i<=matrix.varCount;i++) {
            if(i != impactorIndex && i != impactedIndex) {
                indices.add(i);
            }
        }
        return indices;
    }

    
    private boolean indexIsValid(int index) {
        return index > 0 && index <= matrix.varCount;
    }
    
    
}
