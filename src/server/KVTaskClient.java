package server;

import exceptions.IncorrectLoadFromServerRequestException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class KVTaskClient {

    private final String API_TOKEN;
    private  HttpClient client;
    private String host;
    private String saveUrl = "/save/";
    private String loadUrl = "/load/";
    private String registerUrl = "/register";

    public KVTaskClient(String host) throws InterruptedException, IOException {
        client = HttpClient.newHttpClient();
        this.host = host;
        URI url = URI.create(host + registerUrl);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        this.API_TOKEN = response.body();
    }

    public void put(Key key, String json) throws IOException, InterruptedException {
        URI url = URI.create(host + saveUrl + key + "?API_TOKEN=" + API_TOKEN);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println(response.statusCode());
    }

    public String load (Key key) throws IOException, InterruptedException, IncorrectLoadFromServerRequestException {
        URI url = URI.create(host + loadUrl + key + "?API_TOKEN=" + API_TOKEN);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 400) {
            throw new IncorrectLoadFromServerRequestException("Ключ не был ранее использован для сохранения");
        }
        return response.body();
    }

    public enum Key {
        TASKS,
        SUBTASKS,
        EPICS,
        HISTORY
    }
}
