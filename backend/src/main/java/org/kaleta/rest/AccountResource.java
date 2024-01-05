package org.kaleta.rest;

import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.jboss.resteasy.annotations.jaxrs.PathParam;
import org.kaleta.AccountUtils;
import org.kaleta.Constants;
import org.kaleta.dto.YearAccountDto;
import org.kaleta.dto.YearAccountOverviewDto;
import org.kaleta.entity.Account;
import org.kaleta.entity.Transaction;
import org.kaleta.model.SchemaClass;
import org.kaleta.service.AccountService;
import org.kaleta.service.SchemaService;
import org.kaleta.service.TransactionService;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Path("/account")
public class AccountResource
{
    @Inject
    AccountService accountService;

    @Inject
    SchemaService schemaService;

    @Inject
    TransactionService transactionService;

    @GET
    @Secured
    @SecurityRequirement(name = "AccountantSecurity")
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{year}")
    public Response getAllAccounts(@PathParam String year)
    {
        return Endpoint.process(() -> {
            ParamValidators.validateYear(year);
        }, () -> {
            List<YearAccountDto> accounts = YearAccountDto.from(accountService.list(year));
            Map<String, SchemaClass> schema = schemaService.getSchema(year);
            accounts.forEach(account -> {
                String schemaId = account.getSchemaId();
                SchemaClass clazz = schema.get(schemaId.substring(0,1));
                account.setClazz(clazz.getName());
                SchemaClass.Group group = clazz.getGroup(schemaId.substring(0,2));
                account.setGroup(group.getName());
                account.setAccount(group.getAccount(schemaId).getName());
            });
            return accounts;
        });
    }

    @GET
    @Secured
    @SecurityRequirement(name = "AccountantSecurity")
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{year}/{schemaId}")
    public Response getAccountsOverview(@PathParam String year, @PathParam String schemaId)
    {
        return Endpoint.process(() -> {
            ParamValidators.validateYear(year);
            ParamValidators.validateSchemaAccountId(schemaId);
        }, () -> {
            Constants.AccountType accountType = schemaService.getAccountType(year, schemaId);
            List<Account> accounts = accountService.listBySchema(year, schemaId);
            List<Transaction> transactions = transactionService.getTransactionsMatching(year, schemaId);
            List<YearAccountOverviewDto> dtoList = new ArrayList<>();
            for (Account account : accounts)
            {
                YearAccountOverviewDto dto = new YearAccountOverviewDto();
                dto.setId(account.getFullId());
                dto.setName(account.getName());
                Integer debit = 0;
                Integer credit = 0;
                for (Transaction transaction : transactions)
                {
                    if (transaction.getDebit().equals(account.getFullId()) && transaction.getCredit().equals(Constants.Account.INIT_ACC_ID)) dto.setInitial(transaction.getAmount());
                    if (transaction.getCredit().equals(account.getFullId()) && transaction.getDebit().equals(Constants.Account.INIT_ACC_ID)) dto.setInitial(dto.getInitial() + transaction.getAmount());
                    if (transaction.getDebit().equals(account.getFullId())) debit += transaction.getAmount();
                    if (transaction.getCredit().equals(account.getFullId())) credit += transaction.getAmount();
                }
                if (AccountUtils.isDebit(accountType)) {
                    dto.setTurnover(debit);
                    dto.setBalance(debit - credit);
                } else {
                    dto.setTurnover(credit);
                    dto.setBalance(credit - debit);
                }
                dtoList.add(dto);
            }
            return dtoList;
        });
    }
}
