/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package exit;


import exit.estimators.ImpactChainSampler;
import exit.procedures.Timer;
import exit.matrices.RandomInputMatrixGenerator;
import exit.matrices.CrossImpactMatrix;

import exit.matrices.EXITImpactMatrix;
import exit.io.EXITargumentException;
import exit.procedures.EXITinput;
import exit.procedures.EXITprocedure;
import exit.procedures.EXITresult;
import exit.procedures.StratifiedSamplingProcedure;
import exit.estimators.QuickSampler;
import exit.io.EXITinputfileException;
import exit.io.Option;
import exit.io.Options;
import exit.io.Reader;
import exit.matrices.EasyChainMatrix;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;



/**
 *
 * @author jmpaon
 */
public class EXIT {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)  {

        // System.out.println(System.getProperty("user.dir"));
        
        /* New standard calculation */
        // new_exit_analysis(args);
        

        //test_new_features();
        printPhases();
    }
    
    
    public static void new_exit_analysis(String[] args) {
        try {
            EXITprocedure procedure = new StratifiedSamplingProcedure();
            Options options = procedure.options(args);
            Reader reader = new Reader(System.out);
            
            EXITImpactMatrix inputMatrix = reader.readInputMatrixFromFile(
                    options.queryString("-i"), 
                    options.queryDouble("-m"), 
                    options.hasValue("-sep") ? options.queryChar("-sep") : ';'  );
            
            EXITinput input = new EXITinput(inputMatrix, options);            
            EXITresult result = procedure.compute(input, System.out);
            
            PrintStream output = options.hasValue("-o") ? new PrintStream(new File(options.queryString("-o"))) : System.out;
            result.print(output);
            
     
        }catch(FileNotFoundException ex) {
            System.out.printf("Input file not found: %s%n", ex.getMessage());
        }catch(IOException ex) {
            System.out.printf("Error reading input file: %s%n", ex.getMessage());
        }catch(EXITinputfileException ex) {
            System.out.printf("Error reading input file: %s%n", ex.getMessage());
        }catch(EXITargumentException ex) {
            System.out.println("Argument error: " + ex.getMessage());
            //Logger.getLogger(EXIT.class.getName()).log(Level.SEVERE, null, ex);
        } catch(EXITexception ex) {
            System.out.println("EXIT error: " + ex.getMessage());
            //Logger.getLogger(EXIT.class.getName()).log(Level.SEVERE, null, ex);
        } catch(Exception ex) {
            System.out.println("Error: " + ex.getMessage());
            //Logger.getLogger(EXIT.class.getName()).log(Level.SEVERE, null, ex);
        }        
    }
    
    public static void printPhases() {
        CrossImpactMatrix testImpactMatrix = RandomInputMatrixGenerator.generateCrossImpactMatrix(5, 1., 2., 3., -4., 5., 3., -2., -1.);
        EXITImpactMatrix testEXITMatrix = new EXITImpactMatrix(testImpactMatrix, 5);
        
        ImpactChainSampler s = new ImpactChainSampler(testEXITMatrix);
        
        for(int i=2;i<=5;i++) {
            System.out.printf("Summed impacts of chains of length %d:\n", i);
            System.out.println(s.estimateSummedImpactMatrix(10000, i));
        }
        
        EasyChainMatrix orig = new EasyChainMatrix(testImpactMatrix.scale(1.0));
        EasyChainMatrix easy = new EasyChainMatrix(testImpactMatrix.scale(1.0));
        System.out.println(easy); // 2
        
        easy = easy.power(easy);
        System.out.println(easy); // 3
        
        easy = easy.power(easy);        
        System.out.println(easy); // 4
        
        easy = easy.power(easy);        
        System.out.println(easy); // 5
        
        
        
    }
    

    
    public static void test_new_features() {
        
        CrossImpactMatrix testImpactMatrix = RandomInputMatrixGenerator.generateCrossImpactMatrix(10, 1., 2., 3., -4., 5., 3., -2., -1.);
        EXITImpactMatrix testEXITMatrix = new EXITImpactMatrix(testImpactMatrix, 5);
        
        QuickSampler s = new QuickSampler(testEXITMatrix,System.out);
        

        //CrossImpactMatrix m1 = s.estimateSummedImpactMatrix(500000);
        CrossImpactMatrix m2 = s.estimateSummedImpactMatrix(100000);


        System.out.println("Sampler::estimate");
        System.out.println(m2.scale(1));
    }
    
    public static void printList(List<? extends Object> l) {
        l.stream().forEach((o) -> { System.out.println(o.toString()); });
    }
    


    public static void argsTester() {
        
        Option<String> optInputfile = new Option<String>("-i", "input file name", true, true, String::valueOf);
        Option<Double> optMaxImpact = new Option<Double>("-m", "maximum impact", true, true, Double::valueOf);
        Option<Integer> optSampleSize = new Option<Integer>("-s", "sample size", true, false, Integer::valueOf);
        Option<Integer> optComputeTo = new Option<Integer>("-c", "full computation up to chain length", true, false, Integer::valueOf);
        Option<String> optOutputfile = new Option<String>("-o", "output file name", true, false, String::valueOf);
        Option<Character> optSeparator = new Option<Character>("-sep", "separator character", true, false, (String v) -> v.charAt(0));
        
        optMaxImpact.addCondition(v -> v > 0, "Maximum impact value must be greater than 0");
        optSampleSize.addCondition(v -> v > 0, "Sample size must be greater than 0");
        optComputeTo.addCondition(v -> v >= 2,  "Computation length must be 2 or greater");
        optComputeTo.addCondition(v -> v <= 20, "Computation length greater than 20 is not supported");
        
        
        try {
            
            String[] args = {"-i", "src/exit/eltran1.csv", "-m", "4",  "-s", "100000" };
            Options ops = new Options();
            ops.addOption(optInputfile, optMaxImpact, optSampleSize, optComputeTo, optOutputfile, optSeparator);
            ops.parse(args);
            
            System.out.println(ops);
            System.out.println("commandLineArgs size: " + ops.commandLineArguments.size());
            
            Double oo = (Double)ops.query("-m").get();
            Option<Double> ooo = ops.query("-c");
            
            EXITImpactMatrix eim = new Reader().readInputMatrixFromFile(ops.queryString("-i"), ops.queryDouble("-m"), ';');
            System.out.println(eim);
            
            
                        
        } catch(Exception e) {
            e.printStackTrace();
        }
        
    }
    

}
