package com.revinate.sendgrid;

import com.revinate.sendgrid.exception.ResourceNotFoundException;
import com.revinate.sendgrid.model.*;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.assumeThat;

public class ApiTest {

    private static final String API_KEY = System.getenv("SENDGRID_API_KEY");

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    SendGrid sendGrid;

    @Before
    public void setUp() throws Exception {
        assumeThat(API_KEY, notNullValue());
        sendGrid = SendGrid.create(API_KEY).build();
    }

    @Test
    public void getAccount_shouldReturnAccount() throws Exception {
        Account account = sendGrid.account().retrieve();
        assertThat(account, notNullValue());
    }

    @Test
    public void getApiKeys_shouldReturnApiKeys() throws Exception {
        List<ApiKey> apiKeys = sendGrid.apiKeys().list();
        assertThat(apiKeys, notNullValue());
    }

    @Test
    public void getEventWebhookSettings_shouldReturnSettings() throws Exception {
        EventWebhookSettings settings = sendGrid.eventWebhookSettings().retrieve();
        assertThat(settings, notNullValue());
    }

    @Test
    public void getIps_shouldReturnIps() throws Exception {
        List<Ip> ips = sendGrid.ips().list();
        assertThat(ips, notNullValue());
    }

    @Test
    public void getIpPools_shouldReturnIpPools() throws Exception {
        List<IpPool> ipPools = sendGrid.ipPools().list();
        assertThat(ipPools, notNullValue());
    }

    @Test
    public void getMailSettings_shouldReturnSettings() throws Exception {
        List<MailSetting> settings = sendGrid.mailSettings().list();
        assertThat(settings, notNullValue());
    }

    @Test
    public void getSubusers_shouldReturnSubusers() throws Exception {
        List<Subuser> subusers = sendGrid.subusers().list();
        assertThat(subusers, notNullValue());

        Subuser subuser = sendGrid.subuser("testsubuser123").retrieve();
        assertThat(subuser, notNullValue());
    }

    @Test
    public void getSubusersWithLimit_shouldReturnSubusers() throws Exception {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("limit", 2);

        List<Subuser> subusers = sendGrid.subusers().list(parameters);
        assertThat(subusers, notNullValue());
        assertThat(subusers, hasSize(2));
    }

    @Test
    public void getResourcesOnBehalfOf_shouldReturnResources() throws Exception {
        List<IpPool> ipPools = sendGrid.onBehalfOf("testsubuser123").ipPools().list();
        assertThat(ipPools, notNullValue());

        IpPool ipPool = sendGrid.onBehalfOf("testsubuser123").ipPool("transactional").retrieve();
        assertThat(ipPool, notNullValue());

        List<ApiKey> apiKeys = sendGrid.onBehalfOf("testsubuser123").apiKeys().list();
        assertThat(apiKeys, notNullValue());
    }

    @Test
    public void createSubuser_shouldReturnSubuser() throws Exception {
        Subuser subuser = new Subuser("testsubuser124", "sendgridjava@mailinator.com", "secretpasswordfortesting124");

        List<Ip> ips = sendGrid.ips().list();
        for (Ip ip : ips) {
            if (ip.getSubusers() == null || ip.getSubusers().isEmpty()) {
                subuser.addIp(ip.getIp());
                break;
            }
        }

        Subuser subuser1 = sendGrid.subusers().create(subuser);

        assertThat(subuser1.getUsername(), equalTo("testsubuser124"));

        sendGrid.subuser(subuser1).delete();

        thrown.expect(ResourceNotFoundException.class);
        thrown.expectMessage("Subuser not found");

        sendGrid.subuser(subuser1.getUsername()).retrieve();
    }

