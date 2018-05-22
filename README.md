Time Period
===========

Time Period has two main classes: TimePeriod and TimePeriodFormat. A TimePeriod object represents a period of time, i.e. a length of time without context of the beginning or end of the period. TimePeriodFormat is used for formatting TimePeriods as strings and parsing TimePeriods from strings.

Prerequisites
-------------
* Java SDK 8
* ant, the Java-based make tool

### adding as a dependency

Page on [search.maven.org](http://search.maven.org/#artifactdetails%7Ccom.adashrod.timeperiod%7Ctimeperiod%7C0.1.0%7Cpom)

Page on [mvnrepository.com](http://mvnrepository.com/artifact/com.adashrod.timeperiod/timeperiod)

##### Maven:
~~~~
<dependency>
    <groupId>com.adashrod.timeperiod</groupId>
    <artifactId>timeperiod</artifactId>
    <version>0.1.0</version>
</dependency>
~~~~
##### Ivy:
~~~~
<dependency org="com.adashrod.timeperiod" name="timeperiod" rev="0.1.0" />
~~~~

Building
--------
the "create-jar.runtime" ant task will compile the classes into a jar (without the tests)
