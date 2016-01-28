package io.github.tomacla.failsafe.sample.rest;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTestNg;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.github.tomacla.failsafe.sample.JerseyConfiguration;
import io.github.tomacla.failsafe.sample.SpringConfiguration;
import io.github.tomacla.failsafe.sample.domain.Sample;

public class SampleRestIT extends JerseyTestNg.ContainerPerClassTest {

    private Map<String, Long> entities;

    @BeforeClass
    public void beforeClass() {
	System.out.println("RUN WITHOUT JETTY");
	entities = new HashMap<>();
	entities.put("First entity", -1L);
	entities.put("Second entity", -1L);
    }

    @Override
    protected Application configure() {
	AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(SpringConfiguration.class);
	ResourceConfig rc = new JerseyConfiguration();
	rc.property("contextConfig", ctx);

	return rc;
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
	Response response = target("/").request().post(Entity.text(entityName));
	Assert.assertEquals(Status.ACCEPTED.getStatusCode(), response.getStatusInfo().getStatusCode());
	Sample newEntity = response.readEntity(Sample.class);
	entities.put(newEntity.getName(), newEntity.getId());
	response.close();
    }

    @Test(dependsOnMethods = "test_1_create", dataProvider = "entities")
    public void test_2_verifyCreation(Long entityCode, String entityName) {
	Response response = target("/" + entityCode.toString()).request().get();
	Assert.assertEquals(Status.OK.getStatusCode(), response.getStatusInfo().getStatusCode());
	Sample getEntity = response.readEntity(Sample.class);
	Assert.assertEquals(entityName, getEntity.getName());
	response.close();
    }

    @Test(dependsOnMethods = "test_2_verifyCreation", dataProvider = "entities")
    public void test_3_delete(Long entityCode, String entityName) {
	Response response = target("/" + entityCode.toString()).request().delete();
	Assert.assertEquals(Status.ACCEPTED.getStatusCode(), response.getStatusInfo().getStatusCode());
	response.close();
    }

    @Test(dependsOnMethods = "test_3_delete", dataProvider = "entities")
    public void test_4_verifyDeletion(Long entityCode, String entityName) {
	Response response = target("/" + entityCode.toString()).request().get();
	Assert.assertEquals(Status.NOT_FOUND.getStatusCode(), response.getStatusInfo().getStatusCode());
	response.close();
    }

}