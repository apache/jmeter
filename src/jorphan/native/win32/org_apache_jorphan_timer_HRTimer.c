/*
 * ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002,2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 * if any, must include the following acknowledgment:
 * "This product includes software developed by the
 * Apache Software Foundation (http://www.apache.org/)."
 * Alternately, this acknowledgment may appear in the software itself,
 * if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 * "Apache JMeter" must not be used to endorse or promote products
 * derived from this software without prior written permission. For
 * written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 * "Apache JMeter", nor may "Apache" appear in their name, without
 * prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
/* ------------------------------------------------------------------------- */
/*
 * A win32 implementation of JNI methods in com.vladium.utils.timing.HRTimer
 * class. The author compiled it using Microsoft Visual C++ but the code
 * should be easy to use with any compiler for win32 platform.
 *
 * For simplicity, this implementaion assumes JNI 1.2+ and omits error handling.
 *
 * (C) 2002, Vladimir Roubtsov [vroubtsov@illinoisalumni.org]
 */

/* ------------------------------------------------------------------------- */

/*
 * @author (C) <a href="mailto:vroubtsov@illinoisalumni.org">Vlad Roubtsov</a>, 2002
 * @author Originally published in <a href="http://www.javaworld.com/javaworld/javaqa/2003-01/01-qa-0110-timing.html">JavaWorld</a>
 */
 
#if !defined NDEBUG
#include <stdio.h>
#endif // NDEBUG

#include <windows.h>

#include "org_apache_jorphan_timer_HRTimer.h"

// scale factor for converting a performancce counter reading into milliseconds:
static jdouble s_scaleFactor;

/* ------------------------------------------------------------------------- */

/*
 * This method was added in JNI 1.2. It is executed once before any other
 * methods are called and is ostensibly for negotiating JNI spec versions, but
 * can also be conveniently used for initializing variables that will not
 * change throughout the lifetime of this process.
 */
JNIEXPORT jint JNICALL
JNI_OnLoad (JavaVM * vm, void * reserved)
{
    LARGE_INTEGER counterFrequency;

    QueryPerformanceFrequency (& counterFrequency);

    // NOTE: counterFrequency will be zero for a machine that does not have
    // support for a high-resolution counter. This is only likely for very
    // old hardware but for a robust implementation you should handle this
    // case.

#if !defined NDEBUG
    printf ("PCFrequency called: %I64d\n", counterFrequency.QuadPart);
#endif

    s_scaleFactor = counterFrequency.QuadPart / 1000.0;


    return JNI_VERSION_1_2;
}
/* ......................................................................... */

/*
 * Class:     org_apache_jorphan_timer_HRTimer
 * Method:    getTime
 * Signature: ()D
 */
JNIEXPORT jdouble JNICALL
Java_org_apache_jorphan_timer_HRTimer_getTime (JNIEnv * e, jclass cls)
{
    LARGE_INTEGER counterReading;

    QueryPerformanceCounter (& counterReading);

    return counterReading.QuadPart / s_scaleFactor;
}

/* ------------------------------------------------------------------------- */
/* end of file */
