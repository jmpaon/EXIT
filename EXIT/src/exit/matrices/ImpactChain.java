/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package exit.matrices;

import exit.estimators.ImpactChainSampler;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.List;
import java.util.LinkedList;


/**
 * Instances of this class represent chains of variables in a cross-impact matrix.
 * The indices of variables are stored in <code>chainMembers</code>.
 * <code>ImpactChain</code> consists of 
 * <i>impactor</i> variable, <i>intermediary variables</i> and <i>impacted</i> variable.
 * The <code>impact</code> of the chain is the impact of <i>impactor</i>
 * on the <i>impacted</i> variable, through the chain of intermediary variables
 * between <i>impactor</i> and <i>impacted</i>. 
 * In a direct impact, there are no intermediary variables.
 * @author jmpaon
 */
public class ImpactChain implements Comparable<ImpactChain>  {
    
    /** The cross-impact matrix from whose variables the impact chain is formed from */
    public final EXITImpactMatrix matrix;
    
    /** Indices of the variables that are present in this chain */
    public final List<Integer> chainMembers;
    
    /** Number of chain members / variables in this chain */
    public final int memberCount;
    
    
    /**
     * @param matrix The cross-impact matrix from whose variables 
     * this <code>ImpactChain</code> is constructed of
     * @param chainMembers The indices of the variables in the matrix.
     * All indices must be present in <b>matrix</b>.
     */
    public ImpactChain(EXITImpactMatrix matrix, List<Integer> chainMembers) {
        if(matrix == null) throw new NullPointerException("matrix is null");
        this.matrix = matrix;
        if(chainMembers == null) {
            this.chainMembers = new LinkedList();
        } else {
            
            // Test chainMembers for duplicates
            Set<Integer> woDuplicates = new TreeSet<>(chainMembers);
            if(woDuplicates.size() != chainMembers.size()) { throw new IllegalArgumentException("duplicate items in chainMembers"); }
            
            // Test that indices in chainMembers are present in matrix of this impactChain
            for(Integer i : chainMembers) {
                if(i < 0 || i > matrix.getVarCount()) {
                    throw new IndexOutOfBoundsException("Chain member " +chainMembers.get(i)+ " not present in impact matrix");
                }
            }
            this.chainMembers = chainMembers;
        }
        
        this.memberCount = this.chainMembers.size();
    }
    
    
    /**
     * @param matrix The cross-impact matrix from whose variables 
     * this <code>ImpactChain</code> is constructed of
     * @param chainMembers The indices of the variables in the matrix.
     * All indices must be present in <b>matrix</b>.
     */
    public ImpactChain(EXITImpactMatrix matrix, int... chainMembers) {
        if(matrix == null) throw new NullPointerException("matrix is null");
        this.matrix = matrix;
        if(chainMembers == null || chainMembers.length == 0) {
            this.chainMembers = new LinkedList();
        } else {
            // Test chainMembers for duplicates
            List<Integer> list = new LinkedList<>();
            for(int i : chainMembers) {list.add(i);}
            Set<Integer> woDuplicates = new TreeSet<>(list);
            if(woDuplicates.size() != list.size()) { throw new IllegalArgumentException("duplicate items in chainMembers"); }
            
            // Test that indices in chainMembers are present in matrix of this impactChain
            for(Integer i : chainMembers) {
                if(i < 0 || i > matrix.getVarCount()) {
                    throw new IndexOutOfBoundsException(String.format("Chain member %d not present in impact matrix", i));
                }
            }
            this.chainMembers = list;
        }
        this.memberCount = this.chainMembers.size();
    }
    
    
    /**
     * Returns an impact chain where
     * variable with index <b>impactorIndex</b> is the impactor,
     * variable with index <b>impactedIndex</b> is the impacted,
     * and totalLength is <b>totalLength</b>.
     * The number of randomly picked variables in the chain
     * will therefore be <u><b>totalLength</b>-2</u>.
     * @param matrix The <tt>EXITImpactMatrix</tt> the random chain derives from
     * @param impactorIndex Index of impactor variable of the returned chain
     * @param impactedIndex Index of impacted variable of the returned chain
     * @param totalLength Total totalLength of the returned chain
     * @return Impact chain with randomly picked <u>intermediary</u> variables and defined <u>impactor</u> and <u>impacted</u> variables.
     */
    public static ImpactChain randomChain(EXITImpactMatrix matrix, int impactorIndex, int impactedIndex, int totalLength) {
        assert matrix.isIndexValid(impactorIndex);
        assert matrix.isIndexValid(impactedIndex);
        assert totalLength > 1 : "Chain length > 1 required; length is " + totalLength;
        assert totalLength <= matrix.getVarCount() : "length is " + totalLength;
        totalLength -= 2;
        List<Integer> chainMembers = new ArrayList<>();
        List<Integer> l = intermediaryIndices(matrix, impactorIndex, impactedIndex);
        int i = 0;
        Collections.shuffle(l);
        chainMembers.add(impactorIndex);
        while (totalLength-- > 0) {
            chainMembers.add(l.get(i++));
        }
        chainMembers.add(impactedIndex);
        return new ImpactChain(matrix, chainMembers);
    }
    
