import biz.source_code.dsp.filter.FilterPassType;
import biz.source_code.dsp.filter.FilterCharacteristicsType;
import biz.source_code.dsp.filter.IirFilterCoefficients;
import biz.source_code.dsp.filter.IirFilterDesignFisher;

import java.util.Arrays; // For printing arrays when debugging

// Contains transform function for performing bandpass filter of EEG data. Also contains python-generated coefficients
public class Filter2 {

    // ------------------------------------------------------------------------
    // Variables
    private String filterType;
    private double fs;
    private double[] b;
    private double[] a;
    private int nB;
    private int nA;

    // ------------------------------------------------------------------------
    // Constructor

    public Filter2(double samplingFrequency, String inputFilterType, int filterOrder, double fc1, double fc2) {


        filterType = inputFilterType;
        FilterCharacteristicsType filterCharacteristicsType = FilterCharacteristicsType.butterworth;

        FilterPassType filterPassType = FilterPassType.lowpass;

        if (filterType.contains("lowpass")) {
            filterPassType = FilterPassType.lowpass;

        } else if (filterType.contains("highpass")) {
            filterPassType = FilterPassType.highpass;

        } else if (filterType.contains("bandstop")) {
            filterPassType = FilterPassType.bandstop;
        
        } else if (filterType.contains("bandpass")) {
            filterPassType = FilterPassType.bandpass;

        } else {
            throw new java.lang.RuntimeException("this is not quite as bad");
        }  

        double fc1Norm = fc1/samplingFrequency;
        double fc2Norm = fc2/samplingFrequency;
        IirFilterCoefficients coeffs = IirFilterDesignFisher.design(filterPassType, filterCharacteristicsType, filterOrder, 0., fc1Norm, fc2Norm);

        b = coeffs.b;
        a = coeffs.a;

        nB = b.length;
        nA = a.length;

    }

    // ---------------------------------------------------------------------
    // Methods
    // Performs filter difference equation
    // (b*x - a*y)/a[0]
    public double[] transform(double[][] x, double[][] y) {

        int nbCh = x[0].length;
        double[] filtSum = new double[nbCh];

        // Filter channel by channel, and sample by sample
        for (int c = 0; c < x[0].length; c++) {
            filtSum[c] = b[0]*x[nB-1][c];
            for (int i = 1; i < nB; i++) {
                filtSum[c] += b[i]*x[nB-i-1][c] - a[i]*y[nA-i-1][c];
            }
            filtSum[c] /= a[0];
        }

        return filtSum;

    }

    public double[] transformDFIIT(double x, double[] z) {
        // This function implements the Discrete Form II Transposed of 
        // a linear filter.
        //
        // Args:
        //  x: the current sample to be filtered
        //  z: the internal state of the filter
        //
        // Returns:
        //  the updated internal state of the filter, with the new 
        //  filtered value in the last position. This is a hack
        //  that allows to pass both the internal state and the 
        //  output of the filter at once.

        z[z.length - 1] = 0;
        double y = b[0]*x + z[0];

        for (int i = 1; i < nB; i++) {
            z[i-1] = b[i]*x + z[i] - a[i]*y;
         }

        z[z.length - 1] = y;

        return z;

    }

    public double[][] transformDFIIT(double[] x, double[][] z) {
        // This function implements the Discrete Form II Transposed of 
        // a linear filter for multichannel signals
        //
        // Args:
        //  x: the current channel samples to be filtered
        //  z: the internal state of the filter for each channels [nbCh,nbPoints]
        //
        // Returns:
        //  the updated internal states of the filter, with the new 
        //  filtered values in the last position. [nbCh,nbPoints]
        //  This is a hack that allows to pass both the internal state and the 
        //  output of the filter at once.

        // double[] zNew = new double[z[0].length];

        for (int i = 0; i < x.length; i++) { 
            z[i] = transformDFIIT(x[i],z[i]);
            // System.arraycopy(z[i], 0, zNew, 0, z[i].length);
            // zNew = transformDFIIT(x[i],zNew);
            // System.arraycopy(zNew, 0, z[i], 0, z[i].length);
        }

        return z;
        
    }

    public static double[] extractFilteredSamples(double[][] z) {
        double[] filtSignal = new double[z.length];
        for (int i = 0; i < z.length; i++) {
            filtSignal[i] = z[i][z[0].length - 1];
        }
        return filtSignal;
    }

    public int getNB() {
        return nB;
    }

    public int getNA() {
        return nA;
    }

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
        double amp2 = 20.0;
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

        double fs = 256.;

        System.out.println("Notch filter @ 60 Hz");
        Filter2 notchFilter = new Filter2(fs, "bandstop", 5, 55, 65);
        System.out.println(Arrays.toString(notchFilter.b));
        System.out.println(Arrays.toString(notchFilter.a));

        System.out.println("Bandpass filter 2-36 Hz");
        Filter2 lowFilter = new Filter2(fs, "lowpass", 5, 5, 0);
        System.out.println(Arrays.toString(lowFilter.b));
        System.out.println(Arrays.toString(lowFilter.a));

        // 1. Create fake signal
        int nbCh = 4;
        double duration = 10;
        double[][] fakeSignal = generateFakeSignal(duration, nbCh, fs);

        double[] x;
        double[][] filtSignal = new double[fakeSignal.length][nbCh];
        double[][] z = new double[nbCh][notchFilter.getNB()];

        int chNb = 3;

        for (int i = 0; i < (int) duration*fs; i++) {
            x = fakeSignal[i];
            z = notchFilter.transformDFIIT(x,z);
            filtSignal[i] = extractFilteredSamples(z);
            // for (int c = 0; c < nbCh; c++) {
            //     filtSignal[i][c] = z[c][z[0].length - 1];
            // }
            System.out.println(x[chNb]);
            System.out.println(filtSignal[i][chNb]);
        }

    }

}