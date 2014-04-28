/*
 * Copyright 2014 Blazebit.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.blazebit.persistence.expression;

/**
 *
 * @author cpbec
 */
public class FunctionExpression implements Expression {
    
    private final String name;
    private final Expression[] expressions;

    private FunctionExpression(String name, Expression... expressions) {
        this.name = name;
        this.expressions = expressions;
    }

    public String getName() {
        return name;
    }

    public Expression[] getExpressions() {
        return expressions;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
    
}
