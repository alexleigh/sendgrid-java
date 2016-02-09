package com.revinate.sendgrid.resource;

import com.revinate.sendgrid.BaseSendGridTest;
import com.revinate.sendgrid.model.Ip;
import com.revinate.sendgrid.model.IpCollection;
import com.revinate.sendgrid.net.SendGridHttpClient;
import com.revinate.sendgrid.net.auth.Credential;
import com.revinate.sendgrid.util.JsonUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class IpResourceTest extends BaseSendGridTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Mock
    SendGridHttpClient client;

    @Mock
    Credential credential;

    IpResource resource;

    @Before
    public void setUp() throws Exception {
        resource = new IpResource("https://api.sendgrid.com/v3/ips", client, credential);
    }

    @Test
    public void list_shouldReturnIps() throws Exception {
        IpCollection response = JsonUtils.fromJson(readFile("/responses/ips.json"),
                IpCollection.class);

        when(client.get("https://api.sendgrid.com/v3/ips", IpCollection.class, credential))
                .thenReturn(response);

        List<Ip> ips = resource.list();

        assertThat(ips, sameInstance(response.getData()));
    }

    @Test
    public void retrieve_shouldReturnIp() throws Exception {
        Ip response = JsonUtils.fromJson(readFile("/responses/ip.json"), Ip.class);

        when(client.get("https://api.sendgrid.com/v3/ips/" + response.getIp(),
                Ip.class, credential)).thenReturn(response);

        Ip ip = resource.retrieve(response.getIp());

        assertThat(ip, sameInstance(response));
    }
}
