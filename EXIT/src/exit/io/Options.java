/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package exit.io;

import exit.EXIT;
import exit.EXITexception;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jmpaon
 */
public class Options {
    
    public List<String> commandLineArguments;
    public final List<Option> options;
    private String usageText;
    
    
    public Options() {
        this.options = new ArrayList<>();
    }
    
    /**
     * Adds an option 
     * @param option to be added
     */
    public void addOption(Option option) {
        this.options.add(option);
    }
    
    /**
     * Adds an arbitrary number of options
     * @param options to be added
     */
    public void addOption(Option... options) {
        for(Option option : options) Options.this.addOption(option);
    }
    
    /**
     * Returns true if option with id <b>id</b> has a value
     * @param id id of an <tt>Option</tt>
     * @return 
     */
    public boolean hasValue(String id) {
        if(Objects.nonNull(query(id))) return Objects.nonNull(query(id).value);
        return false;
    }
    
    
    /**
     * Returns the </tt>Option</tt> with id <b>id</b>.
     * @param id id of the option queried
     * @return Option 
     */
    public Option query(String id) {
        for(Option o : options) if(o.id.equals(id)) return o;
        throw new EXITexception("Option with id %s queried but not found", id);
    }
    
    public String queryString(String id) throws EXITargumentException {
        Option o = query(id);
        try {return (String)o.get();} 
        catch(ClassCastException e) {throw new EXITargumentException("Value of %s is not a String", id);}
        catch(Exception e)          {throw new EXITargumentException("Error in querying %s as String", id);}
    }
    
    public Double queryDouble(String id) throws EXITargumentException {
        Option o = query(id);
        try {return (Double)o.get();} 
        catch(ClassCastException e) {throw new EXITargumentException("Value of %s is not a double", id);}
        catch(Exception e)          {throw new EXITargumentException("Error in querying %s as double", id);}
    }
    
    public Integer queryInt(String id) throws EXITargumentException {
        Option o = query(id);
        try {return (Integer)o.get();} 
        catch(ClassCastException e) {throw new EXITargumentException("Value of %s is not an int", id);}
        catch(Exception e) {throw new EXITargumentException("Error in querying %s as int", id);}
    }
    
    public Character queryChar(String id) throws EXITargumentException {
        Option o = query(id);
        try {return (Character)o.get();}
        catch(ClassCastException e) {throw new EXITargumentException("Value of %s is not a char", id);}
        catch(Exception e)          {throw new EXITargumentException("Error in querying %s as char", id);}        
    }
    
    /**
     * Returns the Option value as <tt>String</tt>.
     * <table><tr><th></th><th>Valued</th><th>Non-valued</th></tr><tr><td>Present</td><td>value in String</td><td>"true"</td></tr><tr><td>Not present</td><td>null</td><td>null</td></tr></table>
     * Throws exception for valued options where the value doesn't look like a value.
     * @param option
     * @return 
     */
    private String readValue(Option option) throws EXITargumentException {
        /* value or "true" if found, null if not */
        assert option != null;
        
        /* Options not present */
        if(!lineContains(option)) return null;
        
        /* Non-valued present options return "true" */
        if(!option.isValued) return "true";
        
        
        int indexOfOpt = commandLineArguments.indexOf(option.id);
        int indexOfVal = indexOfOpt + 1;
        
        if(indexOfVal >= commandLineArguments.size())
            throw new EXITargumentException("No value for argument %s", option.id);
        
        if(isFlag(commandLineArguments.get(indexOfVal)))
            throw new EXITargumentException("Invalid value (%s) for argument %s", commandLineArguments.get(indexOfVal), option.id);

        try {
            return commandLineArguments.get(indexOfVal);
        } 
        catch(IndexOutOfBoundsException e) {throw new EXITargumentException("No value for argument %s", option.id);}
        catch(Exception e) { throw new EXITargumentException
            ("Error reading argument %s from command line %s", option.id, commandLineArguments);}
        
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
    
    private void removeArg(Option option) {
        assert option != null;
        int removeIndex = commandLineArguments.indexOf(option.id);
        if(removeIndex == -1) return;
        commandLineArguments.remove(removeIndex);
        if(option.isValued) commandLineArguments.remove(removeIndex);
    }
    

    
    private boolean lineContains(String id)     { return commandLineArguments.contains(id); }
    private boolean lineContains(Option opt) { return lineContains(opt.id); }
    
    private void consume(Option option) {
        /* read & delete */
        option.readIn(readValue(option));
        removeArg(option);
    }
    
    /**
     * Parses the command line arguments into this <tt>Options</tt>.
     * @param args String[] : the unprocessed command line arguments 
     * @throws EXITargumentException 
     */
    public void parse(String[] args) throws EXITargumentException {
        
        this.commandLineArguments = new ArrayList(Arrays.asList(args));
        
        for(Option option : options) {
            consume(option);
        }        
        
        if(!commandLineArguments.isEmpty()) {
            throw new EXITargumentException("Unrecognized options: %s", commandLineArguments.toString());
        }
    }
    
    public void setUsageText(String text) {
        assert text != null;
        this.usageText = text;
    }
    
    /**
     * Returns a String with information how the application should be called 
     * from the command line.
     * @return String : Usage information text
     */
    public String usage() {
        return usageText + "\n" + availableOptions();
    }
    
    
    public String availableOptions() {
        StringBuilder sb = new StringBuilder("Available options:\n");
        for(Option o : options) {
            String id       = o.id;
            String required = o.isRequired ? "Required" : "Optional";
            String valued   = o.isValued   ? "Requires value" : "Flag";
            String name     = o.name != null ? o.name : "(N/A)" ;
            String optionDescription = String.format("%s (%s, %s) : %s\n", id, required, valued, name);
            sb.append(optionDescription);
        }
        return sb.toString();
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for(Option option : options) sb.append(option.state()).append("\n\n");
        return sb.toString();
    }
    
    private void out(String s, Object... objs) {
        System.out.printf(s, objs);
    }
    
    
    
    
    
}
