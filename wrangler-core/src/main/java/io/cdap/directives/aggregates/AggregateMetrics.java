/*
 * Copyright © 2017-2019 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package io.cdap.directives.aggregates;

import io.cdap.cdap.api.annotation.Description;
import io.cdap.cdap.api.annotation.Name;
import io.cdap.cdap.api.annotation.Plugin;
import io.cdap.wrangler.api.Arguments;
import io.cdap.wrangler.api.Directive;
import io.cdap.wrangler.api.DirectiveParseException;
import io.cdap.wrangler.api.ExecutorContext;
import io.cdap.wrangler.api.Row;
import io.cdap.wrangler.api.annotations.Categories;
import io.cdap.wrangler.api.parser.ColumnName;
import io.cdap.wrangler.api.parser.TokenType;
import io.cdap.wrangler.api.parser.UsageDefinition;

import java.util.List;

/**
 * A directive that aggregates byte sizes and time durations
 * into target columns with specified units.
 */
@Plugin(type = Directive.TYPE)
@Name("aggregate-metrics")
@Categories(categories = { "aggregator", "metrics" })
@Description("Aggregates byte sizes and time durations into target columns with specified units.")
public class AggregateMetrics implements Directive {
    private String byteSizeColumn;
    private String timeDurationColumn;
    private String targetSizeColumn;
    private String targetTimeColumn;
    private String sizeUnit;
    private String timeUnit;
    private String aggregationType;

    @Override
    public UsageDefinition define() {
        UsageDefinition.Builder builder = UsageDefinition.builder("aggregate-metrics");
        builder.define("byteSizeColumn", TokenType.COLUMN_NAME);
        builder.define("timeDurationColumn", TokenType.COLUMN_NAME);
        builder.define("targetSizeColumn", TokenType.COLUMN_NAME);
        builder.define("targetTimeColumn", TokenType.COLUMN_NAME);
        builder.define("sizeUnit", TokenType.TEXT, true); // Optional
        builder.define("timeUnit", TokenType.TEXT, true); // Optional
        builder.define("aggregationType", TokenType.TEXT, true); // Optional
        return builder.build();
    }

    @Override
    public void initialize(Arguments args) throws DirectiveParseException {
        byteSizeColumn = ((ColumnName) args.value("byteSizeColumn")).value();
        timeDurationColumn = ((ColumnName) args.value("timeDurationColumn")).value();
        targetSizeColumn = ((ColumnName) args.value("targetSizeColumn")).value();
        targetTimeColumn = ((ColumnName) args.value("targetTimeColumn")).value();
        sizeUnit = args.value("sizeUnit") != null ? args.value("sizeUnit").toString() : "B";
        timeUnit = args.value("timeUnit") != null ? args.value("timeUnit").toString() : "s";
        aggregationType = args.value("aggregationType") != null ? args.value("aggregationType").toString() : "total";
    }

    @Override
    public List<Row> execute(List<Row> rows, ExecutorContext context) {
        double totalSize = 0;
        double totalTime = 0;
        int count = 0;
        for (Row row : rows) {
            Object sizeObj = row.getValue(byteSizeColumn);
            Object timeObj = row.getValue(timeDurationColumn);
            if (sizeObj != null && timeObj != null) {
                double size = parseByteSize(sizeObj);
                double time = parseTimeDuration(timeObj);
                totalSize += size;
                totalTime += time;
                count++;
            }
        }

        double resultSize = convertSize(totalSize, sizeUnit);
        double resultTime;
        if ("average".equalsIgnoreCase(aggregationType) && count > 0) {
            resultTime = convertTime(totalTime / count, timeUnit);
        } else {
            resultTime = convertTime(totalTime, timeUnit);
        }

        for (Row row : rows) {
            row.addOrSet(targetSizeColumn, resultSize);
            row.addOrSet(targetTimeColumn, resultTime);
        }
        return rows;
    }

    private double parseByteSize(Object value) {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        } else if (value instanceof String) {
            String strValue = (String) value;
            try {
                if (strValue.matches("\\d+(\\.\\d+)?[KkMmGgTt]?[Bb]?")) {
                    double size = Double.parseDouble(strValue.replaceAll("[^\\d.]", ""));
                    String unit = strValue.replaceAll("[\\d.]", "").toUpperCase();
                    switch (unit) {
                        case "KB":
                        case "K":
                            return size * 1024;
                        case "MB":
                        case "M":
                            return size * 1024 * 1024;
                        case "GB":
                        case "G":
                            return size * 1024 * 1024 * 1024;
                        case "TB":
                        case "T":
                            return size * 1024 * 1024 * 1024 * 1024;
                        default:
                            return size;
                    }
                }
                return Double.parseDouble(strValue);
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }

    private double parseTimeDuration(Object value) {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        } else if (value instanceof String) {
            String strValue = (String) value;
            try {
                if (strValue.matches("\\d+(\\.\\d+)?[smhd]")) {
                    double time = Double.parseDouble(strValue.replaceAll("[^\\d.]", ""));
                    String unit = strValue.replaceAll("[\\d.]", "").toLowerCase();

                    switch (unit) {
                        case "ms":
                            return time / 1000;
                        case "m":
                            return time * 60;
                        case "h":
                            return time * 3600;
                        case "d":
                            return time * 3600 * 24;
                        default:
                            return time; // Assume seconds
                    }
                }
                return Double.parseDouble(strValue);
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }

    private double convertSize(double bytes, String targetUnit) {
        switch (targetUnit.toUpperCase()) {
            case "KB":
                return bytes / 1024;
            case "MB":
                return bytes / (1024 * 1024);
            case "GB":
                return bytes / (1024 * 1024 * 1024);
            case "TB":
                return bytes / (1024 * 1024 * 1024 * 1024);
            default:
                return bytes; // Return as bytes
        }
    }

    private double convertTime(double seconds, String targetUnit) {
        switch (targetUnit.toLowerCase()) {
            case "ms":
                return seconds * 1000;
            case "m":
                return seconds / 60;
            case "h":
                return seconds / 3600;
            case "d":
                return seconds / (3600 * 24);
            default:
                return seconds; // Return as seconds
        }
    }

    @Override
    public void destroy() {
        // No-op
    }

}
