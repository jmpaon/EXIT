/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package exit;


import exit.io.EXITarguments;

import exit.procedures.Timer;
import exit.matrices.RandomInputMatrixGenerator;
import exit.matrices.CrossImpactMatrix;
import exit.io.InputFileReader;

import exit.matrices.EXITImpactMatrix;
import exit.io.EXITargumentException;
import exit.procedures.EXITinput;
import exit.procedures.EXITprocedure;
import exit.procedures.EXITresult;
import exit.procedures.StratifiedSamplingProcedure;
import exit.estimators.QuickSampler;

import java.io.FileNotFoundException;
import java.io.IOException;


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

        // System.out.println(Arrays.asList(args));
        // System.out.println(System.getProperty("user.dir"));
        
        /* New standard calculation */
        new_exit_analysis(args);
    
    }
    

    
    
    
    public static void test_new_features() {
        
        EXITImpactMatrix sme = new EXITImpactMatrix(RandomInputMatrixGenerator.generateCrossImpactMatrix(15, 1.,2.,0.,4.,5.,3.), 5);
        QuickSampler s = new QuickSampler(sme,System.out);
        
        Timer t = new Timer();
        //CrossImpactMatrix m1 = s.estimateSummedImpactMatrix(500000);
        CrossImpactMatrix m2 = s.estimateSummedImpactMatrix(100000);
        t.stopTime("Sampling time: ");

        System.out.println("Sampler::estimate");        

        System.out.println(m2.scale(1));



        
        
        
        
    }
    
    public static void printList(List<? extends Object> l) {
        l.stream().forEach((o) -> { System.out.println(o.toString()); });
    }
    
    public static void new_exit_analysis(String[] args) {
        try {
            
            EXITarguments arguments = new EXITarguments(args);
            InputFileReader reader = new InputFileReader();
            EXITImpactMatrix inputMatrix = reader.readInputFile(arguments);
            
            EXITinput input = new EXITinput(inputMatrix, arguments);
            EXITprocedure procedure = new StratifiedSamplingProcedure();
            EXITresult result = procedure.compute(input, System.out);
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
