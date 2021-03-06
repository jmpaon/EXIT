/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package exit.matrices;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.DoubleFunction;
import java.util.function.DoubleSupplier;

/**
 *
 * @author juha
 */
public class CrossImpactMatrix extends SquareMatrix{

    /** Are only integers allowed as matrix values? */
    protected final boolean onlyIntegers;
    
    /** Is the matrix locked? If locked, matrix contents cannot be changed */
    private boolean isLocked;
    
    /**
     * 
     */
    public enum InfluenceDependenceClass {
        STABLE,   /* Low  influence, low dependence */
        REACTIVE, /* Low influence, high dependence */
        DRIVER,   /* High influence, low dependence */
        CRITICAL;  /* High influence,high dependence */
        
        @Override
        public String toString() {
            switch(this) {
                case CRITICAL: return "Critical"; 
                case REACTIVE: return "Reactive"; 
                case DRIVER:   return "Driver";   
                case STABLE:   return "Stable";   
            }
            throw new EnumConstantNotPresentException(this.getClass(), this.name());
        }
         
    }
    
    public enum Orientation {
        INFLUENCE,
        DEPENDENCE;
        
        @Override
        public String toString() {
            switch(this) {
                case INFLUENCE:  return "Influence";  
                case DEPENDENCE: return "Dependence"; 
            }
            throw new EnumConstantNotPresentException(this.getClass(), this.name());
        }
    }

    public CrossImpactMatrix(int varCount, String[] names, double[] values, boolean onlyIntegers) {
        super(varCount, names, values);
        this.onlyIntegers = onlyIntegers;
        if(onlyIntegers) {
            for(double d : values) assert isInteger(d);
        }
    }

    public CrossImpactMatrix(int varCount, String[] names, boolean onlyIntegers) {
        this(varCount, names, new double[varCount*varCount], onlyIntegers);
    }

    public CrossImpactMatrix(int varCount, boolean onlyIntegers) {
        this(varCount, createNames(varCount), new double[varCount*varCount], onlyIntegers);
    }

    public CrossImpactMatrix(int varCount, String[] names) {
        this(varCount, names, new double[varCount*varCount], false);
    }

    public CrossImpactMatrix(int varCount) {
        this(varCount, createNames(varCount), new double[varCount*varCount], false);
    }

    public CrossImpactMatrix(String[] names, double[][] values, boolean onlyIntegers) {
        this(values.length, names, flattenArray(values), onlyIntegers);
    }
    
    public CrossImpactMatrix(SquareMatrix matrix) {
        this(matrix.varCount, matrix.names, matrix.values, false);
    }
    
    public CrossImpactMatrix(CrossImpactMatrix matrix) {
        this(matrix.varCount, matrix.names, matrix.values, matrix.onlyIntegers);
    }
    
    
    
    @Override
    public CrossImpactMatrix copy() {
        return new CrossImpactMatrix(this);
    }
    
    
    
    /**
     * Sets an impact value in an entry of the <tt>CrossImpactMatrix</tt>.
     * Non-zero values cannot be set to any matrix entry (i,j) in case i==j.
     * @param impactor Index of the impactor variable (matrix row index)
     * @param impacted Index of the impacted variable (matrix column index)
     * @param value Value of the matrix entry
     */
    @Override
    public void setValue(int impactor, int impacted, double value) {
        
        /* Cannot change locked matrix */
        assert !isLocked() : "Attempt to change a locked CrossImpactMatrix";
        
        /* Variables cannot have an impact on themselves */
        if(impactor == impacted && value != 0) throw new IllegalArgumentException
        (String.format("Variable ('%s') cannot have impact on itself in a cross-impact matrix.", this.getName(impactor)));
        
        super.setValue(impactor, impacted, value);
    }
    