    @Test
    public void createApiKey_shouldReturnApiKey() throws Exception {
        ApiKey apiKey = new ApiKey("testapikey");
        apiKey.addScope("mail.send");

        ApiKey apiKey1 = sendGrid.apiKeys().create(apiKey);

        assertThat(apiKey1, notNullValue());
        assertThat(apiKey1.getName(), equalTo("testapikey"));
        assertThat(apiKey1.getApiKeyId(), notNullValue());
        assertThat(apiKey1.getApiKey(), notNullValue());

        ApiKey apiKey2 = sendGrid.apiKey(apiKey1.getApiKeyId()).retrieve();

        assertThat(apiKey2, notNullValue());
        assertThat(apiKey2.getName(), equalTo("testapikey"));
        assertThat(apiKey2.getApiKeyId(), equalTo(apiKey1.getApiKeyId()));

        sendGrid.apiKey(apiKey1).delete();

        ApiKey apiKey3 = sendGrid.apiKey(apiKey1.getApiKeyId()).retrieve();

        // SendGrid does not throw a 404 here, so we get an API key with unset fields
        assertThat(apiKey3.getApiKeyId(), nullValue());
    }

    @Test
    public void createApiKeyOnBehalfOf_shouldReturnApiKey() throws Exception {
        ApiKey apiKey = new ApiKey("testapikey");
        apiKey.addScope("mail.send");

        ApiKey apiKey1 = sendGrid.onBehalfOf("testsubuser123").apiKeys().create(apiKey);

        assertThat(apiKey1, notNullValue());
        assertThat(apiKey1.getName(), equalTo("testapikey"));
        assertThat(apiKey1.getApiKeyId(), notNullValue());
        assertThat(apiKey1.getApiKey(), notNullValue());

        ApiKey apiKey2 = sendGrid.onBehalfOf("testsubuser123").apiKey(apiKey1.getApiKeyId()).retrieve();

        assertThat(apiKey2, notNullValue());
        assertThat(apiKey2.getName(), equalTo("testapikey"));
        assertThat(apiKey2.getApiKeyId(), equalTo(apiKey1.getApiKeyId()));

        sendGrid.onBehalfOf("testsubuser123").apiKey(apiKey1).delete();

        ApiKey apiKey3 = sendGrid.onBehalfOf("testsubuser123").apiKey(apiKey1.getApiKeyId()).retrieve();

        // SendGrid does not throw a 404 here, so we get an API key with unset fields
        assertThat(apiKey3.getApiKeyId(), nullValue());
    }

    @Test
    public void createDomainWhitelabelOnBehalfOf_shouldReturnWhitelabel() throws Exception {
        Whitelabel whitelabel = new Whitelabel("email.com", "m");
        whitelabel.setAutomaticSecurity(true);
        whitelabel.setDefault(true);

        Whitelabel whitelabel1 = sendGrid.onBehalfOf("testsubuser123").domainWhitelabels().create(whitelabel);

        assertThat(whitelabel1, notNullValue());
        assertThat(whitelabel1.getId(), notNullValue());
        assertThat(whitelabel1.getDomain(), equalTo("email.com"));
        assertThat(whitelabel1.getSubdomain(), equalTo("m"));

        Whitelabel whitelabel2 = sendGrid.onBehalfOf("testsubuser123").domainWhitelabel(whitelabel1).retrieve();

        assertThat(whitelabel2, notNullValue());
        assertThat(whitelabel2.getId(), equalTo(whitelabel1.getId()));
        assertThat(whitelabel2.getDomain(), equalTo("email.com"));
        assertThat(whitelabel2.getSubdomain(), equalTo("m"));

        WhitelabelValidation validation = sendGrid.onBehalfOf("testsubuser123").domainWhitelabel(whitelabel1).validate();

        assertThat(validation, notNullValue());

        sendGrid.onBehalfOf("testsubuser123").domainWhitelabel(whitelabel1).delete();

        thrown.expect(ResourceNotFoundException.class);
        thrown.expectMessage("Whitelabel domain not found");

        sendGrid.onBehalfOf("testsubuser123").domainWhitelabel(whitelabel1).retrieve();
    }

