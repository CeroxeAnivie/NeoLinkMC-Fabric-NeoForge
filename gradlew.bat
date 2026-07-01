@chcp 65001 >nul
@echo off
setlocal

set "JAVA_HOME=C:\Program Files\Java\graalvm-jdk-25.0.3+9.1"
set "APP_HOME=%~dp0"
if "%APP_HOME%"=="" set "APP_HOME=."
for %%i in ("%APP_HOME%") do set "APP_HOME=%%~fi"

set "JAVA_EXE=%JAVA_HOME%\bin\java.exe"
if not exist "%JAVA_EXE%" (
  echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME% 1>&2
  exit /b 1
)

set "DEFAULT_JVM_OPTS=-Xmx64m -Xms64m"
set "CLASSPATH=%APP_HOME%\gradle\wrapper\gradle-wrapper.jar"

"%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %GRADLE_OPTS% "-Dorg.gradle.appname=gradlew" -classpath "%CLASSPATH%" -jar "%APP_HOME%\gradle\wrapper\gradle-wrapper.jar" %*
set "EXIT_CODE=%ERRORLEVEL%"
endlocal & exit /b %EXIT_CODE%
