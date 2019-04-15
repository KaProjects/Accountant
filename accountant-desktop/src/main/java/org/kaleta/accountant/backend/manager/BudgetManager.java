package org.kaleta.accountant.backend.manager;

import org.kaleta.accountant.Initializer;
import org.kaleta.accountant.backend.model.BudgetModel;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.SchemaFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class BudgetManager  implements Manager<BudgetModel>{
    private final String schemaUri;
    private final String schemaFileUri;
    private final String year;

    public BudgetManager(String year) {
        this.year = year;
        schemaUri = "/schema/budget.xsd";
        schemaFileUri = Initializer.getDataSource() + year + File.separator + "budget.xml";
    }

    @Override
    public void create() throws ManagerException {
        BudgetModel newModel = new BudgetModel();
        newModel.setYear(year);

        List<BudgetModel.Row.Month> emptyMonths = new ArrayList<>();
        for (int m=1;m<=12;m++){
            BudgetModel.Row.Month emptyMonth = new BudgetModel.Row.Month();
            emptyMonth.setNumber(String.valueOf(m));
            emptyMonth.setValue("0");
            emptyMonths.add(emptyMonth);
        }

        newModel.getRow().add(new BudgetModel.Row("0", "income", "Net Salary", emptyMonths));
        newModel.getRow().add(new BudgetModel.Row("1", "income", "Side Job", emptyMonths));

        newModel.getRow().add(new BudgetModel.Row("2", "mandatory-expense", "Rent", emptyMonths));
        newModel.getRow().add(new BudgetModel.Row("3", "mandatory-expense", "Splatky AB", emptyMonths));
        newModel.getRow().add(new BudgetModel.Row("4", "mandatory-expense", "Splatky CSOB", emptyMonths));
        newModel.getRow().add(new BudgetModel.Row("5", "mandatory-expense", "poistka", emptyMonths));
        newModel.getRow().add(new BudgetModel.Row("6", "mandatory-expense", "splatky SPF", emptyMonths));

        newModel.getRow().add(new BudgetModel.Row("7", "variable-expense", "Consumption", emptyMonths));
        newModel.getRow().add(new BudgetModel.Row("8", "variable-expense", "Prevadzky", emptyMonths));

        newModel.getRow().add(new BudgetModel.Row("9", "desired-cf", "Sluzby", emptyMonths));
        newModel.getRow().add(new BudgetModel.Row("10", "desired-cf", "Oblecenie", emptyMonths));
        newModel.getRow().add(new BudgetModel.Row("11", "desired-cf", "Zeny", emptyMonths));
        newModel.getRow().add(new BudgetModel.Row("12", "desired-cf", "Investovanie", emptyMonths));
        newModel.getRow().add(new BudgetModel.Row("13", "desired-cf", "Zaloha na auto", emptyMonths));
        newModel.getRow().add(new BudgetModel.Row("14", "desired-cf", "Dovolenky", emptyMonths));

        update(newModel);
        Initializer.LOG.info("File created: '" + schemaFileUri + "'");
    }

    @Override
    public BudgetModel retrieve() throws ManagerException {
        try {
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            javax.xml.validation.Schema xmlSchema = schemaFactory.newSchema(this.getClass().getResource(schemaUri));

            JAXBContext context = JAXBContext.newInstance(BudgetModel.class);

            Unmarshaller unmarshaller = context.createUnmarshaller();
            unmarshaller.setSchema(xmlSchema);

            File file = new File(schemaFileUri);
            return (BudgetModel) unmarshaller.unmarshal(file);
        } catch (Exception e) {
            throw new ManagerException("Error while retrieving procedures data: ",e);
        }
    }

    @Override
    public void update(BudgetModel budgetModel) throws ManagerException {
        try {
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            javax.xml.validation.Schema xmlSchema = schemaFactory.newSchema(this.getClass().getResource(schemaUri));

            JAXBContext context = JAXBContext.newInstance(BudgetModel.class);

            Marshaller marshaller = context.createMarshaller();
            marshaller.setSchema(xmlSchema);
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            File file = new File(schemaFileUri);
            marshaller.marshal(budgetModel,new DefaultHandler());
            marshaller.marshal(budgetModel, file);
        } catch (Exception e) {
            throw new ManagerException("Error while updating procedures data: ",e);
        }
    }
}