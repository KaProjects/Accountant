package org.kaleta.service;

import org.kaleta.entity.Transaction;

import java.util.List;
import java.util.Map;

public interface ViewService {

    Map<String, List<Transaction>> getVacationMap(String year);
}
