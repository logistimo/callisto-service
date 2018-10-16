/*
 * Copyright © 2018 Logistimo.
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

package com.logistimo.callisto;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class QueryResultsTest {

  @Test
  public void fillResultsTest() {
    QueryResults results = new QueryResults();
    results.setHeadings(Arrays.asList("a", "b", "c", "d"));
    results.setDataTypes(Arrays.asList(
        CallistoDataType.NUMBER,
        CallistoDataType.STRING,
        CallistoDataType.NUMBER,
        CallistoDataType.STRING));
    results.addRow(Arrays.asList("10", "r1", "344", "m2"));
    results.addRow(Arrays.asList("20", "r3", "545", "m4"));
    results.addRow(Arrays.asList("24", "r3", "343", "m5"));
    results.addRow(Arrays.asList("9", "r4", "934", "m6"));
    results.addRow(Arrays.asList("32", "r6", "134", "m8"));
    List<String> rowHeadings = Arrays.asList("r1", "r2", "r3", "r4", "r5", "r6", "r7");
    assertEquals(5, results.getRows().size());
    final int index = 1;
    results.fillResults(rowHeadings, index);
    assertEquals(8, results.getRows().size());
    Set<String> set = results.getRows().stream().map(l -> l.get(index)).collect(Collectors.toSet());
    assertTrue(set.contains("r1"));
    assertTrue(set.contains("r2"));
    assertTrue(set.contains("r3"));
    assertTrue(set.contains("r4"));
    assertTrue(set.contains("r5"));
    assertTrue(set.contains("r6"));
    assertTrue(set.contains("r7"));
  }
}