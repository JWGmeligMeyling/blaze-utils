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
package com.blazebit.persistence.impl;

import com.blazebit.persistence.CaseWhenBuilder;
import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.HavingOrBuilder;
import com.blazebit.persistence.JoinType;
import com.blazebit.persistence.ModelUtils;
import com.blazebit.persistence.ObjectBuilder;
import com.blazebit.persistence.PaginatedCriteriaBuilder;
import com.blazebit.persistence.QueryBuilder;
import com.blazebit.persistence.RestrictionBuilder;
import com.blazebit.persistence.SelectObjectBuilder;
import com.blazebit.persistence.SimpleCaseWhenBuilder;
import com.blazebit.persistence.WhereOrBuilder;
import com.blazebit.persistence.expression.CompositeExpression;
import com.blazebit.persistence.expression.Expression;
import com.blazebit.persistence.expression.ExpressionUtils;
import com.blazebit.persistence.expression.PathExpression;
import com.blazebit.persistence.predicate.AndPredicate;
import com.blazebit.persistence.predicate.Predicate;
import com.blazebit.persistence.predicate.PredicateBuilder;
import com.blazebit.reflection.ReflectionUtils;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;
import org.hibernate.Query;
import org.hibernate.transform.ResultTransformer;

/**
 *
 * @author ccbem
 */
public abstract class AbstractCriteriaBuilder<T, U extends QueryBuilder<T, U>> implements QueryBuilder<T, U> {

    protected static final Logger log = Logger.getLogger(CriteriaBuilderImpl.class.getName());

    protected final Class<T> clazz;
    protected final EntityManager em;

    protected final ParameterManager parameterManager;
    protected final SelectManager<T> selectManager;
    protected final WhereManager<U> whereManager;
    protected final HavingManager<U> havingManager;
    protected final GroupByManager groupByManager;
    protected final OrderByManager orderByManager;
    protected final JoinManager joinManager;
    private final ArrayExpressionTransformer arrayTransformer;
    private final QueryGenerator queryGenerator;

    /**
     * Create flat copy of builder
     *
     * @param builder
     */
    protected AbstractCriteriaBuilder(AbstractCriteriaBuilder<T, ? extends QueryBuilder<T, ?>> builder) {
        this.clazz = builder.clazz;
        this.orderByManager = builder.orderByManager;
        this.parameterManager = builder.parameterManager;
        this.selectManager = builder.selectManager;
        this.whereManager = (WhereManager<U>) builder.whereManager;
        this.havingManager = (HavingManager<U>) builder.havingManager;
        this.groupByManager = builder.groupByManager;
        this.joinManager = builder.joinManager;
        this.arrayTransformer = builder.arrayTransformer;
        this.queryGenerator = builder.queryGenerator;
        this.em = builder.em;
    }

    public AbstractCriteriaBuilder(EntityManager em, Class<T> clazz, String alias) {
        this.clazz = clazz;
        
        this.joinManager = new JoinManager(alias, clazz);
                
        this.parameterManager = new ParameterManager();
        
        this.arrayTransformer = new ArrayExpressionTransformer();
        this.queryGenerator = new QueryGenerator(parameterManager);
        
        
        this.whereManager = new WhereManager<U>(queryGenerator, arrayTransformer);
        this.havingManager = new HavingManager<U>(queryGenerator, arrayTransformer);
        this.groupByManager = new GroupByManager(queryGenerator, arrayTransformer);
                
        this.selectManager = new SelectManager<T>(queryGenerator);
        this.orderByManager = new OrderByManager(queryGenerator, arrayTransformer);
        
        //resolve cyclic dependencies
        this.arrayTransformer.setRootWherePredicate(whereManager.getRootPredicate());
        this.queryGenerator.setSelectManager(selectManager);
        this.em = em;
    }
    
    @Override
    public List<T> getResultList(EntityManager em) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /*
     * Select methods
     */
    @Override
    public U distinct() {
        selectManager.distinct();
        return (U) this;
    }

    /* CASE (WHEN condition THEN scalarExpression)+ ELSE scalarExpression END */
    @Override
    public CaseWhenBuilder<U> selectCase() {
        return new CaseWhenBuilderImpl<U>((U) this);
    }

