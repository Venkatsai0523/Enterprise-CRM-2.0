@echo off
setlocal
set MAVEN_PROJECTBASEDIR=%~dp0
if "%MAVEN_PROJECTBASEDIR:~-1%"=="\" set MAVEN_PROJECTBASEDIR=%MAVEN_PROJECTBASEDIR:~0,-1%

set JAVA_HOME=C:\Program Files\Java\jdk-25.0.2
set MAVEN_HOME=%MAVEN_PROJECTBASEDIR%\.mvn\dist\apache-maven-3.9.9

if exist "%MAVEN_HOME%\bin\mvn.cmd" (
    call "%MAVEN_HOME%\bin\mvn.cmd" %*
) else (
    echo Error: Maven binaries not found in %MAVEN_HOME%
    exit /b 1
)
endlocal
