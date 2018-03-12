package com.home.buffa.movementscience;

import org.opencv.core.Point;

import java.util.ArrayList;

/**
 * Created by buffa on 6/9/2017.
 */

public class nonlinearMath {

    public double sampen(ArrayList<Float> y){
        // standardize
        //find mean
        float sum = 0;
        for (Float ys : y){
            sum += ys;
        }
        float yMean = sum / y.size();
        //subtract mean
        int c=0;
        for (Float ys : y){
            float t = ys - yMean;
            y.set(c,t);
            c++;
        }
        //find mean square value
        float ct=0;
        for (Float ys : y){
            float t = ys * ys;
            ct = ct + t;
        }
        ct=ct / y.size();
        float s = (float) Math.sqrt((double) ct);
        //divide by mean square
        c=0;
        for (Float ys : y){
            y.set(c,ys/s);
        }

        int wlen = 3;
        float r = (float) 0.2;
        int A = 0;
        int B = 0;
        int i,j,k;
        double m;
        int numSamples = y.size();
        int shift = 1;

        for (i=0; i < numSamples-wlen*shift-shift; i += shift) {
        /* compare to all following windows > i */
            for (j=i+shift; j < numSamples-wlen*shift-shift; j+=shift) {
                m = 0; /* maximum so far */
                /* get max cheb. distance */
                for (k=0; k < wlen; k++){
                    m = Math.max(m, Math.abs(y.get(i + k * shift) - y.get(j + k * shift)));
                }
                /* first case, distance lower in first wlen positions */
                if (m < r){
                    B++;
                }
            /* Second case, distance lower if we add the next element */
                if (Math.max(m, Math.abs(y.get(i+wlen*shift)-y.get(j+wlen*shift))) < r){
                    A++;
                }
            }
        }
        /* return -log A/B */
        if (A>0 && B >0)
            return (-1 * Math.log(((double) A) / ((double) B)));
        else
            return 0;
    }

    public static ArrayList<Double> twoPointGlobalAngle(ArrayList<Point> points){
        //retuns an angle relative to the top of the screen in degrees
        ArrayList<Double> angsOut = new ArrayList<Double>();
        for (int i = 0; i < points.size(); i = i + 2){
            Point p1 = points.get(i);
            Point p2 = points.get(i+1);

            double dy = p2.y - p1.y;
            double dx = p2.x - p1.x;

            double result = Math.toDegrees(Math.abs(Math.atan2(dy,dx)));
            result = Math.abs(result);

            if (result >= 180) {
                angsOut.add(360 - result);
            }else {
                angsOut.add(result);
            }
        }
        return angsOut;
    }

    public static ArrayList<Double> threePointSegmentAngle(ArrayList<Point> points) {
        ArrayList<Double> angsOut = new ArrayList<Double>();
        for (int i = 0; i < points.size(); i = i + 3){
            Point p1 = points.get(i);
            Point p2 = points.get(i+1);
            Point p3 = points.get(i+2);

            double result = Math.toDegrees(Math.atan2(p3.y - p1.y, p3.x - p1.x) - Math.atan2(p2.y - p1.y, p2.x - p1.x));
            result = Math.abs(result);

            if (result >= 180) {
                angsOut.add(360 - result);
            }else {
                angsOut.add(result);
            }

        }
        return angsOut;
    }

