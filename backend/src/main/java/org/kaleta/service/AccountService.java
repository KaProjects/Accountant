package org.kaleta.service;

import org.kaleta.entity.Account;

import java.util.List;
import java.util.Map;

public interface AccountService {

    Map<String, String> getAccountNamesMap(String year);

    List<Account> list(String year);
}
