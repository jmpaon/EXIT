/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package exit.matrices;


import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * <code>SquareMatrix</code> represents 
 * a square numeric data matrix,
 * square matrix being a matrix 
 * where row and column counts are equal.
 * 
 * @author jmpaon
 */
public class SquareMatrix {
    
    /** Number of variables (and rows and columns) in this matrix */
    protected final int varCount;
    /** Matrix contents */
    protected final double[] values;
    /** Variable/row/column names */
    protected final String[] names;

    
    /**
     * Constructor for <code>SquareMatrix</code>.
     * @param varCount Number of rows, columns and variables in the matrix
     * @param names Array of row/column/variable names or labels, length must be equal to varCount
     * @param values Array of values, length must be equal to varCount^2
     */
    public SquareMatrix(int varCount, String[] names, double[] values) {
        if (varCount < 1) { throw new IllegalArgumentException("varCount cannot be smaller than 1"); }
        if(values == null) throw new NullPointerException("values array is null");
        if(names == null) throw new NullPointerException("names array is null");
        
        this.varCount = varCount;
        if(values.length/varCount != varCount) 
            throw new IllegalArgumentException(
                    String.format("values array has %d elements, while varCount^2 is %d", values.length, varCount*varCount));
        
        if(names.length != varCount)
            throw new IllegalArgumentException(
                    String.format("names array has %d elements, while varCount is %d", names.length, varCount));
        
        this.values = values;
        this.names = names;
        
    }
    
    
    /**
     * Constructor for <code>SquareMatrix</code>.
     * @param varCount Number of rows, columns and variables in the matrix
     * @param names Array of row/column/variable names or labels, length must be equal to varCount
     */
    public SquareMatrix(int varCount, String[] names) {
        this(varCount, names, new double[varCount*varCount]);
    }
    
    
    /**
     * Constructor for <code>SquareMatrix</code>
     * @param varCount Number of rows, columns and variables in the matrix
     */
    public SquareMatrix(int varCount) {
        this(varCount, createNames(varCount), new double[varCount*varCount]);
    }
    
    
    /**
     * Constructor for <code>SquareMatrix</code>
     * @param names Array of row/column/variable names or labels, length must be equal to rows and columns in <b>values</b>
     * @param values 2-dimensional array. Each column must have the same number of elements as  there are rows in the array.
     */
    public SquareMatrix(String[] names, double[][] values) {
        this(values.length, names, flattenArray(values));
    }
    
