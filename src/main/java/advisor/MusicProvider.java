package advisor;

import advisor.spotify.Album;
import advisor.spotify.Category;
import advisor.spotify.Playlist;

public interface MusicProvider {
    Page<Playlist> getFeaturedPlaylists(String accessToken);

    Page<Album> getNewAlbums(String accessToken);

    Page<Category> getCategories(String accessToken);

    Page<Playlist> getPlaylistsByCategory(String accessToken, String category);
}
