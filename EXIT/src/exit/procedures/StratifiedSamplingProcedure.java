/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package exit.procedures;

import exit.matrices.CrossImpactMatrix;
import exit.samplers.QuickSampler;
import exit.samplers.Sampler;

/**
 *
 * @author jmpaon
 */
public class StratifiedSamplingProcedure extends EXITprocedure {

    @Override
    public EXITresult compute(EXITinput input) {
        
        Sampler sampler = new QuickSampler(input.directImpactMatrix, input.arguments.computeUpToLength);
        CrossImpactMatrix summedImpactMatrix = sampler.estimateSummedImpactMatrix(input.arguments.sampleSize);
        EXITresult result = new StratifiedSamplingResult(summedImpactMatrix);
        
    }
    
}
