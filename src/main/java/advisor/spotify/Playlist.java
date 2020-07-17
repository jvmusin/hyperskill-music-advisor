package advisor.spotify;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.Map;

@Data
public class Playlist {
    private String name;
    @SerializedName("external_urls")
    private Map<String, String> externalUrls;

    public String getSingleUrl() {
        if (externalUrls.size() != 1) throw new IllegalStateException();
        return externalUrls.values().iterator().next();
    }

    @Override
    public String toString() {
        return String.format("%s%n%s", getName(), getSingleUrl());
    }
}
