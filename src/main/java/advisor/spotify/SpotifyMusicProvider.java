package advisor.spotify;

import advisor.MusicProvider;
import advisor.Page;

public class SpotifyMusicProvider implements MusicProvider {

    private final SpotifyClient client;

    public SpotifyMusicProvider(SpotifyClient client) {
        this.client = client;
    }

    @Override
    public Page<Playlist> getFeaturedPlaylists(String accessToken) {
        return client.getFeaturedPlaylists(accessToken);
    }

    public Page<Album> getNewAlbums(String accessToken) {
        return client.getNewAlbums(accessToken);
    }

    @Override
    public Page<Category> getCategories(String accessToken) {
        return client.getCategories(accessToken);
    }

    @Override
    public Page<Playlist> getPlaylistsByCategory(String accessToken, String category) {
        return client.getPlaylistsByCategory(accessToken, category);
    }
}
