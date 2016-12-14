/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package exit;


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
        
        test_new_features();
        
        /* New standard calculation */
        // new_exit_analysis(args);
        
        /* Normal calculation procedure */
        //standard_exit_analysis(args);
        
        /* JL-procedure */
        //JL_exit(3500);
        
        /* Test features */
        //test_features(args);
    
    }
    
    
    public static void test_new_features() {
        double d[][] = new double[4][4];
        d[0][0] = 2;
        d[0][1] = 3;
        d[0][2] = 4;
        d[0][3] = -1;
        d[1][0] = -2;
        d[1][1] = -3;
        d[1][2] = -4;
        d[1][3] = 5;
        d[2][0] = -5;
        d[2][1] = 3;
        d[2][2] = 0;
        d[2][3] = 0;
        d[3][0] = 2;
        d[3][1] = 1;
        d[3][2] = 0;
        d[3][3] = 3;
        String[] nam = {"mem","moo","mou","mau"};
        
        SquareMatrix sm = new SquareMatrix(nam, d);
        CrossImpactMatrix cm = new CrossImpactMatrix(sm);
        CrossImpactMatrix.Ordering ord = cm.getOrdering(CrossImpactMatrix.Orientation.INFLUENCE);
        System.out.println(ord);
        System.out.println(cm.scale(100).toString());
        
        // System.out.println(sm.toString());
    }
    
    public static void new_exit_analysis(String[] args) {
        try {

            EXITarguments arguments = new EXITarguments(args);
            PrintStream output;
            
            if(arguments.outputFilename == null) {
                output = System.out;
            } else {
                output = new PrintStream(arguments.outputFilename);
            }
            
            /* output.println( arguments ); */
            Reporter.requiredReportingLevel = 0;
            Reporter.output = output;
            
            InputFileReader ifr = new InputFileReader();
            EXITImpactMatrix inputMatrix = ifr.readInputFile(arguments);
            
            output.println("Direct impact matrix:");
            output.println(inputMatrix.toString());
            
            if(arguments.extraReports) {
                output.printf("Importance matrix derived from input matrix; max value %f:%n", arguments.maxImpact);
                output.println(inputMatrix.importanceMatrix().round().scale(arguments.maxImpact.intValue()));
                
                output.println("Input matrix driver-driven report:");
                output.println(new EXITImpactMatrix(inputMatrix.scale(1)).driverDriven().toString());
            }
            

            
            if (arguments.impactOf != null || arguments.impactOn != null) 
            /* Print requested impact chains and their summed impacts */
            {
                
                Integer impactOfIndex = arguments.impactOf != null ? 
                        isInteger(arguments.impactOf) ? Integer.parseInt(arguments.impactOf) : inputMatrix.getIndex(arguments.impactOf)
                        : null;
                Integer impactOnIndex = arguments.impactOn != null ?
                        isInteger(arguments.impactOn) ? Integer.parseInt(arguments.impactOn) : inputMatrix.getIndex(arguments.impactOn)
                        : null;
                    
                List<ImpactChain> significantChains = inputMatrix.indirectImpacts(impactOfIndex, impactOnIndex, arguments.treshold);
                Map<List<String>, Double> chainsSummed = new HashMap<>();
                
                String impactChainDescription = impactOfIndex != null ?
                        impactOnIndex != null ? 
                            String.format("Impacts of %s on %s", inputMatrix.getName(impactOfIndex), inputMatrix.getName(impactOnIndex))
                            : String.format("Impacts of %s", inputMatrix.getName(impactOfIndex))
                        : String.format("Impacts on %s", inputMatrix.getName(impactOnIndex));
                
                        
                output.printf("%s with significant (%1.3f) impact:%n", impactChainDescription,  arguments.treshold );
                for (ImpactChain chain : significantChains) {
                    output.println( chain );
                    List<String> key = new LinkedList<>(Arrays.asList( chain.impactorName(), chain.impactedName() ));
                    if(chainsSummed.containsKey(key)) {
                        chainsSummed.put(key, chainsSummed.get(key) + chain.impact() );
                    } else {
                        chainsSummed.put(key, chain.impact());
                    }
                }
                
                for (Map.Entry<List<String>, Double> entry : chainsSummed.entrySet()) {
                    List<String> key = entry.getKey();
                    Double value = entry.getValue();
                    output.printf("Impact of %s on %s : %2.2f%n", key.get(0), key.get(1), value);
                }
                
                
            } else
                
            /* Print full cross-impact matrix displaying direct and indirect impacts */
            {
                Timer timer = new Timer();
                CrossImpactMatrix resultMatrix = inputMatrix.summedImpactMatrix(arguments.treshold);
                timer.stopTime("Process duration: ");
                
                
                output.printf("Summed impact matrix%n");
                output.println(resultMatrix);
                
                output.printf("Result impact matrix with summed direct and indirect impacts between variables, scaled to %f:%n", inputMatrix.getMaxImpact());
                output.println(resultMatrix.scale(inputMatrix.getMaxImpact()));
                
                
                if(arguments.extraReports) {
                    output.printf("%nImportance matrix derived from result matrix; max value %f:%n", arguments.maxImpact);
                    output.println(resultMatrix.importanceMatrix().scale(arguments.maxImpact));
                }
                
                output.printf("%nDifference matrix of normalized result matrix and normalized input matrix, scaled to %f:%n", inputMatrix.getMaxImpact());
                // output.println(resultMatrix.normalize().scale(inputMatrix.getMaxImpact()).differenceMatrix(inputMatrix.normalize().scale(inputMatrix.getMaxImpact())));
                output.println(resultMatrix.normalize().differenceMatrix(inputMatrix.normalize()));
                
                if(arguments.extraReports) {
                    output.printf("%nDifference matrix of result matrix and input matrix scaled to %f and rounded:%n", inputMatrix.getMaxImpact());
                    //output.println(resultMatrix.scale(inputMatrix.getMaxImpact()).differenceMatrix(inputMatrix).round( (int) inputMatrix.getMaxImpact()  ));                    
                
                    output.println("%nResult matrix driver-driven report");
                    //output.println(resultMatrix.driverDriven().toString());
                
                    // output.println(resultMatrix.reportDrivingVariables());
                }
                
            }
     
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
    
    public static void standard_exit_analysis(String[] args) {
        try {
            
            String[] arggs = {"src/exit/eltran1.csv", "-max", "4", "-t", "0.0001", "-extra"};
            EXITarguments arguments = new EXITarguments(args);
            PrintStream output;
            
            if(arguments.outputFilename == null) {
                output = System.out;
            } else {
                output = new PrintStream(arguments.outputFilename);
            }
            
            /* output.println( arguments ); */
            Reporter.requiredReportingLevel = 0;
            Reporter.output = output;
            
            InputFileReader ifr = new InputFileReader();
            EXITImpactMatrix inputMatrix = ifr.readInputFile(arguments);
            
            output.println("Input matrix describing direct impacts:");
            output.println(inputMatrix.toString());
            if(arguments.extraReports) {
                output.printf("Importance matrix derived from input matrix; max value %f:%n", arguments.maxImpact);
                //output.println(inputMatrix.importanceMatrix().round(arguments.maxImpact.intValue()));
                
                output.println("Input matrix driver-driven report:");
                //output.println(inputMatrix.scale(1).driverDriven().toString());                
                
            }
            

            
            if (arguments.impactOf != null || arguments.impactOn != null) 
            /* Print requested impact chains and their summed impacts */
            {
                
                Integer impactOfIndex = arguments.impactOf != null ? 
                        isInteger(arguments.impactOf) ? Integer.parseInt(arguments.impactOf) : inputMatrix.getIndex(arguments.impactOf)
                        : null;
                Integer impactOnIndex = arguments.impactOn != null ?
                        isInteger(arguments.impactOn) ? Integer.parseInt(arguments.impactOn) : inputMatrix.getIndex(arguments.impactOn)
                        : null;
                    
                List<ImpactChain> significantChains = inputMatrix.indirectImpacts(impactOfIndex, impactOnIndex, arguments.treshold);
                Map<List<String>, Double> chainsSummed = new HashMap<>();
                
                String impactChainDescription = impactOfIndex != null ?
                        impactOnIndex != null ? 
                            String.format("Impacts of %s on %s", inputMatrix.getName(impactOfIndex), inputMatrix.getName(impactOnIndex))
                            : String.format("Impacts of %s", inputMatrix.getName(impactOfIndex))
                        : String.format("Impacts on %s", inputMatrix.getName(impactOnIndex));
                
                        
                output.printf("%s with significant (%1.3f) impact:%n", impactChainDescription,  arguments.treshold );
                for (ImpactChain chain : significantChains) {
                    output.println( chain );
                    List<String> key = new LinkedList<>(Arrays.asList( chain.impactorName(), chain.impactedName() ));
                    if(chainsSummed.containsKey(key)) {
                        chainsSummed.put(key, chainsSummed.get(key) + chain.impact() );
                    } else {
                        chainsSummed.put(key, chain.impact());
                    }
                }
                
                for (Map.Entry<List<String>, Double> entry : chainsSummed.entrySet()) {
                    List<String> key = entry.getKey();
                    Double value = entry.getValue();
                    output.printf("Impact of %s on %s : %2.2f%n", key.get(0), key.get(1), value);
                }
                
                
            } else
                
            /* Print full cross-impact matrix displaying direct and indirect impacts */
            {
                Timer timer = new Timer();
                CrossImpactMatrix resultMatrix = inputMatrix.summedImpactMatrix(arguments.treshold);
                timer.stopTime("Process duration: ");
                
                
                output.printf("Result impact matrix with summed direct and indirect impacts between variables, not scaled%n");
                output.println(resultMatrix);
                
                output.printf("Result impact matrix with summed direct and indirect impacts between variables, scaled to %f:%n", inputMatrix.getMaxImpact());
                output.println(resultMatrix.scale(inputMatrix.getMaxImpact()));
                
                
                if(arguments.extraReports) {
                    output.printf("Importance matrix derived from result matrix; max value %f:%n", arguments.maxImpact);
                    output.println(resultMatrix.importanceMatrix().scale(arguments.maxImpact));
                }
                
                output.printf("Difference matrix of result matrix and input matrix, both scaled to max value %f:%n", arguments.maxImpact);
                output.println(resultMatrix.scale(arguments.maxImpact).differenceMatrix(inputMatrix.scale(arguments.maxImpact)));
                
                if(arguments.extraReports) {
                    output.printf("Difference matrix of result matrix and input matrix scaled to %f and rounded:%n", inputMatrix.getMaxImpact());
                    output.println(resultMatrix.scale(inputMatrix.getMaxImpact()).differenceMatrix(inputMatrix).round().scale((int) inputMatrix.getMaxImpact())  );

                }
                
            }
     
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
    

    
    
    private static void test_features(String[] args) {
        try {
            Reporter.requiredReportingLevel = 0;
            String[] arggs = {"src/exit/eltran1.csv", "-max", "5", "-t", "0.00000000001" };
            EXITarguments arguments = new EXITarguments(arggs);
            
            InputFileReader ifr = new InputFileReader();
            EXITImpactMatrix directImpactMatrix = ifr.readInputFile(arguments);
//            double mem[] = {0,1,0,1,0,1,1,0,0};
//            String names[] = {"A","B","C"};
//            double mem2[] = {0,3,-1,-2,1,-1,0,3,-3,2,3,2,0,2,-2,1,0,-1,0,2,-3,-2,0,-1,0};
//            String names2[] = {"A","B","C","D","E"};
//            double mem3[] = {0,3,0,4,0,0,4,0,1,2,0,3,4,0,4,0};
//            String names3[] = {"A","B","C","D"};
            //double article_data[] = {0,-2,2,1,5,3,0,-3,-2,1,0,1,0,-1,0,4,3,1,0,-5,2,-3,1,-1,0};
            //String article_vars[] = {"VarA","VarB","VarC","VarD","VarE"};
            //directImpactMatrix = new EXITImpactMatrix(5, 5, true, article_vars, article_data);
            
            
            System.out.println("Direct impact matrix");
            System.out.println(directImpactMatrix);
            CrossImpactMatrix resultMatrix = directImpactMatrix.summedImpactMatrix(0.0000000001);
            
            System.out.println("Result matrix");
            System.out.println(resultMatrix);
            
            System.out.println("Direct impact matrix normalized");
            System.out.println(directImpactMatrix.normalize());
            
            System.out.println("Direct impact matrix normalized + scaled to 5");
            System.out.println(directImpactMatrix.normalize().scale(5));
            
         
            
            
        } catch (Exception ex) {
            Logger.getLogger(EXIT.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static boolean isInteger(String str) {  
        try {int d = Integer.parseInt(str);}  
        catch(NumberFormatException nfe){ return false;}  
        return true;  
    }

    
    

}