    /* CASE caseOperand (WHEN scalarExpression THEN scalarExpression)+ ELSE scalarExpression END */
    @Override
    public SimpleCaseWhenBuilder<U> selectCase(String expression) {
        return new SimpleCaseWhenBuilderImpl<U>((U) this, expression);
    }

    @Override
    public PaginatedCriteriaBuilder<T> page(int page, int objectsPerPage) {
        return new PaginatedCriteriaBuilderImpl<T>(this, page, objectsPerPage);

    }

    @Override
    public U select(String... expressions) {
        for (String expression : expressions) {
            select(expression);
        }
        return (U) this;
    }

    @Override
    public U select(String expression) {
        return select(expression, null);
    }

    @Override
    public U select(String expression, String selectAlias) {
        if (expression == null) {
            throw new NullPointerException("expression");
        }
        if (expression.isEmpty() || (selectAlias != null && selectAlias.isEmpty())) {
            throw new IllegalArgumentException("selectAlias");
        }
        verifyBuilderEnded();
        selectManager.select(this, expression, selectAlias);
        return (U) this;
    }

    @Override
    public U select(Class<? extends T> clazz) {
        throw new UnsupportedOperationException();
    }

    @Override
    public U select(Constructor<? extends T> constructor) {
        throw new UnsupportedOperationException();
    }

    // TODO: needed?
    @Override
    public U select(ObjectBuilder<? extends T> builder) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <Y> SelectObjectBuilder<? extends QueryBuilder<Y, ?>> selectNew(Class<Y> clazz) {
        verifyBuilderEnded();
        return selectManager.selectNew(this, clazz);
    }

    @Override
    public <Y> SelectObjectBuilder<? extends QueryBuilder<Y, ?>> selectNew(Constructor<Y> constructor) {
        verifyBuilderEnded();
        return selectManager.selectNew(this, constructor);
    }

    @Override
    public SelectObjectBuilder<U> selectNew(ObjectBuilder<? extends T> builder) {
        verifyBuilderEnded();
        return selectManager.selectNew(builder);
    }

    /*
     * Where methods
     */
    @Override
    public RestrictionBuilder<U> where(String expression) {
        if (expression == null) {
            throw new NullPointerException("expression");
        }
        if (expression.isEmpty()) {
            throw new IllegalArgumentException("expression");
        }
        return whereManager.restrict(this, expression);
    }

    @Override
    public WhereOrBuilder<U> whereOr() {
        return whereManager.whereOr(this);
    }

    @Override
    public U groupBy(String... paths) {
        for (String path : paths) {
            groupBy(path);
        }
        return (U) this;
    }

    @Override
    public U groupBy(String expression) {
        verifyBuilderEnded();
        groupByManager.groupBy(expression);
        return (U) this;
    }

    /*
     * Having methods
     */
    @Override
    public RestrictionBuilder<U> having(String expression) {
        if (groupByManager.getGroupByInfos().isEmpty()) {
            throw new IllegalStateException();
        }
        return havingManager.restrict(this, expression);
    }

    @Override
    public HavingOrBuilder<U> havingOr() {
        return havingManager.havingOr(this);
    }

    /*
     * Order by methods
     */
    @Override
    public U orderByDesc(String path) {
        return orderBy(path, false, false);
    }

    @Override
    public U orderByAsc(String path) {
        return orderBy(path, true, false);
    }

    @Override
    public U orderByDesc(String path, boolean nullFirst) {
        return orderBy(path, false, nullFirst);
    }

    @Override
    public U orderByAsc(String path, boolean nullFirst) {
        return orderBy(path, true, nullFirst);
    }

    protected void verifyBuilderEnded() {
        whereManager.verifyBuilderEnded();
        havingManager.verifyBuilderEnded();
        selectManager.verifyBuilderEnded();
    }

    

    @Override
    public U orderBy(String expression, boolean ascending, boolean nullFirst) {
        if (expression == null) {
            throw new NullPointerException("expression");
        }
        if (expression.isEmpty()) {
            throw new IllegalArgumentException("expression");
        }
        verifyBuilderEnded();
        orderByManager.orderBy(expression, ascending, nullFirst);
        return (U) this;
    }

