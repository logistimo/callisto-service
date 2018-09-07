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

import com.logistimo.callisto.ICallistoFunction;
import com.logistimo.callisto.exception.CallistoException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by chandrakant on 26/05/17.
 */
@RunWith(SpringJUnit4ClassRunner.class)
public class DateTimeFunctionTest {

  ICallistoFunction dateTime;

  @Before
  public void setup(){
    dateTime = new DateTimeFunction();
  }

  @Test
  public void getParamTest() throws CallistoException {
    String arg = "datetime(2017-10,YYYY-MM,YYYY-MM-dd)";
    List args = DateTimeFunction.getParameters(arg);
    assertEquals("2017-10", args.get(0));
    FunctionParam param = new FunctionParam();
    param.function=(arg);
    assertEquals("2017-10-01", dateTime.getResult(param));

    arg = "datetime(2017-10,'YYYY-MM','YYYY:MM')";
    param = new FunctionParam();
    param.function=(arg);
    assertEquals("2017:10", dateTime.getResult(param));

    arg = "datetime('2017-10-01', YYYY-MM-dd, 'YYYY-MM, dd')";
    param = new FunctionParam();
    param.function=(arg);
    assertEquals("2017-10, 01", dateTime.getResult(param));

    arg = "datetime(2017-10, 'YYYY-,MM' , ' YYYY-MM DD')";
    args = DateTimeFunction.getParameters(arg);
    assertEquals("2017-10", args.get(0));
    assertEquals("YYYY-,MM", args.get(1));
    assertEquals(" YYYY-MM DD", args.get(2));

    arg = "datetime(2017-10  ,YYYY-MM,'YYYY, MM, DD')";
    args = DateTimeFunction.getParameters(arg);
    assertEquals("2017-10", args.get(0));
    assertEquals("YYYY-MM", args.get(1));
    assertEquals("YYYY, MM, DD", args.get(2));

    arg = "datetime(2017-10, 'YYYY-MM', 'yyy,,y,:mm')";
    args = DateTimeFunction.getParameters(arg);
    assertEquals("2017-10", args.get(0));
    assertEquals("YYYY-MM", args.get(1));
    assertEquals("yyy,,y,:mm", args.get(2));
  }
}
