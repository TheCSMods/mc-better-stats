@echo off 

rem **Explanation of important parts:**
rem @echo off  - Prevents each line of the script from being displayed as it executes.
rem FOR /D %%D IN (*) DO - Iterates over each directory (%%D) within the current folder.
rem IF EXIST %%D\gradlew.bat - Checks for the presence of 'gradlew.bat' inside the directory. 

FOR /D %%D IN (*) DO (
    IF EXIST %%D\gradlew.bat (
        echo Building Gradle project in %%D
        pushd %%D
        call gradlew clean
        call gradlew build
        popd
    )
)