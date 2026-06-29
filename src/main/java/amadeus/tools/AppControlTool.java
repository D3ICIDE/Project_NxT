package amadeus.tools;

import amadeus.core.App_Index;
import amadeus.core.NirCmdService;
import amadeus.helperForTools.AppControlToolHelper;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;

import java.io.IOException;

public class AppControlTool {
    public enum WindowState {
        min, max
    }
    public enum SnapState{
        left,right,top_left,top_right
    }

    public static App_Index index = new App_Index();


    @Tool("Close local desktop applications using nirCmd verbs")
    public String closeProcess(
            @P("The full standard commercial name of the application. Expand acronyms.") String target) {
        System.out.println("[DEBUG]" + target);
        String appName = AppControlToolHelper.applicationNameSanitizer(target);

        String processKey = index.findKeyInMap(App_Index.processMap, appName);
        System.out.println("[DEBUG]" + processKey);

        if (processKey == null) {
            String shortCommand = String.format("nircmd.exe win close ititle \"%s\"",target.replace(".exe",""));
            NirCmdService.runProcess(shortCommand);
            System.out.println("[AMADEUS]: Process name could not be resolved for window manipulation: " + appName);
            return "{ \"message\":\"System could not resolve the internal process name for '" + appName + "' to manipulate its window. So tried execution using the windows name. Ask the user for confirmation and stop execution\"}";
        }

        String rawProcessName = App_Index.processMap.get(processKey);
        String processExe = rawProcessName.endsWith(".exe") ? rawProcessName : rawProcessName + ".exe";
        if (processExe.equals("explorer.exe")) {
            try {
                Runtime.getRuntime().exec("nircmdc win close class \"CabinetWClass\"");
                return "Success";
            } catch (Exception e) {
                System.out.println("[AMADEUS]: Failed to close CabinetWClass");
                return "Unsuccessful";

            }
        }

        try {
            NirCmdService.executeShape("process", "closeprocess", processExe, "");
            Thread.sleep(500);
            if (AppControlToolHelper.isProcessAlive(processExe)) {
                NirCmdService.executeShape("process", "killprocess", processExe, "");
                if (AppControlToolHelper.isProcessAlive(processExe)) {
                    System.out.println("Exeecution even after Success");
                    return "Cannot stop this process for some reasom";
                }
            } else {
                return "Successfully Close " + appName;
            }
        } catch (InterruptedException e) {
            return "Some error Occured while closing the process";

        }
        return "Successfully Close " + appName;
    }

    @Tool("Used to open local desktop applications using cmd commands.")
    public String openApplication(
            @P("The full standard commercial name of the application. Expand acronyms.") String target) {
        System.out.println("Using openApplication");
        String appName = AppControlToolHelper.applicationNameSanitizer(target);

        String win32Key = index.findKeyInMap(App_Index.win32PathIndex, appName);
        if (win32Key != null) {
            String exePath = App_Index.win32PathIndex.get(win32Key);
            ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/c", "start", "\"\"", exePath);
            return AppControlToolHelper.spawnProcess(pb, appName);
        }

