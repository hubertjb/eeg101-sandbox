import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;
import java.util.Arrays; // For printing arrays when debugging


public class CircBufferEJML {
    
    private int bufferLength;
    private int nbCh;
    private int index;
    private int pts;
    private DenseMatrix64F buffer;
    private double[][] newData2D;
    private int[] colsIndices;

    public CircBufferEJML(int n, int m) {
        bufferLength = n;
        nbCh = m;
        index = 0;
        pts = 0;
        buffer = new DenseMatrix64F(bufferLength,nbCh);
        newData2D = new double[1][nbCh]; // used for updating with EJML

        colsIndices = new int[nbCh];
        for(int i = 0; i < nbCh; i++) {
            colsIndices[i] = i; // used for extracting rows with EJML
        }
    }

    public void update(double[] newData) {

        if (newData.length == nbCh) {
            newData2D[0] = newData;
            CommonOps.insert(new DenseMatrix64F(newData2D),buffer,index,0);

            index++;
            pts++;
            if (index >= bufferLength) { index = 0;}

        } else {
            System.out.println("All channels must be updated at once.");
        }
    }

    public DenseMatrix64F extract(int nbSamples) {

        // Get indices to extract
        int[] indicesToExtract = new int[nbSamples];
        for(int i = 0; i < nbSamples; i++) {
            indicesToExtract[i] = mod(index - nbSamples + i, bufferLength);
        }

        // Extract from matrix
        DenseMatrix64F extractedArray = new DenseMatrix64F(nbSamples,nbCh);
        CommonOps.extract(buffer,indicesToExtract,nbSamples,colsIndices,nbCh,extractedArray);

        return extractedArray;
    }

    public void resetPts() {
        pts = 0;
    }

    public void print() {
        buffer.print();
    }

    private int mod(int a, int b) {
        // Modulo operation that always return a positive number
        int c = a % b;
        return (c < 0) ? c + b : c;
    }

    public static void main(String[] args ) {

        // Create buffer of 220 samples by 4 channels
        int testNbCh = 4;
        int testBufferLength = 220;
        CircBufferEJML testBuffer = new CircBufferEJML(testBufferLength,testNbCh);

        // Update buffer a few times with fake data
        double[] fakeSamples = new double[]{0.,1.,2.,3.};
        int nbUpdates = 1;
        for(int i = 0; i < nbUpdates; i++){
            testBuffer.update(fakeSamples);
        }

        // Print buffer
        testBuffer.print();

        // Extract latest 12 samples from buffer
        DenseMatrix64F testExtractedArray = testBuffer.extract(12);
        testExtractedArray.print();

        // Reset number of collected points
        testBuffer.resetPts();

    }

}