    /*
     * Join methods
     */
    @Override
    public U innerJoin(String path, String alias) {
        return join(path, alias, JoinType.INNER, false);
    }

    @Override
    public U innerJoinFetch(String path, String alias) {
        return join(path, alias, JoinType.INNER, true);
    }

    @Override
    public U leftJoin(String path, String alias) {
        return join(path, alias, JoinType.LEFT, false);
    }

    @Override
    public U leftJoinFetch(String path, String alias) {
        return join(path, alias, JoinType.LEFT, true);
    }

    @Override
    public U rightJoin(String path, String alias) {
        return join(path, alias, JoinType.RIGHT, false);
    }

    @Override
    public U rightJoinFetch(String path, String alias) {
        return join(path, alias, JoinType.RIGHT, true);
    }

    @Override
    public U outerJoin(String path, String alias) {
        return join(path, alias, JoinType.OUTER, false);
    }

    @Override
    public U outerJoinFetch(String path, String alias) {
        return join(path, alias, JoinType.OUTER, true);
    }

    @Override
    public U join(String path, String alias, JoinType type, boolean fetch) {
        if (path == null || alias == null || type == null) {
            throw new NullPointerException();
        }
        if (alias.isEmpty()) {
            throw new IllegalArgumentException();
        }
        verifyBuilderEnded();
        joinManager.join(path, alias, type, fetch);
        return (U) this;
    }

    protected void applyImplicitJoins() {
        final JoinVisitor joinVisitor = new JoinVisitor(joinManager, selectManager);
        // carry out implicit joins
        joinVisitor.setFromSelect(true);
        selectManager.acceptVisitor(joinVisitor);
        joinVisitor.setFromSelect(false);
        
        whereManager.acceptVisitor(joinVisitor);
        groupByManager.acceptVisitor(joinVisitor);
        
        havingManager.acceptVisitor(joinVisitor);
        joinVisitor.setJoinWithObjectLeafAllowed(false);
        orderByManager.acceptVisitor(joinVisitor);
        joinVisitor.setJoinWithObjectLeafAllowed(true);
    }
    
    protected void applyArrayTransformations(){
        // run through expressions
        // for each arrayExpression, look up the alias in the joinManager's aliasMap
        // do the transformation using the alias
        // exchange old arrayExpression with new PathExpression
        // introduce applyTransformer method in managers
        // transformer has a method that returns the transformed Expression
        // the applyTransformer method will replace the transformed expression with the original one
        selectManager.applyTransformer(arrayTransformer);
    }

    @Override
    public String getQueryString() {
        verifyBuilderEnded();
        StringBuilder sb = new StringBuilder();
        // resolve unresolved aliases, object model etc.
        // we must do implicit joining at the end because we can only do
        // the aliases resolving at the end and alias resolving must happen before
        // the implicit joins
        // it makes no sense to do implicit joining before this point, since
        // the user can call the api in arbitrary orders
        // so where("b.c").join("a.b") but also
        // join("a.b", "b").where("b.c")
        // in the first case
        applyImplicitJoins();
        
        sb.append(selectManager.buildSelect());
        sb.append("FROM ").append(clazz.getSimpleName()).append(' ').append(joinManager.getRootAlias());
        sb.append(joinManager.buildJoins(true));
        sb.append(whereManager.buildClause());
        sb.append(groupByManager.buildGroupBy());        
        sb.append(havingManager.buildClause());
        sb.append(orderByManager.buildOrderBy());
        return sb.toString();
    }

    @Override
    public TypedQuery<T> getQuery(EntityManager em) {
        TypedQuery<T> query = (TypedQuery) em.createQuery(getQueryString(), Object[].class);
        if (selectManager.getSelectObjectTransformer() != null) {
            // get hibernate query
            Query hQuery = query.unwrap(Query.class);
            hQuery.setResultTransformer(selectManager.getSelectObjectTransformer());
        }
        for (Map.Entry<String, Object> entry : parameterManager.getParameters().entrySet()) {
            query.setParameter(entry.getKey(), entry.getValue());
        }
        return query;
    }

}
