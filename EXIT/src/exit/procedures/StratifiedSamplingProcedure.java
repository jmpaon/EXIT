/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package exit.procedures;

import exit.matrices.CrossImpactMatrix;
import exit.estimators.QuickSampler;
import exit.estimators.Sampler;
import exit.io.Option;
import exit.io.Options;
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
        
        Integer computeUpToLength = input.options.queryInt("-c");
        Integer sampleSize = input.options.hasValue("-s") ? input.options.queryInt("-s") : 1000000;
        
        QuickSampler sampler = new QuickSampler(input.directImpactMatrix, computeUpToLength, reportingStream);
        Timer samplingTimer = new Timer(true);
        CrossImpactMatrix summedImpactMatrix = sampler.estimateSummedImpactMatrix(sampleSize);
        Timer.Time duration = samplingTimer.stopGet();
        // String duration = samplingTimer.stop(Timer.TimeUnit.S);
        
        EXITresult result = new EXITresult(summedImpactMatrix);
        
        
        StringBuilder computationDetails = new StringBuilder();
        String inputfilename = input.options.queryString("-i");
        computationDetails.append(String.format("%30.30s: %s%n", "Input file name", inputfilename));
        computationDetails.append(String.format("%30.30s: %s variables%n", "Full computation up to", computeUpToLength != null ? computeUpToLength : sampler.sensibleComputeUpToLength()));
        computationDetails.append(String.format("%30.30s: %s chains%n", "Sample size", sampleSize));
        computationDetails.append(String.format("%30.30s: %s%n", "Process duration", duration.value(Timer.TimeUnit.S)));
        
        result.addPrintable("EXIT analysis with the arguments:", computationDetails.toString());
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
    
    /**
     * Provides the <tt>Options</tt> instance to be used with this procedure
     * @param args
     * @return 
     */
    @Override
    public Options options(String[] args) {
        Option<String> optInputfile = new Option<String>("-i", "input file name", true, true, String::valueOf);
        Option<Double> optMaxImpact = new Option<Double>("-m", "maximum impact", true, true, Double::valueOf);
        Option<Integer> optSampleSize = new Option<Integer>("-s", "sample size", true, false, Integer::valueOf);
        Option<Integer> optComputeTo = new Option<Integer>("-c", "full computation up to chain length", true, false, Integer::valueOf);
        Option<String> optOutputfile = new Option<String>("-o", "output file name", true, false, String::valueOf);
        Option<Character> optSeparator = new Option<Character>("-sep", "separator character", true, false, (String v) -> v.charAt(0));

        optMaxImpact.addCondition(v -> v > 0, "Maximum impact value must be greater than 0");
        optSampleSize.addCondition(v -> v > 0, "Sample size must be greater than 0");
        optComputeTo.addCondition(v -> v >= 2,  "Computation length must be 2 or greater");
        optComputeTo.addCondition(v -> v <= 20, "Full computation length greater than 20 is not supported");

        Options ops = new Options();
        ops.addOption(optInputfile, optMaxImpact, optSampleSize, optComputeTo, optOutputfile, optSeparator);
        ops.setUsageText("Usage: java -jar exit.jar [-optionid optionvalue]...\n" + 
                "Example: java -jar exit.jar -i inputfile.csv -m 5 -s 300000 -c 5");
        
        ops.parse(args);
        return ops;        
    }
    
    

    
    
    
}
