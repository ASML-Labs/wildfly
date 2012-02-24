package org.jboss.as.arquillian.container;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jboss.arquillian.container.spi.Container;
import org.jboss.arquillian.container.spi.event.container.BeforeDeploy;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.test.spi.context.ClassContext;
import org.jboss.arquillian.test.spi.event.suite.AfterClass;
import org.jboss.as.arquillian.api.ServerSetup;
import org.jboss.as.arquillian.api.ServerSetupTask;

/**
 * @author Stuart Douglas
 */
public class ServerSetupObserver {


    @Inject
    private Instance<ManagementClient> managementClient;

    @Inject
    private Instance<ClassContext> classContextInstance;

    private Set<ContainerClassHolder> alreadyRun = new HashSet<ContainerClassHolder>();

    private final List<ManagementClient> active = new ArrayList<ManagementClient>();
    private ServerSetupTask current;

    public synchronized void handleBeforeDeployment(@Observes BeforeDeploy event, Container container) throws IllegalAccessException, InstantiationException {

        final ClassContext classContext = classContextInstance.get();
        if (classContext == null) {
            return;
        }

        final Class<?> currentClass = classContext.getActiveId();
        final ContainerClassHolder holder = new ContainerClassHolder(container.getName(), currentClass);
        if (alreadyRun.contains(holder)) {
            return;
        }
        alreadyRun.add(holder);
        ServerSetup setup = currentClass.getAnnotation(ServerSetup.class);
        if (setup == null) {
            return;
        }
        if (current == null) {
            current = setup.value().newInstance();
        } else if (current.getClass() != setup.value()) {
            throw new RuntimeException("Mismatched ServerSetupTask current is " + current + " but " + currentClass + " is expecting " + setup.value());
        }

        final ManagementClient client = managementClient.get();
        current.setup(client);
        active.add(client);
    }

    public synchronized void handleAfterClass(@Observes AfterClass event) {
        if (current != null) {
            for(final ManagementClient client : active) {
                current.tearDown(client);
            }
            active.clear();
            current = null;
        }
    }


    private static final class ContainerClassHolder {
        private final Class<?> testClass;
        private final String name;

        private ContainerClassHolder(final String name, final Class<?> testClass) {
            this.name = name;
            this.testClass = testClass;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            final ContainerClassHolder that = (ContainerClassHolder) o;

            if (name != null ? !name.equals(that.name) : that.name != null) return false;
            if (testClass != null ? !testClass.equals(that.testClass) : that.testClass != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = testClass != null ? testClass.hashCode() : 0;
            result = 31 * result + (name != null ? name.hashCode() : 0);
            return result;
        }
    }


}