    /**
     * 
     * @param matrix 
     */
    public SquareMatrix(SquareMatrix matrix) {
        this(matrix.varCount, matrix.names.clone(), matrix.values.clone());
    }

    
    /**
     * Is a number a perfect square?
     * @param number Number to test
     * @return <b>true</b> if number is a perfect square
     */
    static protected boolean isPerfectSquare(int number) {
        double sqrt = Math.sqrt(number);
        int x = (int) sqrt;
        return (Math.pow(sqrt,2) == Math.pow(x,2));
    }
    
    
    /**
     * Returns the sum of the values in a specific row of the matrix.
     * @param row index of the row whose values are summed
     * @param absolute if <i>true</i>, sum of absolute values is returned;
     * otherwise sum of values is returned
     * @return Sum of values in row with index <b>row</b>
     */
    public double rowSum(int row, boolean absolute) {
        double sum = 0;
        for (int i = 1; i <= varCount; i++) {
            sum += absolute ? Math.abs(getValue(row, i)) : getValue(row, i);
        }
        return sum;
    }
    
    
    /**
     * Returns the sum of the values in a specific column of the matrix.
     * @param column index of the column whose values are summed
     * @param absolute if <i>true</i>, sum of absolute values is
     * returned; otherwise sum of values is returned
     * @return Sum of values in column with index <b>column</b>
     */
    public double columnSum(int column, boolean absolute) {
        double sum = 0;
        for (int i = 1; i <= varCount; i++) {
            sum += absolute ? Math.abs(getValue(i, column)) : getValue(i, column);
        }
        return sum;
    }

    
    /**
     * Returns the average of values of specific row.
     * @param row Index of the variable which values are averaged
     * @param absolute If <i>true</i> average of absolute values is returned
     * @return Average of values in row with index <b>row</b>
     */
    public double rowAverage(int row, boolean absolute) {
        return rowSum(row, absolute) / varCount;
    }
    
    
    /**
     * Returns the average of values on specific column.
     * @param column Index of the variable which values are averaged
     * @param absolute If <i>true</i> average of absolute values is returned
     * @return Average of values in column with index <b>column</b>
     * <b>column</b>
     */
    public double columnAverage(int column, boolean absolute) {
        return columnSum(column, absolute) / varCount;
    }
    
    
    /**
     * Returns the maximum value in a specific row of the matrix.
     * @param row Row index
     * @param absolute if true, maximum of absolute values is returned; else maximum of values is returned
     * @return the maximum value in row <b>row</b>
     */
    public double rowMax(int row, boolean absolute) {
        double max = absolute ? Math.abs(this.getValue(row, 1)) : this.getValue(row, 1);
        for (int i = 1; i <= varCount; i++) {
            if(absolute ? Math.abs(this.getValue(row, i)) > max : this.getValue(row, i) > max)
                max = absolute ? Math.abs(this.getValue(row, i)) : this.getValue(row, i);
        }
        return max;
    }

    
    /**
     * Returns the maximum value in a specific column of the matrix
     * @param column Column index
     * @param absolute if <i>true</i>, maximum of absolute values is returned; else maximum of values is returned
     * @return the maximum value in column <b>column</b>
     */
    public double columnMax(int column, boolean absolute) {
        double max = absolute ? Math.abs(this.getValue(1, column)) : this.getValue(1, column);
        for (int i = 1; i <= varCount; i++) {
            if(absolute ? Math.abs(this.getValue(i, column)) > max : this.getValue(i, column) > max)
                max = absolute ? Math.abs(this.getValue(i, column)) : this.getValue(i, column);
        }
        return max;
    }

    /**
     * Returns the average of values in the matrix.
     * @param absolute If <i>true</i>, average of absolute values is returned, otherwise average is returned
     * @return Average of values in the matrix
     */
    public double matrixMean(boolean absolute) {
        double sum = 0;
        for(Double val : this.values) {
            sum += absolute ? Math.abs(val) : val ;
        }
        return sum / (varCount*varCount);
    }
    
    /**
     * Returns the median of matrix values
     * @param absolute If <i>true</i> median of absolute values is returned
     * @return The median of matrix values
     */
    public double matrixMedian(boolean absolute) {
        
        double[] sortedValues = this.values.clone();
        if(absolute) {
            for(int i=0;i<sortedValues.length;i++) 
                sortedValues[i] = Math.abs(sortedValues[i]);
        }
        
        Arrays.sort(sortedValues);
        int n = sortedValues.length;
        if(n % 2 == 0) {
            return sortedValues[(n/2)] + sortedValues[(n/2-1)] / 2;
        } else {
            return sortedValues[((n-1)/2)];
        }
    }
    
    
    /**
     * @return The maximum <u>absolute</u> value in the matrix.
     */
    public double matrixMax() {
        return matrixMax(true);
    }
    
    /**
     * @param absolute If <i>true</i> maximum of absolute values is returned
     * @return The maximum value or maximum absolute value in the matrix
     */
    public double matrixMax(boolean absolute) {
        double max = absolute ? Math.abs(values[0]) : values[0];
        for(int i=1;i<values.length;i++) {
            double v = absolute ? Math.abs(values[i]) : values[i];
            if(v > max) {
                max = v;
            }
        }
        return max;
    }
    
    
    /**
     * Returns the smallest value in the matrix
     * @return The smallest value in the matrix
     */
    public double matrixMin() {
        double min = values[0];
        for(int i=1;i<values.length;i++) {
            if(values[i] < min) min = values[i];
        }
        return min;
    }
    
    
    /**
     * Creates variable names for the matrix. Variable names are
     * numbered from 1 to <b>nameCount</b>.
     * @param nameCount How many names will be generated
     * @return Array containing variable names
     */
    protected static String[] createNames(int nameCount) {
        return createNames(nameCount, "Variable ");
    }
    
