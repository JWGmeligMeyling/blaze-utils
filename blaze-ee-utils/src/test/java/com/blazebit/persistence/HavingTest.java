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

import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author ccbem
 */
public class HavingTest {
    @Test
    public void testHaving(){
        CriteriaBuilderImpl<Document> criteria = CriteriaBuilderImpl.from(Document.class, "d");
        criteria.groupBy("d.owner").having("d.age").gt(0);
        
        assertEquals("FROM Document d GROUP BY d.owner HAVING d.age > :param_0", criteria.getQueryString());
    }
    
    @Test
    public void testHavingPath(){
        CriteriaBuilderImpl<Document> criteria = CriteriaBuilderImpl.from(Document.class, "d");
        criteria.groupBy("d.owner").having("d.partners.age").gt(0);
        
        assertEquals("FROM Document d LEFT JOIN d.partners partners GROUP BY d.owner HAVING partners.age > :param_0", criteria.getQueryString());
    }
    
    @Test(expected = javax.jms.IllegalStateException.class)
    public void testHavingWithoutGroupBy(){
        CriteriaBuilderImpl<Document> criteria = CriteriaBuilderImpl.from(Document.class, "d");
        criteria.having("d.partners.age");        
    }
    
    @Test(expected = NullPointerException.class)
    public void testHavingNull(){
        CriteriaBuilderImpl<Document> criteria = CriteriaBuilderImpl.from(Document.class, "d");
        criteria.groupBy("d.owner").having(null);      
    }
    
    @Test(expected = NullPointerException.class)
    public void testHavingAnd(){
        CriteriaBuilderImpl<Document> criteria = CriteriaBuilderImpl.from(Document.class, "d");
        criteria.groupBy("d.owner").having("d.partners.age").gt(0).having("d.locations.url").like("http://%");     
        
        assertEquals("FROM Document d GROUP BY d.owner HAVING d.partners.age > :param_0 and d.locations.url LIKE :param_1", criteria.getQueryString());
    }
}
