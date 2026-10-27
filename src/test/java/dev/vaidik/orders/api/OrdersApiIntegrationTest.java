package dev.vaidik.orders.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end through the real security chain, controllers, JPA and Flyway migrations (H2 in PostgreSQL mode).
 */
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext
class OrdersApiIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper json;

    @Test
    void customerCanRegisterOrderAndCancel() throws Exception {
        String token = register("buyer-" + UUID.randomUUID() + "@test.dev");

        long productId = firstSeededProductId();

        String orderBody = """
                {"items":[{"productId":%d,"quantity":2}]}""".formatted(productId);
        JsonNode order = read(mvc.perform(auth(post("/api/v1/orders"), token)
                        .contentType(MediaType.APPLICATION_JSON).content(orderBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.items[0].quantity").value(2))
                .andReturn().getResponse().getContentAsString());

        mvc.perform(auth(get("/api/v1/orders"), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));

        mvc.perform(auth(post("/api/v1/orders/" + order.get("id").asLong() + "/cancel"), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void customerCannotSeeSomeoneElsesOrder() throws Exception {
        String alice = register("alice-" + UUID.randomUUID() + "@test.dev");
        String bob = register("bob-" + UUID.randomUUID() + "@test.dev");

        String body = """
                {"items":[{"productId":%d,"quantity":1}]}""".formatted(firstSeededProductId());
        JsonNode order = read(mvc.perform(auth(post("/api/v1/orders"), alice)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString());

        mvc.perform(auth(get("/api/v1/orders/" + order.get("id").asLong()), bob))
                .andExpect(status().isForbidden());
    }

    @Test
    void onlyAdminsCanCreateProducts() throws Exception {
        String product = """
                {"sku":"T-%s","name":"Test","price":10.00,"stock":3}""".formatted(UUID.randomUUID());

        mvc.perform(post("/api/v1/products").contentType(MediaType.APPLICATION_JSON).content(product))
                .andExpect(status().isUnauthorized());

        String customer = register("c-" + UUID.randomUUID() + "@test.dev");
        mvc.perform(auth(post("/api/v1/products"), customer)
                        .contentType(MediaType.APPLICATION_JSON).content(product))
                .andExpect(status().isForbidden());

        String admin = login("admin@test.dev", "admin-password-123");
        mvc.perform(auth(post("/api/v1/products"), admin)
                        .contentType(MediaType.APPLICATION_JSON).content(product))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.stock").value(3));
    }

    @Test
    void invalidInputReturnsProblemDetailWithFieldErrors() throws Exception {
        mvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"not-an-email","password":"short","fullName":""}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.password").exists())
                .andExpect(jsonPath("$.errors.fullName").exists());
    }

    @Test
    void orderingMoreThanStockIsRejected() throws Exception {
        String token = register("greedy-" + UUID.randomUUID() + "@test.dev");
        String body = """
                {"items":[{"productId":%d,"quantity":1000}]}""".formatted(firstSeededProductId());

        mvc.perform(auth(post("/api/v1/orders"), token)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value(org.hamcrest.Matchers.containsString("Not enough stock")));
    }

    // --- helpers -------------------------------------------------------------------------

    private String register(String email) throws Exception {
        String body = """
                {"email":"%s","password":"password123","fullName":"Test User"}""".formatted(email);
        return read(mvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString()).get("accessToken").asText();
    }

    private String login(String email, String password) throws Exception {
        String body = """
                {"email":"%s","password":"%s"}""".formatted(email, password);
        return read(mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString()).get("accessToken").asText();
    }

    private long firstSeededProductId() throws Exception {
        return read(mvc.perform(get("/api/v1/products").param("q", "keyboard"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString())
                .get("content").get(0).get("id").asLong();
    }

    private static MockHttpServletRequestBuilder auth(MockHttpServletRequestBuilder req, String token) {
        return req.header("Authorization", "Bearer " + token);
    }

    private JsonNode read(String body) throws Exception {
        return json.readTree(body);
    }
}
