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

package io.cdap.wrangler.api.parser;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import io.cdap.wrangler.api.annotations.PublicEvolving;

@PublicEvolving
public class ByteSize implements Token{
    private final double value;
    private final String unit;

    public ByteSize(String token) {
        int index = 0;
        while (index < token.length() && (Character.isDigit(token.charAt(index)) || token.charAt(index) == '.')
                || token.charAt(index) == '-') {
            index++;
        }
        this.value = Double.parseDouble(token.substring(0, index));
        this.unit = token.substring(index);
    }

    public long getBytes() {
        if (unit.equalsIgnoreCase("KB")) {
            return (long) (value * 1024);
        } else if (unit.equalsIgnoreCase("MB")) {
            return (long) (value * 1024 * 1024);
        } else if (unit.equalsIgnoreCase("GB")) {
            return (long) (value * 1024 * 1024 * 1024);
        } else if (unit.equalsIgnoreCase("TB")) {
            return (long) (value * 1024 * 1024 * 1024 * 1024);
        } else {
            return (long) value;
        }
    }

    @Override
    public Object value() {
        return getBytes();
    }

    @Override
    public TokenType type() {
        return TokenType.BYTE_SIZE;
    }

    @Override
    public JsonElement toJson() {
        return new JsonPrimitive(getBytes());
    }

    @Override
    public String toString() {
        return value + unit;
    }
}