    /**
     * Returns a new <tt>ImpactChain</tt> where impactor and impacted variables 
     * are the same as in this chain, total totalLength is equal to <b>totalLength</b>
     * and the intermediary variables are randomly picked variables from 
     * yet available variables in the matrix
     * @param totalLength Total length (including impactor and impacted variables) of the chain
     * @return ImpactChain
     */
    public ImpactChain randomChain(int totalLength) {
        assert totalLength > 1 && totalLength <= this.memberCount : String.format("Requested length is %d, creating chain is of length %d", totalLength, this.memberCount);
        return ImpactChain.randomChain(matrix, this.impactorIndex(), this.impactedIndex(), totalLength);
    }
    
    
    /**
     * Returns a list of possible indices of intermediary variables for chain generation for <b>matrix</b>.
     * @param impactorIndex Index of impactor variable of the chain
     * @param impactedIndex Index of impacted variable of the chain
     * @return List&lt;Integer&gt; : possible intermediary indices between impactor and impacted 
     */    
    static List<Integer> intermediaryIndices(EXITImpactMatrix matrix, int impactorIndex, int impactedIndex) {
        assert matrix.isIndexValid(impactorIndex) && matrix.isIndexValid(impactedIndex);
        return new ArrayList<>(new ImpactChain(matrix, impactorIndex, impactedIndex).expandableBy());
    }
    
    
    /**
     * Combines two impact chains so that 
     * members of <b>chain</b> will be appended to the end of <b>this</b> chain.
     * Combined chains cannot contain same chain members.
     * @see ImpactChain#isCombinableWith(exit.ImpactChain)
     * @param chain Chain to append
     * @return Combined <code>ImpactChain</code>
     */
    public ImpactChain combineWith(ImpactChain chain) {
        if(! this.matrix.equals(chain.matrix)) throw new IllegalArgumentException("Combined chains refer to different matrices");
        for(Integer i : chain.chainMembers) {
            if(this.chainMembers.contains(i)) throw new IllegalArgumentException("Duplicate indices in combined chains");
        }
        List<Integer> combinedMembers = new LinkedList<>();
        combinedMembers.addAll(this.chainMembers);
        combinedMembers.addAll(chain.chainMembers);
        return new ImpactChain(this.matrix, combinedMembers);
    }
    
    
    /**
     * Tests whether two ImpactChains can be combined.
     * Combination is possible if the chains are formed
     * from the same impact matrix and do not contain same variables.
     * @param chain Chain whose combinability with this chain is tested
     * @return <i>true</i> if chains can be combined, false otherwise
     */
    public boolean isCombinableWith(ImpactChain chain) {
        if(! this.matrix.equals(chain.matrix)) return false;
        for(Integer i : chain.chainMembers) {
            if(this.chainMembers.contains(i)) return false;
        }
        return true;
    }
    
    
    
