package com.moinak.ecomdemo.productservice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moinak.ecomdemo.productservice.model.ProductRequest;
import com.moinak.ecomdemo.productservice.repository.ProductRepository;
import org.junit.Before;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MockMvcBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
class ProductServiceApplicationTests {

	@Container
	static MongoDBContainer mongoDbContainer = new MongoDBContainer("mongo:7.0.7");
	@DynamicPropertySource
	static void setProperties(DynamicPropertyRegistry dynamicPropertyRegistry) {
		dynamicPropertyRegistry.add("spring.data.mongodb.uri", mongoDbContainer::getReplicaSetUrl);
	}
	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private ProductRepository productRepository;

	@BeforeEach
	public void clearRepository() {
		productRepository.deleteAll();
	}

	@Test
	void testCreateSingleProduct() throws Exception {
		ProductRequest request = getProductRequest("sample-product-1", "This is the first sample product", 1.0);

		String requestString = objectMapper.writeValueAsString(request);

		mockMvc.perform(MockMvcRequestBuilders
						.post("/api/product")
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestString))
				.andExpect(status().isCreated());

		Assertions.assertEquals(1, productRepository.findAll().size());
	}

	@Test
	void testGetAllProducts() throws Exception {


		ProductRequest requestOne = getProductRequest("sample-product-1", "This is the first sample product", 1.0);
		ProductRequest requestTwo = getProductRequest("sample-product-2", "This is the second sample product", 2.0);

		mockMvc.perform(MockMvcRequestBuilders
					.post("/api/product")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(requestOne)))
				.andExpect(status().isCreated());

		mockMvc.perform(MockMvcRequestBuilders
						.post("/api/product")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(requestTwo)))
				.andExpect(status().isCreated());

		mockMvc.perform(MockMvcRequestBuilders
					.get("/api/product"))
				.andExpect(status().isOk());

		Assertions.assertEquals(2, productRepository.findAll().size());
	}

	@Test
	void testDeleteAllProducts() throws Exception {
		ProductRequest request = getProductRequest("sample-product", "sample-product", 123.321);

		mockMvc.perform(MockMvcRequestBuilders.post("/api/product")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated());

		Assertions.assertEquals(1, productRepository.findAll().size());

		mockMvc.perform(MockMvcRequestBuilders.delete("/api/product"))
				.andExpect(status().isOk());

		Assertions.assertEquals(0, productRepository.findAll().size());
	}

	private ProductRequest getProductRequest(String name, String description, double price) {
		return ProductRequest.builder()
				.name(name)
				.description(description)
				.price(BigDecimal.valueOf(price))
				.build();
	}
}
