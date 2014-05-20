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

import com.blazebit.persistence.impl.JoinNode;

/**
 *
 * @author cpbec
 */
public class PropertyExpression implements Expression {

    private final String property;
    private JoinNode baseNode;
    private String field;

    public PropertyExpression(String property) {
        this.property = property;
    }
    
    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public String getProperty() {
        return property;
    }

    public JoinNode getBaseNode() {
        return baseNode;
    }

    public void setBaseNode(JoinNode baseNode) {
        this.baseNode = baseNode;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }
    
    @Override
    public String toString() {
        return property;
    }
    
}
