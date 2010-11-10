This directory contains a number of scripts that are used to collect
benchmarks for FreeTTS. They are described here.

Script: 'benchmark"
    Collects a standard set of benchmarks and places the results in the
    results directory.

Script: 'collectMetrics'
     Collects benchmarks for a number of input sizes and voices.  Sends
     output to stadout.  Typically run by the benchmark script.
   
Script 'dukesays'
     Generates a wave dump for a standard utterance
     
Script 'jprof'
   Runs FreeTTS under the Java profiler. Collects profile information
   in log.txt. Also prints out heap summary information.

Script 'lines'
   Collects time to first sample information for inputs of various
   sizes (client and server VM).
