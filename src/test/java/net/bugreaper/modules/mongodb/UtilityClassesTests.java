package net.bugreaper.modules.mongodb;

import net.bugreaper.modules.mongodb.logger.Log;
import net.bugreaper.modules.mongodb.matcher.JsonMatcher;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.assertThrows;

class UtilityClassesTests {

    @Test
    void utilityJsonMatcherTest() throws NoSuchMethodException {
        Constructor<JsonMatcher> constructor = JsonMatcher.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        InvocationTargetException thrown = assertThrows(InvocationTargetException.class, constructor::newInstance);

        Throwable cause = thrown.getCause();
        assert (cause instanceof IllegalStateException);
        assert ("Utility class".equals(cause.getMessage()));
    }

    @Test
    void utilityLogTest() throws NoSuchMethodException {
        Constructor<Log> constructor = Log.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        InvocationTargetException thrown = assertThrows(InvocationTargetException.class, constructor::newInstance);

        Throwable cause = thrown.getCause();
        assert (cause instanceof IllegalStateException);
        assert ("Utility class".equals(cause.getMessage()));
    }

}
