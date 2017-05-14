package com.logistimo.callisto.controller;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

/**
 * @author Mohan Raja
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class QueryControllerTest {

  @Autowired
  private MockMvc mvc;

  @Test
  public void aSaveQuery() throws Exception {
    mvc.perform(MockMvcRequestBuilders.put("/query/save").content("{\n"
        + "  \"user_id\": \"mohan\",\n"
        + "  \"query_id\": \"test_kiosk_tags\",\n"
        + "  \"query\": \"SELECT KIOSKID FROM KIOSK_TAGS WHERE ID IN (SELECT ID FROM TAG WHERE TYPE = 0 AND NAME = 'TOKEN_TAG'\",\n"
        + "  \"server_id\": \"1\"\n"
        + "}").contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().isOk()).andExpect(content().string("Query saved successfully"));
  }

  @Test
  public void bDeleteQuery() throws Exception {
    mvc.perform(MockMvcRequestBuilders.delete("/query/delete")
        .param("userId", "mohan")
        .param("queryId", "test_kiosk_tags").contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().isOk()).andExpect(content().string("Query deleted successfully"));
  }

}
