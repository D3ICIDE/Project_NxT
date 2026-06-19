package com.abyss.amadeus.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;

public class App_Index {
    public static final HashMap<String, String> appMap = new HashMap<>();

    public void indexBuilder() {
        // Step 1: Pre-seed standard Windows system utilities
        seedSystemApps();

        // Step 2: Proceed with scanning the standard Start Menu locations
        String command = """
                $paths = @("$env:ProgramData\\Microsoft\\Windows\\Start Menu\\Programs",
                            "$env:APPDATA\\Microsoft\\Windows\\Start Menu\\Programs");
                              $sh = New-Object -ComObject WScript.Shell;
                              Get-ChildItem -Path $paths -Filter *.lnk -Recurse -ErrorAction SilentlyContinue | ForEach-Object {
                              $link = $sh.CreateShortcut($_.FullName);
                              if ($link.TargetPath -and $link.TargetPath.EndsWith('.exe')) {
                              Write-Output ($_.BaseName + '|' + $link.TargetPath);
                              }
                             }
                """;
        String encodedScript = Base64.getEncoder().encodeToString(command.getBytes(StandardCharsets.UTF_16LE));

        ProcessBuilder pb = new ProcessBuilder("powershell.exe", "-NoProfile", "-ExecutionPolicy", "Bypass", "-EncodedCommand", encodedScript);
        pb.redirectErrorStream(true);
        try {
            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("|")) {
                    String[] parts = line.split("\\|");
                    if (parts.length == 2) {
                        // Put inside the map (normalized to lower case)
                        appMap.put(parts[0].trim().toLowerCase(), parts[1].trim().toLowerCase());
                    }
                } else {
                    System.out.println("PS System Message: " + line);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Seeds the index with core Windows system applications that lack traditional
     * Start Menu shortcuts but are globally accessible via System32 execution aliases.
     */
    private void seedSystemApps() {
        String windir = System.getenv("WINDIR"); // Dynamically catches C:\Windows
        if (windir == null) windir = "C:\\Windows";

        String system32 = windir + "\\System32\\";
        String mainWin = windir + "\\";

        // Map standard user queries to their native System32 binary equivalents
        appMap.put("notepad", system32 + "notepad.exe");
        appMap.put("command prompt", system32 + "cmd.exe");
        appMap.put("cmd", system32 + "cmd.exe");
        appMap.put("task manager", system32 + "taskmgr.exe");
        appMap.put("taskmgr", system32 + "taskmgr.exe");
        appMap.put("paint", system32 + "mspaint.exe");
        appMap.put("mspaint", system32 + "mspaint.exe");
        appMap.put("registry editor", mainWin + "regedit.exe");
        appMap.put("regedit", mainWin + "regedit.exe");
        appMap.put("file explorer", mainWin + "explorer.exe");
        appMap.put("explorer", mainWin + "explorer.exe");

        // Windows 10/11 redirects calc.exe to the UWP Calculator interface automatically!
        appMap.put("calculator", system32 + "calc.exe");
        appMap.put("calc", system32 + "calc.exe");
    }

    public String findAppKey(String searchName) {
        if (searchName == null) return null;

        String cleanSearchName = searchName.toLowerCase().trim();

        // 1. Try an exact match first
        if (appMap.containsKey(cleanSearchName)) {
            return cleanSearchName;
        }

        // 2. Try fuzzy matching (partial containment)
        for (String installedApp : appMap.keySet()) {
            if (installedApp.contains(cleanSearchName) || cleanSearchName.contains(installedApp)) {
                return installedApp;
            }
        }

        // 3. Nothing found
        return null;
    }
}