package advisor.command;

import advisor.MusicProvider;
import advisor.Page;
import advisor.spotify.Category;

public class CategoriesCommand implements Command {
    private final MusicProvider service;

    public CategoriesCommand(MusicProvider service) {
        this.service = service;
    }

    @Override
    public String getShortName() {
        return "categories";
    }

    @Override
    public Page<?> execute(String accessToken, String arguments) {
        Page<Category> categories = service.getCategories(accessToken);
        categories.print();
        return categories;
    }
}
