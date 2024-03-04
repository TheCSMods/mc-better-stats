:: Script and path setup
@echo off
@cd /D %~dp0
cd ..\..\..\..

:: Ensure 'gradlew.bat' is present
if not exist gradlew.bat (
	echo "[Error] gradlew.bat not found!"
	goto end
)

:: Execute commands
call gradlew.bat clean
call gradlew.bat build

:end
pause