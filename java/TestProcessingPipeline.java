import java.util.Arrays; // For printing arrays when debugging

public class TestProcessingPipeline {

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

	public static void main(String[] args) {

		// 1. Create fake signal
		int nbCh = 4;
		double fs = 220.;
		double[][] fakeSignal = generateFakeSignal(30, nbCh, fs);

		// 2. Initialize processing parameters
		int windowLength = (int)fs;
		int step = (int)fs/10; // Each 10th of a second
		int fftLength = 128; // Should be 256

		// 3. Initialize filter and buffers
		Filter bpFilt = new Filter(220., 1);
		int bufferLength = 220;
		CircBuffer rawBuffer = new CircBuffer(bufferLength,nbCh);
		CircBuffer filtBuffer = new CircBuffer(bufferLength,nbCh);

		// 4. Initialize FFT transform and buffers
		FFT fft = new FFT(windowLength, fftLength, fs);
		double[] f = fft.getFreqBins();
 		int fftBufferLength = 20;
        int nbBins = f.length;
    	PSDBuffer psdBuffer = new PSDBuffer(fftBufferLength,nbCh,nbBins);

    	// 5. Initialize noise detector
    	NoiseDetector noiseDetector = new NoiseDetector(10.0); // Should probably be around 400 (uV^2)
    	boolean[] noiseDecisions = new boolean[nbCh];

    	// 6. Initialize band power extractor and buffer to get bandPowers
    	BandPowerExtractor bandPowerExtractor = new BandPowerExtractor(fft.getFreqBins());
    	PSDBuffer bandPowerBuffer = new PSDBuffer(fftBufferLength,nbCh,bandPowerExtractor.getNbBands());

    	// 7. Emulate raw data coming in sample by sample
		double[][] x;
		double[][] y;

		double[][] rawWindow = new double[windowLength][nbCh];
		double[][] filtWindow = new double[windowLength][nbCh];

		double[] filtResult = new double[nbCh];
		double[][] logpower = new double[nbCh][nbBins];
		double[][] smoothLogPower = new double[nbCh][nbBins];


		for (int i = 0; i < fakeSignal.length; i++) {

			// Write new raw sample in buffers
			rawBuffer.update(fakeSignal[i]);

			// Extract latest raw and filtered samples
			x = rawBuffer.extract(bpFilt.getNB());
			y = filtBuffer.extract(bpFilt.getNA()-1);

			// Filter new raw sample
			filtResult = bpFilt.transform(x, y);

			// Update filtered buffer
			filtBuffer.update(filtResult);

			// Process data if `step` samples have passed
			if (rawBuffer.getPts() > step) {
				rawBuffer.resetPts();

				/* ARTEFACT DETECTION */

				filtWindow = filtBuffer.extractTransposed(windowLength); // Extract latest filtered samples 
				noiseDecisions = noiseDetector.detectArtefact(filtWindow); // Detect artefacts
				// TODO: Highlight that epoch in the live plot if it is bad


				/* PSD COMPUTATION */

				rawWindow = rawBuffer.extractTransposed(windowLength); // Extract latest raw samples for FFT computation

				// Compute log-PSD
				for (int c = 0; c < nbCh; c++) {
					logpower[c] = fft.computeLogPSD(rawWindow[c]);
				}

				psdBuffer.update(logpower,noiseDecisions); // Write new log-PSD in buffer
				smoothLogPower = psdBuffer.mean(); // Compute average log-PSD over buffer
				// TODO: Plot smoothLogPower!


				/* BAND POWER COMPUTATION */

				double[][] bandPowers = bandPowerExtractor.extract(logpower);
				bandPowerBuffer.update(bandPowers);

			}

		}
	}
} 