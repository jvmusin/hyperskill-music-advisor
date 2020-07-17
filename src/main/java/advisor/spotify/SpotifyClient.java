package advisor.spotify;

import advisor.Page;
import com.google.gson.*;
import lombok.SneakyThrows;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import static java.net.http.HttpResponse.BodyHandlers.ofString;
import static java.util.stream.Collectors.toList;

public class SpotifyClient {
    private static final String CLIENT_ID = "cd86285b92e4459aa880af3ae3392220";
    private static final String CLIENT_SECRET = "5702ee19e59846fe9489413810c0e64b";
    private static final String CLIENT_TOKEN = Base64.getEncoder().encodeToString(
            (CLIENT_ID + ":" + CLIENT_SECRET).getBytes()
    );
    private final Gson gson = new Gson();
    private final HttpClient client = HttpClient.newHttpClient();
    private final String accessApiUrl;
    private final String resourceApiUrl;
    private final int pageSize;

    public SpotifyClient(String accessApiUrl, String resourceApiUrl, int pageSize) {
        this.accessApiUrl = accessApiUrl;
        this.resourceApiUrl = resourceApiUrl + "/v1";
        this.pageSize = pageSize;
    }

    private void validateResponse(HttpResponse<String> response) {
        if (response.statusCode() == 404) throw new ApiException(404, "Specified id doesn't exist");
        JsonElement err = JsonParser.parseString(response.body())
                .getAsJsonObject()
                .get("error");
        if (err == null) return;
        JsonObject elem = err.getAsJsonObject();
        throw new ApiException(elem.get("status").getAsInt(), elem.get("message").getAsString());
    }

    @SneakyThrows
    private HttpResponse<String> sendRequest(String path, String accessToken) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(resourceApiUrl + path))
                .GET()
                .header("Authorization", "Bearer " + accessToken)
                .build();
        HttpResponse<String> response = client.send(request, ofString());
        validateResponse(response);
        return response;
    }

    @SneakyThrows
    private HttpResponse<String> sendRequest(String path, int pageNumber, String accessToken) {
        int offset = (pageNumber - 1) * pageSize;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(String.format("%s?offset=%d&limit=%d", resourceApiUrl + path, offset, pageSize)))
                .GET()
                .header("Authorization", "Bearer " + accessToken)
                .build();
        HttpResponse<String> response = client.send(request, ofString());
        validateResponse(response);
        return response;
    }

    @SneakyThrows
    public String getAccessToken(String code) {
        HttpRequest request = HttpRequest.newBuilder()
                .header("Content-Type", "application/x-www-form-urlencoded")
                .uri(URI.create(accessApiUrl + "/api/token"))
                .POST(HttpRequest.BodyPublishers.ofString("grant_type=authorization_code&redirect_uri=http://localhost:8080&code=" + code))
                .header("Authorization", "Basic " + CLIENT_TOKEN)
                .build();

        System.out.println("Making http request for access_token...");
        HttpResponse<String> response = client.send(request, ofString());
        validateResponse(response);

        return JsonParser.parseString(response.body()).getAsJsonObject().get("access_token").getAsString();
    }

    private <T> List<T> getList(String path, String rootName, Class<T> type, String accessToken) {
        HttpResponse<String> response = sendRequest(path, accessToken);
        validateResponse(response);
        String res = response.body();
        JsonArray items = JsonParser.parseString(res)
                .getAsJsonObject()
                .getAsJsonObject(rootName)
                .getAsJsonArray("items");
        return Arrays.stream(gson.fromJson(items, Map[].class))
                .map(gson::toJson)
                .map(item -> gson.fromJson(item, type))
                .collect(toList());
    }

    private <T> List<T> getList(String path, String rootName, Class<T> type, int pageNumber, String accessToken) {
        JsonObject object = getObject(path, rootName, pageNumber, accessToken);
        JsonArray items = object.getAsJsonArray("items");
        return parseArray(items, type);
    }

    private JsonObject getObject(String path, String rootName, int pageNumber, String accessToken) {
        String res = sendRequest(path, pageNumber, accessToken).body();
        return JsonParser.parseString(res).getAsJsonObject().getAsJsonObject(rootName);
    }

    private <T> List<T> parseArray(JsonArray items, Class<T> type) {
        return Arrays.stream(gson.fromJson(items, Map[].class))
                .map(gson::toJson)
                .map(item -> gson.fromJson(item, type))
                .collect(toList());
    }

    private <T> List<T> paged(List<T> list, int pageNumber) {
        return list.stream()
                .skip(pageSize * (pageNumber - 1))
                .limit(pageSize)
                .collect(toList());
    }

    private <T> Page<T> getPage(String path, String rootName, Class<T> type, String accessToken) {
        JsonObject object = getObject(path, rootName, 1, accessToken);
        List<T> items = paged(parseArray(object.get("items").getAsJsonArray(), type), 1);
        int pageCount = (object.get("total").getAsInt() + pageSize - 1) / pageSize;

        return new Page<>(pageCount, !rootName.equals("categories"), items,
                number -> paged(getList(path, rootName, type, number, accessToken), number));
    }

    public Page<Album> getNewAlbums(String accessToken) {
        return getPage("/browse/new-releases", "albums", Album.class, accessToken);
    }

    public Page<Playlist> getFeaturedPlaylists(String accessToken) {
        return getPage("/browse/featured-playlists", "playlists", Playlist.class, accessToken);
    }

    public Page<Category> getCategories(String accessToken) {
        return getPage("/browse/categories", "categories", Category.class, accessToken);
    }

    public Page<Playlist> getPlaylistsByCategory(String accessToken, String categoryName) {
        String categoryId = getCategoryId(accessToken, categoryName);
        String path = "/browse/categories/" + categoryId + "/playlists";
        return getPage(path, "playlists", Playlist.class, accessToken);
    }

    private String getCategoryId(String accessToken, String categoryName) {
        List<Category> categories = getList("/browse/categories", "categories", Category.class, accessToken);
        return categories.stream()
                .filter(c -> c.getName().equals(categoryName))
                .findFirst()
                .map(Category::getId)
                .orElseThrow();
    }
}
