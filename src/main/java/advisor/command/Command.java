package advisor.command;

import advisor.Page;

public interface Command {
    String getShortName();
    Page<?> execute(String accessToken, String arguments);
}
