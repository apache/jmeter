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
