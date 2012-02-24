/*
 * JBoss, Home of Professional Open Source.
 * Copyright (c) 2011, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.as.test.integration.web.security.form;

import java.net.URL;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.as.arquillian.api.ServerSetup;
import org.jboss.as.test.integration.web.security.SecuredServlet;
import org.jboss.as.test.integration.web.security.WebSecurityPasswordBasedBase;
import org.jboss.as.test.integration.web.security.WebTestsSecurityDomainSetup;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Unit Test web security
 *
 * @author <a href="mailto:mmoyses@redhat.com">Marcus Moyses</a>
 */
@RunWith(Arquillian.class)
@RunAsClient
@ServerSetup(WebTestsSecurityDomainSetup.class)
public class WebSecurityJBossWebXmlSecurityRolesTestCase extends WebSecurityFORMTestCase {

    public static final String deploymentName = "web-secure.war";

    @ArquillianResource
    @OperateOnDeployment(deploymentName)
    URL deploymentUrl;

    @Before
    public void init() {
        // make URL params flexible in case we are running tests against something different than localhost:8080
        setHostname(deploymentUrl.getHost());
        setPort(deploymentUrl.getPort());
    }


    @Deployment(name = deploymentName, order = 1, testable = false)
    public static WebArchive deployment() throws Exception {

        WebArchive war = ShrinkWrap.create(WebArchive.class, "web-secure.war");
        war.addClasses(SecuredServlet.class);

        war.addAsWebResource(WebSecurityJBossWebXmlSecurityRolesTestCase.class.getPackage(), "login.jsp", "login.jsp");
        war.addAsWebResource(WebSecurityJBossWebXmlSecurityRolesTestCase.class.getPackage(), "error.jsp", "error.jsp");

        war.addAsWebInfResource(WebSecurityJBossWebXmlSecurityRolesTestCase.class.getPackage(), "jboss-web-role-mapping.xml", "jboss-web.xml");
        war.addAsWebInfResource(WebSecurityJBossWebXmlSecurityRolesTestCase.class.getPackage(), "web.xml", "web.xml");

        war.addAsResource(WebSecurityJBossWebXmlSecurityRolesTestCase.class.getPackage(), "users.properties", "users.properties");
        war.addAsResource(WebSecurityJBossWebXmlSecurityRolesTestCase.class.getPackage(), "roles.properties", "roles.properties");
        WebSecurityPasswordBasedBase.printWar(war);
        return war;
    }


    /**
     * Override test behavior to check if role mapping in jboss-web.xml works.
     */
    @Override
    @Test
    public void testPasswordBasedUnsuccessfulAuth() throws Exception {
        makeCall("marcus", "marcus", 200);
    }

    /**
     * Negative test to see if mapping is not performed on username instead of role.
     *
     * @throws Exception
     */
    @Test
    public void testPrincipalMappingOnRole() throws Exception {
        makeCall("peter", "peter", 403);
    }
}