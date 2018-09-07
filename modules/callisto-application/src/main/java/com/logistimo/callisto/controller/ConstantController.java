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

package com.logistimo.callisto.controller;

import com.logistimo.callisto.model.ConstantText;
import com.logistimo.callisto.service.IConstantService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Created by chandrakant on 24/05/17. */
@RestController
@RequestMapping("/constant")
public class ConstantController {

  @Autowired
  IConstantService constantService;

  @RequestMapping(value = "/save", method = RequestMethod.PUT)
  public String saveConstant(@RequestBody ConstantText constant) {
    return constantService.saveConstant(constant);
  }

  @RequestMapping(value = "/get", method = RequestMethod.GET)
  public ConstantText getConstant(@RequestParam(defaultValue = "logistimo") String userId, @RequestParam String constId) {
    return constantService.readConstant(userId, constId);
  }
}
