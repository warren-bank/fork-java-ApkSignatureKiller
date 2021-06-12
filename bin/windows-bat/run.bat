@echo off

call "%~dp0.\env.bat"

cd "%PROJECT_HOME%\dist"

java -jar nkstool.jar

echo.
pause
