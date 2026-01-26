@echo off
setlocal enabledelayedexpansion

echo   UI-Utils Multi-Version Build Script
echo.

call gradlew.bat --stop 2>nul

md "releases" 2>nul

set "versions[0]=1.21,1.21+build.9,0.102.0+1.21"
set "versions[1]=1.21.1,1.21.1+build.3,0.105.0+1.21.1"
set "versions[2]=1.21.2,1.21.2+build.1,0.106.0+1.21.2"
set "versions[3]=1.21.3,1.21.3+build.2,0.114.0+1.21.3"
set "versions[4]=1.21.4,1.21.4+build.8,0.114.0+1.21.4"
set "versions[5]=1.21.5,1.21.5+build.1,0.127.1+1.21.5"
set "versions[6]=1.21.6,1.21.6+build.1,0.128.1+1.21.6"
set "versions[7]=1.21.7,1.21.7+build.8,0.128.2+1.21.7"
REM Versions 1.21.8-1.21.11 - yarn mappings incomplete

set "total=8"
set "success=0"
set "failed=0"

copy "gradle.properties" "gradle.properties.backup" >nul

for /L %%i in (0,1,7) do (
    for /f "tokens=1,2,3 delims=," %%a in ("!versions[%%i]!") do (
        set "mc_ver=%%a"
        set "yarn=%%b"
        set "fabric=%%c"
        
        echo.
        echo Building for Minecraft !mc_ver!
        
        (
            echo # Done to increase the memory available to gradle.
            echo org.gradle.jvmargs=-Xmx4G
            echo org.gradle.parallel=true
            echo.
            echo # Fabric Properties
            echo minecraft_version=!mc_ver!
            echo yarn_mappings=!yarn!
            echo loader_version=0.18.4
            echo.
            echo # Fabric API
            echo fabric_version=!fabric!
            echo # Mod Properties
            echo mod_version = 2.4.1
            echo.
            echo maven_group = com.ui_utils
            echo archives_base_name = ui_utils
        ) > gradle.properties
        
        call gradlew.bat --stop 2>nul
        timeout /t 2 /nobreak >nul
        
        call gradlew.bat build --quiet --no-daemon
        
        if !errorlevel! equ 0 (
            if exist "build\libs\ui_utils-2.4.1.jar" (
                copy "build\libs\ui_utils-2.4.1.jar" "releases\ui_utils-2.4.1-mc!mc_ver!.jar" >nul
                echo [OK] Minecraft !mc_ver! - ui_utils-2.4.1-mc!mc_ver!.jar
            ) else (
                echo [OK] Minecraft !mc_ver! - JAR built but copy failed
            )
            set /a success+=1
        ) else (
            echo [FAIL] Minecraft !mc_ver! build failed
            set /a failed+=1
        )
    )
)

move /y "gradle.properties.backup" "gradle.properties" >nul

echo.
echo   Success: !success!
echo   Failed:  !failed!
echo.
echo   JARs are in: releases\
echo.

dir /b "releases\*.jar" 2>nul

pause
