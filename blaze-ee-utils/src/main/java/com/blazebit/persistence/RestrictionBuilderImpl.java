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
package com.blazebit.persistence;

import com.blazebit.persistence.expression.Expression;
import com.blazebit.persistence.expression.ExpressionUtils;
import com.blazebit.persistence.expression.ParameterExpression;
import com.blazebit.persistence.predicate.BetweenPredicate;
import com.blazebit.persistence.predicate.EqPredicate;
import com.blazebit.persistence.predicate.GePredicate;
import com.blazebit.persistence.predicate.GtPredicate;
import com.blazebit.persistence.predicate.InPredicate;
import com.blazebit.persistence.predicate.IsEmptyPredicate;
import com.blazebit.persistence.predicate.IsMemberOfPredicate;
import com.blazebit.persistence.predicate.IsNullPredicate;
import com.blazebit.persistence.predicate.LePredicate;
import com.blazebit.persistence.predicate.LikePredicate;
import com.blazebit.persistence.predicate.LtPredicate;
import com.blazebit.persistence.predicate.NotPredicate;
import com.blazebit.persistence.predicate.Predicate;
import com.blazebit.persistence.predicate.PredicateBuilder;
import java.util.List;

/**
 *
 * @author cpbec
 */
public class RestrictionBuilderImpl<T extends BuilderEndedListener> extends AbstractBuilderEndedListener implements RestrictionBuilder<T> {

    private final T result;
    private final Expression leftExpression;
    private Predicate predicate;
    
    public RestrictionBuilderImpl(T result, Expression leftExpression) {
        this.leftExpression = leftExpression;
        this.result = result;
    }
    
    private T chain(Predicate predicate) {
        this.predicate = predicate;
        result.onBuilderEnded(this);
        return result;
    }
    
    @Override
    public void onBuilderEnded(PredicateBuilder o) {
        super.onBuilderEnded(o);
        predicate = o.getPredicate();
        result.onBuilderEnded(this);
    }

    @Override
    public Predicate getPredicate() {
        return predicate;
    }

    @Override
    public T between(Object start, Object end) {
        return chain(new BetweenPredicate(leftExpression, new ParameterExpression(start), new ParameterExpression(end)));
    }

    @Override
    public T notBetween(Object start, Object end) {
        return chain(new NotPredicate(new BetweenPredicate(leftExpression, new ParameterExpression(start), new ParameterExpression(end))));
    }

    @Override
    public QuantifiableBinaryPredicateBuilder<T> equalTo() {
        QuantifiableBinaryPredicateBuilder<T> builder = new EqPredicate.EqPredicateBuilder<T>(result, this, leftExpression, false);
        startedBuilders.add(builder);
        return builder;
    }

    @Override
    public T equalTo(Object value) {
        return chain(new EqPredicate(leftExpression, new ParameterExpression(value)));
    }

    @Override
    public T equalToExpression(String expression) {
        return chain(new EqPredicate(leftExpression, ExpressionUtils.parse(expression)));
    }

    @Override
    public QuantifiableBinaryPredicateBuilder<T> notEqualTo() {
        QuantifiableBinaryPredicateBuilder<T> builder =  new EqPredicate.EqPredicateBuilder<T>(result, this, leftExpression, true);
        startedBuilders.add(builder);
        return builder;
    }

    @Override
    public T notEqualTo(Object value) {
        return chain(new NotPredicate(new EqPredicate(leftExpression, new ParameterExpression(value))));
    }

    @Override
    public T notEqualToExpression(String expression) {
        return chain(new NotPredicate(new EqPredicate(leftExpression, ExpressionUtils.parse(expression))));
    }

    @Override
    public QuantifiableBinaryPredicateBuilder<T> greaterThan() {
        QuantifiableBinaryPredicateBuilder<T> builder =  new GtPredicate.GtPredicateBuilder<T>(result, this, leftExpression);
        startedBuilders.add(builder);
        return builder;
    }

    @Override
    public T greaterThan(Object value) {
        return chain(new GtPredicate(leftExpression, new ParameterExpression(value)));
    }

    @Override
    public T greaterThanExpression(String expression) {
        return chain(new GtPredicate(leftExpression, ExpressionUtils.parse(expression)));
    }

    @Override
    public QuantifiableBinaryPredicateBuilder<T> greaterOrEqualThan() {
        QuantifiableBinaryPredicateBuilder<T> builder =  new GePredicate.GePredicateBuilder<T>(result, this, leftExpression);
        startedBuilders.add(builder);
        return builder;
    }

