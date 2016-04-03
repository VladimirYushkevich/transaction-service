package company.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Files;
import company.Application;
import company.dto.TransactionDTO;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.RestAssured.when;
import static com.jayway.restassured.http.ContentType.JSON;
import static org.hamcrest.Matchers.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@IntegrationTest("server.port:0")
public class TransactionControllerIT {

    @Autowired
    private ObjectMapper objectMapper;

    @Value("http://localhost:${local.server.port}/transactionservice/")
    private String base;

    private String transactionJsonFileName = "json/transaction.json";

    private TransactionDTO transactionDTO;
    private Long existedTransactionId = 10L;
    private Long nonExistedTransactionId = 1L;

    @Before
    public void setUp() throws Exception {
        transactionDTO = objectMapper.readValue(Files.toString(
                new File(Optional.ofNullable(getClass().getClassLoader().getResource(transactionJsonFileName)).get().getFile()),
                StandardCharsets.UTF_8), new TypeReference<TransactionDTO>() {
        });
    }

    @Test
    public void testGetTransactionById_notFound() {
        String url = String.format(base + "transaction/%s", nonExistedTransactionId);
        when()
            .get(url).prettyPeek()
        .then()
            .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    public void testGetTransactionById_found() throws Exception {
        createAndVerifyTransaction(existedTransactionId, transactionDTO, 5000.0f, "cars", null);

        String url = String.format(base + "transaction/%s", existedTransactionId);
        when()
            .get(url).prettyPeek()
        .then()
            .statusCode(HttpStatus.OK.value())
            .body("amount", is(5000.0f))
            .body("type", equalTo("cars"))
            .body("parent_id", nullValue());
    }

    @Test
    public void testGetIdsByType_found() throws Exception {
        createAndVerifyTransaction(21L, TransactionDTO.builder().amount(5000.0).type("toys").build(),
                5000.0f, "toys", null);
        createAndVerifyTransaction(22L, TransactionDTO.builder().amount(10000.0).type("booking").parentId(21L).build(),
                10000.0f, "booking", 21);
        createAndVerifyTransaction(23L, TransactionDTO.builder().amount(20000.0).type("booking").parentId(22L).build(),
                20000.0f, "booking", 22);

        String url = String.format(base + "types/%s", "booking");
        when()
            .get(url).prettyPeek()
        .then()
            .body("$", containsInAnyOrder(22, 23))
            .statusCode(HttpStatus.OK.value());
    }

    @Test
    public void testGetIdsByType_notFound() throws Exception {
        String url = String.format(base + "types/%s", "notExisted");
        when()
            .get(url).prettyPeek()
        .then()
            .body("$", empty())
            .statusCode(HttpStatus.OK.value());
    }

    @Test
    public void testGetTransactionSum_transitive() throws Exception {
        createAndVerifyTransaction(11L, TransactionDTO.builder().amount(5000.0).type("cars").build(),
                5000.0f, "cars", null);
        createAndVerifyTransaction(12L, TransactionDTO.builder().amount(10000.0).type("shopping").parentId(11L).build(),
                10000.0f, "shopping", 11);
        createAndVerifyTransaction(13L, TransactionDTO.builder().amount(20000.0).type("shopping").parentId(12L).build(),
                20000.0f, "shopping", 12);

        verifySum(11L, 35000.0f);
        verifySum(12L, 30000.0f);
        verifySum(13L, 20000.0f);
    }

    @Test
    public void testCreateTransaction_noFields() {
        String url = String.format(base + "transaction/%s", nonExistedTransactionId);
        given()
            .contentType(JSON)
            .body(new JSONObject().toString())
        .when()
            .put(url).prettyPeek()
        .then()
            .body("errors.field", containsInAnyOrder("amount", "type"))
            .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    public void testCreateTransaction_noParentFound() {
        String url = String.format(base + "transaction/%s", 101L);
        given()
            .contentType(JSON)
            .body(new JSONObject().toString())
            .body(new JSONObject().put("amount", 5000.0).put("type", "cars").put("parent_id", 202L).toString())
        .when()
            .put(url).prettyPeek()
        .then()
            .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    public void testCreateTransaction_incorrectFields() {
        String url = String.format(base + "transaction/%s", nonExistedTransactionId);
        given()
            .contentType(JSON)
            .body(new JSONObject().put("incorrectAmount", 5000.0).put("incorrectType", "cars")
                    .put("incorrectParentId", nonExistedTransactionId).toString())
        .when()
            .put(url).prettyPeek()
        .then()
            .body("errors.field", containsInAnyOrder("amount", "type"))
            .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    private void verifySum(Long parentId, Float sum) {
        String url = String.format(base + "sum/%s", parentId);
        when()
            .get(url).prettyPeek()
        .then()
            .statusCode(HttpStatus.OK.value())
            .body("sum", is(sum));
    }

    private void createAndVerifyTransaction(Long transactionId, TransactionDTO dto, Float amount, String type, Integer parentId)
            throws Exception {
        String url = String.format(base + "transaction/%s", transactionId);
        given()
            .contentType(JSON)
            .body(objectMapper.writeValueAsString(dto))
        .when()
            .put(url).prettyPeek()
        .then()
            .statusCode(HttpStatus.OK.value())
            .body("amount", is(amount))
            .body("type", equalTo(type))
            .body("parent_id", is(parentId));
    }
}
