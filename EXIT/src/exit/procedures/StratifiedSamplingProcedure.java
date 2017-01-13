/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package exit.procedures;

import exit.matrices.CrossImpactMatrix;
import exit.estimators.QuickSampler;
import exit.estimators.Sampler;
import exit.io.EXITargumentException;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author jmpaon
 */
public class StratifiedSamplingProcedure extends EXITprocedure {

    @Override
    public EXITresult compute(EXITinput input, PrintStream reportingStream) throws FileNotFoundException {
        
        assert input != null;
        
        Sampler sampler = new QuickSampler(input.directImpactMatrix, input.arguments.computeUpToLength, reportingStream);
        CrossImpactMatrix summedImpactMatrix = sampler.estimateSummedImpactMatrix(input.arguments.sampleSize);
        EXITresult result = new EXITresult(input, summedImpactMatrix);
        
        result.addPrintable("EXIT analysis with the arguments:", input.arguments.toString());
        result.addPrintable("Direct impact matrix:", input.directImpactMatrix.toString());
        result.addPrintable("Direct impact matrix normalized:", input.directImpactMatrix.normalize().toString());
        result.addPrintable("Direct impact matrix variable classification:", input.directImpactMatrix.getInfluenceDependencyClassification());
        result.addPrintable("Summed impact matrix:", summedImpactMatrix.toString());
        result.addPrintable("Summed impact matrix normalized:", summedImpactMatrix.normalize().toString());
        result.addPrintable("Summed impact matrix variable classification:", summedImpactMatrix.getInfluenceDependencyClassification());
        result.addPrintable("Difference matrix of normalized input and output matrices:", 
                input.directImpactMatrix.normalize().differenceMatrix(summedImpactMatrix.normalize()).toString());
        
        
        return result;
    }

    @Override
    public EXITresult compute(EXITinput input) throws FileNotFoundException {
        return compute(input, null);
    }
    
    public class ArgumentValidator extends exit.io.Arguments {
        
        
        
        
        public ArgumentValidator(String[] args) throws EXITargumentException {
            super(args);
        }
        

        @Override
        public Map<String, String> knownOptions() {
            Map<String, String> options = new LinkedHashMap<>();
            options.put("-max",       "Maximum impact value");
            options.put("-o",         "Output file name");
            options.put("-sep",       "Input file separator character");
            
            return options;
        }

        @Override
        protected void testForInvalidOptions() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
        
    }
    
    
    
}
