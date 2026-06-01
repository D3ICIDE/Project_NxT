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
        try{
            pb.redirectErrorStream(true);
            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("|")) {
                    String[] parts = line.split("\\|");
                    if (parts.length == 2) {
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
                return installedApp; // Returns the full key name like "adobe photoshop 2025"
            }
        }

        // 3. Nothing found
        return null;
    }
}
