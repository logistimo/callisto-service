/*
 * Copyright © 2017 Logistimo.
 *
 * This file is part of Logistimo.
 *
 * Logistimo software is a mobile & web platform for supply chain management and remote temperature monitoring in
 * low-resource settings, made available under the terms of the GNU Affero General Public License (AGPL).
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 *
 * You can be released from the requirements of the license by purchasing a commercial license. To know more about
 * the commercial license, please contact us at opensource@logistimo.com
 */

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
