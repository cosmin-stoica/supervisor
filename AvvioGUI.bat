@echo off
:: Verifica se si dispone di privilegi amministrativi
net session >nul 2>&1
if %errorlevel% neq 0 (
    :: Se non si dispone di privilegi, richiedi l'elevazione
    echo Richiesta di privilegi amministrativi...
    goto UACPrompt
) else (
    goto :RunJava
)

:UACPrompt
    echo Set UAC = CreateObject^("Shell.Application"^) > "%temp%\getadmin.vbs"
    set params = %*:"=""
    echo UAC.ShellExecute "cmd.exe", "/c %~s0 %params%", "", "runas", 1 >> "%temp%\getadmin.vbs"
    "%temp%\getadmin.vbs"
    del "%temp%\getadmin.vbs"
    exit /B

:RunJava
    echo Esecuzione di Java...
    java -jar Supervisor.jar

:: Aggiungi il comando pause per mantenere aperto il prompt dei comandi
pause