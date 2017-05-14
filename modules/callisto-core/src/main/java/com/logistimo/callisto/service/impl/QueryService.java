package com.logistimo.callisto.service.impl;

import com.logistimo.callisto.*;
import com.logistimo.callisto.Exception.CallistoException;
import com.logistimo.callisto.model.QueryText;
import com.logistimo.callisto.model.ServerConfig;
import com.logistimo.callisto.repository.QueryRepository;
import com.logistimo.callisto.service.IDataBaseService;
import com.logistimo.callisto.service.IQueryService;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author Mohan Raja
 * @author Chandrakant
 */
@Service
public class QueryService implements IQueryService {

  private static final Logger logger = Logger.getLogger(QueryService.class);

  @Autowired private QueryRepository queryRepository;

  @Autowired private UserService userService;

  @Autowired private DataBaseCollection dataBaseCollection;

  public String saveQuery(QueryText q) {
    String res = "failure";
    try {
      List<QueryText> existing = queryRepository.readQuery(q.getUserId(), q.getQueryId());
      if (existing == null || existing.size() == 0) {
        queryRepository.save(q);
        res = "Query saved successfully";
      } else {
        res = "duplicate query Id, try updating the previous one";
      }
    } catch (Exception e) {
      logger.error("Error while saving query", e);
    }
    return res;
  }

  public String updateQuery(QueryText q) {
    String res = "failure";
    try {
      List<QueryText> queryList = queryRepository.readQuery(q.getUserId(), q.getQueryId());
      if (queryList != null && queryList.size() == 1) {
        q.setId(queryList.get(0).getId());
        queryRepository.save(q);
        res = "Query updated successfully";
      }
    } catch (Exception e) {
      logger.error("Error while updating query", e);
    }
    return res;
  }

  public String deleteQuery(String userId, String queryId) {
    try {
      queryRepository.delete(readQuery(userId, queryId));
    } catch (Exception e) {
      logger.error("Error while deleting query", e);
      return "Error while deleting query";
    }
    return "Query deleted successfully";
  }

  public QueryText readQuery(String userId, String queryId) {
    try {
      List<QueryText> queryList = queryRepository.readQuery(userId, queryId, new PageRequest(0, 1));
      if (queryList != null && queryList.size() > 0) {
        return queryList.get(0);
      }
    } catch (Exception e) {
      logger.error("Error while reading query for userId " + userId + " and queryId " + queryId, e);
    }
    return null;
  }

  public List<String> readQueryIds(String userId) {
    List<String> queryIds = null;
    try {
      List<QueryText> queryList = queryRepository.readQueryIds(userId);
      queryIds = new ArrayList<>(queryList.size());
      for (QueryText queryText : queryList) {
        queryIds.add(queryText.getQueryId());
      }
    } catch (Exception e) {
      logger.error("Error while reading query Ids for userId " + userId, e);
    }
    return queryIds;
  }

  @Override
  public QueryResults readData(
      String userId, String queryId, Map<String, String> filters, Integer size, Integer offset)
      throws CallistoException {
    QueryText queryText = readQuery(userId, queryId);
    List<String> rowHeadings = null;
    if (queryText == null) {
      logger.warn("Query " + queryId + " not found for user " + userId);
      return null;
    }
    ServerConfig serverConfig = userService.readServerConfig(userId, queryText.getServerId());
    List<String> functions = getAllFunctions(queryText.getQuery());
    if (functions.size() > 0) {
      for (String functionText : functions) {
        QueryFunction function = getQueryFunction(functionText, filters);
        QueryResults results =
            readData(userId, function.queryID, filters, function.size, function.offset);
        if (results == null || results.getRows() == null || results.getRows().size()==0) {
          logger.warn("Got no results from function with query id " + function.queryID);
          return null;
        }
        if (FunctionType.CSV.equals(function.type)) {
          String data = getCSV(results, serverConfig.getEscaping());
          queryText.setQuery(queryText.getQuery().replace(functionText, data));
        } else if (FunctionType.ENCLOSE_CSV.equals(function.type)) {
          String data = getEncloseCSV(results, serverConfig.getEscaping());
          queryText.setQuery(queryText.getQuery().replace(functionText, data));
        }
        if (function.fill) {
          rowHeadings = new ArrayList<>(results.getRows().size());
          for (List<String> rows : results.getRows()) {
            if (StringUtils.isNotEmpty(rows.get(0))) rowHeadings.add(rows.get(0));
          }
        }
      }
    }
    QueryResults queryResults =
        executeQuery(serverConfig, queryText.getQuery(), filters, size, offset);
    if (rowHeadings != null) {
      queryResults.setRowHeadings(rowHeadings);
    }
    return queryResults;
  }

