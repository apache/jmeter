/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.apache.jmeter.visualizers;

import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/*
 * TODO : - implement ImageProducer interface - suggestions ;-)
 */

/**
 * This class implements the representation of an interpolated Spline curve.
 * <P>
 * The curve described by such an object interpolates an arbitrary number of
 * fixed points called <I>nodes</I>. The distance between two nodes should
 * currently be constant. This is about to change in a later version but it can
 * last a while as it's not really needed. Nevertheless, if you need the
 * feature, just <a href="mailto:norguet@bigfoot.com?subject=Spline3eq">write me
 * a note</a> and I'll write it asap.
 * <P>
 * The interpolated Spline curve can't be described by an polynomial analytic
 * equation, the degree of which would be as high as the number of nodes, which
 * would cause extreme oscillations of the curve on the edges.
 * <P>
 * The solution is to split the curve accross a lot of little <I>intervals</I> :
 * an interval starts at one node and ends at the next one. Then, the
 * interpolation is done on each interval, according to the following conditions :
 * <OL>
 * <LI>the interpolated curve is degree 3 : it's a cubic curve ;
 * <LI>the interpolated curve contains the two points delimiting the interval.
 * This condition obviously implies the curve is continuous ;
 * <LI>the interpolated curve has a smooth slope : the curvature has to be the
 * same on the left and the right sides of each node ;
 * <LI>the curvature of the global curve is 0 at both edges.
 * </OL>
 * Every part of the global curve is represented by a cubic (degree-3)
 * polynomial, the coefficients of which have to be computed in order to meet
 * the above conditions.
 * <P>
 * This leads to a n-unknow n-equation system to resolve. One can resolve an
 * equation system by several manners ; this class uses the Jacobi iterative
 * method, particularly well adapted to this situation, as the diagonal of the
 * system matrix is strong compared to the other elements. This implies the
 * algorithm always converges ! This is not the case of the Gauss-Seidel
 * algorithm, which is quite faster (it uses intermediate results of each
 * iteration to speed up the convergence) but it doesn't converge in all the
 * cases or it converges to a wrong value. This is not acceptable and that's why
 * the Jacobi method is safer. Anyway, the gain of speed is about a factor of 3
 * but, for a 100x100 system, it means 10 ms instead of 30 ms, which is a pretty
 * good reason not to explore the question any further :)
 * <P>
 * Here is a little piece of code showing how to use this class :
 *
 * <PRE>
 *  // ...
 *  float[] nodes = {3F, 2F, 4F, 1F, 2.5F, 5F, 3F};
 *  Spline3 curve = new Spline3(nodes);
 *  // ...
 *  public void paint(Graphics g) {
 *    int[] plot = curve.getPlots();
 *    for (int i = 1; i &lt; n; i++) {
 *      g.drawLine(i - 1, plot[i - 1], i, plot[i]);
 *    }
 *  }
 *  // ...
 * </PRE>
 *
 */
public class Spline3 {
    private static final Logger log = LoggingManager.getLoggerForClass();

    protected float[][] _coefficients;

    protected float[][] _A;

    protected float[] _B;

    protected float[] _r;

    protected float[] _rS;

    protected int _m; // number of nodes

    protected int _n; // number of non extreme nodes (_m-2)

    static final protected float DEFAULT_PRECISION = (float) 1E-1;

    static final protected int DEFAULT_MAX_ITERATIONS = 100;

    protected float _minPrecision = DEFAULT_PRECISION;

    protected int _maxIterations = DEFAULT_MAX_ITERATIONS;

    /**
     * Creates a new Spline curve by calculating the coefficients of each part
     * of the curve, i.e. by resolving the equation system implied by the
     * interpolation condition on every interval.
     *
     * @param r
     *            an array of float containing the vertical coordinates of the
     *            nodes to interpolate ; the vertical coordinates start at 0 and
     *            are equidistant with a step of 1.
     */
    public Spline3(float[] r) {
        int n = r.length;

        // the number of nodes is defined by the length of r
        this._m = n;
        // grab the nodes
        this._r = new float[n];
        System.arraycopy(r, 0, _r, 0, _r.length);
        // the number of non extreme nodes is the number of intervals
        // minus 1, i.e. the length of r minus 2
        this._n = n - 2;
        // computes interpolation coefficients
        try {
            long startTime = System.currentTimeMillis();

            this.interpolation();
            if (log.isDebugEnabled()) {
                long endTime = System.currentTimeMillis();
                long elapsedTime = endTime - startTime;

                if (log.isDebugEnabled()) {
                    log.debug("New Spline curve interpolated in ");
                    log.debug(elapsedTime + " ms");
                }
            }
        } catch (Exception e) {
            log.error("Error when interpolating : ", e);
        }

    }

