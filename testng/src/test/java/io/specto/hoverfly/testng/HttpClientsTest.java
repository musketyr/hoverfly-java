package io.specto.hoverfly.testng;

import io.specto.hoverfly.junit.core.SslConfigurer;
import io.specto.hoverfly.testng.api.TestNgClassRule;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static io.specto.hoverfly.junit.core.SimulationSource.classpath;
import static net.javacrumbs.jsonunit.fluent.JsonFluentAssert.assertThatJson;

@Listeners(HoverflyListener.class)
public class HttpClientsTest {
    
    private static final String TEST_URL = "https://www.my-test.com/api/bookings/1";
    
    @TestNgClassRule
    public static HoverflyExtension hoverflyExtension = HoverflyExtension.inSimulationMode(classpath("test-service.json"));

    @Test
    public void shouldWorkWithApacheHttpClient() throws Exception {

        // Given
        HttpClient httpClient = HttpClients.createSystem();
        final HttpGet httpGet = new HttpGet(TEST_URL);

        // When
        final HttpResponse response = httpClient.execute(httpGet);

        // Then
        assertJsonResponseBody(EntityUtils.toString(response.getEntity()));
    }

    @Test
    public void shouldWorkWithSpringWebRestTemplate() {
        // Given
        RestTemplate restTemplate = new RestTemplate();

        // When
        ResponseEntity<String> response = restTemplate.getForEntity(TEST_URL, String.class);

        // Then
        assertJsonResponseBody(response.getBody());
    }

    @Test
    public void shouldWorkWithOkHttpClient() throws Exception {
        // Given
        SslConfigurer sslConfigurer = hoverflyExtension.getSslConfigurer();
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .sslSocketFactory(sslConfigurer.getSslContext().getSocketFactory(), sslConfigurer.getTrustManager())
                .build();

        // When
        Response response = okHttpClient.newCall(new Request.Builder().url(TEST_URL).build()).execute();

        // Then
        assertJsonResponseBody(response.body().string());
    }

    private void assertJsonResponseBody(String body) {
        assertThatJson(body).isEqualTo("{" +
                "\"bookingId\":\"1\"," +
                "\"origin\":\"London\"," +
                "\"destination\":\"Singapore\"," +
                "\"time\":\"2011-09-01T12:30\"," +
                "\"_links\":{\"self\":{\"href\":\"http://localhost/api/bookings/1\"}}" +
                "}");
    }
}
