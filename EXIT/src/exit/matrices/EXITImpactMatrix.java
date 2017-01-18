/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package exit.matrices;



import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;



/**
 * <p><code>EXITImpactMatrix</code> represents 
 * a set of impacts variables have on each other 
 * in EXIT cross-impact analysis.
 * Impacts are usually integers, ranging from 
 * negative <b>maxImpact</b> to positive <b>maxImpact</b>.
 * Negative impact of variable X on variable Y represents
 * probability-decreasing effect of X on Y; 
 * Positive impact of variable X on variable Y represents
 * probability-increasing effect of X on Y.</p>
 * 
 * <p>Cross-impact matrix can be used both for representing 
 * the matrix of direct impact values
 * that are inputs for the EXIT calculation 
 * and the matrix of summed direct and indirect values 
 * that are the result of the EXIT calculation.
 * It is also used for presenting the many transformations 
 * that can be performed on
 * both input matrices and summed matrices,
 * to facilitate interpretation and analysis of the impact matrices.
 * These transformations include
 * <ul>
 *  <li>scaling the matrix {@link CrossImpactMatrix#scale(double) }</li>
 *  <li>rounding the matrix {@link CrossImpactMatrix#round(int) }</li>
 *  <li>deriving an importance matrix {@link CrossImpactMatrix#newImportanceMatrix() }</li>
 *  <li>deriving a difference matrix {@link CrossImpactMatrix#differenceMatrix(exit.CrossImpactMatrix) }</li>
 * </ul>
 * </p>
 * @author jmpaon
 * @version 0.9
 */
public final class EXITImpactMatrix extends CrossImpactMatrix {
    
    /** The maximum absolute value allowed in this matrix */
    private double maxImpact;
    
    /**
     * Constructor for <code>CrossImpactMatrix</code>.
     * @param maxImpact The maximum value allowed in the matrix
     * Minimum allowed value is negative <b>maxImpact</b> and maximum <b>maxImpact</b>.
     * <b>maxImpact</b> must be greater than 0.
     * @param varCount The number of variables in the matrix; 
     * will be also the number of rows and the number of columns.
     * @param onlyIntegers <b>true</b> if matrix contains only integers
     * @param names <code>String</code> array of variable names
     * @param impacts array containing matrix contents
     */
    public EXITImpactMatrix(double maxImpact, int varCount, boolean onlyIntegers, String[] names, double[] impacts) {
        super(varCount, names, impacts, onlyIntegers);
        
        if(maxImpact <= 0) {throw new IllegalArgumentException("maxImpact must be greater than 0");}
        if(varCount < 2) {throw new IllegalArgumentException("Cross-impact matrix must have at least 2 variables");}
        
        this.maxImpact = maxImpact;
    }
    
    
    /**
     * Constructor for <code>CrossImpactMatrix</code>.
     * @param maxImpact The maximum value allowed in the matrix
     * Minimum allowed value is negative <b>maxImpact</b> and maximum <b>maxImpact</b>.
     * <b>maxImpact</b> must be greater than 0.
     * @param varCount The number of variables in the matrix; 
     * will be also the number of rows and the number of columns.
     * @param onlyIntegers <b>true</b> if matrix contains only integers
     * @param names <code>String</code> array of variable names
     */
    public EXITImpactMatrix(double maxImpact, int varCount, boolean onlyIntegers, String[] names)  {
        this(maxImpact, varCount, onlyIntegers, names, new double[varCount*varCount]);
    }