    /**
     * Returns true if <code>SquareMatrix</code> is locked, false otherwise.
     * SquareMatrix being locked means
     * that impact values cannot be changed anymore.
     * @return <i>true</i> if matrix is locked, <i>false</i> otherwise.
     */
    public boolean isLocked() {
        return isLocked;
    }

    
    /**
     * Locks the matrix so that contents cannot be changed.
     */
    public void lock() {
        this.isLocked = true;
    }
    
    
    /**
     * Returns an importanceValue matrix for the impact matrix.
     * Importance for each impactor-impacted pair or matrix entry
     * is calculated by dividing the impact
     * by the sum of the absolute impacts on the impacted variable.
     * @return Importance matrix derived from this <code>EXITImpactMatrix</code>
     */    
    public CrossImpactMatrix importanceMatrix() {
        
        CrossImpactMatrix importance = new CrossImpactMatrix(this);
        for (int impacted = 1; impacted <= this.varCount; impacted++) {
            double columnSum = this.columnSum(impacted, true);
            for (int impactor = 1; impactor <= this.varCount; impactor++) {
                double shareOfAbsoluteSum = columnSum != 0 ? this.getValue(impactor, impacted) / columnSum : 0;
                double absShare = Math.abs(shareOfAbsoluteSum);
                double importanceValue = shareOfAbsoluteSum < 0 ? -absShare : absShare;
                importance.setValue(impactor, impacted, importanceValue);
            }
        }
        return importance;
    }
    
    
    /**
     * Returns a matrix where values of <b>subtractMatrix</b> have
     * been subtracted from values of this matrix.
     * @param subtractMatrix <tt>SquareMatrix</tt> whose values are subtracted from values of this matrix
     * @return Difference matrix
     */
    public CrossImpactMatrix differenceMatrix(SquareMatrix subtractMatrix) {
        assert this.varCount == subtractMatrix.varCount : "Comparison matrix is of different size";
        boolean bothMatricesIntegral = this.allValuesAreIntegers() && subtractMatrix.allValuesAreIntegers();
        CrossImpactMatrix differenceMatrix = new CrossImpactMatrix(this.varCount, this.names, this.values, bothMatricesIntegral);
        for (int i = 0; i < differenceMatrix.values.length; i++) {
            differenceMatrix.values[i] -= subtractMatrix.values[i];
        }
        return differenceMatrix;
    }
    
    /**
     * Booleanizes (see {@link CrossImpactMatrix#booleanize(double)}) 
     * using 1 as the threshold value
     * @return 
     */
    public CrossImpactMatrix booleanize() {
        return this.booleanize(0);
    }
    
    /**
     * Booleanizes the cross-impact matrix:
     * the transformed matrix will have only values 0 and 1, 0 for each value of the original matrix
     * that is less than or equal to <b>threshold</b> and 1 for each value that is greater than threshold.
     * The transformed matrix shows the presence of an impact of some strength and direction, 
     * the required strength defined by the threshold value.
     * @param threshold Threshold value for booleanization
     * @return Booleanized matrix
     */
    public CrossImpactMatrix booleanize(double threshold) {
        assert threshold > 0 : "Threshold value is less than or equal to 0";
        double[] booleanizedValues = this.values.clone();
        for(int i=0;i<booleanizedValues.length;i++) {
            booleanizedValues[i] = Math.abs(booleanizedValues[i]) > threshold ? 1 : 0;
        }
        return new CrossImpactMatrix(varCount, names.clone(), booleanizedValues, true);
    }
    
    
    
