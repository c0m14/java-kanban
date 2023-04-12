package server;

import exceptions.IncorrectLoadFromServerRequestException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class KVTaskClient {

    private String API_TOKEN;
    private final HttpClient client;
    private final String host;
    private final String saveUrl = "/save/";
    private final String loadUrl = "/load/";
    private final String registerUrl = "/register";

    public KVTaskClient(String host) throws InterruptedException, IOException {
        client = HttpClient.newHttpClient();
        this.host = host;
        registerOnServer();
    }

    public void registerOnServer() throws IOException, InterruptedException {
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
        if (response.statusCode() == 403) {
            registerOnServer();
            put(key, json);
        }
    }

    public String load(Key key) throws IOException, InterruptedException, IncorrectLoadFromServerRequestException {
        URI url = URI.create(host + loadUrl + key + "?API_TOKEN=" + API_TOKEN);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 400) {
            throw new IncorrectLoadFromServerRequestException("Ключ не был ранее использован для сохранения");
        } else if (response.statusCode() == 403) {
            registerOnServer();
            load(key);
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
