package advisor.command;

import advisor.MusicProvider;
import advisor.Page;
import advisor.spotify.Album;

public class NewAlbumsCommand implements Command {
    private final MusicProvider service;

    public NewAlbumsCommand(MusicProvider service) {
        this.service = service;
    }

    @Override
    public String getShortName() {
        return "new";
    }

    @Override
    public Page<?> execute(String accessToken, String arguments) {
        Page<Album> newAlbums = service.getNewAlbums(accessToken);
        newAlbums.print();
        return newAlbums;
    }
}
