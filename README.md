# splicer
A specialized set of tools for Hadoop eco system that enables very fast and very complex analytics on wide tables (thousands of attributes) comprising of binary and ternary values. The goal is to enable evaluation of complex ad-hoc boolean expressions across billions of rows minimizing the latency and storage requirement and on a sample of larger data set. 

Some of tools where this could be available

* HBase
* Apache Phoenix
* Hive
* Pig
* Spark
* Flink
* Presto

Design goals (draft)
* Minimise the storage of the data to be analysed 
* Provide Serde functions to encode and decode to efficient data structure
* Enable support for real time streaming
* Constant time operation on fetching an attribute value for evaluation
* Efficient grid wide caching for evaluated expressions
