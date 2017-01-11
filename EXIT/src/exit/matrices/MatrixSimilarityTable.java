/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package exit.matrices;

/**
 * A similarity matrix of cross-impact matrices.
 * Used for testing purposes of various summed impact estimation approaches.
 * @author jmpaon
 */
public class MatrixSimilarityTable {
        
    public boolean[][] similarityMatrix;
    
    public MatrixSimilarityTable(double maxDifference, SquareMatrix... matrices) {
        assert maxDifference >= 0 && maxDifference <= 1;
        similarityMatrix = new boolean[matrices.length][matrices.length];
        for(int i = 0; i<matrices.length; i++) {
            for(int ii = 0; ii<matrices.length; ii++) {
                similarityMatrix[i][ii] = matrices[i].equalsApproximately(matrices[ii], maxDifference);
            }
        }
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for(int i=0;i<similarityMatrix.length;i++) {
            sb.append(i+1 + ":\t");
            for(int ii=0;ii<similarityMatrix.length;ii++) {
                sb.append(similarityMatrix[i][ii]).append("\t");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
    
    
    
}
