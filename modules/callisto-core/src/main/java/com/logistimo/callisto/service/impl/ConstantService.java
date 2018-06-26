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

package com.logistimo.callisto.service.impl;

import com.logistimo.callisto.model.ConstantText;
import com.logistimo.callisto.repository.ConstantRepository;
import com.logistimo.callisto.service.IConstantService;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/** Created by chandrakant on 19/05/17. */
@Service
public class ConstantService implements IConstantService {

  private static final Logger logger = Logger.getLogger(UserService.class);

  @Resource private ConstantRepository repository;

  @Override
  public ConstantText readConstant(String userId, String constId) {
    ConstantText constant = null;
    try {
      List<ConstantText> constants = repository.readConstant(userId, constId);
      if (constants != null && !constants.isEmpty()) {
        constant = constants.get(0);
      } else {
        logger.warn("No constant found with id: " + constId);
      }
    } catch (Exception e) {
      logger.warn("Error in getting constant with id: " + constId);
    }
    return constant;
  }

  @Override
  public String saveConstant(ConstantText constant) {
    String res = "failure";
    try {
      repository.insert(constant);
      res = "success";
    } catch (Exception e) {
      logger.error("Error while saving constant", e);
    }
    return res;
  }
}
