package advisor.spotify;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

@Data
public class Album {
    private String name;
    private List<Artist> artists;
    @SerializedName("external_urls")
    private Map<String, String> externalUrls;

    public String getSingleUrl() {
        if (externalUrls.size() != 1) throw new IllegalStateException();
        return externalUrls.values().iterator().next();
    }

    @Override
    public String toString() {
        return String.format("%s%n%s%n%s", name, artists.stream().map(Artist::getName).collect(toList()), getSingleUrl());
    }
}
