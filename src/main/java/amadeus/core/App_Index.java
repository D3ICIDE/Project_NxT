package amadeus.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.regex.Pattern;

public class App_Index {
    // Index 1: Legacy Win32 Apps (Friendly Name -> Executable Path)
    public static final HashMap<String, String> win32PathIndex = new HashMap<>();

    // Index 2: Modern Apps & Settings URIs (Friendly Name -> AppID or URI)
    public static final HashMap<String, String> modernShellIndex = new HashMap<>();

    // Index 3: Universal Window Controller (Friendly Name -> Process Name for NirCmd)
    public static final HashMap<String, String> processMap = new HashMap<>();

    public void indexBuilder() {
        seedSystemApps();

        String scriptPath = "src/main/resources/Application_Indexer.ps1";
        ProcessBuilder pb = new ProcessBuilder("powershell.exe", "-NoProfile", "-ExecutionPolicy", "Bypass", "-File", scriptPath);
        pb.redirectErrorStream(true);

        try {
            System.out.println("Initiating App Indexing:");
            System.out.println("-------------------------------------------------------------");
            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;

            while ((line = reader.readLine()) != null) {
                // Split with a limit of 4, just in case an app name contains a pipe symbol natively
                if (line.contains("|")) {
                    String[] parts = line.split("\\|", 4);
                    if (parts.length == 4) {
                        String type = parts[0].trim();
                        String friendlyName = parts[1].trim().toLowerCase();
                        String launchTarget = parts[2].trim();
                        String processName = parts[3].trim();

                        if (type.equals("WIN32")) {
                                win32PathIndex.putIfAbsent(friendlyName, launchTarget.toLowerCase());
                        } else if (type.equals("MODERN")) {
                            modernShellIndex.put(friendlyName, launchTarget);
                        }

                        if (!processName.equals("UNKNOWN")) {
                            processMap.put(friendlyName, processName);
                        }
                    }
                }
            }
            System.out.println("App Indexing Completed");
            System.out.println("-----------------------------------------------------------------------");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void seedSystemApps() {
        String windir = System.getenv("WINDIR") != null ? System.getenv("WINDIR") : "C:\\Windows";
        String system32 = windir + "\\System32\\";
        String mainWin = windir + "\\";

        // Pre-seed Win32
        win32PathIndex.put("notepad", system32 + "notepad.exe");
        processMap.put("notepad", "notepad");

        win32PathIndex.put("command prompt", system32 + "cmd.exe");
        win32PathIndex.put("cmd", system32 + "cmd.exe");
        processMap.put("command prompt", "cmd");
        processMap.put("cmd", "cmd");

        win32PathIndex.put("task manager", system32 + "taskmgr.exe");
        win32PathIndex.put("taskmgr", system32 + "taskmgr.exe");
        processMap.put("task manager", "taskmgr");
        processMap.put("taskmgr", "taskmgr");

        win32PathIndex.put("paint", system32 + "mspaint.exe");
        processMap.put("paint", "mspaint");

        win32PathIndex.put("file explorer", mainWin + "explorer.exe");
        win32PathIndex.put("explorer", mainWin + "explorer.exe");
        processMap.put("file explorer", "explorer");
        processMap.put("explorer", "explorer");

        // Pre-seed Modern Shell
        modernShellIndex.put("settings", "ms-settings:");
        processMap.put("settings", "SystemSettings");

        modernShellIndex.put("wifi", "ms-settings:network-wifi");
        modernShellIndex.put("wi-fi", "ms-settings:network-wifi");
        processMap.put("wifi", "SystemSettings");
        processMap.put("wi-fi", "SystemSettings");

        modernShellIndex.put("bluetooth", "ms-settings:bluetooth");
        processMap.put("bluetooth", "SystemSettings");
    }

    public String findKeyInMap(HashMap<String, String> map, String searchName) {
        if (searchName == null) return null;
        String cleanSearchName = searchName.toLowerCase().trim();

        if (map.containsKey(cleanSearchName)) {
            return cleanSearchName;
        }
        for (String installedApp : map.keySet()) {
            String cleanInstalledApp = installedApp.toLowerCase();
            String appRegex = "\\b" + Pattern.quote(cleanInstalledApp) + "\\b";
            if (Pattern.compile(appRegex).matcher(cleanSearchName).find()) {
                return installedApp;
            }
            String searchRegex = "\\b" + Pattern.quote(cleanSearchName) + "\\b";
            if (Pattern.compile(searchRegex).matcher(cleanInstalledApp).find()) {
                return installedApp;
            }
        }
        for (String installedApp : map.keySet()) {
            if (installedApp.toLowerCase().equals(cleanSearchName) || cleanSearchName.equals(installedApp.toLowerCase())) {
                return installedApp;
            }
        }

        return null;
    }
}