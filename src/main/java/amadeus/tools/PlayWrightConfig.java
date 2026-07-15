package amadeus.tools;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.ViewportSize;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;

public class PlayWrightConfig {
    public static Playwright playwright;
    public static BrowserContext context;
    public static Page page;

  /*  public PlayWrightConfig() {*/
        public static void init() {
        // 2. Only launch the browser if it hasn't been launched yet
        if (playwright == null) {
            System.out.println("First run detected: Launching Edge...");
            playwright = Playwright.create();

            context = playwright.chromium().launchPersistentContext(
                    Paths.get("C:\\EdgeDevProfile"),
                    new BrowserType.LaunchPersistentContextOptions()
                            .setChannel("msedge")
                            .setHeadless(false)
                            .setViewportSize((ViewportSize) null)
                            .setIgnoreDefaultArgs(Arrays.asList("--disable-extensions", "--disable-component-extensions-with-background-pages"))
                            .setArgs(Arrays.asList("--start-maximized"))
            );

            // Grab the default open tab
            page = context.pages().isEmpty() ? context.newPage() : context.pages().get(0);
        } else {
            System.out.println("Edge is already running. Reusing the open window.");
        }
    }
}
