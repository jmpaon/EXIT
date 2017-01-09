/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package exit.io;

import exit.EXITexception;
import exit.matrices.EXITImpactMatrix;
import exit.procedures.Reporter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.List;


/**
 *
 * @author jmpaon
 */
public class InputFileReader {
    
    public EXITImpactMatrix readInputFile(EXITarguments arguments) throws IOException, EXITexception {
        
        /* At this point only CSV files are read */
        EXITImpactMatrix eim = readCSVfile(arguments);
        return eim;

    }
    
    /**
     * Creates a new <tt>EXITImpactMatrix</tt> from the contents of a valid 
     * EXIT input file in CSV format.
     * @param arguments
     * @return
     * @throws IOException
     * @throws EXITexception 
     */
    EXITImpactMatrix readCSVfile(EXITarguments arguments) throws IOException, EXITexception {
        
        Reporter.msg(String.format("Reading impact matrix data from file %s%n", arguments.inputFilename),5);
        
        if(! fileExists(arguments.inputFilename)) throw new FileNotFoundException(String.format("Input file %s not found", arguments.inputFilename));
        
        List<String> lines = Files.readAllLines(Paths.get(arguments.inputFilename));
        eliminateEmptyLines(lines);

        int variableCount = lines.size();
        int var=1;
        
        EXITImpactMatrix cim = new EXITImpactMatrix(arguments.maxImpact, variableCount, arguments.onlyIntegers);
        
        for(String l : lines) {

            Scanner sc = new Scanner(l).useDelimiter(String.valueOf(arguments.separator));
            
            cim.setName(var, sc.next() );
            int imp=0;
            while(sc.hasNextDouble()) {
                imp++;
                cim.setValue(var, imp, sc.nextDouble());
            }

            if (imp != variableCount) {
                throw new EXITargumentException(String.format("Wrong number of impact values: number of lines in input file suggests that there are %d variables, but line %d (Variable '%s') contains %d impact values", variableCount, var, cim.getName(var), imp));
            }

            var++;
        }

        cim.lock();
        Reporter.msg(String.format("Read %d variables from input file.%n", cim.getVarCount()),4);
        return cim;

    }
    
    /**
     * Reads EXIT input data from a TXT file (?)
     * @param filename
     * @param separator
     * @return 
     */
    EXITImpactMatrix readTXTfile(String filename, String separator) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
    
    /**
     * Processes a list of <tt>String</tt>s representing lines of content  
     * in an input file so that lines that only contain 
     * whitespace or are empty are removed.
     * @param lines List of <tt>String</tt>s representing lines of content in an input file
     */
    void eliminateEmptyLines(List<String> lines) {
        
        List<String> removed = new LinkedList<>();
        
        for(String l : lines) {
            if(l.trim().length() == 0) { removed.add(l); }
        }
        
        lines.removeAll(removed);
        
    }
    
    /**
     * Tests that <i>filename</i> exists and is not a directory.
     * @param filename path to be tested
     * @return true if file exists
     */
    boolean fileExists(String filename) {
        File f = new File(filename);
        return f.exists() && !f.isDirectory();
    }
    
    
    boolean isInteger(String str) {  
        try {int d = Integer.parseInt(str);}  
        catch(NumberFormatException nfe){ return false;}  
        return true;  
    }    
    
}
