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

package com.logistimo.callisto.function;

import com.logistimo.callisto.CallistoApplication;
import com.logistimo.callisto.exception.CallistoException;
import com.logistimo.callisto.QueryResults;
import com.logistimo.callisto.ResultManager;
import com.logistimo.callisto.model.QueryRequestModel;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;

/** Created by chandrakant on 26/05/17. */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = CallistoApplication.class)
public class ResultManagerTest {

  @Autowired ResultManager resultManager;

  @Test
  public void getDesiredResultTest() throws CallistoException {
    QueryRequestModel request = new QueryRequestModel();
    QueryResults results = new QueryResults();
    List<String> headings = Arrays.asList("abc", "def", "pqr");
    results.setHeadings(headings);
    results.addRow(Arrays.asList("result of abc", "125", "250"));
    results.addRow(Arrays.asList("another result of abc", "49", "123"));
    results.addRow(Arrays.asList("seriously?", "24", "340"));
    LinkedHashMap<String, String> desiredResultFormat = new LinkedHashMap<>();
    desiredResultFormat.put("Display format of abc", "$abc $def $$math(100/(2.5*2))$$");
    desiredResultFormat.put("Modified 2nd column", "$$math($pqr/$def)$$");
    QueryResults newResult = resultManager.getDesiredResult(request, results, desiredResultFormat);
    assertEquals("result of abc 125 20", newResult.getRows().get(0).get(0));
    assertEquals("2", newResult.getRows().get(0).get(1));
  }

}
