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
        eclipse.project {

            natures 'com.android.ide.eclipse.adt.AndroidNature',
                    'org.springsource.ide.eclipse.gradle.core.nature',
                    'org.eclipse.jdt.core.javanature',
                    'org.eclipse.jdt.groovy.core.groovyNature'

            buildCommand 'com.android.ide.eclipse.adt.ResourceManagerBuilder'
            buildCommand 'com.android.ide.eclipse.adt.PreCompilerBuilder'
            buildCommand 'org.eclipse.jdt.core.javabuilder'
            buildCommand 'com.android.ide.eclipse.adt.ApkBuilder'

        };
        project.sourceSets {
            main { java { srcDir 'gen' } }
        };

        project.android {

            defaultConfig {
                versionName = project.version
            }

            sourceSets {
                main {
                    manifest.srcFile 'AndroidManifest.xml'
                    java.srcDirs = ['src/main/java']
                    resources.srcDirs = ['src/main/resources']
                    res.srcDirs = ['res']
                    assets.srcDirs = ['assets']
                }

                test.setRoot('tests')
                test {
                    java.srcDirs = ['tests/src']
                    res.srcDirs = ['tests/res']
                    assets.srcDirs = ['tests/assets']
                    resources.srcDirs = ['tests/src']
                }

                instrumentTest.setRoot('tests')
                instrumentTest {
                    java.srcDirs = ['tests/src']
                    res.srcDirs = ['tests/res']
                    assets.srcDirs = ['tests/assets']
                    resources.srcDirs = ['tests/src']
                }
            }

            buildTypes {
                release {
                    runProguard false
                    //proguardFile getDefaultProguardFile('proguard-android.txt')
                    proguardFile 'proguard-project.txt'
                    signingConfig signingConfigs.release
                }
            }

            packagingOptions { exclude 'META-INF/LICENSE.txt' }

            lintOptions { abortOnError false }

        }
    }

}

