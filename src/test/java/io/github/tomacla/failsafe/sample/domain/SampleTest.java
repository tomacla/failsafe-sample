package io.github.tomacla.failsafe.sample.domain;

import org.junit.Test;

public class SampleTest {

    @Test(expected = IllegalArgumentException.class)
    public void setName_null() {
	Sample s = new Sample();
	s.setName(null);
    }

    @Test
    public void setName_ok() {
	Sample s = new Sample();
	s.setName("foobar");
    }

    @Test(expected = IllegalArgumentException.class)
    public void setName_tooshort() {
	Sample s = new Sample();
	s.setName("f");
    }

}
