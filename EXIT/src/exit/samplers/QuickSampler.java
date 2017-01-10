/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package exit.samplers;

import exit.matrices.CrossImpactMatrix;
import exit.matrices.EXITImpactMatrix;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.List;

/**
 * QuickSampler is a computationally more efficient implementation of <tt>Sampler</tt> 
 * (compared to <tt>ImpactChainSampler</tt>.
 * QuickSampler estimates the summed impact matrix by performing full computation
 * for short impact chains and estimating the summed impacts of longer chains
 * by means of a stratified sample.
 * @author juha
 */
public class QuickSampler extends Sampler {

    private final int computeUpToLength;

    
    /**
     * Constructor for <tt>QuickSampler</tt>
     * @param matrix Direct impact matrix that is sampled by this sampler
     */
    public QuickSampler(EXITImpactMatrix matrix) {
        super(matrix);
        this.computeUpToLength = 7; /* Chains of length 7 are computed and longer chains sampled */
    }
    
    
    /**
     * Constructor for <tt>QuickSampler</tt>
     * @param matrix Direct impact matrix that is sampled by this sampler
     * @param computeUpToLength The maximum length of chains that are computed 
     * (instead of estimated by sampling) 
     * when summed impacts are determined
     */
    public QuickSampler(EXITImpactMatrix matrix, int computeUpToLength) {
        super(matrix);
        this.computeUpToLength = computeUpToLength;
    }
    
    public QuickSampler(EXITImpactMatrix matrix, int computeUpToLength, PrintStream reportingStream) {
        super(matrix, reportingStream);
        this.computeUpToLength = computeUpToLength;
    }
    
    public QuickSampler(EXITImpactMatrix matrix, PrintStream reportingStream) {
        this(matrix, 7, reportingStream);
    }
    
    
    /**
     * Returns a <tt>CrossImpactMatrix</tt> that contains the <u>estimated</u> summed 
     * direct and indirect impacts between the variables present in <b>matrix</b>. 
     * For short chains, the summed impacts are computed;
     * for longer chains, the summed impacts are estimated based on a stratified sample.
     * @param sampleSize Size of the drawn sample of chains of a specific length; 
     * for each variable pair, the average impact of chains of length x is based on 
     * a sample of size <b>sampleSize</b>.
     * @return CrossImpactMatrix : matrix that contains summed direct and indirect impacts.
     */
    @Override
    public CrossImpactMatrix estimateSummedImpactMatrix(int sampleSize) {
        assert sampleSize > 0 : "SampleSize must be greater than 0";
        CrossImpactMatrix summedImpactMatrix = new CrossImpactMatrix(matrix.copy().flush());
        
        report("Estimating summed impacts...");
        for(int impactor = 1; impactor <= matrix.getVarCount(); impactor++) {
            for(int impacted = 1; impacted <= matrix.getVarCount(); impacted++) {
                if(impactor != impacted) {
                    //report(String.format("Estimating impact of %s on %s", matrix.getNameShort(impactor), matrix.getNameShort(impacted)));
                    reportf("Estimating impact of %s on %s...%n", matrix.getNameShort(impactor), matrix.getNameShort(impacted));
                    summedImpactMatrix.setValue(impactor, impacted, computeOrSampleSummedImpact(impactor, impacted, sampleSize));
                }
            }
        }
        return summedImpactMatrix;
    }
    
