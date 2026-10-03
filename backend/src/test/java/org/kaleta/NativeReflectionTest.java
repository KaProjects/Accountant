package org.kaleta;

import io.quarkus.runtime.annotations.RegisterForReflection;
import org.junit.jupiter.api.Test;
import org.kaleta.dto.FinancialAssetsDto;
import org.kaleta.model.ChartData;
import org.kaleta.rest.error.Problem;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;

/**
 * A native image writes JSON by reflection and keeps only the metadata it was told to keep.
 * Every endpoint returns {@code jakarta.ws.rs.core.Response}, which hides the entity's type
 * from the build, so none of these classes is registered automatically.
 * <p>
 * A class that misses the annotation serialises as an empty object in the native production
 * binary while every test on the JVM keeps passing, because the JVM needs no such metadata.
 * That is how a release once shipped with every view broken and only the login working, the
 * login being the one endpoint that answers with plain text. Since the JVM cannot reproduce
 * the failure, checking that the annotation is present is the guard we have.
 * <p>
 * The annotation does not reach nested classes, so each one is checked in its own right.
 */
public class NativeReflectionTest
{
    @Test
    public void everyTypeCrossingTheRestBoundaryIsRegisteredForReflection() throws Exception
    {
        List<Class<?>> unregistered = new ArrayList<>();

        for (Class<?> type : typesCrossingTheRestBoundary())
        {
            if (!type.isAnnotationPresent(RegisterForReflection.class)) unregistered.add(type);
        }

        assertThat("these serialise as '{}' in the native image: " + unregistered, unregistered, is(empty()));
    }

    /**
     * Everything in the dto package, plus the response bodies modelled outside it: the chart
     * configuration, and the problem details every error is answered with.
     */
    private List<Class<?>> typesCrossingTheRestBoundary() throws Exception
    {
        List<Class<?>> types = new ArrayList<>();
        types.add(ChartData.Config.class);
        types.add(ChartData.Config.ChartType.class);
        types.add(Problem.class);
        types.add(Problem.Violation.class);

        // Anchored on a known class so this reads the compiled main classes rather than
        // whichever copy of the package name the test classpath happens to offer first.
        Path dtoDirectory = Path.of(FinancialAssetsDto.class
                .getResource("FinancialAssetsDto.class").toURI()).getParent();

        try (Stream<Path> files = Files.list(dtoDirectory))
        {
            for (Path file : files.sorted().toList())
            {
                String name = file.getFileName().toString();
                if (!name.endsWith(".class")) continue;

                String simpleName = name.substring(0, name.length() - ".class".length());
                if (simpleName.matches(".*\\$\\d+")) continue; // anonymous, never serialised

                types.add(Class.forName(FinancialAssetsDto.class.getPackageName() + "." + simpleName));
            }
        }
        return types;
    }
}
