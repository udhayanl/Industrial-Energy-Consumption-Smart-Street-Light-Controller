package com.example.industrial_energy_consumption;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
class IndustrialEnergyConsumptionApplicationTests {

	@Test
	void contextLoads() {
	}

}
