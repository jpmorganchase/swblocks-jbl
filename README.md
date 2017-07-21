[![Build Status](https://travis-ci.org/jpmorganchase/swblocks-jbl.svg?branch=master)](https://travis-ci.org/jpmorganchase/swblocks-jbl)

# swblocks-jbl

## Overview

swblocks-jbl library is a set of core Java utilities based on Java 8 which provides as set of core error handling tools and additional utilites used across the swblocks projects.
It has been written to avoid the problem of including a number of large external dependencies which are only required for one or two classes.
This has resulted in a few duplicatations of populate utility classes (CollectionUtils) within the library, but this is considered preferable to including lots of separate dependencies.
If these utilities are moved in the main JDK, then they will be removed from the swblocks-jbl library. 

#### Error Handling Principles
swblocks-jbl provides support for the following Error Handling principles.
1. All relevant and most detailed error information should arrive at the handling point and it should never be lost and / or ignored as part of the EH logic and / or during the error propagation process; usually most detailed error information should be captured at the error origination point / time and then no error information should be stripped and / or lost as part of the error propagation process; when possible the error and its context / data should also be enhanced as part of the error propagation process, but it should be done additively and without loss of any existing error information / context
2. The error propagation process should never terminate early without the originated error (with its entire context) arrives at the target error handler (i.e. no swallowing of errors, partial or full, is permitted)
3. EH must follow the EH neutrality principle otherwise it is difficult to do correct error handling in a generic context.
The EH neutrality principle states that since the majority of the code does not have sufficient context to understand and handle the majority of the errors, it should not do anything to interfere with the error propagation and the error handling process. In the majority of the cases the only thing the code needs to do wrt to expecting errors is to do proper error handling cleanup only, so the code remains in consistent state and it works correctly in both the normal and error handling paths:
    1. Error remapping, even when the original error is chained, should be generally avoided (as it constitutes interference with the error handling and violates the neutrality principle)
    2. Error remapping, even when the original error is chained, should not be used as a mechanism for error information enhancement on the error propagation path. Rather an exception hierarchy should be used where the base exception supports mechanism for adding information / properties to it in generic fashion (e.g. see the design of Boost.Exception)
    3. Error remapping can be used very carefully in some well understood strategic EH wrappers where leaving the original error cannot be supported for some reason (e.g. as it will lead to data loss or inability to do error enhancement)
4. Error handling cleanup should be done correctly and systematically throughout the code base, so each line of the code is always exception safe, ideally the strong or nothrow guarantees for exception safety should be provided, but at minimum the basic guarantee should always be provided
    1. Using RAII systematically in C++ / C++11 and using try/finally blocks liberally in languages such as Java/C#
5. Information added to exception objects, either at origination time or via enhancement, will be outside of the control of the code which added it and could end up in logs or stored in other places, so one should be very careful to not add sensitive information from security and privacy perspective (e.g. user name & password, social security #, etc) that could potentially be misused or open the system easily for security attacks or information leakage. Sensitive data should always be redacted appropriately before any information is added to exception objects.
6. All sections of the code that can be executed as part of the error propagation path (i.e. finally blocks, close() / dispose() methods, destructors) should implement the nothrow exception safety guarantee (i.e. they should be defined 'nothrow sections' - see above) and where there is a language support for that it should be used (e.g. 'noexcept' in C++11); failure to do so will result in masking and swallowing the original exception being thrown and thus violating principle #1 above (in addition to often causing memory leaks, inconsistent state and various other severe problems); ultimately if an error has to or can happen in such nothrow section the possible options are the following:
    1. log the error and swallow it
    2. log the error and attach it to the original error, but continue the propagation of the original error
    3. log the error and abort the application / operation
7. Errors should never be used to control and express normal program flow (because of the potential very significant cost on the error propagation path)
8. Handling of errors should be done rarely and when done it should be only for very specific errors that are expected in that part of the code and the handling code knows how to handle the errors meaningfully without violating principle #1 above; typically handling of errors in generic way should be done in a top level exception handler only
9. Errors in the category #2 above (i.e. developer usage errors and bugs) should be left unhandled (so they can be troubleshooted properly; attempts to handle them impedes troubleshooting and especially RCA - i.e. root cause analysis)
10. As a result of the 4 principles / rules above an obvious corollary is that real error handlers (i.e. the kind that terminates the error propagation and do repair + continue) should be observed fairly rarely in a code base where EH is done according to these principles and rules; most of the error handlers, and especially error handlers which handle errors generically, should be used only to do error enhancement + resume of the error propagation of the original error (i.e. re-throw the original exception after the additional data / context is appended)

#### Execution Guards
swblocks-jbl provides support for the ability to retry operations, especially out of process operations, with multiple options for retrying.

#### Testing helpers
swblocks-jbl provides support for unit testing utilities to extend test coverage of singletons and POJO's. 

## License

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this library except in compliance with the License. You may obtain a copy of the License at:

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.

See the [LICENSE](LICENSE) file for additional license information

 
## Java Build

The project is built with [Gradle](http://gradle.org/) using this [build.gradle](build.gradle) file.

You require the following to build swblocks-jbl:

* Latest stable [Oracle JDK 8](http://www.oracle.com/technetwork/java/)

Default target provides a full clean, build, and install into local maven repository
