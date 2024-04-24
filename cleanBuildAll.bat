@echo off 

rem This script automatically executes `gradlew clean` and `gradlew build`
rem for each gradle project in this directory.

rem **Explanation of important parts:**
rem @echo off  - Prevents each line of the script from being displayed as it executes.
rem FOR /D %%D IN (*) DO - Iterates over each directory (%%D) within the current folder.
rem IF EXIST %%D\gradlew.bat - Checks for the presence of 'gradlew.bat' inside the directory. 

echo ==================================================
echo Starting...

rem Iterate directories in the current directory
FOR /D %%D IN (*) DO (
	rem Check if `gradlew.bat` is present in the next directory
    IF EXIST %%D\gradlew.bat (
		echo ==================================================
		rem if `gradlew.bat` is present, enter the directory...
        pushd %%D
		
		rem ...and then clean build the project by executing `gradlew.bat`...
        call gradlew clean build
		
		rem ...and then leave the directory, and continue iterating.
        popd
    )
)
echo ==================================================

rem Pause after finishing
echo Done. Press any key to continue...
pause > nul