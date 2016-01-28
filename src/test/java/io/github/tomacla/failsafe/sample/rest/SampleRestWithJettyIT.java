package io.github.tomacla.failsafe.sample.rest;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.github.tomacla.failsafe.sample.domain.Sample;

public class SampleRestWithJettyIT {

    private Map<String, Long> entities;
    private Client client;

    @BeforeClass
    public void beforeClass() {
	System.out.println("RUN WITH JETTY");
	client = ClientBuilder.newClient();
	entities = new HashMap<>();
	entities.put("First entity", -1L);
	entities.put("Second entity", -1L);
    }

    @DataProvider(name = "entities")
    public Object[][] entities() {
	Object[][] data = new Object[entities.size()][2];
	int i = 0;
	for (Entry<String, Long> entries : entities.entrySet()) {
	    data[i][0] = entries.getValue();
	    data[i][1] = entries.getKey();
	    i++;
	}
	return data;
    }

    @Test(dataProvider = "entities")
    public void test_1_create(Long entityCode, String entityName) {
	WebTarget target = client.target("http://localhost:8080").path("/");
	Response response = target.request().post(Entity.text(entityName));
	Assert.assertEquals(Status.ACCEPTED.getStatusCode(), response.getStatusInfo().getStatusCode());
	Sample newEntity = response.readEntity(Sample.class);
	entities.put(newEntity.getName(), newEntity.getId());
	response.close();
    }

    @Test(dependsOnMethods = "test_1_create", dataProvider = "entities")
    public void test_2_verifyCreation(Long entityCode, String entityName) {
	WebTarget target = client.target("http://localhost:8080").path("/" + entityCode.toString());
	Response response = target.request().get();
	Assert.assertEquals(Status.OK.getStatusCode(), response.getStatusInfo().getStatusCode());
	Sample getEntity = response.readEntity(Sample.class);
	Assert.assertEquals(entityName, getEntity.getName());
	response.close();
    }

    @Test(dependsOnMethods = "test_2_verifyCreation", dataProvider = "entities")
    public void test_3_delete(Long entityCode, String entityName) {
	WebTarget target = client.target("http://localhost:8080").path("/" + entityCode.toString());
	Response response = target.request().delete();
	Assert.assertEquals(Status.ACCEPTED.getStatusCode(), response.getStatusInfo().getStatusCode());
	response.close();
    }

    @Test(dependsOnMethods = "test_3_delete", dataProvider = "entities")
    public void test_4_verifyDeletion(Long entityCode, String entityName) {
	WebTarget target = client.target("http://localhost:8080").path("/" + entityCode.toString());
	Response response = target.request().get();
	Assert.assertEquals(Status.NOT_FOUND.getStatusCode(), response.getStatusInfo().getStatusCode());
	response.close();
    }

}