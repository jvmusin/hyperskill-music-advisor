package advisor.spotify;

import lombok.Data;

@Data
public class Artist {
    private String name;

    @Override
    public String toString() {
        return name;
    }
}