    /**
     * Creates a Normalized copy of this matrix 
     * by dividing each impact value
     * by the average distance of values from zero 
     * (or the mean of absolute values).
     * @return New <tt>CrossImpactMatrix</tt> based on this matrix, 
     * values normalized to unit of mean of absolute values of original.
     */
    public CrossImpactMatrix normalize() {
        double[] normalizedValues = this.values.clone();
        double averageDistanceFromZero = this.matrixMean(true);
        for (int i=0; i < normalizedValues.length; i++) {
            normalizedValues[i] /= averageDistanceFromZero;
        }
        CrossImpactMatrix normalized = new CrossImpactMatrix(this.varCount, this.names.clone(), normalizedValues, false);
        return normalized;
    }
    
    
    /**
     * Creates a normalized copy of this matrix.
     * Normalization is performed by dividing each matrix entry value
     * by <b>normalizationValue</b>, which can be e.g. 
     * the standard deviation of the matrix or the average absolute difference 
     * of each entry value to zero.
     * @param normalizationValue Value which the normalization is based on
     * @return CrossImpactMatrix : new normalized <tt>CrossImpactMatrix</tt>
     */
    public CrossImpactMatrix normalize(double normalizationValue) {
        double[] normalizedValues = this.values.clone();
        for (int i=0; i < normalizedValues.length; i++) {
            normalizedValues[i] /= normalizationValue;
        }
        return new CrossImpactMatrix(this.varCount, this.names.clone(), normalizedValues, false);
    }
    
    /**
     * Returns a new <tt>CrossImpactMatrix</tt> based on this matrix
     * where each matrix entry value has been transformed according to
     * the operation provided by <b>valueTransformer</b>.
     * @param valueTransformer Function to perform the transformation on the values
     * @return CrossImpactMatrix : new matrix with transformed values
     */
    public CrossImpactMatrix transform(DoubleFunction<Double> valueTransformer) {
        double[] transformedValues = this.values.clone();
        for (int i=0; i < transformedValues.length; i++) {
            transformedValues[i] = valueTransformer.apply(i);
        }
        return new CrossImpactMatrix(this.varCount, this.names.clone(), transformedValues, false);
    }
    
    
    /**
     * Returns a new <tt>CrossImpactMatrix</tt> derived from this impact matrix
     * where values are rounded to the nearest integer.
     * @return 
     */
    public CrossImpactMatrix round() {
        double[] rounded = this.values.clone();
        for(int i=0;i<rounded.length;i++) {
            rounded[i] = Math.round(rounded[i]);
        }
        return new CrossImpactMatrix(varCount, names, rounded, true);
    }
    
    /**
     * Returns a new <tt>CrossImpactMatrix</tt> derived from this impact matrix
     * where values are floored to the nearest integer.
     * @return 
     */    
    public CrossImpactMatrix floor() {
        double[] floored = this.values.clone();
        for(int i=0;i<floored.length;i++) {
            floored[i] = Math.floor(floored[i]);
        }
        return new CrossImpactMatrix(varCount, names, floored, true);        
    }


    /**
     * Scales the impact matrix to have its greatest absolute value
     * to be equal to value of <b>to</b>.
     * @param to The value that the greatest absolute impact value in the matrix will be scaled to
     * @return <code>EXITImpactMatrix</code> scaled according to <b>to</b> argument.
     */
    public CrossImpactMatrix scale(double to) {
        assert to != 0 : "Scaling to 0 not possible";
        double max = matrixMax();
        double[] scaled = this.values.clone();
        for (int i = 0; i < values.length; i++) {
            scaled[i] = values[i] / max * to;
        }
        return new CrossImpactMatrix(this.varCount, this.names, scaled, false);
    }
    
