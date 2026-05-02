package net.bugreaper.modules.mongodb.matcher;

import org.bson.Document;
import org.bson.json.JsonWriterSettings;

import java.util.*;


@SuppressWarnings("java:S2259")
public class JsonMatcher {

    private JsonMatcher() {
        throw new IllegalStateException("Utility class");
    }

    // ignore Mongo auto-generated field
    public static final String IGNORED_FIELD = "_id";

    public static void assertMatches(int num,
                                     Document expected,
                                     Document actual,
                                     boolean strict) {

        List<String> errors = new ArrayList<>();

        compare(expected, actual, "", errors, strict);

        //generate message for element error
        if (!errors.isEmpty()) {
            throw new AssertionError(buildError(num, actual, errors));
        }
    }

    //compare objects
    private static void compare(Object expected,
                                Object actual,
                                String path,
                                List<String> errors,
                                boolean strict) {

        if (expected == null) {
            handleNull(actual, path, errors);
            return;
        }

        if (isDocument(expected, actual)) {
            compareDocuments((Document) expected, (Document) actual, path, errors, strict);
            return;
        }

        if (isList(expected, actual)) {
            compareLists((List<?>) expected, (List<?>) actual, path, errors, strict);
            return;
        }

        compareValues(expected, actual, path, errors);
    }

    private static void handleNull(Object actual,
                                   String path,
                                   List<String> errors) {

        if (actual != null) {
            errors.add(path + ": expected null but was " + actual);
        }
    }

    private static boolean isDocument(Object expected, Object actual) {
        return expected instanceof Document && actual instanceof Document;
    }

    private static boolean isList(Object expected, Object actual) {
        return expected instanceof List && actual instanceof List;
    }

    private static void compareDocuments(Document expected,
                                         Document actual,
                                         String path,
                                         List<String> errors,
                                         boolean strict) {

        compareExpectedFields(expected, actual, path, errors, strict);

        if (strict) {
            compareUnexpectedFields(expected, actual, path, errors);
        }
    }

    private static void compareExpectedFields(Document expected,
                                              Document actual,
                                              String path,
                                              List<String> errors,
                                              boolean strict) {

        for (var entry : expected.entrySet()) {

            String key = entry.getKey();
            Object expectedValue = entry.getValue();

            String newPath = buildPath(path, key);

            if (!actual.containsKey(key)) {
                errors.add(newPath + ": field missing");
                continue;
            }

            Object actualValue = actual.get(key);

            compare(expectedValue, actualValue, newPath, errors, strict);
        }
    }

    private static void compareUnexpectedFields(Document expected,
                                                Document actual,
                                                String path,
                                                List<String> errors) {

        for (String key : actual.keySet()) {

            if (isIgnoredField(key)) {
                continue;
            }

            if (!expected.containsKey(key)) {
                errors.add(buildPath(path, key) + ": unexpected field");
            }
        }
    }

    private static void compareLists(List<?> expected,
                                     List<?> actual,
                                     String path,
                                     List<String> errors,
                                     boolean strict) {

        if (strict) {
            compareListsStrict(expected, actual, path, errors);
        } else {
            compareListsPartial(expected, actual, path, errors);
        }
    }

    private static void compareListsStrict(List<?> expected,
                                           List<?> actual,
                                           String path,
                                           List<String> errors) {

        if (expected.size() != actual.size()) {
            errors.add(path + ": array size mismatch. expected "
                    + expected.size() + " but was " + actual.size());
            return;
        }

        for (int i = 0; i < expected.size(); i++) {
            compare(expected.get(i), actual.get(i),
                    path + "[" + i + "]", errors, true);
        }
    }

    private static void compareListsPartial(List<?> expected,
                                            List<?> actual,
                                            String path,
                                            List<String> errors) {

        for (Object expItem : expected) {

            if (!containsMatch(expItem, actual, path)) {
                errors.add(path + ": array does not contain " + expItem);
            }
        }
    }

    private static boolean containsMatch(Object expectedItem,
                                         List<?> actualList,
                                         String path) {

        for (Object actItem : actualList) {
            List<String> tmp = new ArrayList<>();
            compare(expectedItem, actItem, path, tmp, false);

            if (tmp.isEmpty()) {
                return true;
            }
        }

        return false;
    }

    private static void compareValues(Object expected,
                                      Object actual,
                                      String path,
                                      List<String> errors) {

        if (!Objects.equals(expected, actual)) {
            errors.add(path + ": expected [" + expected + "] but was [" + actual + "]");
        }
    }

    private static boolean isIgnoredField(String key) {
        return IGNORED_FIELD.equals(key);
    }

    private static String buildPath(String path, String key) {
        return path.isEmpty() ? key : path + "." + key;
    }



    // Error message builder

    private static String buildError(int num,
                                     Document actual,
                                     List<String> errors) {

        StringBuilder sb = new StringBuilder();

       actual.remove(IGNORED_FIELD);

        JsonWriterSettings pretty = JsonWriterSettings.builder()
                .indent(true)
                .build();

        sb.append(String.format("%n--- Actual #%d---%n", num))
                .append(actual.toJson(pretty));

        sb.append("\n--- Differences ---\n");
        errors.forEach(e -> sb.append(" • ").append(e).append("\n"));

        return sb.toString();
    }

}