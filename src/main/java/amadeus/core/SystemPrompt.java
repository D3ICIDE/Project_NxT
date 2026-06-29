package amadeus.core;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class SystemPrompt {
    private  String path;
    private File sysPrompt;
    public SystemPrompt(String sysPath){
        this.path = sysPath;
        if (sysPath != null) {
            this.sysPrompt = new File(sysPath);
        }
    }
    public String promptLoader(){
        String systemPrompt = null;
        try{
            if(sysPrompt.exists()){
                systemPrompt = Files.readString(sysPrompt.toPath());
            }
            else{
                System.out.println("System prompt not found");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return systemPrompt;
    }
}