    /**
     * Return a <tt>Map</tt> with variable indices and the 
     * influence-dependence classifications of the corresponding variables of the
     * <tt>CrossImpactMatrix</tt>.
     * @return 
     */
    public Map<Integer, InfluenceDependenceClass> classifyVariables() {
        
        double rowSumAverage=0, colSumAverage=0;
        
        for(int i=1;i<=this.varCount;i++) {
            rowSumAverage += this.rowSum(i, true);
            colSumAverage += this.columnSum(i, true);
        }
        
        rowSumAverage /= this.varCount;
        colSumAverage /= this.varCount;
        
        Map<Integer, InfluenceDependenceClass> classification = new TreeMap<>();
        for(int i=1;i<=this.varCount;i++) {
            if(this.rowSum(i, true) > rowSumAverage) {
                if(this.columnSum(i, true) > colSumAverage) {
                    classification.put(i, InfluenceDependenceClass.CRITICAL);
                } else {
                    classification.put(i, InfluenceDependenceClass.DRIVER);
                }
            } else {
                if(this.columnSum(i, true) > colSumAverage) {
                    classification.put(i, InfluenceDependenceClass.REACTIVE);
                } else {
                    classification.put(i, InfluenceDependenceClass.STABLE);
                }                
            }
        }
        return classification;
    }
    
    public Classification getInfluenceDependencyClassification() {
        Map<String, String> map = new LinkedHashMap<>();
        for(Map.Entry<Integer, InfluenceDependenceClass> e : classifyVariables().entrySet()) {
            map.put(this.getNamePrint(e.getKey()), e.getValue().toString());
        }
        return new Classification(map);
    }
    
    
    /**
     * Returns a distribution of values in the matrix.
     * The returned <tt>Map</tt> contains an entry for each value present in the matrix
     * and a count of occurrence for that value.
     * @return <tt>Map</tt> where matrix impact values are <i>keys</i> and their counts are <i>values</i>.
     */
    public Map<Double, Integer> valueDistribution() {
        Map<Double, Integer> distribution = new TreeMap<>();
        for(Double d : this.values) {
            if( distribution.containsKey(d)) {
                distribution.put(d, distribution.get(d) + 1);
            } else {
                distribution.put(d, 1);
            }
        }
        return distribution;
    }
    
    /**
     * Returns the <b>density</b> of the matrix,
     * i.e.the percentage of non-zero impact values in entries that can have an impact value
     * (where row != column).
     * @return Percentage of non-zero impact values in the matrix.
     */
    public double getDensity() {
        return getDensity(0);
    }
    
    /**
     * Returns the density of the matrix:
     * the percentage of impact values greater than 
     * <b>countThreshold</b> is returned
     * @param countThreshold Values greater than countThreshold are counted
     * @return Density value of the matrix (using countThreshold)
     */
    public double getDensity(double countThreshold) {
        int counted = 0;
        for(double value : values) {
            if ( Math.abs(value) > countThreshold ) counted++;
        }
        return counted / (varCount*varCount-varCount);
    }
    
    
    public Ordering getOrdering(Orientation orientation) {
        return new Ordering(this, orientation);
    }
    
    
    public class Classification<K,V> {
        
        Map<K, V> map;
        