    @Override
    public T greaterOrEqualThan(Object value) {
        return chain(new GePredicate(leftExpression, new ParameterExpression(value)));
    }

    @Override
    public T greaterOrEqualThanExpression(String expression) {
        return chain(new GePredicate(leftExpression, ExpressionUtils.parse(expression)));
    }

    @Override
    public QuantifiableBinaryPredicateBuilder<T> lessThan() {
        QuantifiableBinaryPredicateBuilder<T> builder =  new LtPredicate.LtPredicateBuilder<T>(result, this, leftExpression);
        startedBuilders.add(builder);
        return builder;
    }

    @Override
    public T lessThan(Object value) {
        return chain(new LtPredicate(leftExpression, new ParameterExpression(value)));
    }

    @Override
    public T lessThanExpression(String expression) {
        return chain(new LtPredicate(leftExpression, ExpressionUtils.parse(expression)));
    }

    @Override
    public QuantifiableBinaryPredicateBuilder<T> lessOrEqualThan() {
        QuantifiableBinaryPredicateBuilder<T> builder =  new LePredicate.LePredicateBuilder<T>(result, this, leftExpression);
        startedBuilders.add(builder);
        return builder;
    }

    @Override
    public T lessOrEqualThan(Object value) {
        return chain(new LePredicate(leftExpression, new ParameterExpression(value)));
    }

    @Override
    public T lessOrEqualThanExpression(String expression) {
        return chain(new LePredicate(leftExpression, ExpressionUtils.parse(expression)));
    }

    @Override
    public QuantizedBinaryPredicateBuilder<T> in() {
        QuantizedBinaryPredicateBuilder<T> builder =  new InPredicate.InPredicateBuilder<T>(result, this, leftExpression);
        startedBuilders.add(builder);
        return builder;
    }

    @Override
    public T in(List<?> values) {
        return chain(new InPredicate(leftExpression, new ParameterExpression(values)));
    }

//    @Override
//    public T inElements(String expression) {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }
//
//    @Override
//    public T inIndices(String expression) {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }
    
    @Override
    public T isNull() {
        return chain(new IsNullPredicate(leftExpression));
    }

    @Override
    public T isNotNull() {
        return chain(new NotPredicate(new IsNullPredicate(leftExpression)));
    }

    @Override
    public T isEmpty() {
        return chain(new IsEmptyPredicate(leftExpression));
    }

    @Override
    public T isNotEmpty() {
        return chain(new NotPredicate(new IsEmptyPredicate(leftExpression)));
    }

    @Override
    public T isMemberOf(String expression) {
        return chain(new IsMemberOfPredicate(leftExpression));
    }

    @Override
    public T isNotMemberOf(String expression) {
        return chain(new NotPredicate(new IsMemberOfPredicate(leftExpression)));
    }
    
    @Override
    public T like(String value) {
        return chain(new LikePredicate(leftExpression, new ParameterExpression(value), true, null));
    }

    @Override
    public T like(String value, boolean caseSensitive, Character escapeCharacter) {
        return chain(new LikePredicate(leftExpression, new ParameterExpression(value), caseSensitive, escapeCharacter));
    }
    
    @Override
    public T likeExpression(String expression) {
        return chain(new LikePredicate(leftExpression, ExpressionUtils.parse(expression), true, null));
    }

    @Override
    public T likeExpression(String expression, boolean caseSensitive, Character escapeCharacter) {
        return chain(new LikePredicate(leftExpression, ExpressionUtils.parse(expression), caseSensitive, escapeCharacter));
    }

    @Override
    public T notLike(String value) {
        return chain(new NotPredicate(new LikePredicate(leftExpression, new ParameterExpression(value), true, null)));
    }

    @Override
    public T notLike(String value, boolean caseSensitive, Character escapeCharacter) {
        return chain(new NotPredicate(new LikePredicate(leftExpression, new ParameterExpression(value), caseSensitive, escapeCharacter)));
    }

    @Override
    public T notLikeExpression(String expression) {
        return chain(new NotPredicate(new LikePredicate(leftExpression, ExpressionUtils.parse(expression), true, null)));
    }

    @Override
    public T notLikeExpression(String expression, boolean caseSensitive, Character escapeCharacter) {
        return chain(new NotPredicate(new LikePredicate(leftExpression, ExpressionUtils.parse(expression), caseSensitive, escapeCharacter)));
    }
    
}
