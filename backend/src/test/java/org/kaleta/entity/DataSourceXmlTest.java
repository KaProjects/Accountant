package org.kaleta.entity;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.junit.jupiter.api.Test;
import org.kaleta.entity.xml.Config;
import org.kaleta.entity.xml.Schema;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

/**
 * The desktop app owns the datasource files and evolves them independently of this
 * app, so these tests pin down how much of that evolution the sync has to survive.
 */
public class DataSourceXmlTest
{
    private final XmlMapper xmlMapper = new XmlMapper();

    @Test
    void configIsReadDespiteSectionsThisAppDoesNotKnow() throws Exception
    {
        // 'imports' and the credit mappings are written by the desktop app only;
        // before they were ignored they made every sync fail with a 500.
        String xml = """
                <config>
                    <years active="2026">
                        <year name="2025"/>
                        <year name="2026"/>
                    </years>
                    <imports>
                        <source name="Some Bank CSV" format="account-statement-csv" account="210.0"/>
                    </imports>
                    <mapping>
                        <debit substring="SHOP" account="510.0"/>
                        <credit substring="SALARY" account="610.0"/>
                    </mapping>
                    <somethingAddedLater value="42"/>
                </config>
                """;

        Config config = xmlMapper.readValue(xml, Config.class);

        assertThat(config.getYears().getActive(), is("2026"));
        assertThat(config.getYears().getYear().stream().map(Config.Years.Year::getName).toList(),
                contains("2025", "2026"));
    }

    @Test
    void schemaIsReadWithoutAYearOfItsOwn() throws Exception
    {
        // The schema became a singleton shared by all years, so it no longer
        // carries a year attribute; the year comes from the caller when syncing.
        String xml = """
                <schema>
                    <class id="2" name="Finance">
                        <group id="3" name="dl. financny majetok">
                            <account id="2" name="komodity" type="A"/>
                        </group>
                    </class>
                </schema>
                """;

        Schema schema = xmlMapper.readValue(xml, Schema.class);

        assertThat(schema.getClazz(), hasSize(1));
        Schema.Clazz clazz = schema.getClazz().get(0);
        assertThat(clazz.getId(), is("2"));
        assertThat(clazz.getGroup().get(0).getId(), is("3"));
        assertThat(clazz.getGroup().get(0).getAccount().get(0).getName(), is("komodity"));
        assertThat(clazz.getGroup().get(0).getAccount().get(0).getType(), is("A"));
    }
}
