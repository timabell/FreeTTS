@ECHO off

IF "%JAVA_HOME%"=="" GOTO java_home_missing

%JAVA_HOME%\bin\java -version
%JAVA_HOME%\bin\java -cp ..\..\..\lib\demo.jar;..\..\..\classes;..\..\..\lib\cmuawb.jar;..\..\..\lib\cmukal16.jar;..\..\..\lib\cmukal8.jar;..\..\..\lib\cmulex.jar;..\..\..\lib\cmutimelex.jar;..\..\..\lib\freetts.jar;..\..\..\lib\jsapi.jar FreeTTSHelloWorld
GOTO end

:java_home_missing
ECHO Error: JAVA_HOME environment variable is not defined.

:end
@ECHO on
