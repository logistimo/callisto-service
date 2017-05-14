package com.logistimo.callisto.controller;

import com.google.gson.Gson;
import com.logistimo.callisto.QueryResults;
import com.logistimo.callisto.model.QueryRequestModel;
import com.logistimo.callisto.model.QueryText;
import com.logistimo.callisto.service.IQueryService;

import org.apache.log4j.Logger;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author Mohan Raja
 * @author Chandrakant
 */
@RestController
@RequestMapping("/query")
public class QueryController {

  private static final Logger logger = Logger.getLogger(QueryController.class);

  @Resource IQueryService queryService;

  @RequestMapping(value = "/save", method = RequestMethod.PUT)
  public String saveQuery(@RequestBody QueryText queryText) {
    return queryService.saveQuery(queryText);
  }

  @RequestMapping(value = "/udpate", method = RequestMethod.POST)
  public String updateQuery(@RequestBody QueryText queryText) {
    return queryService.updateQuery(queryText);
  }

  @RequestMapping(value = "/get", method = RequestMethod.GET)
  public QueryText getQuery(@RequestParam(defaultValue = "logistimo") String userId, @RequestParam String queryId) {
    QueryText q = queryService.readQuery(userId, queryId);
    return q;
  }

  @RequestMapping(value = "/getdata", method = RequestMethod.POST)
  public String getQueryData(@RequestBody QueryRequestModel model) throws Exception {
    QueryResults q =
        queryService.readData(model.userId, model.queryId, model.filters, model.size, model.offset);
    if(q != null) {
      q.setDataTypes(null);
    }
    return new Gson().toJson(q);
  }

  @RequestMapping(value = "/getids", method = RequestMethod.GET)
  public String getAllQueryIds(@RequestParam(defaultValue = "logistimo") String userId) {
    List<String> queryIds = queryService.readQueryIds(userId);
    return new Gson().toJson(queryIds);
  }

  @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
  public String deleteQuery(@RequestParam(defaultValue = "logistimo") String userId, @RequestParam String queryId) {
    return queryService.deleteQuery(userId, queryId);
  }
}
