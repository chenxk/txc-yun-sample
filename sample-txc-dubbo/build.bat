@echo off
call mvn package
copy %cd%\..\lib\txc-client-2.0.72.jar %cd%\client\lib
cd client/bin
setlocal enabledelayedexpansion
set "str1=0.2.1-SNAPSHOT.jar"
set "str2=0.2.1-SNAPSHOT.jar;^"^%%REPO^%%^^"\txc-client-2.0.72.jar"
for %%i in (*.bat) do (
for /f "usebackq delims=" %%a in ("%%~i") do (
set "var=%%a"
echo !var:%str1%=%str2%!>>"%%~ni.tmp")
move /y "%%~dpni.tmp" "%%~i")