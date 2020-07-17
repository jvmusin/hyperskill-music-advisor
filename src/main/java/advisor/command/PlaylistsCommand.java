package advisor.command;

import advisor.MusicProvider;
import advisor.Page;
import advisor.spotify.Playlist;

public class PlaylistsCommand implements Command {
    private final MusicProvider service;

    public PlaylistsCommand(MusicProvider service) {
        this.service = service;
    }

    @Override
    public String getShortName() {
        return "playlists";
    }

    @Override
    public Page<?> execute(String accessToken, String arguments) {
        String category = arguments.trim();
        Page<Playlist> playlists = service.getPlaylistsByCategory(accessToken, category);
        playlists.print();
        return playlists;
    }
}
