@echo off

call "%~dp0.\env.bat"

java -jar "%PROJECT_HOME%\dist\nkstool.jar" %*

echo.
echo.
pause
