package com.n26.controller;

import com.n26.model.Stat;
import com.n26.model.Txn;
import com.n26.service.TxnService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = TxnController.class)
public class TxnControllerTest {
    @Autowired
    private MockMvc mvc;
    @MockBean
    private TxnService txnService;

    @Test
    public void POST_transactions_should_return_201_on_success() throws Exception {
        when(txnService.create(any())).thenReturn(Optional.of(new Txn(BigDecimal.ONE, Instant.now())));

        mvc.perform(
                post("/transactions")
                        .contentType(APPLICATION_JSON)
                        .content("{" +
                                "\"amount\":\"12.3343\"," +
                                "\"timestamp\":\"2018-07-17T09:59:51.312Z\"" +
                                "}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$").doesNotExist());
    }

    @Test
    public void POST_transactions_should_return_422_when_any_of_the_fields_are_not_parsable() throws Exception {
        String amountValidationError = mvc.perform(
                post("/transactions")
                        .contentType(APPLICATION_JSON)
                        .content("{" +
                                "\"amount\":\"12.3343a\"," +
                                "\"timestamp\":\"2018-07-17T09:59:51.312Z\"" +
                                "}"))
                .andExpect(status().isUnprocessableEntity())
                .andReturn().getResponse().getContentAsString();

        assertEquals("{" +
                "\"fieldErrors\":[{" +
                "\"field\":\"com.n26.model.Txn[\\\"amount\\\"]\"," +
                "\"message\":\"Value '12.3343a' must be serializble to: class java.math.BigDecimal\"" +
                "}]}", amountValidationError);

        String timestampValidationError = mvc.perform(
                post("/transactions")
                        .contentType(APPLICATION_JSON)
                        .content("{" +
                                "\"amount\":\"12.3343\"," +
                                "\"timestamp\":\"2018-07-17_T_09:59:51.312Z\"" +
                                "}"))
                .andExpect(status().isUnprocessableEntity())
                .andReturn().getResponse().getContentAsString();

        assertEquals("{" +
                "\"fieldErrors\":[{" +
                "\"field\":\"com.n26.model.Txn[\\\"timestamp\\\"]\"," +
                "\"message\":\"Value '2018-07-17_T_09:59:51.312Z' must be serializble to: class java.time.Instant\"" +
                "}]}", timestampValidationError);
    }

    @Test
    public void POST_transactions_should_return_422_when_transaction_date_is_in_the_future() throws Exception {
        doCallRealMethod().when(txnService).create(any());

        mvc.perform(
                post("/transactions")
                        .contentType(APPLICATION_JSON)
                        .content("{" +
                                "\"amount\":\"12.3343\"," +
                                "\"timestamp\":\"" + Instant.now().plusMillis(1000) + "\"" +
                                "}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$").doesNotExist());
    }

    @Test
    public void POST_transactions_should_return_204_when_transaction_is_older_than_time_window() throws Exception {
        when(txnService.create(any())).thenReturn(Optional.empty());

        mvc.perform(
                post("/transactions")
                        .contentType(APPLICATION_JSON)
                        .content("{" +
                                "\"amount\":\"12.3343\"," +
                                "\"timestamp\":\"" + Instant.now().minusMillis(70000) + "\"" +
                                "}"))
                .andExpect(status().isNoContent())
                .andExpect(jsonPath("$").doesNotExist());
    }

    @Test
    public void GET_transaction_default_statistics_should_return_200_on_success() throws Exception {
        when(txnService.getTxnStat(any())).thenReturn(new Stat());

        mvc.perform(
                get("/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sum").value("0.00"))
                .andExpect(jsonPath("$.avg").value("0.00"))
                .andExpect(jsonPath("$.max").value("0.00"))
                .andExpect(jsonPath("$.min").value("0.00"))
                .andExpect(jsonPath("$.count").value("0"));
    }

    @Test
    public void DELETE_transactions_should_return_204_on_success_deletion() throws Exception {
        when(txnService.create(any())).thenReturn(Optional.empty());

        mvc.perform(
                delete("/transactions"))
                .andExpect(status().isNoContent());
    }
}