    /**
     * Computes the coefficients of the Spline interpolated curve, on each
     * interval. The matrix system to resolve is <CODE>AX=B</CODE>
     */
    protected void interpolation() {
        // creation of the interpolation structure
        _rS = new float[_m];
        _B = new float[_n];
        _A = new float[_n][_n];
        _coefficients = new float[_n + 1][4];
        // local variables
        int i = 0, j = 0;

        // initialize system structures (just to be safe)
        for (i = 0; i < _n; i++) {
            _B[i] = 0;
            for (j = 0; j < _n; j++) {
                _A[i][j] = 0;
            }
            for (j = 0; j < 4; j++) {
                _coefficients[i][j] = 0;
            }
        }
        for (i = 0; i < _n; i++) {
            _rS[i] = 0;
        }
        // initialize the diagonal of the system matrix (A) to 4
        for (i = 0; i < _n; i++) {
            _A[i][i] = 4;
        }
        // initialize the two minor diagonals of A to 1
        for (i = 1; i < _n; i++) {
            _A[i][i - 1] = 1;
            _A[i - 1][i] = 1;
        }
        // initialize B
        for (i = 0; i < _n; i++) {
            _B[i] = 6 * (_r[i + 2] - 2 * _r[i + 1] + _r[i]);
        }
        // Jacobi system resolving
        this.jacobi(); // results are stored in _rS
        // computes the coefficients (di, ci, bi, ai) from the results
        for (i = 0; i < _n + 1; i++) {
            // di (degree 0)
            _coefficients[i][0] = _r[i];
            // ci (degree 1)
            _coefficients[i][1] = _r[i + 1] - _r[i] - (_rS[i + 1] + 2 * _rS[i]) / 6;
            // bi (degree 2)
            _coefficients[i][2] = _rS[i] / 2;
            // ai (degree 3)
            _coefficients[i][3] = (_rS[i + 1] - _rS[i]) / 6;
        }
    }

    /**
     * Resolves the equation system by a Jacobi algorithm. The use of the slower
     * Jacobi algorithm instead of Gauss-Seidel is choosen here because Jacobi
     * is assured of to be convergent for this particular equation system, as
     * the system matrix has a strong diagonal.
     */
    protected void jacobi() {
        // local variables
        int i = 0, j = 0, iterations = 0;
        // intermediate arrays
        float[] newX = new float[_n];
        float[] oldX = new float[_n];

        // Jacobi convergence test
        if (!converge()) {
            if (log.isDebugEnabled()) {
                log.debug("Warning : equation system resolving is unstable");
            }
        }
        // init newX and oldX arrays to 0
        for (i = 0; i < _n; i++) {
            newX[i] = 0;
            oldX[i] = 0;
        }
        // main iteration
        while ((this.precision(oldX, newX) > this._minPrecision) && (iterations < this._maxIterations)) {
            System.arraycopy(oldX, 0, newX, 0, _n);
            for (i = 0; i < _n; i++) {
                newX[i] = _B[i];
                for (j = 0; j < i; j++) {
                    newX[i] = newX[i] - (_A[i][j] * oldX[j]);
                }
                for (j = i + 1; j < _n; j++) {
                    newX[i] = newX[i] - (_A[i][j] * oldX[j]);
                }
                newX[i] = newX[i] / _A[i][i];
            }
            iterations++;
        }
        if (this.precision(oldX, newX) < this._minPrecision) {
            if (log.isDebugEnabled()) {
                log.debug("Minimal precision (");
                log.debug(this._minPrecision + ") reached after ");
                log.debug(iterations + " iterations");
            }
        } else if (iterations > this._maxIterations) {
            if (log.isDebugEnabled()) {
                log.debug("Maximal number of iterations (");
                log.debug(this._maxIterations + ") reached");
                log.debug("Warning : precision is only ");
                log.debug("" + this.precision(oldX, newX));
                log.debug(", divergence is possible");
            }
        }
        System.arraycopy(newX, 0, _rS, 1, _n);
    }

