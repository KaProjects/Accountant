package org.kaleta.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.kaleta.dto.ViewDto;
import org.kaleta.persistence.entity.Account;
import org.kaleta.persistence.entity.Transaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class ViewService
{
    private final TransactionService transactionService;
    private final AccountService accountService;
    private final SchemaService schemaService;

    @Inject
    public ViewService(TransactionService transactionService, AccountService accountService, SchemaService schemaService)
    {
        this.transactionService = transactionService;
        this.accountService = accountService;
        this.schemaService = schemaService;
    }

    /**
     * @return map of vacation's transactions for specified year
     */
    public Map<String, List<Transaction>> getVacationMap(String year)
    {
        List<Transaction> vacationTransactions = transactionService.getTransactionsMatchingDescription(year,"vac=");
        Map<String, List<Transaction>> map = new HashMap<>();

        for (Transaction transaction : vacationTransactions){
            String key = extractKey(transaction.getDescription(), "vac=");
            if (!map.containsKey(key)){
                map.put(key, new ArrayList<>());
            }
            map.get(key).add(transaction);
        }

        return map;
    }

    /**
     * @return map of view's transactions for specified year
     */
    public Map<String, List<Transaction>> getViewMap(String year)
    {
        List<Transaction> allTransactions = transactionService.getBalanceTransactions(year);
        List<Account> viewAccounts = accountService.listMatchingMetadata(year, "view=");

        Map<String, List<Transaction>> map = new HashMap<>();

        for (Transaction transaction : allTransactions){
            String key = null;
            if (transaction.getDescription().contains("view="))
            {
                key = extractKey(transaction.getDescription(), "view=");
            }
            for (Account account : viewAccounts)
            {
                if (transaction.getDebit().equals(account.getFullId()) || transaction.getCredit().equals(account.getFullId()))
                {
                    key = extractKey(account.getMetadata(), "view=");
                }
            }
            if (key != null){
                if (!map.containsKey(key)){
                    map.put(key, new ArrayList<>());
                }
                map.get(key).add(transaction);
            }
        }
        return map;
    }

    private String extractKey(String description, String key)
    {
        for (String split : description.split(" ")) {
            if (split.startsWith(key)){
                return split.substring(key.length());
            }
        }
        throw new IllegalArgumentException("Couldn't find '" + key + "' in '" + description + "'");
    }

    /**
     * @return the vacations of the specified year, each with its transactions, expenses and the
     * expenses by schema for its chart, sorted
     */
    public ViewDto getVacations(String year)
    {
        Map<String, List<Transaction>> vacations = getVacationMap(year);
        Map<String, String> accountNames = accountService.getAccountNamesMap(year);
        Map<String, String> schemaNames = schemaService.getSchemaNames(year);

        ViewDto dto = new ViewDto();

        for (String key : vacations.keySet()){
            ViewDto.View view = new ViewDto.View();
            view.setName(key);
            view.setExpenses(String.valueOf(sumExpensesOf(vacations.get(key))));

            for (Transaction transaction : vacations.get(key)){
                ViewDto.View.Transaction vacTr = new ViewDto.View.Transaction();
                vacTr.setDate(transaction.getDate());
                vacTr.setDescription(transaction.getDescription());
                vacTr.setDescription(vacTr.getDescription().replace("vac="+key, ""));
                vacTr.setDebit(accountNames.get(transaction.getDebit()));
                vacTr.setCredit(accountNames.get(transaction.getCredit()));
                vacTr.setAmount(constructAmount(transaction));
                view.getTransactions().add(vacTr);

                setChartData(schemaNames, view, transaction);
            }
            view.getTransactions().sort(ViewDto::compare);
            dto.getViews().add(view);
        }
        dto.getViews().sort(ViewDto::compare);
        return dto;
    }

    /**
     * @return the views of the specified year, each with its transactions, expenses and the
     * expenses by schema for its chart, sorted
     */
    public ViewDto getViews(String year)
    {
        ViewDto dto = new ViewDto();

        Map<String, List<Transaction>> views = getViewMap(year);
        Map<String, String> accountNames = accountService.getAccountNamesMap(year);
        Map<String, String> schemaNames = schemaService.getSchemaNames(year);

        for (String key : views.keySet())
        {
            ViewDto.View view = new ViewDto.View();
            view.setName(key);
            view.setExpenses(String.valueOf(sumExpensesOf(views.get(key))));

            for (Transaction transaction : views.get(key))
            {
                ViewDto.View.Transaction trDto = new ViewDto.View.Transaction();
                trDto.setDate(transaction.getDate());
                trDto.setDescription(transaction.getDescription().replace("view="+key, ""));
                trDto.setDebit(accountNames.get(transaction.getDebit()));
                trDto.setCredit(accountNames.get(transaction.getCredit()));
                trDto.setAmount(constructAmount(transaction));
                view.getTransactions().add(trDto);

                setChartData(schemaNames, view, transaction);
            }
            view.getTransactions().sort(ViewDto::compare);
            dto.getViews().add(view);
        }
        dto.getViews().sort(ViewDto::compare);
        return dto;
    }

    private static void setChartData(Map<String, String> schemaNames, ViewDto.View view, Transaction transaction)
    {
        if (transaction.getDebit().startsWith("5")){
            if (!transaction.getCredit().startsWith("5"))
            { // 5 & !5
                String schemaName = schemaNames.get(transaction.getDebit().substring(0,3));
                Integer value = transaction.getAmount();
                view.addChartData(schemaName, value);
            } else
            { // 5 & 5
                Integer value = transaction.getAmount();
                String creditSchemaName = schemaNames.get(transaction.getCredit().substring(0,3));
                view.addChartData(creditSchemaName, -value);
                String debitSchemaName = schemaNames.get(transaction.getDebit().substring(0,3));
                view.addChartData(debitSchemaName, value);
            }
        } else { // !5 & ?
            String schemaName = schemaNames.get(transaction.getCredit().substring(0,3));
            Integer value = transaction.getAmount();
            view.addChartData(schemaName, -value);
        }
    }

    private String constructAmount(Transaction transaction)
    {
        String amountPrefix = transaction.getDebit().startsWith("5")
                ? transaction.getCredit().startsWith("5") ? "~" : ""
                : "-";
        return amountPrefix + transaction.getAmount();
    }

    private Integer sumExpensesOf(List<Transaction> transactions)
    {
        Integer sum = 0;
        for (Transaction transaction : transactions) {
            if (transaction.getDebit().startsWith("5"))
            {
                if (!transaction.getCredit().startsWith("5"))
                { // 5 & !5
                    sum += transaction.getAmount();
                }
                // 5 & 5 - no action - it's just change of expense
            } else { // !5 & ?
                sum -= transaction.getAmount();
            }
        }
        return sum;
    }
}
