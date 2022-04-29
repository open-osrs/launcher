/*
version = '3.0.0'
*/
import org.apache.tools.ant.filters.ReplaceTokens

buildscript {
    repositories {
        gradlePluginPortal()
    }
    dependencies {
        classpath("com.github.ben-manes:gradle-versions-plugin:0.28.0")
    }
}

plugins {
    id("com.github.johnrengelman.shadow") version "6.0.0"

    java
    checkstyle
}

group = "com.openosrs"
version = "3.0.0"
description = "OpenOSRS Launcher"

repositories {
    mavenLocal()
    maven {
        url = uri("https://repo1.maven.org/maven2")
    }

    maven {
        url = uri("https://raw.githubusercontent.com/open-osrs/maven-repo/master")
    }
}

dependencies {
    annotationProcessor(group = "org.projectlombok", name = "lombok", version = "1.18.20")

    compileOnly(group = "javax.annotation", name = "javax.annotation-api", version = "1.3.2")
    compileOnly(group = "org.projectlombok", name = "lombok", version = "1.18.20")

    implementation(group = "org.slf4j", name = "slf4j-api", version = "1.7.25")
    implementation(group = "ch.qos.logback", name = "logback-classic", version = "1.2.9")
    implementation(group = "net.sf.jopt-simple", name = "jopt-simple", version = "5.0.1")
    implementation(group = "com.google.code.gson", name = "gson", version = "2.8.5")
    implementation(group = "com.google.code.findbugs", name = "jsr305", version = "3.0.2")
    implementation(group = "com.google.guava", name = "guava", version = "23.2-jre") {
        exclude(group = "com.google.code.findbugs", module = "jsr305")
        exclude(group = "com.google.errorprone", module = "error_prone_annotations")
        exclude(group = "com.google.j2objc", module = "j2objc-annotations")
        exclude(group = "org.codehaus.mojo", module = "animal-sniffer-annotations")
    }
    implementation(group = "com.vdurmont", name = "semver4j", version = "3.1.0")

    testImplementation(group = "junit", name = "junit", version = "4.12")
}

configure<CheckstyleExtension> {
    maxWarnings = 0
    toolVersion = "8.25"
    isShowViolations = true
    isIgnoreFailures = false
}

tasks {
    java {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    withType<JavaCompile> {
        options.encoding = "UTF-8"
    }

    build {
        finalizedBy("shadowJar")
    }

    processResources {
        val tokens = mapOf(
                "basedir"         to project.projectDir.path,
                "finalName"       to "OpenOSRS",
                "artifact"        to "launcher",
                "project.version" to project.version,
                "project.group"   to project.group,
                "description"     to "OpenOSRS launcher"
        )

        doLast {
            copy {
                from("${rootDir}/packr") {
                    include("Info.plist")
                }
                from("${rootDir}/innosetup") {
                    include("openosrs.iss")
                    include("openosrs32.iss")
                }
                from("${rootDir}/appimage") {
                    include("openosrs.desktop")
                }
                into("${buildDir}/filtered-resources/")

                filter(ReplaceTokens::class, "tokens" to tokens)
                filteringCharset = "UTF-8"
            }

            copy {
                from("src/main/resources") {
                    include("launcher.properties")
                }
                into("${buildDir}/resources/main/net/runelite/launcher")

                filter(ReplaceTokens::class, "tokens" to tokens)
                filteringCharset = "UTF-8"
            }
        }
    }

    jar {
        manifest {
            attributes(mutableMapOf("Main-Class" to "net.runelite.launcher.Launcher"))
        }
    }

    shadowJar {
        archiveName = "OpenOSRS-shaded.jar"
        exclude("net/runelite/injector/**")
    }
}
