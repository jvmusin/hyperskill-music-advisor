package advisor.spotify;

import lombok.Data;

@Data
public class Category {
    private String id;
    private String name;

    @Override
    public String toString() {
        return name;
    }
}