    protected static String[] createNames(int nameCount, String preamble) {
        if (preamble == null) preamble = "V";
        int i  = 0;
        String[] n = new String[nameCount];
        while(i < nameCount) {
            n[i] = preamble + ++i;
        }
        return n;
    }

    
    /**
     * Returns the name of variable with index <b>varIndex</b>.
     * @param varIndex Index of variable in question
     * @return Name of variable with index <b>varIndex</b>.
     * @throws IndexOutOfBoundsException
     */
    public String getName(int varIndex) throws IndexOutOfBoundsException {
        if (! isIndexValid(varIndex)) {
        //if (varIndex < 1 || varIndex > varCount) {
            String s = String.format("No name for index [%d], varCount for the matrix is %d.", varIndex, varCount);
            throw new IndexOutOfBoundsException(s);
        }
        return names[varIndex - 1];
    }
    
    
    /**
     * Returns the name of the variable with index <b>varIndex</b>,
     * so that each name will have whitespace as much as is needed
     * to make all variable names equal in length.
     * The longest variable name will not have any whitespace.
     * @param varIndex
     * @return 
     */
    public String getNamePrint(int varIndex) {
        String varName = getName(varIndex);
        int longestNameLength = 0;
        for(String name : names) {
            longestNameLength = name.length() > longestNameLength ? name.length() : longestNameLength;
        }
        int whitespaceChars = longestNameLength - varName.length();
        StringBuilder sb = new StringBuilder();
        while(whitespaceChars>0) {
            sb.append(" ");
            whitespaceChars--;
        }
        sb.append(varName);
        return sb.toString();
    } 

    /**
     * Get a short (V1, V2, V3 ...) name for variable <i>varIndex</i>
     * @param varIndex Variable index
     * @return A short name for variable <i>varIndex</i>
     */
    public String getNameShort(int varIndex) {
        //if (varIndex < 1 || varIndex > varCount) {
        if (! isIndexValid(varIndex )) {
            String s = String.format("No name for index [%d], varCount for the matrix is %d.", varIndex, varCount);
            throw new IndexOutOfBoundsException(s);
        }
        return "V" + varIndex;
    }
    
    
    /**
     * If <b>s</b> is shorter than <b>len</b>, <b>s</b> is returned;
     * Otherwise, first <b>len</b>-3 characters of <b>exceptionMsg</b> 
     * appended with three dots are returned.
     * @param s String (name) to truncate
     * @param len Length of <b>exceptionMsg</b> after truncation.
     * @return Truncated String/name.
     */
    public static String truncateName(String s, int len) {
        return s.length()<=len ? s : s.substring(0, len-3) + "...";
    }
    
    
    /**
     * Returns the index of variable with name <b>varName</b>.
     * @param varName Name/label of variable
     * @return Index of variable with name <b>varName</b>
     */
    public int getIndex(String varName) {
        for (int i = 0; i < names.length; i++) {
            if (names[i].equals(varName)) {
                return i + 1;
            }
        }
        throw new NoSuchElementException(String.format("impact matrix doesn't have a variable with name %s", varName));
    }

    
    /**
     * Sets the variable name for variable at <i>varIndex</i>.
     * @param varIndex Index of a variable in <i>matrix</i>
     * @param varName New name for variable
     * @throws IllegalArgumentException
     * @throws IndexOutOfBoundsException
     */
    public void setName(int varIndex, String varName) throws IllegalArgumentException, IndexOutOfBoundsException, IllegalStateException  {
        
        // throw new IllegalStateException("The impact matrix is locked and cannot be modified");
        
        if (! isIndexValid(varIndex)) {
        // if (varIndex < 0 || varIndex > varCount) {
            throw new IndexOutOfBoundsException("Invalid variable index");
        }
        if (varName == null) {
            throw new IllegalArgumentException("Variable name cannot be null");
        }
        this.names[varIndex - 1] = varName;
    }
    
