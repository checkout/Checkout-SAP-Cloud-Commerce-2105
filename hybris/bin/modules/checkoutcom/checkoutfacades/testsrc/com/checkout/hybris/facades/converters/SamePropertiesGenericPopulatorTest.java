package com.checkout.hybris.facades.converters;

import de.hybris.bootstrap.annotations.UnitTest;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

@UnitTest
public class SamePropertiesGenericPopulatorTest {

	private SamePropertiesGenericPopulator testObj;

	@Before
	public void setUp() throws Exception {
		testObj = new SamePropertiesGenericPopulator();
	}

	@Test
	public void populate_shouldCallDataMapper() {
		final Car source = new Car("rollsRoyce", 20L);
		final Plane result = new Plane();

		testObj.populate(source, result);

		assertThat(result).hasFieldOrPropertyWithValue("engine", "rollsRoyce");
		assertThat(result).hasFieldOrPropertyWithValue("passengers", 20L);
	}


	private static class Car {
		private String engine;
		private Long passengers;

		public Car() {
		}

		private Car(final String engine, final Long passengers) {
			this.engine = engine;
			this.passengers = passengers;
		}

		public String getEngine() {
			return engine;
		}

		public void setEngine(final String engine) {
			this.engine = engine;
		}

		public Long getPassengers() {
			return passengers;
		}

		public void setPassengers(final Long passengers) {
			this.passengers = passengers;
		}
	}

	private static class Plane {
		private String engine;
		private Long passengers;

		public Plane() {
		}

		private Plane(final String engine, final Long passengers) {
			this.engine = engine;
			this.passengers = passengers;
		}

		public String getEngine() {
			return engine;
		}

		public void setEngine(final String engine) {
			this.engine = engine;
		}

		public Long getPassengers() {
			return passengers;
		}

		public void setPassengers(final Long passengers) {
			this.passengers = passengers;
		}
	}
}