  private String getEncloseCSV(QueryResults results, String escaping) {
    return getCSV(results, true, escaping);
  }

  private QueryFunction getQueryFunction(String functionText, Map<String, String> filters)
      throws CallistoException {
    QueryFunction function = new QueryFunction();
    int index = functionText.indexOf(CharacterConstants.OPEN_BRACKET);
    if (index == -1) {
      logger.warn("Invalid function found. " + function);
      throw new CallistoException("Q001", functionText);
    }
    String[] functionParams;
    function.type =
        FunctionType.getFunctionType(
            functionText.substring(CharacterConstants.FN_ENCLOSE.length(), index).trim());
    functionParams =
        functionText
            .substring(index + 1, functionText.indexOf(CharacterConstants.CLOSE_BRACKET))
            .trim()
            .split(CharacterConstants.COMMA);
    if (functionParams.length >= 1) {
      String qId = functionParams[0].trim();
      if(filters.containsKey(qId)){
          function.queryID = filters.get(qId);
      }else{
          function.queryID = qId;
      }
    }
    if (functionParams.length >= 2) {
      String size = functionParams[1].trim();
      if (filters.containsKey(size)) {
        function.size = Integer.parseInt(filters.get(size));
      } else {
        function.size = Integer.parseInt(size);
      }
    }
    if (functionParams.length >= 3) {
      String offset = functionParams[2].trim();
      if (filters.containsKey(offset)) {
        function.offset = Integer.parseInt(filters.get(offset));
      } else {
        function.offset = Integer.parseInt(offset);
      }
    }
    if (functionParams.length >= 4) {
      function.fill = Boolean.parseBoolean(functionParams[3].trim());
    }
    return function;
  }

  private QueryResults executeQuery(
      ServerConfig serverConfig,
      String query,
      Map<String, String> filters,
      Integer size,
      Integer offset) {
    IDataBaseService dataBaseService =
        dataBaseCollection.getDataBaseService(serverConfig.getType());
    return dataBaseService.fetchRows(
        serverConfig, query, filters, Optional.ofNullable(size), Optional.ofNullable(offset));
  }

  private List<String> getAllFunctions(String text) {
    return getAllFunctions(text, 0);
  }

  private List<String> getAllFunctions(String text, int start) {
    List<String> matches = new ArrayList<>();
    if (text.contains(CharacterConstants.FN_ENCLOSE)) {
      int ss = text.indexOf(CharacterConstants.FN_ENCLOSE, start);
      int se =
          text.indexOf(CharacterConstants.FN_ENCLOSE, ss + 1)
              + CharacterConstants.FN_ENCLOSE.length();
      String subStr = text.substring(ss, se);
      matches.add(subStr);
      if (text.indexOf(CharacterConstants.FN_ENCLOSE, se + 1) >= 0) {
        matches.addAll(getAllFunctions(text, se + 1));
      }
    }
    return matches;
  }

  private String getCSV(QueryResults results, String escaping) {
    return getCSV(results, false, escaping);
  }

  private String getCSV(QueryResults results, boolean forceEnclose, String escaping) {
    StringBuilder csv = new StringBuilder();
    for (List<String> strings : results.getRows()) {
      if (!forceEnclose
          && results.getDataTypes() != null
          && CallistoDataType.NUMBER.equals(results.getDataTypes().get(0))) {
        csv.append(strings.get(0)).append(CharacterConstants.COMMA);
      } else {
        String enclosing = CharacterConstants.SINGLE_QUOTE;
        if (strings.get(0).contains(CharacterConstants.SINGLE_QUOTE)
            && StringUtils.isNotEmpty(escaping)) {
          enclosing = escaping;
        }
        csv.append(enclosing)
            .append(strings.get(0))
            .append(enclosing)
            .append(CharacterConstants.COMMA);
      }
    }
    csv.setLength(csv.length() - 1);
    return csv.toString();
  }
}
