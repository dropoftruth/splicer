# splicer
A specialized set of tools for Hadoop eco system that enables very fast and very complex analytics on wide tables (thousands of attributes). The goal is to enable evaluation of complex ad-hoc expressions across billions of rows minimizing the latency and storage requirement and on a sample of larger data set. 

Goal is to make available tool(s) for following platforms

* Redis
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
* Efficient grid wide caching (zero copy framework)