    /**
     * Returns an estimate of the 
     * summed impact of variable with index <b>impactorIndex</b> on
     * variable with index <b>impactedIndex</b>.
     * For short chains, the impacts are computed exactly. 
     * For longer chains, the impacts are estimated, based on a stratified sampling strategy.
     * @param impactorIndex Index of impactor variable
     * @param impactedIndex Index of impacted variable
     * @param sampleSize Size of sample in sampling
     * @return double : summed (total) impact of impactor on impacted
     */
    double computeOrSampleSummedImpact(int impactorIndex, int impactedIndex, int sampleSize) {
        double summedImpact = 0;
        for(int length=2;length<=matrix.getVarCount();length++) {
            
            if (length <= computeUpToLength) {
                reportf("\tComputing chains of length %d%n", length);
                summedImpact += computeAll(impactorIndex, impactedIndex, length);
            } else {
                reportf("\tEstimating chains of length %d%n", length);
                summedImpact += estimateSummedImpact(impactorIndex, impactedIndex, length, sampleSize);
            }
        }
        reportf("%n");
        return summedImpact;        
    }
    
    
    /**
     * Estimates the summed (total) impact of impactor on impacted based on a stratified sample.
     * @param impactorIndex Index of impactor variable
     * @param impactedIndex Index of impacted variable
     * @param sampleSize Size of sample drawn from each set of chains of specific length
     * @return double : summed impact of impactor variable on impacted variable based on stratified sample
     */
    double estimateSummedImpact(int impactorIndex, int impactedIndex, int sampleSize) {
        double summedImpact = 0;
        for(int length=2;length<=matrix.getVarCount();length++) {
            summedImpact += estimateSummedImpact(impactorIndex, impactedIndex, length, sampleSize);
        }
        return summedImpact;
    }

    
    /**
     * Estimates the summed (total) impact of chains of length <b>chainLength</b>
     * where first variable (impactor) has index <b>impactorIndex</b> and 
     * last variable (impacted) has index <b>impactedIndex</b>.
     * Estimate is based on a sample of size <b>sampleSize</b>,
     * drawn from possible impact chains meeting the specifications, with replacement.
     * @param impactorIndex Variable index of impactor
     * @param impactedIndex Variable index of impacted
     * @param chainLength Length of sampled chains
     * @param sampleSize Size of sample
     * @return double : summed impact of chains of length <b>chainLength</b> between impactor and impacted
     */
    double estimateSummedImpact(int impactorIndex, int impactedIndex, int chainLength, int sampleSize) {
        assert sampleSize > 0 : "Sample size 0 or smaller";
        assert chainLength > 1 : "Chain length must be greater than 1";
        
        /* Chains with length 2 need not be sampled */
        if (chainLength == 2) {
            return matrix.getValue(impactorIndex, impactedIndex)/matrix.getMaxImpact();
        }
        
        int i=0;
        double mean=0;
        while(sampleSize-- > 0) {
            double rndImpact = impactOfChain(randomIndices(impactorIndex, impactedIndex, chainLength));
            mean = (rndImpact + mean * i) / (i+1);
            i++;
        }
        
        double chainCount = EXITImpactMatrix.chainCount_intermediary(matrix.getVarCount(), chainLength-2);
        
        return mean * chainCount;
    }
    
    
    /**
     * Computes the relative impact of an impact chain with the variables 
     * with the <b>indices</b>.
     * @param indices Indices of variables in the chain, ordered as in the chain (impactor first, impacted last)
     * @return double : relative impact of an impact chain
     */
    double impactOfChain(int[] indices) {
        assert indices.length > 1 : "Chain length must be greater than 1";
        double impact = 1;
        double max = matrix.getMaxImpact();
        for(int i=0;i<indices.length-1;i++) {
            impact *= matrix.getValue(indices[i],indices[i+1]) / max;
        }
        return impact;
    }
    
    
    /**
     * Computes the relative impact of an impact chain with the variables 
     * with the <b>indices</b>.
     * @param indices Indices of variables in the chain, ordered as in the chain (impactor first, impacted last)
     * @return double : relative impact of an impact chain
     */
    double impactOfChain(List<Integer> indices) {
        assert indices.size() > 1 : "Chain length must be greater than 1";
        double impact = 1;
        double max = matrix.getMaxImpact();
        Iterator<Integer> it1 = indices.iterator();
        Iterator<Integer> it2 = indices.iterator(); it2.next();
        while(it1.hasNext() && it2.hasNext()) {
            impact *= matrix.getValue(it1.next(), it2.next()) / max;
        }
        return impact;
    }
    
    
    
}
