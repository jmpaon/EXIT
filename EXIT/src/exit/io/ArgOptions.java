/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package exit.io;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author jmpaon
 */
public class ArgOptions {
    
    public List<String> commandLineArguments;
    public final List<ArgOption> options;
    
    
    public ArgOptions() {
        this.options = new ArrayList<>();
    }
    
    /**
     * Adds an option 
     * @param option to be added
     */
    public void addOption(ArgOption option) {
        this.options.add(option);
    }
    
    /**
     * Adds an arbitrary number of options
     * @param options to be added
     */
    public void addOption(ArgOption... options) {
        for(ArgOption option : options) ArgOptions.this.addOption(option);
    }
    
    /**
     * Returns true if option with id <b>id</b> has a value
     * @param id id of an <tt>ArgOption</tt>
     * @return 
     */
    public boolean hasValue(String id) {
        if(Objects.nonNull(query(id))) return Objects.isNull(query(id).value);
        return false;
    }
    
    
    /**
     * Returns the </tt>ArgOption</tt> with id <b>id</b>.
     * @param id id of the option queried
     * @return ArgOption 
     */
    public ArgOption query(String id) {
        for(ArgOption o : options) if(o.id.equals(id)) return o;
        return null;
    }
    
    public String queryString(String id) throws EXITargumentException {
        ArgOption o = query(id);
        try {return (String)o.get();} 
        catch(ClassCastException e) {throw new EXITargumentException("Value of %s is not a String", id);}
        catch(Exception e)          {throw new EXITargumentException("Error in querying %s as String", id);}
    }
    
    public double queryDouble(String id) throws EXITargumentException {
        ArgOption o = query(id);
        try {return (double)o.get();} 
        catch(ClassCastException e) {throw new EXITargumentException("Value of %s is not a double", id);}
        catch(Exception e)          {throw new EXITargumentException("Error in querying %s as double", id);}
    }
    
    public int queryInt(String id) throws EXITargumentException {
        ArgOption o = query(id);
        try {return (int)o.get();} 
        catch(ClassCastException e) {throw new EXITargumentException("Value of %s is not an int", id);}
        catch(Exception e)          {throw new EXITargumentException("Error in querying %s as int", id);}
    }
    
    /**
     * Returns the option value or String "true" for non-valued options.
     * Throws exception for valued options where the value doesn't look like a value.
     * @param option
     * @return 
     */
    private String readArgValue(ArgOption option) {
        
    }
    
    private String fetchArgument(String id) throws EXITargumentException {
        try {
            int indexOfArgLabel = commandLineArguments.indexOf(id);
            int indexOfArgValue = indexOfArgLabel + 1;
            if(commandLineArguments.get(indexOfArgValue).charAt(0) == '-') {
                throw new EXITargumentException("No value for argument " + id);
            }
            return commandLineArguments.get(indexOfArgValue);
        } catch(Exception e) {
            throw new EXITargumentException(String.format("No value for argument %s", id));
        }
    }
    
    private String fetchArgument(ArgOption option) throws EXITargumentException {
        return fetchArgument(option.id);
    }
    
    /**
     * Returns <i>true</i> if the argument String looks like a <u>flag</u>,
     * i.e.if it starts with a hyphen.
     * @param arg String to be tested
     * @return boolean : true if string starts with a hyphen
     */
    private boolean isFlag(String arg) {
        return arg.charAt(0) == '-';
    }
    
    private void removeArg(ArgOption option, boolean alsoRemoveValue) {
        assert option != null;
        int removeIndex = commandLineArguments.indexOf(option.id);
        assert removeIndex != -1;
        commandLineArguments.remove(removeIndex);
        if(alsoRemoveValue) commandLineArguments.remove(removeIndex);
    }
    
    private void consumeArgument(ArgOption option) throws EXITargumentException {
        
        if(commandLineArguments.contains(option.id)) {
            if(option.isValued) {
                String arg = fetchArgument(option);
                option.readIn(arg);
                int removeIndex = commandLineArguments.indexOf(option.id);
                commandLineArguments.remove(removeIndex); // remove argument label
                commandLineArguments.remove(removeIndex); // remove argument value
            }
            
            else {
                option.readIn(option.id);
                commandLineArguments.remove(commandLineArguments.indexOf(option.id));
            }
        }
        
        else {
            if(option.isRequired) {
                throw new EXITargumentException(String.format("Required argument %s is missing", option.toString()));
            }
            else {
                option.readIn(null);
            }
            
        }
        
    }
    
    private boolean lineContains(String id)     { return commandLineArguments.contains(id); }
    private boolean lineContains(ArgOption opt) { return lineContains(opt.id); }
    
    private void consume(ArgOption option) {
        if(lineContains(option)) {
            
        } else {
            option.readIn(null);
        }
    }
    
    /**
     * Parses the command line arguments into this <tt>ArgOptions</tt>.
     * @param args String[] : the unprocessed command line arguments 
     * @throws EXITargumentException 
     */
    public void parse(String[] args) throws EXITargumentException {
        
        // this.commandLineArguments = new ArrayList(Arrays.asList(args));
        System.out.println("Size of commandlineargs: " + commandLineArguments.size());

        for(ArgOption option : options) {
            out("parsing option %s", option);
            consumeArgument(option);
            out(" -> %s\n", Objects.nonNull(option.value) ? option.value.toString() : "null" );
        }        
        
        if(!commandLineArguments.isEmpty()) {
            throw new EXITargumentException(String.format("Unrecognized options: " + commandLineArguments.toString()));
        }
        
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for(ArgOption option : options) sb.append(option.state()).append("\n\n");
        return sb.toString();
    }
    
    private void out(String s, Object... objs) {
        System.out.printf(s, objs);
    }
    
    
    
    
    
}
