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

package com.logistimo.callisto;

import com.logistimo.callisto.exception.CallistoException;
import com.logistimo.callisto.function.FunctionUtil;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/** Created by chandrakant on 26/05/17. */
@RunWith(MockitoJUnitRunner.class)
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
    String str = "$$somefunction(abc,dwd,$dwd,'')$$ as some_column";
    Map<String,String> columns = FunctionUtil.parseColumnText(str);
    assertEquals(1,columns.size());
    List<Map.Entry> entries = new ArrayList<>(columns.entrySet());
    assertEquals("some_column",entries.get(0).getKey());
    assertEquals("$$somefunction(abc,dwd,$dwd,'')$$",entries.get(0).getValue());

    str = "$$somefunction(abc,dwd,$dwd,'')$$ as some_column, $$serious_function(good, bad, ugly)$$ as the_good_bad_and_ugly, $$some_other_function(param1, "
          + "param2, empty)$$";
    columns = FunctionUtil.parseColumnText(str);
    assertEquals(3,columns.size());
    entries = new ArrayList<>(columns.entrySet());
    assertEquals("some_column",entries.get(0).getKey());
    assertEquals("$$somefunction(abc,dwd,$dwd,'')$$",entries.get(0).getValue());
    assertEquals("the_good_bad_and_ugly",entries.get(1).getKey());
    assertEquals("$$serious_function(good, bad, ugly)$$",entries.get(1).getValue());
    assertEquals("$$some_other_function(param1, param2, empty)$$",entries.get(2).getKey());
    assertEquals("$$some_other_function(param1, param2, empty)$$", entries.get(2).getValue());

    str = " $$topx($abc,3)$$ as abc , $$math(($def*100)/$time)$$, $$datetime($time,'YYYY-MM'  , YYYY-MM-dd)$$ as newtime ";
    columns = FunctionUtil.parseColumnText(str);
    assertEquals(3,columns.size());
    entries = new ArrayList<>(columns.entrySet());
    assertEquals("abc",entries.get(0).getKey());
    assertEquals("$$topx($abc,3)$$",entries.get(0).getValue());
    assertEquals("$$math(($def*100)/$time)$$",entries.get(1).getKey());
    assertEquals("$$math(($def*100)/$time)$$",entries.get(1).getValue());
    assertEquals("newtime",entries.get(2).getKey());
    assertEquals("$$datetime($time,'YYYY-MM'  , YYYY-MM-dd)$$", entries.get(2).getValue());
  }

  @Test
  public void extractColumnsCsvTest() {
    Map<String, String> map = new HashMap<>();
    map.put("some_column", "$$somefunction($abc,user,$pwd,'')$$");
    map.put("abc", "$$math($v1/$v5-$somevar*$othervar)$$");
    String[] result = FunctionUtil.extractColumnsCsv(map).split(CharacterConstants.COMMA);
    Arrays.sort(result);
    assertArrayEquals(new String[]{"abc", "othervar", "pwd", "somevar", "v1", "v5"}, result);

    map.put("another_column", "$$somefunction($abc,#user,$pwd,'')$$ $alone_var $$someotherfun"
                              + "(nothing $much, here!)$$");
    map.put("abc", "$$math($outvar/$$math($inside_var,$cagedvar)$$-$somevar*$othervar)$$ "
                   + "$$funfun($funvar, $boringvar)$$");
    map.put("non-abc", "$$nofun($kool)$$ | $notkool");
    result = FunctionUtil.extractColumnsCsv(map).split(CharacterConstants.COMMA);
    Arrays.sort(result);
    assertArrayEquals(new String[]{"abc", "alone_var", "boringvar", "cagedvar", "funvar",
        "inside_var", "kool", "much", "notkool", "othervar", "outvar", "pwd", "somevar"}, result);
  }

  @Test
  public void replaceVariablesTest() throws CallistoException {
    String expr = "$outvar/$$math($inside_var*$cagedvar)$$-$somevar | $othervar $inside_var";
    List<String> headings = Arrays.asList("outvar", "inside_var", "cagedvar", "somevar", "othervar");
    List<String> row = Arrays.asList("out-data", "4824", "23842424353423244224", "some-data",
        "other data");
    String result = FunctionUtil.replaceVariables(expr, headings, row);
    assertEquals("out-data/$$math(4824*23842424353423244224)$$-some-data | other data 4824", result);
  }

  @Test
  public void isFunctionTest() {
    String function = "$$somfun()$$"; // not a function
    assertFalse(FunctionUtil.isFunction(function, false));
    function = "$$somfun(parameter)$$";
    assert(FunctionUtil.isFunction(function, false));
    function = "somfun(parameter)";
    assert(FunctionUtil.isFunction(function, true));
    function = "somfun(parameter)";
    assertFalse(FunctionUtil.isFunction(function, false));
    function = "$$somfun(parameter)$$";
    assert(FunctionUtil.isFunction(function, true));
    function = "$$somfun(parameter,efjnwef,'hdwe(ewe')$$";
    assert(FunctionUtil.isFunction(function, false));
    function = "$$(parameter,efjnwef,'hdwe(ewe')$$";
    assertFalse(FunctionUtil.isFunction(function, false));
    function = "$somefunction(parameter,efjnwef,'hdwe(ewe')$";
    assertFalse(FunctionUtil.isFunction(function, false));
    function = "$$ (parameter,efjnwef,'hdwe)ewe')$$";
    assertFalse(FunctionUtil.isFunction(function, false));
    function = "$$csv(parameter,efjnwef,'hdwe)ewe')$$";
    assert(FunctionUtil.isFunction(function, false));
    function = "$$csv)efwe(parameter,efjnwef,'hdwe)ewe')$$";
    assertFalse(FunctionUtil.isFunction(function, false));
  }

  @Test
  public void getFunctionTypeTest() {
    String function = "$$somefun(param)$$";
    assertEquals("somefun", FunctionUtil.getFunctionType(function));
    function = "$$ somefun (param)$$";
    assertEquals("somefun", FunctionUtil.getFunctionType(function));
  }

  @Test
  public void getAllFunctionsTest() {
    String str = "$abc + $$link(100+100)$$ + $$link(abc)$$";
    List<String> result = FunctionUtil.getAllFunctions(str);
    assertArrayEquals(new String[]{"$$link(100+100)$$","$$link(abc)$$"}, result.toArray());
    str = "$abc $$somefun($abc, efjwe, 'ejfhwef')$$ + $$link(abc) $$ $$randomfun(random_param)$$";
    result = FunctionUtil.getAllFunctions(str);
    assertArrayEquals(new String[]{"$$somefun($abc, efjwe, 'ejfhwef')$$","$$link(abc) $$", "$$randomfun(random_param)$$"}, result
        .toArray());
  }
}
