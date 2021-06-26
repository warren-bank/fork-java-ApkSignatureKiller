@echo off

call "%~dp0.\env.bat"

set SOURCEPATH=%PROJECT_HOME%\src;%PROJECT_HOME%\libs
set MANIFEST=%PROJECT_HOME%\src\META-INF\MANIFEST.MF

set build_dir=%PROJECT_HOME%\out
set final_jar=%PROJECT_HOME%\dist\nkstool.jar

set javac_opts=
set javac_opts=%javac_opts% -nowarn -sourcepath "%SOURCEPATH%"
set javac_opts=%javac_opts% --add-exports "java.base/sun.security.pkcs=ALL-UNNAMED"
set javac_opts=%javac_opts% -J-Dfile.encoding=utf8
set javac_opts=%javac_opts% -d "%build_dir%"

if exist "%build_dir%" rmdir /Q /S "%build_dir%"
mkdir "%build_dir%"

javac %javac_opts% "%PROJECT_HOME%/src/cc/binmt/signature/NKillSignatureTool.java"

jar cvfm "%final_jar%" "%MANIFEST%" -C "%build_dir%" .

echo.
pause
