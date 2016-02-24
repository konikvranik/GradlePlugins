package net.suteren.gradle.plugin

import org.gradle.api.Project
import org.gradle.plugins.ide.eclipse.EclipsePlugin
import org.gradle.testfixtures.ProjectBuilder
import org.testng.annotations.Test

/**
 * Created by vranikp on 24.2.16.
 *
 * @author vranikp
 */
class AndroidEclipsePluginTest extends GroovyTestCase {
    @Test
    public void greeterPluginAddsGreetingTaskToProject() {
        Project project = ProjectBuilder.builder().build()
        project.plugins.apply("net.suteren.android-eclipse");

        assertTrue(project.plugins.contains( EclipsePlugin.class));
    }
}
