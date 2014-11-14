Time Period
===========

Time Period has two main classes: TimePeriod and TimePeriodFormat. A TimePeriod object represents a period of time, i.e. a length of time without context of the beginning or end of the period. TimePeriodFormat is used for formatting TimePeriods as strings and parsing TimePeriods from strings.

Third-party dependencies
------------------------
It's dependent on the following jar and there is currently no dependency management in place. The ant build file will look for this in {project root}/lib.
* junit-4.8.jar (JUnit for unit tests)

Prerequisites
-------------
* Java SDK 8
* ant, the Java-based make tool
* junit jar (only needed for running unit tests)

Building
--------
the "jar" ant task will compile the classes into a jar (without the tests)