    /**
     * Gets a value from the matrix. 
     * @param row Row index
     * @param column Column index
     * @return The value in the matrix at index [row:column]
     * @throws IllegalArgumentException
     */
    public double getValue(int row, int column) throws IndexOutOfBoundsException {
        //if (row < 1 || row > varCount || column < 1 || column > varCount) {
        if(! (isIndexValid(row) && isIndexValid(column)  )) {
            String exceptionMsg = String.format("No value for index [%d:%d], varCount for the matrix is %d.", row, column, varCount);
            throw new IndexOutOfBoundsException(exceptionMsg);
        }
        int index = ((row - 1) * varCount) + (column - 1);
        return values[index];
    }

    /**
     * Sets a value in the matrix.
     *
     * @param row Index of variable that is the impactor variable of the
     * impact
     * @param column Index of variable that is the impacted variable of the impact
     * @param value Value/strength of the impact, between negative
     * <b>maxImpact</b> and positive <b>maxImpact</b>.
     * @throws IllegalArgumentException
     */
    public void setValue(int row, int column, double value) throws IllegalArgumentException, IndexOutOfBoundsException, IllegalStateException {

        // Test if indexes are legal
        if(! (isIndexValid(row) && isIndexValid(column)  )) {
        //if (row < 1 || row > varCount || column < 1 || column > varCount) {
            String s = String.format("Impact for index [%d:%d] cannot be set, varCount for the matrix is %d.", row, column, varCount);
            throw new IndexOutOfBoundsException(s);
        }
        
        int index = ((row - 1) * varCount) + (column - 1);
        values[index] = value;
    }

    /**
     * @return The number of variables in the <code>SquareMatrix</code>.
     */
    public int getVarCount() {
        return varCount;
    }
    
    /**
     * Returns true if <b>tester</b> returns true for any value in the matrix
     * @param tester Predicate&lt;Double&gt; 
     * @return 
     */
    final public boolean testValues(Predicate<Double> tester) {
        for(Double d : values) if(tester.test(d)) return true;
        return false;
    }
    
    final public List<Double> collectValues(Predicate<Double> tester) {
        List<Double> list = new LinkedList<>();
        for(Double d : values) if(tester.test(d)) list.add(d);
        return list;
    }
    
    public SquareMatrix transform(Function<SquareMatrix, SquareMatrix> transformer) {
        return transformer.apply(this);
    }

    /**
     * @return <b>true</b> if all values in matrix are integers, false otherwise
     */
    protected boolean allValuesAreIntegers() {
        for (int i = 0; i < values.length; i++) {
            if ( ! isInteger(values[i])) {
                return false;
            }
        }
        return true;
    }
    
    protected static boolean isInteger(double d) {
        return d == (int) d;
    }
    




    /**
     * Returns a copy of the matrix contents (the values) 
     * in a 2-dimensional array.
     * @return Matrix contents in a 2-dimensional array.
     */
    public double[][] toArray() {
        double[][] copy = new double[varCount][varCount];
        int i = 0;
        int r = 0;
        int c;
        while (i < values.length) {
            for (c = 0; c < varCount; c++, i++) {
                copy[r][c] = values[i];
            }
            r++;
        }
        return copy;
    }
    
    
    /**
     * "Flattens" a square 2-dimensional array 
     * (array with equal number of rows and columns) 
     * into a 1-dimensional array.
     * @param array Array to be flattened
     * @return Flattened array
     */
    static double[] flattenArray(double[][] array) {
        double[] flatArray = new double[array.length * array.length];
        
        int pos=0;
        int rowIndex=0;
        
        for(double[] row : array) {
            if(row.length != array.length) throw new IllegalArgumentException("array has unequal number of rows and columns");
            for (int i = 0; i < row.length; i++, pos++) {
                flatArray[pos] = array[rowIndex][i];
            }
            rowIndex++;
        }
        return flatArray;
    }
    
    
    /**
     * Sums the products of each entry in <b>row</b> and corresponding entry in <b>col</b>.
     * @param row Index of row
     * @param col Index of col
     * @return Sum of pairwise products of entries in row <b>row</b> and column <b>col</b>.
     */
    protected double multiplyEntries(int row, int col) {
        return multiplyEntries(this, this, row, col);
        
        //double result=0;
        //for (int i = 1; i <= varCount; i++) {
        //    result += getValue(row, i) * getValue(i, col);
        //}
        //return result;
    }
    
