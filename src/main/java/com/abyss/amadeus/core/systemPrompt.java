package com.abyss.amadeus.core;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class systemPrompt {
    static File sysprompt = new File("src/main/resources/system_prompt.txt");
    public systemPrompt setFile(File file) {
        sysprompt = file;
        return this;
    }
    public static String promptLoader(){
        String systemPrompt = null;
        try{
            if(sysprompt.exists()){
                systemPrompt = Files.readString(sysprompt.toPath());
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
