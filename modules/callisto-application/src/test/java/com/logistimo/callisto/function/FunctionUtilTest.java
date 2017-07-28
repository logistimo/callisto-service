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

import com.logistimo.callisto.exception.CallistoException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/** Created by chandrakant on 26/05/17. */
@RunWith(SpringRunner.class)
@ContextConfiguration
public class FunctionUtilTest {

  @Test
  public void getAllFunctionsVariablesTest() {
    String str = "$abc + $$link(100+100)$$ + $$link(abc)$$";
    str = str.replaceAll("\\s+", "");
    List fnVars = FunctionUtil.getAllFunctionsAndVariables(str);
    assertEquals(3, fnVars.size());
    assertEquals("$abc", fnVars.get(0));
    assertEquals("$$link(100+100)$$", fnVars.get(1));
    assertEquals("$$link(abc)$$", fnVars.get(2));

    str = "$abc + $$link(100+100)$$ + $def + $$link(abc)$$";
    str = str.replaceAll("\\s+", "");
    fnVars = FunctionUtil.getAllFunctionsAndVariables(str);
    assertEquals(4, fnVars.size());
    assertEquals("$abc", fnVars.get(0));
    assertEquals("$$link(100+100)$$", fnVars.get(1));
    assertEquals("$def", fnVars.get(2));
    assertEquals("$$link(abc)$$", fnVars.get(3));

    str = "$abc + $$link(100+100)$$ + $def + $$link(abc)$$";
    str = str.replaceAll("\\s+", "");
    fnVars = FunctionUtil.getAllFunctionsAndVariables(str);
    assertEquals(4, fnVars.size());
    assertEquals("$abc", fnVars.get(0));
    assertEquals("$$link(100+100)$$", fnVars.get(1));
    assertEquals("$def", fnVars.get(2));
    assertEquals("$$link(abc)$$", fnVars.get(3));

    str = " $$link(idk)$$$fidget + $def$$link(abc)$$ ";
    str = str.replaceAll("\\s+", "");
    fnVars = FunctionUtil.getAllFunctionsAndVariables(str);
    assertEquals(4, fnVars.size());
    assertEquals("$$link(idk)$$", fnVars.get(0));
    assertEquals("$fidget", fnVars.get(1));
    assertEquals("$def", fnVars.get(2));
    assertEquals("$$link(abc)$$", fnVars.get(3));
  }

  @Test
  public void parseColumnTextTest() throws CallistoException {
    String str = " $$topx($abc,3)$$ as abc , $$math(($def*100)/$time)$$, $$datetime($time,'YYYY-MM'  , YYYY-MM-dd)$$ as newtime ";
    Map<String,String> columns = FunctionUtil.parseColumnText(str);
    assertEquals(3,columns.size());
    List<Map.Entry> entries = new ArrayList<>(columns.entrySet());
    assertEquals("abc",entries.get(0).getKey());
    assertEquals("$$topx($abc,3)$$",entries.get(0).getValue());
    assertEquals("$$math(($def*100)/$time)$$",entries.get(1).getKey());
    assertEquals("$$math(($def*100)/$time)$$",entries.get(1).getValue());
    assertEquals("newtime",entries.get(2).getKey());
    assertEquals("$$datetime($time,'YYYY-MM'  , YYYY-MM-dd)$$", entries.get(2).getValue());
  }
}