        public Classification(Map<K,V> map) {
            assert map != null;
            this.map = map;
        }
        
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            for(Map.Entry<K,V> entry : map.entrySet() ) {
                sb.append(entry.getKey().toString()).append(":\t").append(entry.getValue().toString()).append("\n");
            }
            return sb.toString();            
        }
        
    }
    
    
    /**
     * This class represents an ordering of the matrix variables.
     */
    public class Ordering implements Comparable<Ordering> {
        
        public final CrossImpactMatrix matrix;
        public final Orientation orientation;
        public final List<OrderingVariable> ordering;
        
        public Ordering(CrossImpactMatrix matrix, Orientation orientation) {
            this.matrix = matrix;
            this.orientation = orientation;
            this.ordering = new ArrayList<>();
            determineOrdering();
        }
        
        
        /**
         * Compares two orderings. 
         * The variables are compared itemwise: 
         * The variable indices at a position are unequal, their comparison is returned.
         * Otherwise the variable indices at next ordering position are compared.
         * @param o Compared ordering.
         * @return int: -1 for smaller, 0 for equal, 1 greater.
         */
        @Override
        public int compareTo(Ordering o) {
            for(int i=1;i<=this.ordering.size();i++) {
                if(o.ordering.size() < i) return 1;

                OrderingVariable o1 = this.ordering.get(i-1);
                OrderingVariable o2 = o.ordering.get(i-1);
                int indexComparison = Objects.compare(o1, o2, getComparator_index());
                


                if (indexComparison != 0) return indexComparison;
            }
            return this.ordering.size() == o.ordering.size() ?  0 : -1; 
            
        }

        /**
         * Returns a distance measure between two orderings.
         * @param o Ordering to compare this ordering against
         * @return Distance measure; 0 if the orderings are identical
         */        
        public double distance(Ordering o) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        

        /**
         * Tests whether this ordering is identical to <b>o</b>.
         * Orderings are identical if they have the same number of variables
         * and 
         * @param o Ordering tested for identicalness
         * @return <i>true</i> if orderings are identical, false
         */
        public boolean orderingIsIdentical(Ordering o) {
            return this.compareTo(o) == 0;
        }
        


        
        @Override
        public boolean equals(Object o) {
            if (o == this) return true;
            if (!(o instanceof Ordering)) { return false; }
            
            Ordering ord = (Ordering)o;

            
            return this.orientation == ord.orientation &&
                    this.compareTo(ord) == 0;



        }

        @Override
        public int hashCode() {
            return Objects.hash(matrix, orientation, ordering);
        }
        
        
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            for(OrderingVariable v : ordering) {
                sb.append(v.position).append(": " + "V").append(v.index).append(" (").append(v.sum).append(") ");
            }
            return sb.toString();
        }
        
        
        /**
         * Determines the ordering of <i>matrix</i> variables
         * according to the <i>orientation</i> of this <tt>Ordering</tt>.
         */
        private void determineOrdering() {
            
            // Collect the values for the ordering
            for(int i=1;i<=matrix.varCount;i++) {
                switch(orientation) {
                    case INFLUENCE:  this.ordering.add(new OrderingVariable(i, matrix.rowSum(i, true)));
                    break;
                    case DEPENDENCE: this.ordering.add(new OrderingVariable(i, matrix.columnSum(i, true)));
                    break;
                    default: throw new EnumConstantNotPresentException(orientation.getClass(), orientation.name());
                }
            }
            
            // Sort the ordering by sums (row or column sums)
            Collections.sort(ordering, this.getComparator_sum());
            
            // Put greatest value first
            Collections.reverse(ordering);
            
            /* Assign positions for the items in ordering; 
            if the sum for two consecutive items is the same, 
            they get the same position value */
            int position=1;
            for(int i=0; i < ordering.size();i++) {
                ordering.get(i).position = position;
                if(i<ordering.size()-1 && !Objects.equals(ordering.get(i).sum, ordering.get(i+1).sum))
                    position++;
            }            
            
            
        }
        
        public Comparator<OrderingVariable> getComparator_sum() {
            return (OrderingVariable a,OrderingVariable b) -> { return a.sum.compareTo(b.sum); };
        }
        
        public Comparator<OrderingVariable> getComparator_index() {
            return (OrderingVariable a,OrderingVariable b) -> { return a.index.compareTo(b.index); };            
        }
        
        /**
         * Comparator that compares <tt>OrderingVariable</tt> sums first,
         * and if they are equal, returns the <tt>compare</tt> result
         * for indices.
         * @return int
         */
        public Comparator<OrderingVariable> getComparator_sum_index() {
            return (OrderingVariable a, OrderingVariable b) -> {
                int sumComparison = a.sum.compareTo(b.sum);
                return sumComparison != 0 ? sumComparison : a.index.compareTo(b.index);
            };
        }


        /**
         * Container for details of a <i>matrix</i> variable in an <tt>Ordering</tt>
         */
        public class OrderingVariable {
            public final Integer index;
            public final Double sum;
            public Integer position;
            
            public OrderingVariable(int index, double sum) {
                this.index = index;
                this.sum = sum;
            }
            
        }
        
    }
    
}
