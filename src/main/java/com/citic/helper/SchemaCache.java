package com.citic.helper;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Maps;

import org.apache.avro.Schema;


public class SchemaCache {
    private static final Schema.Parser parser = new Schema.Parser();
    private static final LoadingCache<String, Schema> schemaCache = CacheBuilder
            .newBuilder()
            .maximumSize(10000)
            .build(
                    new CacheLoader<String, Schema>() {
                        @Override
                        public Schema load(String schemaString) {
                            return parser.parse(schemaString);
                        }
                    });

    public static Schema getSchema(String schemaString) {
        return schemaCache.getUnchecked(schemaString);
    }
}
