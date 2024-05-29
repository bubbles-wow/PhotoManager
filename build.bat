@echo off
setlocal enabledelayedexpansion
set CLASSPATH=lib\hutool-all-5.8.27.jar
set SOURCE_DIR=src
set TARGET_DIR=bin

echo 正在构建PhotoManager...
rmdir /s /q bin
mkdir bin\
cd bin
jar -xf ..\lib\hutool-all-5.8.27.jar
cd ..

mkdir bin\res
copy src\res bin\res > nul

for /r src %%f in (*.java) do (
    set FILES=!FILES! %%f
)
javac -d bin -cp lib\hutool-all-5.8.27.jar !FILES!

jar cfm PhotoManager.jar src\META-INF\MANIFEST.MF -C bin .
rmdir /s /q bin

echo 已构建PhotoManager.jar，是否运行？(y/n)
set /p choice=
if %choice%==y goto run
if %choice%==Y goto run
goto end
:run
start /min java -jar PhotoManager.jar
:end