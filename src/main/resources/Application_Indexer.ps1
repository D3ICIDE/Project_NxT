$sh = New-Object -ComObject WScript.Shell

# --- SCAN 1: Legacy Win32 Recursive Folder Search ---
# Broadened to the Start Menu root (not just \Programs) and Desktops
$paths = @(
    "$env:ProgramData\Microsoft\Windows\Start Menu",
    "$env:APPDATA\Microsoft\Windows\Start Menu",
    "$env:PUBLIC\Desktop",
    "$env:USERPROFILE\Desktop"
)

Get-ChildItem -Path $paths -Filter *.lnk -Recurse -ErrorAction SilentlyContinue | ForEach-Object {
    try {
        $link = $sh.CreateShortcut($_.FullName)
        # Strip quotes to prevent -like "*.exe" from failing
        $exePath = $link.TargetPath.Replace('"', '')

        if ($exePath) {
            $processName = [System.IO.Path]::GetFileNameWithoutExtension($exePath)

            # CRITICAL FIX: JetBrains Toolbox Wrapper Extraction
            # If the shortcut points to the toolbox, extract the real process from the arguments
            if ($processName -eq "jetbrains-toolbox" -and $link.Arguments) {
                foreach ($token in $link.Arguments.Split('"')) {
                    if ($token -like "*.exe") {
                        $processName = [System.IO.Path]::GetFileNameWithoutExtension($token.Trim())
                        break
                    }
                }
            }

            $name = $_.BaseName
            Write-Output "WIN32|$name|$exePath|$processName"
        }
    } catch {}
}

# --- SCAN 2: Modern Apps & StartApps Failsafe ---
$appxPackages = Get-AppxPackage -ErrorAction SilentlyContinue
$manifestCache = @{}
foreach ($pkg in $appxPackages) {
    $manifestCache[$pkg.PackageFamilyName] = $pkg.InstallLocation
}

$apps = Get-StartApps
foreach ($app in $apps) {
    $id = $app.AppID
    $name = $app.Name

    # Failsafe: Process .lnk files directly from Get-StartApps just like your old legacy map did!
    if ($id -like "*.lnk") {
        try {
            $link = $sh.CreateShortcut($id)
            $exePath = $link.TargetPath.Replace('"', '')
            if ($exePath) {
                $processName = [System.IO.Path]::GetFileNameWithoutExtension($exePath)

                if ($processName -eq "jetbrains-toolbox" -and $link.Arguments) {
                    foreach ($token in $link.Arguments.Split('"')) {
                        if ($token -like "*.exe") {
                            $processName = [System.IO.Path]::GetFileNameWithoutExtension($token.Trim())
                            break
                        }
                    }
                }

                Write-Output "WIN32|$name|$id|$processName"
            }
        } catch {}
    } else {
        $processName = "UNKNOWN"
        $parts = $id.Split('!')

        if ($parts.Length -eq 2) {
            $pkgFamily = $parts[0]
            $appIdNode = $parts[1]
            if ($manifestCache.ContainsKey($pkgFamily)) {
                $manifestPath = "$($manifestCache[$pkgFamily])\AppxManifest.xml"
                if (Test-Path $manifestPath) {
                    [xml]$manifest = Get-Content $manifestPath -ErrorAction SilentlyContinue
                    $node = $manifest.Package.Applications.Application | Where-Object { $_.Id -eq $appIdNode }
                    if ($node -and $node.Executable) {
                        $processName = [System.IO.Path]::GetFileNameWithoutExtension($node.Executable)
                    }
                }
            }
        }

        # System App process fallbacks
        if ($id -eq "Microsoft.Windows.TaskManagement") { $processName = "taskmgr" }
        if ($id -eq "Windows.ImmersiveControlPanel_cw5n1h2txyewy!microsoft.windows.immersivecontrolpanel") { $processName = "SystemSettings" }

        Write-Output "MODERN|$name|$id|$processName"
    }
}