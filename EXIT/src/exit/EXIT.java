/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package exit;


import exit.io.EXITarguments;
import exit.procedures.Reporter;
import exit.procedures.Timer;
import exit.matrices.RandomInputMatrixGenerator;
import exit.matrices.CrossImpactMatrix;
import exit.io.InputFileReader;

import exit.matrices.EXITImpactMatrix;
import exit.io.EXITargumentException;
import exit.matrices.ImpactChain;
import exit.procedures.EXITinput;
import exit.procedures.EXITprocedure;
import exit.procedures.EXITresult;
import exit.procedures.StratifiedSamplingProcedure;
import exit.samplers.QuickSampler;
import exit.samplers.ImpactChainSampler;
import exit.samplers.Sampler;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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

        System.out.println(Arrays.asList(args));
        System.out.println(System.getProperty("user.dir"));
        // test_features(args);
        
        /* New standard calculation */
        new_exit_analysis(args);
    
    }
    

    
    
    
    public static void test_new_features() {
        
        EXITImpactMatrix sme = new EXITImpactMatrix(RandomInputMatrixGenerator.generateCrossImpactMatrix(12, 1.,2.,0.,4.,5.,3.), 5);
        Sampler s = new QuickSampler(sme);
        Sampler s2 = new ImpactChainSampler(sme);
        
        
        System.out.println("Input matrix");
        System.out.println(sme);
   
        //System.out.println("All");
        //System.out.println(s.computeAll());        

        System.out.println("Pruning");
        Timer t1 = new Timer();
        //System.out.println(sme.prunedSummedImpactMatrix(0.00001));
        t1.stopTime();
        
        System.out.println("qs estimate");
        Timer t2 = new Timer();
        System.out.println(s.estimateSummedImpactMatrix(100000));
        t2.stopTime();
        
        //System.out.println("ics estimate");
        //System.out.println(s2.estimateSummedImpactMatrix(100000));        

        
        
        
        
    }
    
    public static void printList(List<? extends Object> l) {
        for(Object o : l) {
            System.out.println(o.toString());
        }
    }
    
    public static void new_exit_analysis(String[] args) {
        try {
            
            EXITarguments arguments = new EXITarguments(args);
            InputFileReader reader = new InputFileReader();
            EXITImpactMatrix inputMatrix = reader.readInputFile(arguments);
            
            EXITinput input = new EXITinput(inputMatrix, arguments);
            EXITprocedure procedure = new StratifiedSamplingProcedure();
            EXITresult result = procedure.compute(input);
            result.print();
            
     
        }catch(FileNotFoundException ex) {
            System.out.printf("Input file not found: %s%n", ex.getMessage());
        }catch(IOException ex) {
            System.out.printf("Error reading input file: %s%n", ex.getMessage());
        }catch(EXITargumentException ex) {
            System.out.println("Argument error occurred: " + ex.getMessage());            
        } catch(EXITexception ex) {
            System.out.println("EXIT error occurred: " + ex.getMessage());
        } catch(Exception ex) {
            System.out.println("Error occurred: " + ex.getMessage());
            // Logger.getLogger(EXIT.class.getName()).log(Level.SEVERE, null, ex);
        }        
    }


    public static boolean isInteger(String str) {  
        try {int d = Integer.parseInt(str);}  
        catch(NumberFormatException nfe){ return false;}  
        return true;  
    }

    
    

}
