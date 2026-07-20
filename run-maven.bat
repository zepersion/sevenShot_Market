@echo off
set JAVA_HOME=C:\Program Files\Java\jdk-23
set PATH=C:\Program Files\Java\jdk-23\bin;%PATH%
echo JAVA_HOME=%JAVA_HOME%
java -version
cd /d "E:\SevenShot Market\SevenShot_Market"
call mvnw.cmd dependency:resolve