    /**
     * Test if the Jacobi resolution of the equation system converges. It's OK
     * if A has a strong diagonal.
     *
     * @return <code>true</code> if equation system converges
     */
    protected boolean converge() {
        boolean converge = true;
        int i = 0, j = 0;
        float lineSum = 0F;

        for (i = 0; i < _n; i++) {
            if (converge) {
                lineSum = 0;
                for (j = 0; j < _n; j++) {
                    lineSum = lineSum + Math.abs(_A[i][j]);
                }
                lineSum = lineSum - Math.abs(_A[i][i]);
                if (lineSum > Math.abs(_A[i][i])) {
                    converge = false;
                    break;
                }
            }
        }
        return converge;
    }

    /**
     * Computes the current precision reached.
     *
     * @param oldX
     *            old values
     * @param newX
     *            new values
     * @return indicator of how different the old and new values are (always
     *         zero or greater, the nearer to zero the more similar)
     */
    protected float precision(float[] oldX, float[] newX) {
        float N = 0F, D = 0F, erreur = 0F;
        int i = 0;

        for (i = 0; i < _n; i++) {
            N = N + Math.abs(newX[i] - oldX[i]);
            D = D + Math.abs(newX[i]);
        }
        if (D != 0F) {
            erreur = N / D;
        } else {
            erreur = Float.MAX_VALUE;
        }
        return erreur;
    }

    /**
     * Computes a (vertical) Y-axis value of the global curve.
     *
     * @param t
     *            abscissa
     * @return computed ordinate
     */
    public float value(float t) {
        int i = 0, splineNumber = 0;
        float abscissa = 0F, result = 0F;

        // verify t belongs to the curve (range [0, _m-1])
        if ((t < 0) || (t > (_m - 1))) {
            if (log.isDebugEnabled()) {
                log.debug("Warning : abscissa " + t + " out of bounds [0, " + (_m - 1) + "]");
            }
            // silent error, consider the curve is constant outside its range
            if (t < 0) {
                t = 0;
            } else {
                t = _m - 1;
            }
        }
        // seek the good interval for t and get the piece of curve on it
        splineNumber = (int) Math.floor(t);
        if (t == (_m - 1)) {
            // the upper limit of the curve range belongs by definition
            // to the last interval
            splineNumber--;
        }
        // computes the value of the curve at the pecified abscissa
        // and relative to the beginning of the right piece of Spline curve
        abscissa = t - splineNumber;
        // the polynomial calculation is done by the (fast) Euler method
        for (i = 0; i < 4; i++) {
            result = result * abscissa;
            result = result + _coefficients[splineNumber][3 - i];
        }
        return result;
    }

    /**
     * Manual check of the curve at the interpolated points.
     */
    public void debugCheck() {
        int i = 0;

        for (i = 0; i < _m; i++) {
            log.info("Point " + i + " : ");
            log.info(_r[i] + " =? " + value(i));
        }
    }

    /**
     * Computes drawable plots from the curve for a given draw space. The values
     * returned are drawable vertically and from the <B>bottom</B> of a Panel.
     *
     * @param width
     *            width within the plots have to be computed
     * @param height
     *            height within the plots are expected to be drawed
     * @return drawable plots within the limits defined, in an array of int (as
     *         many int as the value of the <CODE>width</CODE> parameter)
     */
    public int[] getPlots(int width, int height) {
        int[] plot = new int[width];
        // computes auto-scaling and absolute plots
        float[] y = new float[width];
        float max = java.lang.Integer.MIN_VALUE;
        float min = java.lang.Integer.MAX_VALUE;

        for (int i = 0; i < width; i++) {
            y[i] = value(((float) i) * (_m - 1) / width);
            if (y[i] < min) {
                min = y[i];
            }

            if (y[i] > max) {
                max = y[i];
            }
        }
        if (min < 0) {
            min = 0; // shouldn't draw negative values
        }
        // computes relative auto-scaled plots to fit in the specified area
        for (int i = 0; i < width; i++) {
            plot[i] = Math.round(((y[i] - min) * (height - 1)) / (max - min));
        }
        return plot;
    }

    public void setPrecision(float precision) {
        this._minPrecision = precision;
    }

    public float getPrecision() {
        return this._minPrecision;
    }

    public void setToDefaultPrecision() {
        this._minPrecision = DEFAULT_PRECISION;
    }

    public float getDefaultPrecision() {
        return DEFAULT_PRECISION;
    }

    public void setMaxIterations(int iterations) {
        this._maxIterations = iterations;
    }

    public int getMaxIterations() {
        return this._maxIterations;
    }

    public void setToDefaultMaxIterations() {
        this._maxIterations = DEFAULT_MAX_ITERATIONS;
    }

    public int getDefaultMaxIterations() {
        return DEFAULT_MAX_ITERATIONS;
    }

}
