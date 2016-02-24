package net.suteren.gradle.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.plugins.ide.eclipse.EclipsePlugin
import org.gradle.plugins.ide.idea.IdeaPlugin

class AndroidEclipsePlugin implements Plugin<Project> {
    void apply(Project project) {
        project.plugins.apply("android");
        project.plugins.apply(EclipsePlugin);
        project.plugins.apply(IdeaPlugin);

        project.configurations { provided }

        def libDir = new File(project.projectDir, 'libs')
        project.eclipse.classpath.plusConfigurations += configurations.compile
        project.eclipse.classpath.noExportConfigurations += configurations.compile
        project.eclipse.classpath.containers(
                "com.android.ide.eclipse.adt.ANDROID_FRAMEWORK",
                "com.android.ide.eclipse.adt.DEPENDENCIES",
                "com.android.ide.eclipse.adt.LIBRARIES"
        );
        project.eclipse.classpath.file {
            beforeMerged { classpath ->
                classpath.entries.removeAll() { c ->
                    c.kind == 'con' && c.path == 'com.android.ide.eclipse.adt.ANDROID_FRAMEWORK'
                }
                try {
                    delete libDir
                    libDir.mkdirs()
                } catch (e) {
                    logger.warn(e.message, e)
                }
            };
            whenMerged { classpath ->

                classpath.entries -= classpath.entries.findAll() { c ->
                    def path = c.path
                    path != null && (path.contains('/com.android.support/support-v4'))
                }

                classpath.entries.each() { c ->
                    if (c.kind == "src" && c.path.startsWith("/")) c.exported = true
                    if (c.kind == "con" && c.path == "com.android.ide.eclipse.adt.ANDROID_FRAMEWORK") c.exported = false

                    def path = c.path
                    if (c.kind == "lib"
                            && !(
                            path.contains("appcompat-v7")
                                    || path.contains("support-v4")
                                    || path.contains("slf4j-api")
                    )
                    ) {
                        // ||path.contains("stax")||path.contains("xpp3")
                        copy {
                            from path
                            into libDir
                        }
                    }
                }
            }
        };
        project.eclipse.project.natures += 'com.android.ide.eclipse.adt.AndroidNature';
        project.eclipse.project.natures += 'org.springsource.ide.eclipse.gradle.core.nature';
        project.eclipse.project.natures += 'org.eclipse.jdt.core.javanature';
        project.eclipse.project.natures += 'org.eclipse.jdt.groovy.core.groovyNature';
        project.eclipse.project.buildCommand += 'com.android.ide.eclipse.adt.ResourceManagerBuilder';
        project.eclipse.project.buildCommand += 'com.android.ide.eclipse.adt.PreCompilerBuilder';
        project.eclipse.project.buildCommand += 'org.eclipse.jdt.core.javabuilder';
        project.eclipse.project.buildCommand += 'com.android.ide.eclipse.adt.ApkBuilder';


        project.sourceSets.main.java.srcDir = 'gen';

        project.android.defaultConfig.versionName = project.version;
        project.android.sourceSets.main {
            manifest.srcFile 'AndroidManifest.xml'
            java.srcDirs = ['src/main/java']
            resources.srcDirs = ['src/main/resources']
            res.srcDirs = ['res']
            assets.srcDirs = ['assets']
        }
        project.android.sourceSets.test.setRoot('tests')
        project.android.sourceSets.test {
            java.srcDirs = ['tests/src']
            res.srcDirs = ['tests/res']
            assets.srcDirs = ['tests/assets']
            resources.srcDirs = ['tests/src']
        }

        project.android.sourceSets.instrumentTest.setRoot('tests')
        project.android.sourceSets.instrumentTest {
            java.srcDirs = ['tests/src']
            res.srcDirs = ['tests/res']
            assets.srcDirs = ['tests/assets']
            resources.srcDirs = ['tests/src']
        }
        project.android.sourceSets.project.android.buildTypes.release {
            runProguard false
            //proguardFile getDefaultProguardFile('proguard-android.txt')
            proguardFile 'proguard-project.txt'
            signingConfig signingConfigs.release
        }
        project.android.packagingOptions { exclude 'META-INF/LICENSE.txt' }
        project.android.lintOptions { abortOnError false }


    }

}

