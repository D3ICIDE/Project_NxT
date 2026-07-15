package amadeus.tools;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.AriaSnapshotMode;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;

import java.util.List;
import java.util.Map;

import static amadeus.tools.PlayWrightConfig.page;

public class BrowserControlTool {

  public enum SearchParameter{
        SEARCH,
        URL
    }

    @Tool("Navigates the browser to a specific URL or performs a Google search. ALWAYS use this tool first when exploring the web.")
    public String navigateBrowser(
            @P("The exact URL (starting with http) or the search query") String query,
            @P("Set to 'URL' for direct links, or 'SEARCH' to google it") SearchParameter searchParameter) {



        try {
            PlayWrightConfig.init();
            if (searchParameter == SearchParameter.SEARCH) {
                String encodedQuery = java.net.URLEncoder.encode(query.trim(), java.nio.charset.StandardCharsets.UTF_8.toString());
                page.navigate("https://www.google.com/search?q=" + encodedQuery);
                page.waitForLoadState();
                return "Successfully searched Google for: '" + query + "'. Use the scanPageStructure tool to see the results.";
            } else {
                page.navigate(query.trim());
                page.waitForLoadState();
                return "Successfully navigated to " + query + ". Use the scanPageStructure tool to read the content.";
            }
        } catch (Exception e) {
            return "Failed to navigate: " + e.getMessage();
        }
    }

    @Tool("Scans the current web page and returns a structured list of interactive elements. Use this to 'see' the page before clicking.")
    public String scanPageStructure() {
        if (page == null) return "No page is currently open.";

        try {
            // Wait for the page network and animations to settle
            page.waitForLoadState();

            // Native Playwright Java API optimized specifically for AI consumption!
            // It returns a YAML tree with unique [ref=...] IDs for clickable elements.
            String snapshot = page.ariaSnapshot(new Page.AriaSnapshotOptions()
                    .setMode(AriaSnapshotMode.AI)
                    .setBoxes(true) // Optional: Tells the AI the x/y coordinates and sizes
            );

            if (snapshot == null || snapshot.trim().isEmpty()) {
                return "The page appears to be empty or contains no readable elements.";
            }

            String finalOutput = "--- CURRENT PAGE STRUCTURE ---\n" + snapshot;

            // Limit output size to prevent overflowing the LLM's context window
            if (finalOutput.length() > 6000) {
                return finalOutput.substring(0, 6000) + "\n...[CONTENT TRUNCATED. PAGE TOO LARGE]";
            }

            return finalOutput;

        } catch (Exception e) {
            return "Failed to scan page: " + e.getMessage();
        }
    }

    @Tool("Clicks an element on the page. You MUST provide the exact text and the role you found using scanPageStructure.")
    public String clickElement(
            @P("The exact visible text of the element") String text,
            @P("The role of the element (e.g., BUTTON, LINK, COMBOBOX)") AriaRole role) {

        if (page == null) return "No page is currently open.";

        try {
            page.getByRole(role, new Page.GetByRoleOptions().setName(text).setExact(true)).first().click();
            page.waitForLoadState();
            return "Successfully clicked [" + role + "] '" + text + "'. Use scanPageStructure to see what changed.";
        } catch (Exception e) {
            return "Failed to click. Make sure you used the exact text and role from the scan. Error: " + e.getMessage();
        }
    }

    // Helper method to recursively format the accessibility tree
    private void parseAccessibilityTree(Map<String, Object> node, StringBuilder builder, int depth) {
        if (node == null) return;

        String role = (String) node.get("role");
        String name = (String) node.get("name");

        if (name != null && !name.trim().isEmpty()) {
            String indent = "  ".repeat(depth);
            builder.append(indent)
                    .append("[")
                    .append(role != null ? role.toUpperCase() : "TEXT")
                    .append("] \"")
                    .append(name)
                    .append("\"\n");
        }

        if (node.containsKey("children")) {
            List<Map<String, Object>> children = (List<Map<String, Object>>) node.get("children");
            for (Map<String, Object> child : children) {
                parseAccessibilityTree(child, builder, depth + 1);
            }
        }
    }
}