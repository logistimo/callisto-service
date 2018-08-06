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

package com.logistimo.callisto.reports;

import com.logistimo.callisto.reports.core.ReportRequestHelper;
import com.logistimo.callisto.service.IQueryService;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.HashSet;

import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
public class ReportRequestHelperTest {

  private IQueryService queryService;
  private ReportRequestHelper reportRequestHelper;

  @Before
  public void setUp() {
    reportRequestHelper = new ReportRequestHelper();
    queryService = Mockito.mock(IQueryService.class);
    reportRequestHelper.setQueryService(queryService);
    when(queryService.getAllQueryIds("logistimo")).thenReturn(Arrays.asList("DID", "DID_KID",
        "DID_MID", "DID_KID_MID", "DID_KTAG_MID", "DID_KTAG_MID_CN", "DID_KTAG_MID_CN_ST",
        "DID_KTAG_MID_CN_ST_TALUK"));
  }


  @Test
  public void deriveQueryIdFromFilters() {
    String queryId = reportRequestHelper.deriveQueryIdFromFilters("logistimo", new HashSet<>
        (Arrays.asList("did")));
    Assert.assertEquals("DID", queryId);
    queryId = reportRequestHelper.deriveQueryIdFromFilters("logistimo", new HashSet<>
        (Arrays.asList("did","mid")));
    Assert.assertEquals("DID_MID", queryId);

    queryId = reportRequestHelper.deriveQueryIdFromFilters("logistimo", new HashSet<>
        (Arrays.asList("mid","did")));
    Assert.assertEquals("DID_MID", queryId);

    queryId = reportRequestHelper.deriveQueryIdFromFilters("logistimo", new HashSet<>
        (Arrays.asList("mid","page","did","ktag","cn","size")));
    Assert.assertEquals("DID_KTAG_MID_CN", queryId);

    queryId = reportRequestHelper.deriveQueryIdFromFilters("logistimo", new HashSet<>
        (Arrays.asList("mid","page","did","ktag","cn","size","st")));
    Assert.assertEquals("DID_KTAG_MID_CN_ST", queryId);
  }
}