import org.ejml.simple.SimpleMatrix;
import org.ejml.data.DenseMatrix64F;
import java.util.Arrays; // For printing arrays when debugging
import java.lang.Math; // For creating fake signals

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class TestFilter {

	public static double[][] generateFakeSignal(double duration, int nbCh, double fs) {

		int nbSamples =  (int)(duration*fs);
		double dt = 1./fs;

		// Create time vector
		double[] t = new double[nbSamples];
		for (int i = 0; i < nbSamples; i++) {
			t[i] = i*dt;
		}

		double amp0 = 10.0;
		double amp1 = 5.0;
		double amp2 = 1.0;
		double f0 = 1.0;
		double f1 = 20.0;
		double f2 = 60.0;

		// Make signal
		double[][] signal = new double[nbSamples][nbCh];
		for (int c = 0; c < nbCh; c++) {
			for (int i = 0; i < nbSamples; i++) {
				signal[i][c] = amp0*Math.sin(2*Math.PI*f0*t[i]) + 
							   amp1*Math.sin(2*Math.PI*f1*t[i]) + 
							   amp2*Math.sin(2*Math.PI*f2*t[i]);
			}
		}

		return signal;
	}

	public static double[][] getRealSignal(String filename) {

		double values[][] = new double[3112][1];

		try (BufferedReader br = new BufferedReader(new FileReader(filename))) {

			String sCurrentLine;
			int i = 0;

			while ((sCurrentLine = br.readLine()) != null) {
				values[i][0] = Double.parseDouble(sCurrentLine);
				// System.out.println(i);
				// System.out.println(values[i]);
				i++;
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		return values;

	}

	public static void main(String[] args) {

		// Get recorded signal
		double[][] realSignal = getRealSignal("/home/hubert/Documents/eeg-101/java_scripts/test_data/test_data_listener_raw.txt");
		double[][] filtFakeSignal = new double[realSignal.length][realSignal[0].length];

		// Initialize filters
		Filter bpFilt = new Filter(220., 1);

		// Initialize buffers
		int nbCh = 1;
		int bufferLength = 220;
		CircBuffer rawBuffer = new CircBuffer(bufferLength,nbCh);
		CircBuffer filtBuffer = new CircBuffer(bufferLength,nbCh);

		// Filter sample by sample
		double[][] x;
		double[][] y;
		double[] filtResult;

		for (int i = 0; i < realSignal.length; i++) {

			// Write new raw sample in buffers
			rawBuffer.update(realSignal[i]);

			// Extract latest raw and filtered samples
			x = rawBuffer.extract(bpFilt.getNB());
			y = filtBuffer.extract(bpFilt.getNA()-1);

			// Filter new raw filtResult
			filtResult = bpFilt.transform(x, y);

			// Update filtered buffer
			filtBuffer.update(filtResult);
			filtFakeSignal[i] = filtResult;

			System.out.println(filtResult[0]);
		}




		/* EJML IMPLEMENTATION */
		// // Create fake signal
		// int nbCh = 4;
		// double[][] fakeSignal = generateFakeSignal(30, 4, 220.);
		// double[][] filtFakeSignal1 = new double[fakeSignal.length][fakeSignal[0].length];
		// double[][] filtFakeSignal2 = new double[fakeSignal.length][fakeSignal[0].length];

		// // Initialize filters
		// Filter bpFilt1 = new Filter(220., 1);
		// FilterEJML bpFilt2 = new FilterEJML(220., 1);

		// // Initialize buffers
		// int bufferLength = 220;
		// CircBuffer rawBuffer1 = new CircBuffer(bufferLength,nbCh);
		// CircBuffer filtBuffer1 = new CircBuffer(bufferLength,nbCh);
		// CircBufferEJML rawBuffer2 = new CircBufferEJML(bufferLength,nbCh);
		// CircBufferEJML filtBuffer2 = new CircBufferEJML(bufferLength,nbCh);

		// // Filter sample by sample
		// double[][] x1;
		// double[][] y1;
		// double[] filtResult1;
		// DenseMatrix64F x2;
		// DenseMatrix64F y2;
		// SimpleMatrix filtResult2;

		// for (int i = 0; i < fakeSignal.length; i++) {

		// 	// Write new raw sample in buffers
		// 	rawBuffer1.update(fakeSignal[i]);
		// 	rawBuffer2.update(fakeSignal[i]);

		// 	// Extract latest raw and filtered samples
		// 	x1 = rawBuffer1.extract(bpFilt1.getNB());
		// 	y1 = filtBuffer1.extract(bpFilt1.getNA()-1);
		// 	x2 = rawBuffer2.extract(bpFilt2.getNB());
		// 	y2 = filtBuffer2.extract(bpFilt2.getNA()-1);

		// 	// Filter new raw sample
		// 	filtResult1 = bpFilt1.transform(x1, y1);
		// 	filtResult2 = bpFilt2.transform(SimpleMatrix.wrap(x2), SimpleMatrix.wrap(y2));

		// 	// Update filtered buffer
		// 	filtBuffer1.update(filtResult1);
		// 	filtFakeSignal1[i] = filtResult1;

		// 	filtBuffer2.update(filtResult2.getMatrix().getData());
		// 	filtFakeSignal2[i] = filtResult2.getMatrix().getData();
		// }

		// // Compare Filter and FilterEJML
		// int nbDiff = 0;
		// for (int i = 0; i < fakeSignal.length; i++) {
		// 	if (filtFakeSignal1[i][0] - filtFakeSignal2[i][0] > 1E-6) {
		// 		nbDiff++;
		// 	}
		// }
		// System.out.println((double)nbDiff/fakeSignal.length); // Print agreement

	}

}