    /**
     * Constructor for <code>CrossImpactMatrix</code>.
     * @param maxImpact The maximum value allowed in the matrix
     * Minimum allowed value is <b>-maxImpact</b> and maximum <b>maxImpact</b>.
     * @param varCount The number of variables in the matrix; 
     * @param onlyIntegers <b>true</b> if matrix contains only integers
     * @see CrossImpactMatrix#CrossImpactMatrix(double, int, boolean, java.lang.String[], double[]) 
     */
    public EXITImpactMatrix(double maxImpact, int varCount, boolean onlyIntegers) {
        this(maxImpact, varCount, onlyIntegers, createNames(varCount));
    }
    
    
    /**
     * Constructor for <code>CrossImpactMatrix</code>.
     * @param maxImpact The maximum value allowed in the matrix
     * Minimum allowed value is <b>-maxImpact</b> and maximum <b>maxImpact</b>.
     * <b>maxImpact</b> must be greater than 0.
     * @param varCount The number of variables in the matrix; 
     * will be also the number of rows and the number of columns.
     */
    public EXITImpactMatrix(double maxImpact, int varCount) {
        this(maxImpact, varCount, true);
    }
    
    
    /**
     * Creates a <tt>CrossImpactMatrix</tt> from a <tt>SquareMatrix</tt>.
     * @param matrix <tt>SquareMatrix</tt> that is a valid <tt>EXITImpactMatrix</tt>
     */
    public EXITImpactMatrix(SquareMatrix matrix) {
        this(matrix, matrix.matrixMax());
    }
    
    
    /**
     * Creates a <tt>CrossImpactMatrix</tt> from a <tt>SquareMatrix</tt>.
     * @param matrix <tt>SquareMatrix</tt> that is a valid <tt>EXITImpactMatrix</tt>
     * @param maxImpact The maximum value allowed in the matrix
     */
    public EXITImpactMatrix(SquareMatrix matrix, double maxImpact) {
        this(
                maxImpact, 
                matrix.varCount, 
                matrix.allValuesAreIntegers() && SquareMatrix.isInteger(maxImpact), 
                matrix.names.clone(), 
                matrix.values.clone());
        
        for(int i=1;i<=this.varCount;i++) {
            assert this.getValue(i, i) == 0: 
                    "SquareMatrix "+matrix+" is not valid EXITImpactMatrix. Impact of variable on itself not allowed";
        }
        
        for(double d : this.values) {
            assert d <= this.maxImpact;
        }
    }
    
    
    /**
     * Calculates the relative impact of all possible impact chains 
     * and returns a new <code>EXITImpactMatrix</code> that contains
     * the summed direct and indirect values between the variables.
     * @return CrossImpactMatrix : summed direct and indirect impacts between variables
     */
    public CrossImpactMatrix computeSummedImpactMatrix() {
        CrossImpactMatrix result = new CrossImpactMatrix(new SquareMatrix(this).flush());
        for(int impactor=1;impactor<=varCount;impactor++) {
            for(int impacted=1;impacted<=varCount;impacted++) {
                if (impactor == impacted) continue;
                ImpactChain chain = new ImpactChain(this, impactor, impacted);
                result.setValue(impactor, impacted, computeExpansions(chain));
            }
        }
        return result;
    }
    
    private double computeExpansions(ImpactChain chain) {
        double sum = chain.impact();
        for(ImpactChain expansion : chain.continuedByOneIntermediary()) {
            sum += computeExpansions(expansion);
        }
        return sum;
    }
    
    
    /**
     * Returns a String with information about 
     * the count or approximate count of possible impact chains
     * for this matrix.
     * Count is exact when <i>varCount</i> is smaller than 15
     * and approximate when <i>varCount</i> is greater.
     * @return String for printing out information about count of possible chains
     * in the matrix
     */
    public String chainCount_approximate() {
        int n = varCount;
        double chainCount = chainCount() ;
        
        if(varCount < 15) {
            return new BigDecimal(chainCount).toBigInteger().toString();
        } else {
            int exp = 0;
            while(chainCount >= 10) {
                chainCount /= 10;
                exp ++ ;
            }
            return String.format("approximately %1.2f x 10^%d", chainCount, exp);
        }
    }
    
    
    /**
     * Returns the approximate number of intermediary chains of length <b>length</b>
     * between two variables in a cross-impact system of <b>varCount</b> variables.
     * @param varCount The total number of variables in the cross-impact system
     * @param length The length of the chains whose count is returned (maximum value is <b>varCount</b>-2)
     * @return double : number of possible chains between 
     */
    public static double chainCount_intermediary(int varCount, int length) {
        assert !(length > varCount-2) : String.format("Intermediary chains of length %d not possible in a cross-impact system of %d variables", length, varCount);
        double a = factorial(varCount-2);
        double b = factorial((varCount-2) - length);
        return a / b;
    }


