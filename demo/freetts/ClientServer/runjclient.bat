@ECHO off

IF "%JAVA_HOME%"=="" GOTO java_home_missing

%JAVA_HOME%\bin\java -version
%JAVA_HOME%\bin\java -cp ..\..\classes;..\..\..\lib\cmu_time_awb.jar;..\..\..\lib\cmu_us_kal.jar;..\..\..\lib\cmulex.jar;..\..\..\lib\cmutimelex.jar;..\..\..\lib\en_us.jar;..\..\..\lib\freetts.jar;..\..\..\lib\jsapi.jar -Ddebug=false -Dmetrics=false -Dserver=localhost -Dport=5555 -DsampleRate=16000 Client
GOTO end

:java_home_missing
ECHO Error: JAVA_HOME environment variable is not defined.

:end
@ECHO on
