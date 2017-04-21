import java.util.Arrays; // For printing arrays when debugging

public class CircBuffer {
    // This class implements a circular (or ring) buffer to hold
    // the most recent values of a 1D time series efficiently.
	
	private int bufferLength;
	private int nbCh;
	private int index;
	private int pts;
	private double[][] buffer;

	public CircBuffer(int n, int m) {
        bufferLength = n;
        nbCh = m;
        index = 0;
        pts = 0;
        buffer = new double[bufferLength][nbCh];
    }

    public void update(double[] newData) {

    	if (newData.length == nbCh) {
	    	buffer[index] = newData;
	    	index++;
	    	pts++;
	    	if (index >= bufferLength) { index = 0;}
    	} else {
    		System.out.println("All channels must be updated at once.");
    	}
    }

    public double[][] extract(int nbSamples) {
        // Return an array containing the last `nbSamples` collected in 
        // the circular buffer.
        //
        // The shape of the returned array is [nbSamples, nbCh].

    	int extractIndex;
    	double[][] extractedArray = new double[nbSamples][nbCh];

    	for(int i = 0; i < nbSamples; i++) {
    		extractIndex = mod(index - nbSamples + i, bufferLength);
    		extractedArray[i] = buffer[extractIndex];
    	}

    	return extractedArray;
    }

    public double[][] extractTransposed(int nbSamples) {
        // Return an array containing the last `nbSamples` collected in 
        // the circular buffer.
        //
        // The shape of the returned array is [nbCh, nbSamples].
        //
        // This transposed version is useful to avoid additional looping
        // through the returned array when computing FFT (the looping is
        // instead done here.)
        //
        // TODO: find more efficient way to do that (use EJML?)

        int extractIndex;
        double[][] extractedArray = new double[nbCh][nbSamples];

        for (int c = 0; c < nbCh; c++) {
            for(int i = 0; i < nbSamples; i++) {
                extractIndex = mod(index - nbSamples + i, bufferLength);
                extractedArray[c][i] = buffer[extractIndex][c];
            }
        }

        return extractedArray;
    }


    public int getPts() {
        return pts;
    }

    public void resetPts() {
    	pts = 0;
    }

    public void print() {
    	System.out.println(Arrays.deepToString(buffer));
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
    	CircBuffer testBuffer = new CircBuffer(testBufferLength,testNbCh);

    	// Update buffer a few times with fake data
    	double[] fakeSamples = new double[]{0.,1.,2.,3.};
    	int nbUpdates = 1;
    	for(int i = 0; i < nbUpdates; i++){
    		testBuffer.update(fakeSamples);
    	}

    	// Print buffer
    	testBuffer.print();

    	// Extract latest 12 samples from buffer
    	double[][] testExtractedArray = testBuffer.extract(12);
    	System.out.println(Arrays.deepToString(testExtractedArray));

        // Extract latest 12 samples from buffer, but transposed
        double[][] testExtractedArray2 = testBuffer.extractTransposed(12);
        System.out.println(Arrays.deepToString(testExtractedArray2));

    	// Reset number of collected points
    	testBuffer.resetPts();

    }

}