        // 1B. Check Modern Shell & URI Index Second
        String modernKey = index.findKeyInMap(App_Index.modernShellIndex, appName);
        System.out.println("found in MAP" + modernKey + "App Name" + appName);
        if (modernKey != null) {
            String launchTarget = App_Index.modernShellIndex.get(modernKey);
            ProcessBuilder pb = new ProcessBuilder("explorer.exe", "shell:AppsFolder\\" + launchTarget);

            System.out.println("[AMADEUS]: " + launchTarget);
            return AppControlToolHelper.spawnProcess(pb, launchTarget);
        } else {
            return "Failed to open " + appName;
        }

    }

    @Tool("Minimize or Maximize an application window ")
    public String minOrMax(
            @P("The full standard commercial name of the application. Expand acronyms.") String name,
            @P("The enum value min for minimizing and max for maximizing") WindowState windowState) {
        System.out.println(name);
        String target = name.replace(".exe","");
        System.out.println(target+""+windowState);
        System.out.println("Using min&max");

       String command="";
       boolean isSuccess = false;
        String state = windowState.toString();
        String appName = AppControlToolHelper.applicationNameSanitizer(target);
        String processKey = index.findKeyInMap(App_Index.processMap, appName);
        System.out.println("found in MAP" + processKey + "App Name" + appName);

        if (processKey!=null) {
            String rawProcessName = App_Index.processMap.get(processKey);
            String processName = rawProcessName.replace(".exe", "");
            String processExe = rawProcessName.endsWith(".exe") ? rawProcessName : rawProcessName + ".exe";
             command = String.format("nircmdc win %s process %s", state, processExe);
            NirCmdService.runProcess(command);
            System.out.println(command);

            AppControlToolHelper.waitForWindowAction();

           isSuccess = AppControlToolHelper.isWindowInState(processName, windowState.toString(), false);

            if (isSuccess) return "Successfull Execution";
        }
       // System.out.println("[AMADEUS] IDK MAN LOOPING rn");
        System.out.println("[Line 277: Method:minOrMax: Class AppControlTool]USing Window Approach");
        processKey = index.findKeyInMap(App_Index.modernShellIndex, appName);
        command = String.format("nircmdc win %s ititle \"%s\"", state, processKey);
        NirCmdService.runProcess(command);
        System.out.println(command);

        AppControlToolHelper.waitForWindowAction();

        isSuccess = AppControlToolHelper.isWindowInState(processKey, windowState.toString(), true);
        System.out.println("Prolly Failing");
        if(isSuccess) return  "Successfull Execution";
        command=command = String.format("nircmdc win %s stitle \"%s\"", state, processKey);
        NirCmdService.runProcess(command);
        System.out.println(command);

        AppControlToolHelper.waitForWindowAction();
        isSuccess = AppControlToolHelper.isWindowInState(processKey, windowState.toString(), true);
        return isSuccess ? "Successfull Execution" : "Failed Execution. Do not retry";

    }
    @Tool("Used to snap application window in a particular direction")
    private String SnapTool(
            @P("The full standard commercial name of the application. Expand acronyms.") String target,
            @P("Direction of the snapping of the window")SnapState snapState){
        String command = "";
        String command2=null;
        String appName = target.replace(".exe","");
        boolean focusStatus = focusApplication(appName);
        if(!focusStatus)return "Could Not bring the App to focus.";
        String state=snapState.toString();
        switch (state){
            case "left":
                 command = "nircmd sendkeypress lwin+left";
                break;
            case "right":
                command = "nircmd sendkeypress lwin+right";
                break;
            case "up":
                command = "nircmd sendkeypress lwin+up";
                break;
            case "top_left":
                command = "nircmd sendkeypress lwin+up";
                command2 = "nircmd sendkeypress lwin+left";
                break;
                case  "top_right":
                    command = "nircmd sendkeypress lwin+up";
                    command2 = "nircmd sendkeypress lwin+right";
            default:command2="nircmd sendkeypress lwin+up";



        }
        NirCmdService.runProcess(command);
        if(command2!=null) NirCmdService.runProcess(command2);





        return "Successful";


    }
    private boolean focusApplication(String appName){
        String processKey = index.findKeyInMap(App_Index.processMap, appName);
        String processName = App_Index.processMap.get(processKey);

        String processExe = processName.contains(".exe") ? processName : processName + ".exe";
        try{
        String command = String.format("nircmd.exe win activate process \"%s\" ",processExe);
        NirCmdService.runProcess(command);
        Thread.sleep(200);
        if(inFocus(processName)) return true;
        command = String.format("nircmd.exe win activate ititle \"%s\" ",appName);
            Thread.sleep(200);
        NirCmdService.runProcess(command);
            Thread.sleep(200);
        if (inFocus(processName)) return true;
        command = String.format("nircmd.exe win activate stitle \"%s\" ",appName);
        NirCmdService.runProcess(command);
        if(inFocus(processName)) return true;
        }catch (InterruptedException e){
            System.out.println(e.toString());
            return false;
        }
        return false;




    }
    private boolean inFocus(String appName){
        System.out.println("inFocus:"+appName);
        String scriptPath = "src/main/resources/Focus_Application.ps1";
        System.out.println("[Infocus]"+appName);

       try{
           ProcessBuilder pb = new ProcessBuilder(
                   "powershell.exe",
                   "-ExecutionPolicy", "Bypass",
                   "-File", scriptPath,
                   appName
           );
           Process process = pb.start();
           process.waitFor();
           pb.redirectErrorStream(true);
           System.out.println("Successful in Execution");
           System.out.println(process.exitValue());
           return  process.exitValue() == 0;
       } catch (IOException | InterruptedException e) {
           System.out.println("[AMADEUS]: Could Not Focus on "+appName);
           if (e instanceof InterruptedException) {
               Thread.currentThread().interrupt();
           }
           System.out.println("Failed Focusing");
           return false;
       }


    }



}