    /**
     * @return The index of the last (impacted) variable in the chain
     */
    public int impactedIndex() {
        return chainMembers.get(memberCount-1);
    }
    
    /**
     * @return The index of the first (impactor) variable in the chain
     */
    public int impactorIndex() {
        return chainMembers.get(0);
    }
    
    
    /**
     * @return The name of the last (impacted) variable in the chain
     */
    public String impactedName() {
        return matrix.getName(chainMembers.get(memberCount-1));
    }
    
    
    /**
     * @return The name of the first (impactor) variable in the chain
     */
    public String impactorName() {
        return matrix.getName(chainMembers.get(0));
    }
    
    
    /**
     * Returns the impact of the first variable of the chain (impactor) 
     * on the last variable of the chain (impacted)
     * through the entire chain.
     * If chain has only one variable, <code>impact</code> is 1.
     * If chain has two variables, it represents a direct impact from
     * first variable to the last (second) variable and the impact is equal
     * to the direct impact expressed in the <code>matrix</code>.
     * For longer chains, the chain represents impact of <i>impactor</i> on <i>impacted</i>
     * through all the intermediary variables.
     * @return The effect of first variable on the last variable, through the intermediary variables in the chain
     */
    public double impact() {
        return impact(chainMembers);
    }
    
    
    /**
     * Calculates the impact of the impactor on the impacted through the 
     * intermediary variables in <code>chain</code>.
     * @param chain List containing indices of the variables in the chain whose impact is calculated
     * @return The impact of first variable in the chain on the last variable of the chain trough the chain
     * @see ImpactChain#impact()
     */
    private double impact(List<Integer> chain)  {
        if(chain == null) { throw new NullPointerException("chain argument is null"); }
        if(chain.isEmpty()) return 1;
        if(chain.size()==1) return 1;
        return (matrix.getValue(chain.get(0),chain.get(1))/matrix.getMaxImpact()) * impact(chain.subList(1, chain.size()));
    }
    
    
    /**
     * Returns a <code>Set</code> of <code>ImpactChain</code>s,
     * which are one variable longer than this chain;
     * the chains are continued by variables that are not present in this chain
     * but are present in the matrix.
     * There will be as many continued chains in the returned set as there
     * are variables in the matrix that are not yet present in the chain.
     * @return Set of <code>ImpactChain</code>s.
     */
    public Set<ImpactChain> continuedByOne()  {
        Set<ImpactChain> continued = new TreeSet<>();
        Set<Integer> notIncluded = expandableBy();
        
        for(Integer i : notIncluded) {
            List cm = new LinkedList(chainMembers);
            cm.add(i);
            ImpactChain c = new ImpactChain(this.matrix, cm);
            continued.add(c);
        }
        
        return continued;
        
    }
    
    
    /**
     * Returns a <code>Set</code> of <code>ImpactChain</code>s,
     * that are one variable longer than this chain
     * and where the first and last variables are the same as in this chain.
     * The method is used for generating the <code>ImpactChain</code>s
     * that represents the different direct and indirect impacts between
     * the impactor variable and impacted variable in this chain.
     * The new variables are introduced to the second to last place of the chain,
     * before the impacted variable.
     * @return <code>Set</code> of <code>ImpactChain</code>s
     * that have been expanded to be longer than this chain by one variable.
     */
    public Set<ImpactChain> continuedByOneIntermediary() {
        
        if(chainMembers.size() < 2) {
            return continuedByOne();
        }
        
        Set<ImpactChain> continued = new TreeSet<>();
        Set<Integer> notIncluded = expandableBy();
        for(Integer i : notIncluded) {
            List cm = new LinkedList(chainMembers);
            cm.add(cm.size()-1, i);
            ImpactChain c = new ImpactChain(this.matrix, cm);
            continued.add(c);
        }
        
        return continued;
    }
    
 
    /**
     * Generates all impact chains expanded from this impact chain
     * that are also high-impact 
     * (having higher or equal <code>impact</code> 
     * than <code>impactTreshold</code>).
     * This method generates chains by adding variables to the end of the chain,
     * as impacted variable.
     * @param impactTreshold The minimum impact a chain should have to be included in the returned chain;
     * must be greater than 0 and smaller than 1
     * @return All impact chains expanded from this chain that have higher <code>impact</code> than threshold.
     */
    public Set<ImpactChain> highImpactChains(double impactTreshold)  {
        if(impactTreshold <=0 || impactTreshold >=1) throw new IllegalArgumentException("impactTreshold should be in range ]0..1[");
        
        Set<ImpactChain> chains = new TreeSet<>();
        
        if(Math.abs(this.impact()) >= impactTreshold) {
            
            if(this.memberCount > 0) chains.add(this);
            
            Set<ImpactChain> immediateExpansions = this.continuedByOne();
            for(ImpactChain ic : immediateExpansions) {
                chains.addAll(ic.highImpactChains(impactTreshold));
            }
        }
        
        return chains;
    }    
    

