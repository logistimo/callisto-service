package com.logistimo.callisto.controller;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** @author Chandrakant */
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class UserControllerTest {
  @Autowired private MockMvc mvc;

  @Test
  public void aDeleteUser() throws Exception {
    mvc.perform(
            MockMvcRequestBuilders.delete("/user/delete")
                .param("userId", "ck_test")
                .contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().isOk())
        .andExpect(content().string("success"));
  }

  @Test
  public void bCreateUser() throws Exception {
    mvc.perform(
            MockMvcRequestBuilders.post("/user/register")
                .content(
                    "{\n"
                        + "  \"user_id\": \"ck_test\",\n"
                        + "  \"server_configs\": [{\n"
                        + "    \"id\": 1,\n"
                        + "    \"name\": \"mysql_logistimo\",\n"
                        + "    \"hosts\": [\"localhost\"],\n"
                        + "    \"username\": \"root\",\n"
                        + "    \"password\": \"root\",\n"
                        + "    \"schema\" : \"logistimo\",\n"
                        + "    \"type\": \"mysql\"\n"
                        + "  }, {\n"
                        + "    \"id\": 2,\n"
                        + "    \"name\": \"cassandra_logistimo\",\n"
                        + "    \"hosts\": [\"localhost\"],\n"
                        + "    \"schema\": \"logistimo\",\n"
                        + "    \"type\": \"cassandra\"\n"
                        + "  }]\n"
                        + "}")
                .contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().isOk())
        .andExpect(content().string("success"));
  }

  @Test
  public void cUpdateUser() throws Exception {
    mvc.perform(
            MockMvcRequestBuilders.post("/user/update")
                .content(
                    "{\n"
                        + "  \"id\": \"58c787c5857b668c6a12b759\",\n"
                        + "  \"valid\": true,\n"
                        + "  \"user_id\": \"ck_test\",\n"
                        + "  \"server_configs\": [\n"
                        + "    {\n"
                        + "      \"id\": \"1\",\n"
                        + "      \"name\": \"mysql_logistimo\",\n"
                        + "      \"hosts\": [\n"
                        + "        \"localhost\"\n"
                        + "      ],\n"
                        + "      \"port\": null,\n"
                        + "      \"username\": \"root\",\n"
                        + "      \"password\": \"root\",\n"
                        + "      \"schema\": \"logistimo\",\n"
                        + "      \"type\": \"mysql\"\n"
                        + "    },\n"
                        + "    {\n"
                        + "      \"id\": \"2\",\n"
                        + "      \"name\": \"cassandra_logistimo\",\n"
                        + "      \"hosts\": [\n"
                        + "        \"localhost\"\n"
                        + "      ],\n"
                        + "      \"port\": 9042,\n"
                        + "      \"username\": null,\n"
                        + "      \"password\": null,\n"
                        + "      \"schema\": \"logistimo\",\n"
                        + "      \"type\": \"cassandra\"\n"
                        + "    }\n"
                        + "  ]\n"
                        + "}")
                .contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().isOk())
        .andExpect(content().string("User successfully updated"));
  }

  @Test
  public void dReadUser() throws Exception {
    MvcResult result =
        mvc.perform(
                MockMvcRequestBuilders.get("/user/get")
                    .param("userId", "ck_test")
                    .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andReturn();
    JsonObject json =
        new JsonParser().parse(result.getResponse().getContentAsString()).getAsJsonObject();
    Assert.assertEquals("ck_test", json.get("user_id").getAsString());
  }
}
