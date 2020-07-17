package advisor.command;

import advisor.MusicProvider;
import advisor.Page;
import advisor.spotify.Playlist;

public class FeaturedPlaylistsCommand implements Command {
    private final MusicProvider service;

    public FeaturedPlaylistsCommand(MusicProvider service) {
        this.service = service;
    }

    @Override
    public String getShortName() {
        return "featured";
    }

    @Override
    public Page<?> execute(String accessToken, String arguments) {
        Page<Playlist> featuredPlaylists = service.getFeaturedPlaylists(accessToken);
        featuredPlaylists.print();
        return featuredPlaylists;
    }
}
