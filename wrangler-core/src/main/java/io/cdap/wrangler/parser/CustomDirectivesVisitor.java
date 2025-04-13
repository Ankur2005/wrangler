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

 package io.cdap.wrangler.parser;

 import io.cdap.wrangler.api.parser.ByteSize;
 import io.cdap.wrangler.api.parser.TimeDuration;
 import io.cdap.wrangler.api.parser.Token;

 import org.antlr.v4.runtime.tree.ParseTree;

 import java.util.ArrayList;
 import java.util.List;

/**
 * CustomDirectivesVisitor converts parse tree nodes into corresponding Token objects.
 * Tokens are collected into a list for further directive execution.
 */
public class CustomDirectivesVisitor extends DirectivesBaseVisitor<List<Token>> {

  @Override
  public List<Token> visitDirective(DirectivesParser.DirectiveContext ctx) {
    List<Token> tokenGroup = new ArrayList<>();
    for (ParseTree child : ctx.children) {
      if (child instanceof DirectivesParser.ValueContext) {
        tokenGroup.addAll(visitValue((DirectivesParser.ValueContext) child));
      }
    }
    return tokenGroup;
  }

  @Override
  public List<Token> visitValue(DirectivesParser.ValueContext ctx) {
    String text = ctx.String().getText().toUpperCase();
    List<Token> tokens = new ArrayList<>();

    // Regex pattern for byte sizes (e.g., "10KB", "1.5MB").
    if (text.matches("(?i)^\\d+(\\.\\d+)?(KB|MB|GB|TB|PB|B)$")) {
      tokens.add(new ByteSize(text));
      return tokens;
    }

    text = text.toLowerCase();
    // Regex pattern for time durations (e.g., "150ms", "2.5h", "30sec").
    if (text.matches("(?i)^\\d+(\\.\\d+)?(ms|s|sec|m|min|h|hr|d|day)$")) {
      tokens.add(new TimeDuration(text));
      return tokens;
    }
    return super.visitValue(ctx);
  }

  @Override
  public List<Token> visitByteSizeArg(DirectivesParser.ByteSizeArgContext ctx) {
    List<Token> tokens = new ArrayList<>();
    tokens.add(new ByteSize(ctx.getText()));
    return tokens;
  }

  @Override
  public List<Token> visitTimeDurationArg(DirectivesParser.TimeDurationArgContext ctx) {
    List<Token> tokens = new ArrayList<>();
    tokens.add(new TimeDuration(ctx.getText()));
    return tokens;
  }
}