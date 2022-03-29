REM First make the bin folder if it doesn't exist.
IF NOT EXIST ".\bin" MKDIR ".\bin"

REM Run the compilation process.
REM This will require JDK 8 specifically in order to work.
DIR /S /B .\src\*.java > .TMPSRC
javac -encoding utf8 -sourcepath .\src -d .\bin -cp .\lib\* @.TMPSRC
