@echo off
chcp 65001 >nul
cd /d "%~dp0"
echo ==== START %date% %time% ==== > log.txt
echo CWD: %CD% >> log.txt
echo Running: MiniMilitary3D.exe >> log.txt
echo ==== OUTPUT ==== >> log.txt

MiniMilitary3D.exe >> log.txt 2>&1
set EXITCODE=%ERRORLEVEL%

echo. >> log.txt
echo ==== EXIT CODE: %EXITCODE% ==== >> log.txt
echo ==== END %date% %time% ==== >> log.txt

echo.
echo ============================================
echo   ЛОГ ЗАПИСАН В log.txt
echo   Код выхода: %EXITCODE%
echo ============================================
echo.
type log.txt
echo.
pause