    /**
     * Returns the approximate number of chains (of any length) 
     * in a cross-impact system of <b>varCount</b> variables.
     * @param varCount The total number of variables in the cross-impact system
     * @return double : number of possible chains
     */
    public static double chainCount(int varCount) {
        int n = 0;
        double count = 0;
        while(n <= varCount-2) {
            count += (factorial(varCount) / factorial(n));
            n++;
        }
        return count;        
    }
    
    /**
     * Calculates the approximate number of possible impact chains
     * that can be formed from this cross-impact matrix.
     * Impact chains must have a varCount of at least 2 to be included.
     * Chains of only one variable are not real impact chains,
     * so they aren't included in the count.
     * @return The number of possible impact chains in this matrix.
     */
    public double chainCount() {
        return EXITImpactMatrix.this.chainCount(this.varCount);
    }
    
    /**
     * Returns the factorial of <i>n</i>.
     * @param n 
     * @return Factorial of <i>n</i>.
     */
    public static double factorial(int n) {
        if(n == 1 || n == 0) return 1;
        return n * factorial(n-1);
    }
    
    
    /**
     * Sets an impact value for a variable pair (impactor-impacted pair).
     * @param impactor Index of impactor variable
     * @param impacted Index of impacted variable
     * @param value New value for impact
     */
    @Override
    public void setValue(int impactor, int impacted, double value) {
        
        // Absolute value of impact cannot be greater than maxImpact
        if (maxImpact < Math.abs(value)) {
            throw new IllegalArgumentException(String.format("Impact value from %s to %s  (%2.2f) is greater than the defined maximum value %2.2f", 
                    this.getNameShort(impactor), 
                    this.getNameShort(impacted), 
                    value, 
                    maxImpact));
        }
        
        super.setValue(impactor, impacted, value);
    }
    
    
    
    
    /**
     * @return The defined maximum value for values in this matrix.
     */
    public double getMaxImpact() {
        return maxImpact;
    }
    
    
    
    
    
    
    /**
     * Tests equality of this <code>EXITImpactMatrix</code>
     * with another based on {@link CrossImpactMatrix#hashCode()}.
     * @param impactMatrix <code>EXITImpactMatrix</code> to be tested for equality
     * @return 
     */
    @Override
    public boolean equals(Object impactMatrix){
        if(! (impactMatrix instanceof EXITImpactMatrix)) return false;
        return this.hashCode() == impactMatrix.hashCode();
    }
    
    /**
     * Returns hashcode based on varCount, maxImpact, values array and names array.
     * @return Hashcode
     */
    @Override
    public int hashCode() {
        int hash = 3;
        hash = 29 * hash + (int) (Double.doubleToLongBits(this.maxImpact) ^ (Double.doubleToLongBits(this.maxImpact) >>> 32));
        hash = 29 * hash + this.varCount;
        hash = 29 * hash + Arrays.hashCode(this.values);
        hash = 29 * hash + Arrays.deepHashCode(this.names);
        return hash;
    }

    @Override
    public EXITImpactMatrix copy() {
        return new EXITImpactMatrix(maxImpact, varCount, onlyIntegers, this.names.clone(), this.values.clone());
    }
    
    @Override
    public EXITImpactMatrix flush() {
        return new EXITImpactMatrix(maxImpact, varCount, onlyIntegers, names.clone());
    }

    
}