    /**
     * @return <b>true</b> if this chain can be expanded, false otherwise
     */
    public boolean hasExpansion() {
        return !expandableBy().isEmpty();
    }
    
    
    /**
     * Returns a <code>Set</code> containing 
     * the variable indices in <code>matrix</code> 
     * that are not present in this <code>ImpactChain</code>
     * @return <code>Set</code> with variable indices not included in this <code>ImpactChain</code>
     */
    private Set<Integer> expandableBy() {
        Set<Integer> notInThisChain = new TreeSet<>();
        for(int i = 1; i <= matrix.getVarCount() ; i++ ) {
            if(! chainMembers.contains(i)) {
                notInThisChain.add(i);
            }
        }
        return notInThisChain;
    }
    
    
    /**
     * @return String representation of the impact chain, using long variable names.
     */
    @Override
    public String toString() {
        String s = String.format("  %+2.2f : ", ImpactChain.this.impact());
        
        for(Integer i : chainMembers) {
            s += matrix.getName(i);
            if( ! i.equals(chainMembers.get(chainMembers.size()-1))) { s += " -> "; }
        }
        
        return s;
    }
    
    
    /**
     * @return String representation of the impact chain, using short variable names.
     */
    public String toStringShort() {
        String s = String.format(" %+2.2f : ", ImpactChain.this.impact());
        
        for(Integer i : chainMembers) {
            s += matrix.getNameShort(i);
            if( ! i.equals(chainMembers.get(chainMembers.size()-1))) { s += " -> "; }
        }
        
        return s;        
    }
    
    
    /**
     * Compares two <code>ImpactChain</code>s.
     * Shorter impact chains are ordered before longer impact chains.
     * Equal-length impact chains are ordered by the member indices.
     * @param ic <code>ImpactChain</code> to compare against.
     * @return -1 if <b>ic</b> is greater, 0 if equal, 1 if smaller.
     */
    @Override
    public int compareTo(ImpactChain ic) {
        if(ic == null) return 1;
        if(this.memberCount > ic.memberCount) return 1;
        if(this.memberCount < ic.memberCount) return -1;
        Iterator<Integer> i_this = this.chainMembers.iterator(), i_ic = ic.chainMembers.iterator();
        while(i_this.hasNext() && i_ic.hasNext()) {
            int i1 = i_this.next(), i2 = i_ic.next();
            if(i1 > i2) return  1;
            if(i1 < i2) return -1;
        }
        return 0;
    }
    
    
    public int compareTo(double treshold) {
        if(Math.abs(this.impact()) > treshold) return  1;
        if(Math.abs(this.impact()) < treshold) return -1;
        return 0;
    }


}
