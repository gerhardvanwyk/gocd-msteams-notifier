package com.roxorgaming.gocd.msteams.jsonapi;

import com.roxorgaming.gocd.mstream.configuration.Configuration;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class GoCdClientTest {

    String historyJson = "{\n" +
            "  \"pipelines\": [\n" +
            "    {\n" +
            "      \"build_cause\": {\n" +
            "        \"approver\": \"anonymous\",\n" +
            "        \"material_revisions\": [\n" +
            "          {\n" +
            "            \"modifications\": [\n" +
            "              {\n" +
            "                \"email_address\": null,\n" +
            "                \"id\": 1,\n" +
            "                \"modified_time\": 1434957613000,\n" +
            "                \"user_name\": \"Pick E Reader <pick.e.reader@example.com>\",\n" +
            "                \"comment\": \"my hola mundo changes\",\n" +
            "                \"revision\": \"c194b49db102b705ebc13e604e490ae13ac92d96\"\n" +
            "              }\n" +
            "            ],\n" +
            "            \"material\": {\n" +
            "              \"description\": \"URL: https://github.com/gocd/gocd, Branch: master\",\n" +
            "              \"fingerprint\": \"f6e7a3899c55e1682ffb00383bdf8f882bcee2141e79a8728254190a1fddcf4f\",\n" +
            "              \"type\": \"Git\",\n" +
            "              \"id\": 1\n" +
            "            },\n" +
            "            \"changed\": false\n" +
            "          }\n" +
            "        ],\n" +
            "        \"trigger_forced\": true,\n" +
            "        \"trigger_message\": \"Forced by anonymous\"\n" +
            "      },\n" +
            "      \"name\": \"pipeline1\",\n" +
            "      \"natural_order\": 11,\n" +
            "      \"can_run\": true,\n" +
            "      \"comment\": null,\n" +
            "      \"stages\": [\n" +
            "        {\n" +
            "          \"name\": \"stage1\",\n" +
            "          \"approved_by\": \"admin\",\n" +
            "          \"jobs\": [\n" +
            "            {\n" +
            "              \"name\": \"job1\",\n" +
            "              \"result\": \"Failed\",\n" +
            "              \"state\": \"Completed\",\n" +
            "              \"id\": 13,\n" +
            "              \"scheduled_date\": 1436172201081\n" +
            "            }\n" +
            "          ],\n" +
            "          \"can_run\": true,\n" +
            "          \"result\": \"Failed\",\n" +
            "          \"approval_type\": \"success\",\n" +
            "          \"counter\": \"1\",\n" +
            "          \"id\": 13,\n" +
            "          \"operate_permission\": true,\n" +
            "          \"rerun_of_counter\": null,\n" +
            "          \"scheduled\": true\n" +
            "        }\n" +
            "      ],\n" +
            "      \"counter\": 11,\n" +
            "      \"id\": 13,\n" +
            "      \"preparing_to_schedule\": false,\n" +
            "      \"label\": \"11\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"build_cause\": {\n" +
            "        \"approver\": \"anonymous\",\n" +
            "        \"material_revisions\": [\n" +
            "          {\n" +
            "            \"modifications\": [\n" +
            "              {\n" +
            "                \"email_address\": null,\n" +
            "                \"id\": 1,\n" +
            "                \"modified_time\": 1434957613000,\n" +
            "                \"user_name\": \"Pick E Reader <pick.e.reader@example.com>\",\n" +
            "                \"comment\": \"my hola mundo changes\",\n" +
            "                \"revision\": \"c194b49db102b705ebc13e604e490ae13ac92d96\"\n" +
            "              }\n" +
            "            ],\n" +
            "            \"material\": {\n" +
            "              \"description\": \"URL: https://github.com/gocd/gocd, Branch: master\",\n" +
            "              \"fingerprint\": \"f6e7a3899c55e1682ffb00383bdf8f882bcee2141e79a8728254190a1fddcf4f\",\n" +
            "              \"type\": \"Git\",\n" +
            "              \"id\": 1\n" +
            "            },\n" +
            "            \"changed\": false\n" +
            "          }\n" +
            "        ],\n" +
            "        \"trigger_forced\": true,\n" +
            "        \"trigger_message\": \"Forced by anonymous\"\n" +
            "      },\n" +
            "      \"name\": \"pipeline1\",\n" +
            "      \"natural_order\": 10,\n" +
            "      \"can_run\": true,\n" +
            "      \"comment\": null,\n" +
            "      \"stages\": [\n" +
            "        {\n" +
            "          \"name\": \"stage1\",\n" +
            "          \"approved_by\": \"admin\",\n" +
            "          \"jobs\": [\n" +
            "            {\n" +
            "              \"name\": \"job1\",\n" +
            "              \"result\": \"Passed\",\n" +
            "              \"state\": \"Completed\",\n" +
            "              \"id\": 12,\n" +
            "              \"scheduled_date\": 1436172122024\n" +
            "            }\n" +
            "          ],\n" +
            "          \"can_run\": true,\n" +
            "          \"result\": \"Passed\",\n" +
            "          \"approval_type\": \"success\",\n" +
            "          \"counter\": \"1\",\n" +
            "          \"id\": 12,\n" +
            "          \"operate_permission\": true,\n" +
            "          \"rerun_of_counter\": null,\n" +
            "          \"scheduled\": true\n" +
            "        }\n" +
            "      ],\n" +
            "      \"counter\": 10,\n" +
            "      \"id\": 12,\n" +
            "      \"preparing_to_schedule\": false,\n" +
            "      \"label\": \"10\"\n" +
            "    }\n" +
            "  ],\n" +
            "  \"pagination\": {\n" +
            "    \"offset\": 0,\n" +
            "    \"total\": 2,\n" +
            "    \"page_size\": 10\n" +
            "  }\n" +
            "}";

    String pipeline  = "{\n" +
            "  \"build_cause\": {\n" +
            "    \"approver\": \"\",\n" +
            "    \"material_revisions\": [\n" +
            "      {\n" +
            "        \"modifications\": [\n" +
            "          {\n" +
            "            \"email_address\": null,\n" +
            "            \"id\": 7225,\n" +
            "            \"modified_time\": 1435728005000,\n" +
            "            \"user_name\": \"Pick E Reader <pick.e.reader@example.com>\",\n" +
            "            \"comment\": \"my hola mundo changes\",\n" +
            "            \"revision\": \"a788f1876e2e1f6e5a1e91006e75cd1d467a0edb\"\n" +
            "          }\n" +
            "        ],\n" +
            "        \"material\": {\n" +
            "          \"description\": \"URL: https://github.com/gocd/gocd, Branch: master\",\n" +
            "          \"fingerprint\": \"61e2da369d0207a7ef61f326eed837f964471b35072340a03f8f55d993afe01d\",\n" +
            "          \"type\": \"Git\",\n" +
            "          \"id\": 4\n" +
            "        },\n" +
            "        \"changed\": true\n" +
            "      }\n" +
            "    ],\n" +
            "    \"trigger_forced\": false,\n" +
            "    \"trigger_message\": \"modified by Pick E Reader <pick.e.reader@example.com>\"\n" +
            "  },\n" +
            "  \"name\": \"PipelineName\",\n" +
            "  \"natural_order\": 1,\n" +
            "  \"can_run\": false,\n" +
            "  \"comment\": null,\n" +
            "  \"stages\": [\n" +
            "    {\n" +
            "      \"name\": \"stage1\",\n" +
            "      \"approved_by\": \"changes\",\n" +
            "      \"jobs\": [\n" +
            "        {\n" +
            "          \"name\": \"jsunit\",\n" +
            "          \"result\": \"Passed\",\n" +
            "          \"state\": \"Completed\",\n" +
            "          \"id\": 1,\n" +
            "          \"scheduled_date\": 1398332981981\n" +
            "        }\n" +
            "      ],\n" +
            "      \"can_run\": false,\n" +
            "      \"result\": \"Passed\",\n" +
            "      \"approval_type\": \"success\",\n" +
            "      \"counter\": \"1\",\n" +
            "      \"id\": 1,\n" +
            "      \"operate_permission\": false,\n" +
            "      \"rerun_of_counter\": null,\n" +
            "      \"scheduled\": true\n" +
            "    }\n" +
            "  ],\n" +
            "  \"counter\": 1,\n" +
            "  \"id\": 1,\n" +
            "  \"preparing_to_schedule\": false,\n" +
            "  \"label\": \"14.1.0.1-b14a81825d081411993853ea5ea45266ced578b4\"\n" +
            "}\n";

    @Test
    public void testHistoryUrl()  {

        Configuration configuration = Configuration.builder()
                .goServerHost("http://localhost").build();
        GoCdClient goCdClient = new GoCdClient(configuration);
        URL url = goCdClient.historyUrl("test-pipeline");
        Assertions.assertEquals(80, url.getDefaultPort());
        Assertions.assertEquals("localhost", url.getHost());
        Assertions.assertEquals("http://localhost/go/api/pipelines/test-pipeline/history", url.toExternalForm());

    }

    @Test
    public void testHistoryMapper() throws IOException {
        Configuration configuration = Configuration.builder()
                .goServerHost("http://localhost").build();
        GoCdClient cdClient = new GoCdClient(configuration);
        HttpURLConnection connection = mock(HttpURLConnection.class);
        when(connection.getContent()).thenReturn(historyJson);
        History history = cdClient.getPipeline(new URL("https://example.org/go/api/pipelines/test-pipeline/history"),
                History.class, connection );
        Assertions.assertEquals("pipeline1/11/stage1/Failed", history.getPipelines().get(0).toString());
        Assertions.assertEquals("pipeline1/10/stage1/Passed", history.getPipelines().get(1).toString());
    }

    @Test
    public void testPipelineUrl() {
        Configuration configuration = Configuration.builder()
                .goServerHost("https://example.org").build();

        GoCdClient goCdClient = new GoCdClient(configuration);
        URL url = goCdClient.pipelineUrl("pipeline23", 41);
        Assertions.assertEquals("https://example.org/go/api/pipelines/pipeline23/41", url.toExternalForm());
    }

    @Test
    public void testPipelineMapper() throws IOException {
        Configuration configuration = new Configuration();
        GoCdClient cdClient = new GoCdClient(configuration);
        HttpURLConnection connection = mock(HttpURLConnection.class);
        when(connection.getContent()).thenReturn(pipeline);
        Pipeline pipeline = cdClient.getPipeline(new URL("https://example.org/go/api/pipelines/test-pipeline/history"),
                Pipeline.class, connection );
        Assertions.assertEquals("PipelineName", pipeline.getName());
        Assertions.assertEquals(4, pipeline.getBuildCause().getMaterialRevisions()[0].getMaterial().getId());
    }

    @Test
    public void shouldConnectWithAPIToken() throws IOException {
        Configuration configuration = new Configuration();
        GoCdClient goCdClient = new GoCdClient(configuration);
        configuration.setGoAPIToken("a-valid-token-from-gocd-server");

        HttpURLConnection conn = getHttpURLConnection(goCdClient);

        verify(conn).setRequestProperty("Authorization", "Bearer a-valid-token-from-gocd-server");
    }

    private HttpURLConnection getHttpURLConnection(GoCdClient goCdClient) throws IOException {
        HttpURLConnection conn = mock(HttpURLConnection.class);
        when(conn.getContent()).thenReturn(new Object());
        goCdClient.configureRequest(conn);
        return conn;
    }

    @Test
    public void shouldConnectWithUserPassCredentials() throws IOException {
        Configuration configuration = new Configuration();
        GoCdClient goCdClient = new GoCdClient(configuration);
        configuration.setGoLogin("login");
        configuration.setGoPassword("pass");

        HttpURLConnection conn = getHttpURLConnection(goCdClient);

        verify(conn).setRequestProperty("Authorization", "Basic bG9naW46cGFzcw==");
    }

    @Test
    public void shouldConnectWithAPITokenFavoringOverUserPassCredential() throws IOException { ;
        Configuration configuration = new Configuration();
        GoCdClient goCdClient = new GoCdClient(configuration);
        configuration.setGoAPIToken("a-valid-token-from-gocd-server");
        configuration.setGoLogin("login");
        configuration.setGoPassword("pass");

        HttpURLConnection conn = getHttpURLConnection(goCdClient);

        verify(conn).setRequestProperty("Authorization", "Bearer a-valid-token-from-gocd-server");
    }

    @Test
    public void shouldNotSetAuthorizationHeaderWithoutCredentials() throws IOException {
        Configuration configuration = new Configuration();
        GoCdClient goCdClient = new GoCdClient(configuration);
        HttpURLConnection conn = getHttpURLConnection(goCdClient);
        verify(conn, never()).setRequestProperty(eq("Authorization"), anyString());
    }

    @Test
    public void shouldNotSetAuthorizationHeaderWithEmptyPassword() throws IOException {
        Configuration configuration = new Configuration();
        configuration.setGoLogin("login");
        configuration.setGoPassword(null);
        GoCdClient goCdClient = new GoCdClient(configuration);
        HttpURLConnection conn = getHttpURLConnection(goCdClient);
        verify(conn, never()).setRequestProperty(eq("Authorization"), anyString());
    }

    @Test
    public void shouldNotSetAuthorizationHeaderWithEmptyLoginName() throws IOException {
        Configuration configuration = new Configuration();
        configuration.setGoLogin(null);
        configuration.setGoPassword("pass");
        GoCdClient goCdClient = new GoCdClient(configuration);
        HttpURLConnection conn = getHttpURLConnection(goCdClient);
        verify(conn, never()).setRequestProperty(eq("Authorization"), anyString());
    }

    @Test
    public void shouldNotSetAuthorizationHeaderWithEmptyPasswordCredentials() throws IOException {
        Configuration configuration = new Configuration();
        configuration.setGoLogin("");
        configuration.setGoPassword("");
        GoCdClient goCdClient = new GoCdClient(configuration);
        HttpURLConnection conn = getHttpURLConnection(goCdClient);
        verify(conn, never()).setRequestProperty(eq("Authorization"), anyString());
    }
}
