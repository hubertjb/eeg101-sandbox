import org.ejml.simple.SimpleMatrix;

public class CompareExecutionTime {
	public static void main(String[] args ) {

		int nbUpdates1 = 99999999;
		int nbUpdates2 = 100000;
		int extractLen = 220;
		long startTime;
		long endTime;


		/* CIRCULAR BUFFERS */

		double[] fakeSamples = new double[]{0.,1.,2.,3.};
		int nbCh = 4;
		int nbSamples = 220;

		CircBuffer buffer1 = new CircBuffer(nbSamples,nbCh);
		CircBufferEJML buffer2 = new CircBufferEJML(nbSamples,nbCh);

		// CircBuffer's update()
		startTime = System.currentTimeMillis();
    	for(int i = 0; i < nbUpdates1; i++){
    		buffer1.update(fakeSamples);
    	}
		endTime = System.currentTimeMillis();
		System.out.println("Total execution time 1 (update): " + (endTime-startTime) + "ms");

		// CircBufferEJML's update()
		startTime = System.currentTimeMillis();
    	for(int i = 0; i < nbUpdates1; i++){
    		buffer2.update(fakeSamples);
    	}
		endTime = System.currentTimeMillis();
		System.out.println("Total execution time 2 (update): " + (endTime-startTime) + "ms"); 

		// CircBuffer's extract()
		startTime = System.currentTimeMillis();
    	for(int i = 0; i < nbUpdates2; i++){
    		buffer1.extract(extractLen);
    	}
		endTime = System.currentTimeMillis();
		System.out.println("Total execution time 1 (extract): " + (endTime-startTime) + "ms");

		// CircBufferEJML's extract()
		startTime = System.currentTimeMillis();
    	for(int i = 0; i < nbUpdates2; i++){
    		buffer2.extract(extractLen);
    	}
		endTime = System.currentTimeMillis();
		System.out.println("Total execution time 2 (extract): " + (endTime-startTime) + "ms"); 
	

		/* FILTERING */

		Filter filt1 = new Filter(220., 1);
		FilterEJML filt2 = new FilterEJML(220., 1);

		double[][] x1 = new double[filt1.getNB()][nbCh];
		double[][] y1 = new double[filt1.getNA()-1][nbCh];

		SimpleMatrix x2 = new SimpleMatrix(filt2.getNB(),nbCh);
		SimpleMatrix y2 = new SimpleMatrix(filt2.getNA()-1,nbCh);

		// Filter's transform()
		startTime = System.currentTimeMillis();
    	for(int i = 0; i < nbUpdates2; i++){
    		filt1.transform(x1, y1);
    	}
		endTime = System.currentTimeMillis();
		System.out.println("Total execution time 1 (transform): " + (endTime-startTime) + "ms");

		// FilterEJML's transform()
		startTime = System.currentTimeMillis();
    	for(int i = 0; i < nbUpdates2; i++){
    		filt2.transform(x2, y2);
    	}
		endTime = System.currentTimeMillis();
		System.out.println("Total execution time 2 (transform): " + (endTime-startTime) + "ms");

	}
}