    protected static double multiplyEntries(SquareMatrix rowMatrix, SquareMatrix colMatrix, int row, int col) {
        assert rowMatrix.varCount == colMatrix.varCount : "Differently sized matrices";
        double result=0;
        for (int i = 1; i <= rowMatrix.varCount; i++) {
            result += rowMatrix.getValue(row, i) * colMatrix.getValue(i, col);
        }
        return result;        
    }

    /**
     * Multiplies the matrix by itself (resulting in power matrix).
     * @return Squared matrix
     */
    public SquareMatrix power() {
        MicmacMatrix powerMatrix = new MicmacMatrix(this);
        for (int row = 1; row <= varCount; row++) {
            for (int col = 1; col <= varCount; col++) {
                powerMatrix.setValue(row, col, this.multiplyEntries(row, col));
            }
        }
        return powerMatrix;
    }
    
    
    
    

    /**
     * Tests if values of this matrix deviate
     * from the values of <b>matrix</b>
     * at most by value of <b>maxDifference</b>.
     * Note that equality measured like this is not symmetric.
     * @param matrix matrix to compare against this one in terms of impact sizes
     * @param maxDifference The maximum relative difference allowed to still consider the matrices approximately same in terms of values
     * @return boolean
     */
    boolean equalsApproximately(SquareMatrix matrix, double maxDifference) {
        if(maxDifference <= 0) throw new IllegalArgumentException("maxDifference must be greater than 0");
        if (matrix.values.length != this.values.length) {
            throw new IllegalArgumentException("Matrices are differently sized and cannot be compared");
        }
        for (int i = 0; i < this.values.length; i++) {
            double v1 = this.values[i];
            double v2 = matrix.values[i];
            double rel = v1 > v2 ? v1 / v2 : v2 / v1;
            if ((rel - 1) > maxDifference) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns a copy of this <tt>SquareMatrix</tt>
     * @return 
     */
    public SquareMatrix copy() {
        return new SquareMatrix(this.varCount, this.names.clone(), this.values.clone());
    }
    
    /**
     * Returns a copy of this <tt>SquareMatrix</tt> with no matrix entry values
     * @return 
     */
    public SquareMatrix copyWithoutValues() {
        return new SquareMatrix(this.varCount, this.names.clone());
    }
    
    
    /**
     * Returns the length (in chars) of the longest variable name in the matrix.
     * @return int : Length of the longest variable name
     */
    protected int longestNameLength() {
        int longest = names[0].length();
        for(String s : names) if (s.length() > longest) longest = s.length();
        return longest;
    }
    
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        
        int labelWidth = 55;
        int i=0, c, n=0;
        
        
        sb.append(String.format("%"+labelWidth+"s     \t", " "));
        for(c=0; c<varCount;c++) {
            sb.append(String.format("%s\t", "V"+(c+1)));
        }
        sb.append(String.format("%n"));
        
        while( i < values.length) {
            sb.append(String.format("%"+labelWidth+"s (%s)\t", truncateName(names[n], labelWidth), ("V"+(n+1))));
            n++;
            c=0;
            while(c < varCount) {
                DecimalFormat fmt = new DecimalFormat("+###0.00;-#");
                sb.append(fmt.format(values[i])).append("\t");
                
                c++;
                i++;
            }
            sb.append(String.format("%n"));
        }
        return sb.toString();
    }

    /**
     * Tests whether a variable index is a valid index in matrix <b>matrix</b>.
     * @param index Index to be tested
     * @return true if the index is a valid index in <b>matrix</b>, false otherwise.
     */
    public boolean isIndexValid(int index) {
        return index > 0 && index <= this.getVarCount();
    }
    
    

}
