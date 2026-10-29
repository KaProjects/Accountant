package org.kaleta.accountant;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.kaleta.accountant.common.Constants;

import java.io.File;

/**
 * Which books the app opens is a question for whoever starts it, not for the sources: nothing in
 * the repository names a context any more, so a branch never carries a line that has to be put
 * back before it is merged.
 */
public class InitializerContextTest {

    @After
    public void clearProperty() {
        System.clearProperty(Initializer.CONTEXT_PROPERTY);
    }

    /** A released jar is started with no argument, so saying nothing has to mean the real books. */
    @Test
    public void noPropertyMeansProduction() {
        System.clearProperty(Initializer.CONTEXT_PROPERTY);
        Assert.assertEquals(Constants.Context.PRODUCTION, Initializer.contextFromProperty());
    }

    @Test
    public void eachContextHasAName() {
        System.setProperty(Initializer.CONTEXT_PROPERTY, "production");
        Assert.assertEquals(Constants.Context.PRODUCTION, Initializer.contextFromProperty());
        System.setProperty(Initializer.CONTEXT_PROPERTY, "devel");
        Assert.assertEquals(Constants.Context.DEVEL, Initializer.contextFromProperty());
        System.setProperty(Initializer.CONTEXT_PROPERTY, "test");
        Assert.assertEquals(Constants.Context.TEST, Initializer.contextFromProperty());
    }

    @Test
    public void theNameIsReadLoosely() {
        System.setProperty(Initializer.CONTEXT_PROPERTY, "  DEVEL ");
        Assert.assertEquals(Constants.Context.DEVEL, Initializer.contextFromProperty());
    }

    /** Each context opens its own data directory, which is the whole point of choosing one. */
    @Test
    public void everyContextHasItsOwnDataDirectory() {
        int original = Initializer.CONTEXT;
        try {
            // the production books are kept per release, the ones worked against are not
            Initializer.CONTEXT = Constants.Context.PRODUCTION;
            Assert.assertTrue(Initializer.getDataSource(),
                    Initializer.getDataSource().endsWith("Accountant-" + Initializer.VERSION + "-DATA" + File.separator));
            Initializer.CONTEXT = Constants.Context.DEVEL;
            Assert.assertTrue(Initializer.getDataSource(),
                    Initializer.getDataSource().endsWith("DEVEL-DATA" + File.separator));
            Initializer.CONTEXT = Constants.Context.TEST;
            Assert.assertTrue(Initializer.getDataSource(),
                    Initializer.getDataSource().endsWith("TEST-DATA" + File.separator));
        } finally {
            Initializer.CONTEXT = original;
        }
    }

    /**
     * A mistyped context must stop the app rather than fall back, because the fallback is
     * production: '-Daccountant.context=dev' would otherwise open the real books and write to them.
     */
    @Test
    public void aMistypedContextIsRefused() {
        System.setProperty(Initializer.CONTEXT_PROPERTY, "dev");
        try {
            Initializer.contextFromProperty();
            Assert.fail("a context that is not one of the three should not be accepted");
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage(), e.getMessage().contains("'dev'"));
        }
    }
}
