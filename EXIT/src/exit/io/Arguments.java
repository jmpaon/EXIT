/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package exit.io;


import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author jmpaon
 */
public abstract class Arguments {
    
    final List<String> arguments;
    
    
    public Arguments(String[] args) throws EXITargumentException {
        this.arguments = Arrays.asList(args);
        testForUnknownOptions();
        testForInvalidOptions();
    }
    
    public abstract Map<String, String> knownOptions();

    protected double extractDouble(String id) throws EXITargumentException {
        String value = extractString(id);
        if(value == null) throw new EXITargumentException(String.format("Value for argument %s is missing", id));
        return Double.valueOf(value);
    }
    
    protected double extractDouble(String id, double defaultValue) throws EXITargumentException {
        String value = extractString(id);
        if(value == null) return defaultValue;
        try {
            return Double.valueOf(value);
        } catch(Exception e) {
            return defaultValue;
        }
    }

    protected int extractInt(String id) throws EXITargumentException {
        String value = extractString(id);
        if(value == null) throw new EXITargumentException(String.format("Value for argument %s is missing", id));
        return Integer.valueOf(value);
    }
    
    protected int extractInt(String id, int defaultValue) throws EXITargumentException {
        String value = extractString(id);
        if(value == null) return defaultValue;
        try {
            return Integer.valueOf(value);
        } catch(Exception e) {
            return defaultValue;
        }
    }
    
    /**
     * Extracts a value than follows a specific flag in the arguments list.
     * @param id flag to search from the arguments list
     * @return The value that is in the arguments list at the succeeding index of <b>id</b>
     * @throws EXITargumentException
     */
    protected String extractString(String id) throws EXITargumentException {
        int idPos = arguments.indexOf(id);
        if(idPos == -1) return null;
        if(idPos == arguments.size()-1) throw new EXITargumentException(String.format("value for argument %s is missing", id));
        return arguments.get(idPos+1);
    }
    
    /**
     * Tests if arguments list contains a specific flag (something that has a '-' character in front of it).
     * @param id The flag that is sought from arguments list
     * @return <i>true</i> if arguments list contains flag, false otherwise
     */
    protected boolean hasFlag(final String id) {
        return arguments.contains(id);
    }


    
    protected void testForUnknownOptions() throws EXITargumentException {
        List<String> unknown = unknownOptionsUsed();
        if(!unknown.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append("Following unknown options used:\n");
            int i=1;
            for(String s : unknown) sb.append("(").append(i++).append("): ").append(s).append("\n");
            throw new EXITargumentException(sb.toString());
        }
    }
    
    protected abstract void testForInvalidOptions();

    /**
     * Tests whether arguments contain unknown options.
     * @return <i>true</i> if arguments contain options that are unknown
     * (not in the known options list
     * @see EXITarguments#knownOptions() 
     */
    private List<String> unknownOptionsUsed() {
        List<String> unknown = new LinkedList<>();
        for(String arg : arguments) {
            if(arg.startsWith("-") && !knownOptions().keySet().contains(arg))
                unknown.add(arg);
        }
        return unknown;
    }    
    
    @Override
    public String toString() {
        Class<?> objClass = this.getClass();
        String s = "";

        Field[] fields = objClass.getFields();
        
        for(Field field : fields) {
            String name = field.getName();
            Object value;
            try { value = field.get(this);} 
            catch (Exception ex) { value = "No value"; }
            s += String.format("%s%n", (name + ": " + (value == null ? "No value" : value.toString())));
        }
        
        return s;
    }    
    
    
    
    
    
    
    
}
