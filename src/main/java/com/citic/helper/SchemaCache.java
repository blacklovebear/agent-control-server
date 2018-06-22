package com.citic.helper;

import com.google.common.collect.Maps;
import java.util.Map;
import org.apache.avro.Schema;


public class SchemaCache {

    private static final Schema.Parser parser = new Schema.Parser();
    private static final Map<String, Schema> localCache = Maps.newConcurrentMap();

    private SchemaCache() {
        throw new IllegalStateException("Utility class");
    }


    private static String getTableFieldSchema(Iterable<String> schemaFieldList, String schemaName) {
        StringBuilder builder = new StringBuilder();
        String schema = "{"
            + "\"type\":\"record\","
            + "\"name\":\"" + schemaName + "\","
            + "\"fields\":[";

        builder.append(schema);

        String prefix = "";
        for (String fieldStr : schemaFieldList) {
            String field = "{ \"name\":\"" + fieldStr + "\", \"type\":[\"string\",\"null\"] }";
            builder.append(prefix);
            prefix = ",";
            builder.append(field);
        }

        builder.append("]}");
        return builder.toString();
    }

    /**
     * Gets schema.
     *
     * @param schemaFieldList the schema field list
     * @param schemaName the schema name
     * @return the schema
     */
    public static Schema getSchema(Iterable<String> schemaFieldList, String schemaName) {
        return localCache.computeIfAbsent(schemaName, key -> {
            String schemaString = getTableFieldSchema(schemaFieldList, schemaName);
            return parser.parse(schemaString);
        });
    }
}
