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

package com.logistimo.callisto.service;

import com.google.common.collect.TreeBasedTable;
import com.logistimo.callisto.QueryResults;
import com.logistimo.callisto.model.ServerConfig;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/** @author chandrakant */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = CassandraService.class)
@AutoConfigureMockMvc
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CassandraServiceTest {

  @Autowired private MockMvc mvc;

  @Autowired private IDataBaseService cassandraService;

  @Test
  public void fetchRows() throws Exception {
    ServerConfig serverConfig = new ServerConfig();
    serverConfig.setId("mock1");
    serverConfig.setName("logistimo");
    serverConfig.setHosts(new ArrayList<>(Arrays.asList("localhost")));
    serverConfig.setPort(9042);
    serverConfig.setSchema("logistimo");

    String query = "SELECT drsn FROM DID_MONTH WHERE DID = 1 AND T = '2016-12'";
    Optional<Integer> size = Optional.of(300);
    //Optional<Integer> size = Optional.empty();
    Optional<Integer> offset = Optional.of(0);
    QueryResults queryResults = cassandraService.fetchRows(serverConfig, query, null, size, offset);
    Assert.assertEquals(size.get().longValue(), queryResults.getRows().size());
    //Assert.assertEquals(queryResults.getRows().size(),queryResults.getRows().size());
  }
  /*
  @Test
  public void dateTest() throws ParseException {
      Calendar c = new GregorianCalendar();
      DateFormat sdf = new SimpleDateFormat("yyyy-ww");
      c.setTime( sdf.parse("2015-01"));
      DateTimeParser[] parsers = {
              DateTimeFormat.forPattern( "yyyy-MM-dd" ).getParser()
               };
      DateTimeFormatter formatter = new DateTimeFormatterBuilder().append( null, parsers ).toFormatter();

      DateTime date1 = formatter.parseDateTime("2014-12-29");
      date1.centuryOfEra();
  }

  @Test
  public void constructTableTest() throws Exception {
    String json =
        "{'headings':['ktag','t','tc'],'rows':[['115','2017-02-17','3'],['115','2017-02-22','1'],['115','2017-03-02','0'],"
            + "['115','2017-02-20','2'],['115','2017-03-08','-1'],['115','2017-02-16','4'],['115','2017-02-14','6'],"
            + "['115','2017-02-15','5'],['115','2017-02-13','7'],['116','2017-03-02','-1'],"
            + "                ['116','2017-02-20','0'],['116','2017-02-13','1'],['116','2017-02-17','2'],['116','2017-02-16','3'],"
            + "                ['116','2017-02-15','4'],['116','2017-02-22','5'],['116','2017-03-08','6'],['116','2017-02-14','7'],"
            + "['117','2017-02-13','-1'],['117','2017-02-22','0'],['117','2017-02-17','1'],['117','2017-03-08','2'],['117','2017-02-16','3'],"
            + "['117','2017-02-20','4'],['117','2017-03-02','5'],['117','2017-02-14','6']]}";
    String output = buildReportTableData(json);
  }

  private static final String ZERO = "0";
  private static final String ROWS = "rows";
  private static final String HEADINGS = "headings";
  private static final String LONG = "Long";
  private static final String FLOAT = "Float";
  private static final String TABLE = "table";

  public String buildReportTableData(String json) throws JSONException {
    JSONObject jsonObject = new JSONObject(json);
    JSONArray headersJson = jsonObject.getJSONArray(HEADINGS);
    if (headersJson.length() < 3) {
      //xLogger.warn("Insufficient data found. Expect to have atleast 3 columns");
      return null;
    }
    JSONArray rowsJson = jsonObject.getJSONArray(ROWS);
    TreeBasedTable<String, String, List<String>> treeBasedTable = TreeBasedTable.create();
    int dataSize = 0;
    for (int i = 0; i < rowsJson.length(); i++) {
      dataSize = rowsJson.getJSONArray(i).length() - 2;
      List<String> data = new ArrayList<>(dataSize);
      for (int j = 0; j < dataSize; j++) {
        data.add(rowsJson.getJSONArray(i).getString(j + 2));
      }
      treeBasedTable.put(
          rowsJson.getJSONArray(i).getString(0), rowsJson.getJSONArray(i).getString(1), data);
    }
    List<String> headers = new ArrayList<>(treeBasedTable.columnKeySet().size() + 1);
    headers.add(headersJson.getString(0));
    headers.addAll(treeBasedTable.columnKeySet());
    JSONObject output = new JSONObject();
    Map<String, List<List<String>>> tableMap = new HashMap<>();
    final int finalDataSize = dataSize;
    treeBasedTable
        .rowKeySet()
        .forEach(
            t -> {
              List<List<String>> list = new ArrayList<>();
              for (String s : treeBasedTable.columnKeySet()) {
                if (treeBasedTable.get(t, s) != null) {
                  list.add(treeBasedTable.get(t, s));
                } else {
                  String[] arr = new String[finalDataSize];
                  List<String> emptyList = new ArrayList<>(Arrays.asList(arr));
                  Collections.fill(emptyList, ZERO);
                  list.add(emptyList);
                }
              }
              tableMap.put(t, list);
            });
    output.put(HEADINGS, headers);
    output.put(TABLE, tableMap);
    return output.toString();
  }
  */
}
