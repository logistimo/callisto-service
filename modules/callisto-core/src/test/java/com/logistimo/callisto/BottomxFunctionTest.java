package com.logistimo.callisto;

import com.logistimo.callisto.exception.CallistoException;
import com.logistimo.callisto.function.BottomxFunction;
import com.logistimo.callisto.function.FunctionParam;
import com.logistimo.callisto.model.QueryRequestModel;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;

public class BottomxFunctionTest {

  private ICallistoFunction bottomx;

  @Before
  public void setUp() {
    bottomx = new BottomxFunction();
  }

  @Test
  public void BottomxTest() throws CallistoException {
    String arg = "$$bottomx($map,TOKEN_SIZE,TOKEN_OFFSET)$$";
    QueryRequestModel model = new QueryRequestModel();
    model.filters = new HashMap<>();
    model.filters.put("TOKEN_SIZE", "3");
    model.filters.put("TOKEN_OFFSET", "2");
    FunctionParam fParam= new FunctionParam();
    fParam.setQueryRequestModel(model);
    fParam.function = arg;
    fParam.setResultHeadings(Arrays.asList("abc", "def", "map", "seriously?"));
    fParam.setResultRow(Arrays.asList("IDK", "None",
        "{\"key1\":23,\"key2\":10,\"key3\":8,\"key4\":2,\"key5\":25,\"key6\":12,\"key7\":14,\"key8\":17,\"key9\":20}",
        "seriously?"));
    String nMap = bottomx.getResult(fParam);
    assertEquals("{\"key2\":10,\"key6\":12,\"key7\":14}", nMap);
    //-------------------------------------//
    fParam.function = "$$bottomx($map,TOKEN_SIZE,TOKEN_OFFSET)$$";
    model.filters.put("TOKEN_SIZE", "3");
    model.filters.put("TOKEN_OFFSET", "0");
    nMap = bottomx.getResult(fParam);
    assertEquals("{\"key4\":2,\"key3\":8,\"key2\":10}",nMap);
    //-------------------------------------//
    fParam.function = "$$bottomx($map,TOKEN_SIZE,TOKEN_OFFSET)$$";
    model.filters.put("TOKEN_SIZE", "2");
    model.filters.put("TOKEN_OFFSET", "10");
    nMap = bottomx.getResult(fParam);
    assertEquals("{}",nMap);
    //-------------------------------------//
    fParam.setResultRow(Arrays.asList("IDK", "None",
        "{\"key6\":12,\"key7\":10,\"key3\":10,\"key9\":10,\"key5\":8,\"key2\":10,\"key1\":10,\"key8\":10,\"key4\":10}",
        "seriously?"));
    model.filters.put("TOKEN_SIZE", "3");
    model.filters.put("TOKEN_OFFSET", "0");
    nMap = bottomx.getResult(fParam);
    assertEquals("{\"key5\":8,\"key9\":10,\"key8\":10}", nMap);

  }

}