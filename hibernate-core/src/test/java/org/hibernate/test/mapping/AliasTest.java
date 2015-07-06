/* 
 * Hibernate, Relational Persistence for Idiomatic Java
 * 
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */
package org.hibernate.test.mapping;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Iterator;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.mapping.Table;
import org.hibernate.testing.TestForIssue;
import org.hibernate.testing.junit4.BaseCoreFunctionalTestCase;
import org.junit.Test;

/**
 * Column aliases utilize {@link Table#getUniqueInteger()} for naming.  The
 * unique integer used to be statically generated by the Table class, meaning
 * it was dependent on mapping order.  HHH-2448 made the alias names
 * deterministic by having Configuration determine the unique integers on its
 * second pass over the Tables tree map.  AliasTest and
 * {@link MappingReorderedAliasTest} ensure that the unique integers are the
 * same, regardless of mapping ordering.
 * 
 * @author Brett Meyer
 */
public class AliasTest extends BaseCoreFunctionalTestCase {
	
	@Test
	@TestForIssue( jiraKey = "HHH-8371" )
	public final void testUnderscoreInColumnName() throws Throwable {
		final Session s = openSession();
		s.getTransaction().begin();
		
		UserEntity user = new UserEntity();
		user.setName( "foo" );
		s.persist(user);
		final ConfEntity conf =  new ConfEntity();
		conf.setConfKey("counter");
		conf.setConfValue("3");
		final UserConfEntity uc = new UserConfEntity();
		uc.setUser(user);
		uc.setConf(conf);
		conf.getUserConf().add(uc);
		s.persist(conf);

		s.getTransaction().commit();
		s.clear();
		
		s.getTransaction().begin();
		user = (UserEntity) s.get(UserEntity.class, user.getId());

		try {
			s.flush();
		}
		catch ( HibernateException e ) {
			// original issue from HHH-8371
			fail( "The explicit column name's underscore(s) were not considered during alias creation." );
		}
		
		assertNotNull( user );
		assertEquals( user.getName(), "foo" );

		s.getTransaction().commit();
		s.close();
	}

	@Override
	protected Class<?>[] getAnnotatedClasses() {
		return new Class<?>[] { Table1.class, Table2.class, ConfEntity.class, UserConfEntity.class, UserEntity.class };
	}
}
