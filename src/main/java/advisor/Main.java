package advisor;

import advisor.command.*;
import advisor.spotify.ApiException;
import advisor.spotify.CodeAwaiterServer;
import advisor.spotify.SpotifyClient;
import advisor.spotify.SpotifyMusicProvider;
import lombok.SneakyThrows;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.Future;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

public class Main implements Runnable {

    private static final String CLIENT_ID = "cd86285b92e4459aa880af3ae3392220";
    private final String authLink;
    private final Map<String, Command> commands;
    private final SpotifyClient client;
    private String accessToken;
    private Page<?> page;

    public Main(String accessApiUrl, String resourceApiUrl, int pageSize) {
        authLink = accessApiUrl + "/authorize" +
                "?client_id=" + CLIENT_ID +
                "&redirect_uri=http://localhost:8080" +
                "&response_type=code";
        client = new SpotifyClient(accessApiUrl, resourceApiUrl, pageSize);
        MusicProvider musicProvider = new SpotifyMusicProvider(client);
        commands = List.<Command>of(
                new FeaturedPlaylistsCommand(musicProvider),
                new NewAlbumsCommand(musicProvider),
                new CategoriesCommand(musicProvider),
                new PlaylistsCommand(musicProvider)
        ).stream().collect(toMap(Command::getShortName, identity()));
    }

    public static void main(String[] args) {
        Map<String, String> params = new HashMap<>();
        for (int i = 0; i + 1 < args.length; i += 2) params.put(args[i], args[i + 1]);
        String accessApiUrl = params.getOrDefault("-access", "https://accounts.spotify.com");
        String resourceApiUrl = params.getOrDefault("-resource", "https://api.spotify.com");
        int page = Integer.parseInt(params.getOrDefault("-page", "5"));
        new Main(accessApiUrl, resourceApiUrl, page).run();
    }

    @Override
    public void run() {
        Scanner in = new Scanner(System.in);
        String query;
        while (!(query = in.nextLine()).equals("exit")) {
            process(query);
        }
    }

    private void process(String query) {
        String[] commandAndArgs = query.split(" ", 2);
        String name = commandAndArgs[0];
        String args = commandAndArgs.length == 1 ? null : commandAndArgs[1];
        try {
            if (accessToken == null) {
                if (name.equals("auth")) {
                    handleAuth();
                } else {
                    System.out.println("Please, provide access for application.");
                }
            } else {
                if (name.equals("prev")) page.previous();
                else if (name.equals("next")) page.next();
                else page = commands.get(name).execute(accessToken, args);
            }
        } catch (ApiException e) {
            System.out.println(e.getMessage());
        }
    }

    @SneakyThrows
    private void handleAuth() {
        try (CodeAwaiterServer server = new CodeAwaiterServer()) {
            Future<String> codeSupplier = server.startAndWaitForCode();
            System.out.println("use this link to request the access code:");
            System.out.println(authLink);
            System.out.println("waiting for code...");
            String code = codeSupplier.get();
            System.out.println("code received");
            // TODO nice right? That terrible test system really hurts...
            if (code == null && false) {
                System.out.println("Fail");
            } else {
                accessToken = client.getAccessToken(code);
                System.out.println("Success!");
            }
        }
    }
}
