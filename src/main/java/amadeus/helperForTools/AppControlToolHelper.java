package amadeus.helperForTools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class AppControlToolHelper {
    public static boolean isProcessAlive(String processExe) {
        try {
            Process checkProcess = Runtime.getRuntime().exec("tasklist /FI \"IMAGENAME eq " + processExe + "\"");
            BufferedReader reader = new BufferedReader(new InputStreamReader(checkProcess.getInputStream()));
            String line;
            boolean isStillRunning = false;

            // Read the output of tasklist
            while ((line = reader.readLine()) != null) {
                // If tasklist outputs the name of our .exe, the kill failed
                if (line.toLowerCase().contains(processExe.toLowerCase())) {
                    isStillRunning = true;
                    break;
                }
            }

            if (isStillRunning) {
                System.out.println("[AMADEUS]: Command failed. " + processExe + " is stubbornly holding on.");
                return true;
            } else {
                System.out.println("[AMADEUS]: Command Absolute executed. " + processExe + " has been terminated.");
                return false;
            }
        } catch (Exception e) {
            System.out.println("[AMADEUS]: Command failed. " + processExe);
            return false;
        }
    }

    //helper for open Application

    public static String spawnProcess(ProcessBuilder pb, String target) {
        try {
            pb.redirectErrorStream(true);
            Process process = pb.start();
            Thread.sleep(150); // Small window to let Windows attempt the process spawn

            if (process.isAlive() || process.exitValue() == 0) {
                return "{\"status\":\"success\", \"message\":\"Application '" + target + "' launched successfully via system bypass.\"}";
            } else {
                return "{\"status\":\"error\", \"message\":\"System shell could not find an application named '" + target + "'.\"}";
            }
        } catch (IOException e) {
            System.out.println("[AMADEUS Shell Error]: IO Issue executing bypass for " + target);
            return "{\"status\":\"error\", \"message\":\"OS failed to execute launch process for '" + target + "'.\"}";
        } catch (InterruptedException e) {
            System.out.println("[AMADEUS Thread Error]: Bypass execution interrupted.");
            Thread.currentThread().interrupt(); // Proper thread management
            return "{\"status\":\"error\", \"message\":\"The launch sequence for '" + target + "' was interrupted.\"}";
        }
    }


    //Name Cleaner

    public static String applicationNameSanitizer(String target) {
        if (target == null || target.trim().isEmpty()) {
            System.out.println("[AMADEUS Error]: Missing target or action.");
            return "{\"status\":\"error\", \"message\":\"Missing target or sub_action parameters.\"}";
        }
        //System.out.println(target+" "+sub_action);
        System.out.println("LLM Target " + target);

        String appName = target.toLowerCase().trim();
        return appName;

    }

    //helper for minimizing and maximing tool

    public static void waitForWindowAction() {
        try {
            Thread.sleep(700);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }


    public static boolean isWindowInState(String identifier, String state, boolean useTitle) {
        boolean isMinimizing = state.equalsIgnoreCase("min");
        String apiMethod = isMinimizing ? "IsIconic" : "IsZoomed";

        String escapedIdentifier = identifier.replace("'", "''");

        // Gather all handles without blunt filtering yet

        String targetSelection = useTitle
                ? "$HWNDs = @((Get-Process | Where-Object { $_.MainWindowTitle -like '*" + escapedIdentifier + "*' }).MainWindowHandle);"
                : "$HWNDs = @((Get-Process -Name '" + escapedIdentifier + "' -ErrorAction SilentlyContinue).MainWindowHandle);";

        // Added 'IsWindowVisible' to the Win32 imports
        String psCommand = "$API = Add-Type -MemberDefinition '" +
                "[DllImport(\\\"user32.dll\\\")] public static extern bool IsIconic(IntPtr hWnd); " +
                "[DllImport(\\\"user32.dll\\\")] public static extern bool IsWindowVisible(IntPtr hWnd); " +
                "[DllImport(\\\"user32.dll\\\")] public static extern bool IsZoomed(IntPtr hWnd);' " +
                "-Name Win32 -PassThru; " +
                targetSelection +
                "$anyMatch = $false; " +
                "foreach ($HWND in $HWNDs) { " +
                // CRITICAL FIX: The window MUST be real, active, and visible on the user's desktop
                "    if ($HWND -ne 0 -and $API::IsWindowVisible($HWND)) { " +
                "        if ($API::" + apiMethod + "($HWND)) { $anyMatch = $true; break; } " +
                "    } " +
                "} " +
                "if ($anyMatch) { exit 0 } else { exit 1 }";

        try {
            ProcessBuilder pb = new ProcessBuilder("powershell.exe", "-Command", psCommand);
            pb.redirectOutput(ProcessBuilder.Redirect.DISCARD);
            pb.redirectError(ProcessBuilder.Redirect.DISCARD);
            Process process = pb.start();
            process.waitFor();

            return process.exitValue() == 0;
        } catch (IOException | InterruptedException e) {
            System.out.println("[AMADEUS]: Critical execution error during window state verification for " + identifier);
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            return false;
        }
    }



}
