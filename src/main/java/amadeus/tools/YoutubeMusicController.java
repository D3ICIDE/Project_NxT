package amadeus.tools;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;

import static amadeus.tools.PlayWrightConfig.page;

public class YoutubeMusicController {

    public enum SongState {
        PLAY("Play"),
        PAUSE("Pause"),
        NEXT("Next"),
        RESTART("Previous"),
        PREVIOUS("Previous"),
        LOOP_ON("Repeat all"),
        LOOP_OFF("Repeat off"),
        LOOP_ONCE("Repeat one");

        private final String buttonName;

        SongState(String buttonName) {
            this.buttonName = buttonName;
        }

        public String getButtonName() {
            return this.buttonName;
        }
    }
    public enum Type{
        Song,Album
    }


    @Tool("Searches for and plays a specific song on YouTube Music.")
    public String playMusic(
            @P("The title or name of the song to be played (e.g., 'euphoria', 'e85')")String songName,
            @P("OPTIONAL: The name of the artist or band. Leave this blank or null if the user didn't mention an artist.")String artistName,
    @P("OPTIONAL: The type of request. It can be a song or an album.") Type type) {
        if (PlayWrightConfig.playwright == null) {
            new PlayWrightConfig();
        }
        try {
            String searchQuery = songName.trim();

            if (artistName != null && !artistName.trim().isEmpty()) {
                searchQuery += " " + artistName.trim();
            }
            page.navigate("https://music.youtube.com/search?q=" + searchQuery);

            var songsShelf = page.locator("ytmusic-shelf-renderer:has(h2:has-text(\"Songs\"))");


            if(type.equals(Type.Song)) {
                page.getByRole(AriaRole.TAB, new Page.GetByRoleOptions().setName("Songs")).click();
                page.locator("ytmusic-responsive-list-item-renderer")
                        .locator("ytmusic-play-button-renderer")
                        .first()
                        .click();
            }


            else{

                page.locator("ytmusic-section-list-renderer")
                        .locator("ytmusic-play-button-renderer")
                        .first()
                        .click();
            }
                return "Now Playing" + songName;


        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return "Error";
        }
    }
    @Tool("Controls the playback of YouTube Music, allowing it to be paused, resumed, restart,repeat all/one/off or skipped to the next/previous song.")
    public String musicController(
            @P("The playback state command. Options: 'PLAY' to resume, 'PAUSE' to stop, 'NEXT' to skip to the next song,'RESTART' to restart the current song,'LOOP_ON' to loop song,'LOOP_OFF' to stop looping songs, 'LOOP_ONCE' to loop the song one time or 'Previous' to go back to the previous song.")SongState songState){
        if(songState==null){
            return "Couldn't complete this action. SongState was null";
        }
        try {
            if(songState==SongState.PREVIOUS){
                page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(songState.getButtonName()).setExact(true)).click();
                page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(songState.getButtonName()).setExact(true)).click();
                return "Successfully Completed the action";

            }

            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(songState.getButtonName()).setExact(true)).click();
            return "Succesfully Completed the action";
        }catch (Exception e) {
            return "Failed to toggle play/pause state: " + e.getMessage();
        }
    }


}