    public static ArrayList<Double> fourPointSegmentAngle(ArrayList<Point> points) {
        ArrayList<Double> angsOut = new ArrayList<Double>();
        for (int i = 0; i < points.size(); i = i + 4){
            Point p1 = points.get(i);
            Point p2 = points.get(i+1);
            Point p3 = points.get(i+2);
            Point p4 = points.get(i+3);

            double angle1 = Math.atan2(p3.y - p4.y, p3.x - p4.x);
            double angle2 = Math.atan2(p1.y - p2.y, p1.x - p2.x);

            double result = Math.toDegrees(Math.abs(angle1-angle2));


            if (result >= 180) {
                angsOut.add(Math.abs(360 - result));
            }else {
                angsOut.add(Math.abs(result));
            }

        }
        return angsOut;
    }



//    public ArrayList<Double> LyE(ArrayList<Float> y){
//        int L = 200;
//        int MaxDim = 25;
//        int Rtol = 15;
//        int Atol = 2;
//        int N = y.size();
//        int overlap = N + 1 - L;
//        int increment= 1/overlap;
//        ArrayList<Float> one = new ArrayList<Float>();
////        ArrayList<Float> pA = new ArrayList<Float>();
////        for(int i = 0; i < overlap; i++){
////            one.set(i,(float) 1);
////            pA.set(i,(float) 1/overlap);
////        }
//
//
//        public void AMI{
//
//            int bins = 128;
//            double epsilon = 0.000000001;
//
//            //make all data points positive
//            float yMin = Collections.min(y);
//            int c=0;
//            for (Float yin : y){
//                y.set(c,yin-yMin);
//                c++;
//            }
//
//            //scale the data
//            float yMax = Collections.max(y);
//            c=0;
//            for (Float yin : y){
//                float datai = (float) (1 + Math.floor( (double) (yin / yMax / ((double)bins - epsilon)  ) ));
//                y.set(c,datai);
//                c++;
//            }
//
//            //build pA; pA should sum to 1
//            ArrayList<Float> pA = new ArrayList<Float>();
//            ArrayList<Float> yPart1 = new ArrayList<Float>();
//            for (int i = 0; i < overlap; i++){
//                if ( i == 0){
//                    yPart1.set(i, (float) 0);
//                }
//                yPart1.set(i,y.get(i));
//            }
//            Set<Float> uniqueYpart1 = new HashSet<Float>(yPart1);
//            c=0;
//            for(Iterator<Float> it = uniqueYpart1.iterator(); it.hasNext();){
//                pA.set(c, (float) 0);
//                float yCurr = it.next();
//                for (int j = 0; j < y.size(); j++){
//                    if (yPart1.get(j) == yCurr){
//                        pA.set(c,pA.get(c) + increment);
//                    }
//                }
//                c++;
//            }
//
//
//            ArrayList<Double> v_AMI = new ArrayList<Double>();
//            int tau;
//            for (int i = 0; i < L; i++){//iterate through lags
//                //build pB as lagged vector the same size as pA; should sum to 1
//                ArrayList<Float> pB = new ArrayList<Float>();
//                ArrayList<Float> yPart2 = new ArrayList<Float>();
//                for (int j = 0; j < overlap; j++){
//                    if (j == 0){
//                        yPart2.set(j, (float) 0);
//                    }else{
//                        yPart2.set(j,y.get(j+i+1));
//                    }
//
//                }
//                Set<Float> uniqueYpart2 = new HashSet<Float>(yPart2);
//                uniqueYpart2 = new HashSet<Float>(yPart2);
//                c=0;
//                for(Iterator<Float> it = uniqueYpart2.iterator(); it.hasNext();){
//                    pB.set(c, (float) 0);
//                    float yCurr = it.next();
//                    for (int j = 0; j < y.size(); j++){
//                        if (yPart2.get(j) == yCurr){
//                            pB.set(c,pB.get(c) + increment);
//                        }
//                    }
//                    c++;
//                }
//                //build pAB as probability that two vectors join; will be 2D matrix that sums to 1
//                float[][] pAB = new float[overlap][overlap];
//                for (int j = 0; j < overlap; j++){
//                    for (int k = 0; k < overlap; k++){
//                        pAB[j][k] = 0;
//                    }
//
//                }
//                ArrayList<Integer> indsCols = new ArrayList<Integer>();
//                ArrayList<Integer> indsRows = new ArrayList<Integer>();
//
//                int cj;
//                int ci;
//                ci=0;
//                for(Iterator<Float> it1 = uniqueYpart1.iterator(); it1.hasNext();){
//                    cj = 0;
//                    for(Iterator<Float> it2 = uniqueYpart2.iterator(); it2.hasNext();){
//                        if (it1.next() == it2.next()){
//                            pAB[ci][cj] = pAB[ci][cj] + increment;
//                        }
//                        cj++;
//                    }
//                    ci++;
//                }
//
//                ci = pAB.length;
//                cj = pAB[0].length;
//                for (int j = 0; j < ci; j++){
//                    for (int k = 0; k < cj; k++){
//                        if (pAB[ci][cj] != 0){
//                            indsRows.add(ci);//A in template
//                            indsCols.add(cj);//B in template
//                        }
//                    }
//                }
//                //find v (AMI) for this lag
//                double vTmp = 0;
//                for (int j = 0; j < indsRows.size(); j++){
//                    vTmp = vTmp + ( pAB[indsRows.get(j)][indsCols.get(j)] * Math.log(pAB[indsRows.get(j)][indsCols.get(j)] / (pA.get(indsRows.get(j)) * pB.get(indsCols.get(j)) )  ) );
//                }
//                v_AMI.add(i,vTmp);
//            }
//            //find minima for v_AMI to determine tau
//            ArrayList<Integer> x = new ArrayList<Integer>();
//            for (int i = 1; i < v_AMI.size()-1; i++){
//                if (v_AMI.get(i) < v_AMI.get(i-1)){
//                    if (v_AMI.get(i) < v_AMI.get(i+1)){
//                        x.add(i);
//                    }
//                }
//            }
//            if (x.size() > 0) {
//                tau = x.get(0);
//            }else{
//                tau = 0;
//            }
//
//            //now find the false nearest neighbors to determine embedding dimension
//            int n = y.size() - (tau * MaxDim);
//            SummaryStatistics stats = new SummaryStatistics();
//            for (int i = 0; i < y.size(); i++){
//                stats.addValue(y.get(i));
//            }
//            double RA = stats.getStandardDeviation();
//            ArrayList<Double> z = new ArrayList<Double>();
//            for (int i =0; i < n; i++ ){
//                z.set(i,(double) y.get(i));
//            }
//            ArrayList<Double> data = new ArrayList<Double>();//y in template
//            ArrayList<Double> FN = new ArrayList<Double>();
//
//            int m_search = 2;
//            //indx use z.size();
//
//            for (int dim = 0; dim < MaxDim; dim++){
//                data.addAll(z);
//            }
//
//        }
//
//    }

}
