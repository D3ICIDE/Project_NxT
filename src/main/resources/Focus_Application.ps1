# Focus-App.ps1
param (
    [Parameter(Mandatory=$true)]
    [string]$ProcessName
)

# Strip .exe if it was accidentally passed, as Get-Process doesn't use it
$ProcessName = $ProcessName -replace '\.exe$', ''

# 1. Load Windows API to check current focus
Add-Type @"
    using System;
    using System.Runtime.InteropServices;
    public class User32 {
        [DllImport("user32.dll")]
        public static extern IntPtr GetForegroundWindow();
        [DllImport("user32.dll")]
        public static extern uint GetWindowThreadProcessId(IntPtr hWnd, out uint lpdwProcessId);
    }
"@

$activeHwnd = [User32]::GetForegroundWindow()
$activePid = 0
[User32]::GetWindowThreadProcessId($activeHwnd, [ref]$activePid)

# 2. Find the target process (filter for instances that actually have a visible window)
$targetProcess = Get-Process -Name $ProcessName -ErrorAction SilentlyContinue | Where-Object { $_.MainWindowTitle -ne "" } | Select-Object -First 1

if ($targetProcess) {
    # 3. Check if it is already in focus
    if ($targetProcess.Id -eq $activePid) {
        Write-Host "Process is already in focus. Exiting."
        Exit 0
    }

    # 4. Use NirCmd to target the exact main window title
   # $exactTitle = $targetProcess.MainWindowTitle
    #Start-Process "nircmd.exe" -ArgumentList "win activate title `"$exactTitle`"" -NoNewWindow
} else {
    # Fallback: If no window title is found, try standard process activation
    #Start-Process "nircmd.exe" -ArgumentList "win activate process `"$ProcessName.exe`"" -NoNewWindow
    Exit 1
}