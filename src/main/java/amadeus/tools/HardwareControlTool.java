package amadeus.tools;

import amadeus.core.App_Index;
import amadeus.core.NirCmdService;
import amadeus.helperForTools.AppControlToolHelper;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;

import static amadeus.tools.AppControlTool.index;

public class HardwareControlTool{
    @Tool("Control system-level device hardware settings like volume, screen brightness, power states using NirCmd system verbs.")
    public String hardwareControler(
            @P("The specific NirCmd system verb (e.g., setsysvolume, setbrightness, standby, exitwin,mutesysvolume,lockws).")String sub_action,
            @P("The target value or parameter modification (e.g., '30000', '75', 'poweroff', 'reboot'). Leave empty if none.") String modifier) {

        NirCmdService.executeShape("system", sub_action,"", modifier);
        return "{\"status\":\"success\", \"message\":\"Successfully executed " + sub_action +"\"}";
    }

    @Tool("Control Settings of the Windows PC like bluetooth,Wifi,Windows Update")
    public String SettingController(
            @P("The target setting page that is required to be launched ")String target){
        if (target.startsWith("ms-settings:") || target.startsWith("shell:")) {
            ProcessBuilder pb = new ProcessBuilder("explorer.exe", target);
            System.out.println("[AMADEUS Direct URI]: " + target);
            return AppControlToolHelper.spawnProcess(pb, target);
        }
        String modernKey = index.findKeyInMap(App_Index.modernShellIndex, target);
        if (modernKey != null) {
            String launchTarget = App_Index.modernShellIndex.get(modernKey);
            ProcessBuilder pb= new ProcessBuilder("explorer.exe", launchTarget);
                System.out.println("[AMADEUS]: " + launchTarget);
                return AppControlToolHelper.spawnProcess(pb, launchTarget);

        }
        else {
            return "Could not find the particular Setting";
        }


    }
}
