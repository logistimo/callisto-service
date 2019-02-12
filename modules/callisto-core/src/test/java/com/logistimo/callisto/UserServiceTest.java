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

import com.logistimo.callisto.model.User;
import com.logistimo.callisto.repository.UserRepository;
import com.logistimo.callisto.service.impl.UserService;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UserServiceTest {

  private UserService userService;
  private UserRepository repository;

  @Before
  public void setup() {
    userService = new UserService();
    repository = mock(UserRepository.class);
    userService.setUserRepository(repository);
  }

  @Test
  public void readUserTest() {
    userService.readUser("logistimo");
    verify(repository, times(1)).findOneByUserId(eq("logistimo"));
  }

  @Test
  public void saveUserTest() {
    User user = new User();
    userService.saveUser(user);
    verify(repository, times(1)).insert(eq(user));
  }

  @Test
  public void updateUserTest() {
    User user = new User();
    user.setUsername("logistimo");
    when(repository.findOneByUserId(eq("logistimo"))).thenReturn(Optional.empty());
    userService.updateUser(user);
    verify(repository, times(1)).findOneByUserId("logistimo");
    verify(repository, never()).save(any());
    reset(repository);
    when(repository.findOneByUserId(eq("logistimo"))).thenReturn(Optional.of(user));
    userService.updateUser(user);
    verify(repository, times(1)).findOneByUserId("logistimo");
    verify(repository, times(1)).save(user);
  }

  @Test
  public void deleteUserTest() {
    when(repository.findOneByUserId("logistimo")).thenReturn(Optional.empty());
    userService.deleteUser("logistimo");
    verify(repository, never()).delete(any());

    reset(repository);
    when(repository.findOneByUserId("logistimo")).thenReturn(Optional.of(new User()));
    userService.deleteUser("logistimo");
    verify(repository, times(1)).delete(any());
  }
}