    @Test
    public void createLinkWhitelabelOnBehalfOf_shouldReturnWhitelabel() throws Exception {
        Whitelabel whitelabel = new Whitelabel("email.com", "c");
        whitelabel.setDefault(true);

        Whitelabel whitelabel1 = sendGrid.onBehalfOf("testsubuser123").linkWhitelabels().create(whitelabel);

        assertThat(whitelabel1, notNullValue());
        assertThat(whitelabel1.getId(), notNullValue());
        assertThat(whitelabel1.getDomain(), equalTo("email.com"));
        assertThat(whitelabel1.getSubdomain(), equalTo("c"));

        Whitelabel whitelabel2 = sendGrid.onBehalfOf("testsubuser123").linkWhitelabel(whitelabel1).retrieve();

        assertThat(whitelabel2, notNullValue());
        assertThat(whitelabel2.getId(), equalTo(whitelabel1.getId()));
        assertThat(whitelabel2.getDomain(), equalTo("email.com"));
        assertThat(whitelabel2.getSubdomain(), equalTo("c"));

        WhitelabelValidation validation = sendGrid.onBehalfOf("testsubuser123").linkWhitelabel(whitelabel1).validate();

        assertThat(validation, notNullValue());

        sendGrid.onBehalfOf("testsubuser123").linkWhitelabel(whitelabel1).delete();

        thrown.expect(ResourceNotFoundException.class);
        thrown.expectMessage("Whitelabel email link not found");

        sendGrid.onBehalfOf("testsubuser123").linkWhitelabel(whitelabel1).retrieve();
    }

    @Test
    public void createIpPool_shouldReturnIpPool() throws Exception {
        IpPool ipPool = new IpPool();
        ipPool.setName("testpool");

        IpPool ipPool1 = sendGrid.ipPools().create(ipPool);

        assertThat(ipPool1, notNullValue());
        assertThat(ipPool1.getName(), equalTo(ipPool.getName()));

        IpPool ipPool2 = sendGrid.ipPool(ipPool1.getName()).retrieve();

        assertThat(ipPool2, notNullValue());
        assertThat(ipPool2.getName(), equalTo(ipPool1.getName()));
        assertThat(ipPool2.getIps(), emptyCollectionOf(Ip.class));
        sendGrid.ipPool(ipPool1).delete();

        thrown.expect(ResourceNotFoundException.class);

        sendGrid.ipPool(ipPool1.getName()).retrieve();
    }

    @Test
    public void createMonitor_shouldReturnMonitor() throws Exception {
        Monitor monitor = new Monitor("sendgridjava@mailinator.com", 5000);

        Monitor monitor1 = sendGrid.subuser("testsubuser123").monitor().create(monitor);

        assertThat(monitor1, notNullValue());
        assertThat(monitor1.getEmail(), equalTo(monitor.getEmail()));
        assertThat(monitor1.getFrequency(), equalTo(monitor.getFrequency()));

        Monitor monitor2 = sendGrid.subuser("testsubuser123").monitor().retrieve();

        assertThat(monitor2, notNullValue());
        assertThat(monitor2.getEmail(), equalTo(monitor.getEmail()));
        assertThat(monitor2.getFrequency(), equalTo(monitor.getFrequency()));

        sendGrid.subuser("testsubuser123").monitor().delete();

        thrown.expect(ResourceNotFoundException.class);

        sendGrid.subuser("testsubuser123").monitor().retrieve();
    }

    @Test
    public void updateMailSettingOnBehalfOf_shouldUpdateSetting() throws Exception {
        MailSetting setting = new MailSetting("forward_spam", true, "sendgridjava@mailinator.com");

        MailSetting setting1 = sendGrid.onBehalfOf("testsubuser123").mailSetting(setting).update(setting);

        assertThat(setting1, notNullValue());
        assertThat(setting1.getEmail(), equalTo("sendgridjava@mailinator.com"));
        assertThat(setting1.getEnabled(), equalTo(true));

        setting.setEnabled(false);

        MailSetting setting2 = sendGrid.onBehalfOf("testsubuser123").mailSetting(setting).update(setting);

        assertThat(setting2, notNullValue());
    }

    @Test
    public void updateEventWebhookSettingsOnBehalfOf_shouldUpdateSettings() throws Exception {
        EventWebhookSettings settings = new EventWebhookSettings("http://event.webhook.receiver", true);

        EventWebhookSettings settings1 = sendGrid.onBehalfOf("testsubuser123").eventWebhookSettings().update(settings);

        assertThat(settings1, notNullValue());
        assertThat(settings1.getUrl(), equalTo("http://event.webhook.receiver"));
        assertThat(settings1.getEnabled(), equalTo(true));

        EventWebhookSettings settings2 = new EventWebhookSettings("http://event.webhook.receiver", false);

        EventWebhookSettings settings3 = sendGrid.onBehalfOf("testsubuser123").eventWebhookSettings().update(settings2);

        assertThat(settings3, notNullValue());
    }

    @Test
    public void sendEmail_shouldSendEmail() throws Exception {
        Email email = new Email();
        email.setFrom("sendgridjava@mailinator.com");
        email.addTo("sendgridjava@mailinator.com");
        email.setSubject("Test");
        email.setText("This is a test.");

        Response response = sendGrid.mail().send(email);

        assertThat(response, notNullValue());
        assertThat(response.getMessage(), equalTo("success"));
    }

    @Test
    public void sendEmail_shouldSendEmailWithAttachments() throws Exception {
        Email email = new Email();
        email.setFrom("sendgridjava@mailinator.com");
        email.addTo("sendgridjava@mailinator.com");
        email.setSubject("Test");
        email.setText("This is a test.");
        email.setAttachment("test.txt", getClass().getResourceAsStream("/test.txt"));
        email.setAttachment("image.png", getClass().getResourceAsStream("/image.png"));

        Response response = sendGrid.mail().send(email);

        assertThat(response, notNullValue());
        assertThat(response.getMessage(), equalTo("success"));
    }

    @Test
    public void sendEmail_shouldSendHtmlEmailWithContentIds() throws Exception {
        Email email = new Email();
        email.setFrom("sendgridjava@mailinator.com");
        email.addTo("sendgridjava@mailinator.com");
        email.setSubject("Test");
        email.setText("This is a test.");
        email.setHtml("This is a test. <img src=\"cid:id1\"></img>");
        email.setAttachment("image.png", getClass().getResourceAsStream("/image.png"), "id1");

        Response response = sendGrid.mail().send(email);

        assertThat(response, notNullValue());
        assertThat(response.getMessage(), equalTo("success"));
    }

    @Test
    public void sendEmail_shouldSendEmailUsingSmtpApiTo() throws Exception {
        Email email = new Email();
        email.setFrom("sendgridjava@mailinator.com");
        email.addSmtpApiTo("sendgridjava@mailinator.com", "SendGrid Java");
        email.setSubject("Test");
        email.setText("This is a test.");

        Response response = sendGrid.mail().send(email);

        assertThat(response, notNullValue());
        assertThat(response.getMessage(), equalTo("success"));
    }

    @Test
    public void sendEmail_shouldSendEmailWithCcAndBcc() throws Exception {
        Email email = new Email();
        email.setFrom("sendgridjava@mailinator.com", "SendGrid Java");
        email.addTo("sendgridjava@mailinator.com", "SendGrid Java");
        email.addCc("sendgridjava2@mailinator.com", "SendGrid Java Two");
        email.addBcc("sendgridjava3@mailinator.com", "SendGrid Java Three");
        email.setReplyTo("no-reply@mailinator.com");
        email.setSubject("Test");
        email.setText("This is a test.");

        Response response = sendGrid.mail().send(email);

        assertThat(response, notNullValue());
        assertThat(response.getMessage(), equalTo("success"));
    }

    @Test
    public void sendEmail_shouldSendEmailWithExtraHeaders() throws Exception {
        Email email = new Email();
        email.setFrom("sendgridjava@mailinator.com");
        email.addTo("sendgridjava@mailinator.com", "SendGrid Java");
        email.setSubject("Test");
        email.setText("This is a test.");
        email.setHeader("x-custom-header-1", "test1");
        email.setHeader("x-custom-header-2", "test2");

        Response response = sendGrid.mail().send(email);

        assertThat(response, notNullValue());
        assertThat(response.getMessage(), equalTo("success"));
    }

    @Test
    public void sendEmail_shouldSendEmailWithName() throws Exception {
        Email email = new Email();
        email.setFrom("sendgridjava@mailinator.com");
        email.addTo("sendgridjava@mailinator.com");
        email.addTo("sendgridjava2@mailinator.com");
        email.addToName("SendGrid Java");
        email.addToName("no name");
        email.setSubject("Test");
        email.setText("This is a test.");

        Response response = sendGrid.mail().send(email);

        assertThat(response, notNullValue());
        assertThat(response.getMessage(), equalTo("success"));
    }
}
