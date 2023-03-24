[![Apache Sling](https://sling.apache.org/res/logos/sling.png)](https://sling.apache.org)

&#32;[![Build Status](https://ci-builds.apache.org/job/Sling/job/modules/job/sling-org-apache-sling-jcr-base/job/master/badge/icon)](https://ci-builds.apache.org/job/Sling/job/modules/job/sling-org-apache-sling-jcr-base/job/master/)&#32;[![Test Status](https://img.shields.io/jenkins/tests.svg?jobUrl=https://ci-builds.apache.org/job/Sling/job/modules/job/sling-org-apache-sling-jcr-base/job/master/)](https://ci-builds.apache.org/job/Sling/job/modules/job/sling-org-apache-sling-jcr-base/job/master/test/?width=800&height=600)&#32;[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=apache_sling-org-apache-sling-jcr-base&metric=coverage)](https://sonarcloud.io/dashboard?id=apache_sling-org-apache-sling-jcr-base)&#32;[![Sonarcloud Status](https://sonarcloud.io/api/project_badges/measure?project=apache_sling-org-apache-sling-jcr-base&metric=alert_status)](https://sonarcloud.io/dashboard?id=apache_sling-org-apache-sling-jcr-base)&#32;[![JavaDoc](https://www.javadoc.io/badge/org.apache.sling/org.apache.sling.jcr.base.svg)](https://www.javadoc.io/doc/org.apache.sling/org-apache-sling-jcr-base)&#32;[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.apache.sling/org.apache.sling.jcr.base/badge.svg)](https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22org.apache.sling%22%20a%3A%22org.apache.sling.jcr.base%22)&#32;[![jcr](https://sling.apache.org/badges/group-jcr.svg)](https://github.com/apache/sling-aggregator/blob/master/docs/group/jcr.md) [![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0)

# Apache Sling JCR Base Bundle

This module is part of the [Apache Sling](https://sling.apache.org) project.

The JCR base bundle provides JCR utility classes and support for repository mounts.

# Repository Mount

Apache Sling provides support for pluggable resource providers. While this allows for a very flexible and efficient
integration of custom data providers into Sling, this integration is done on Sling's resource API level. Legacy code
which may rely on being able to adapt a resource into a JCR node and continue with JCR API will not work with such
a resource provider.

To support legacy code, this bundle provides an SPI interface *org.apache.sling.jcr.base.spi.RepositoryMount* which
extends *JackrabbitRepository* (and through this *javax.jcr.Repository*). A service registered as *RepositoryMount* registers
itself with the service registration property *RepositoryMount.MOUNT_POINTS_KEY* which is a String+ property containing
the paths in the JCR tree where the mount takes over the control of the JCR nodes. The *RepositoryMount* can registered
at a single path or multiple.

As *RepositoryMount* extends *JackrabbitRepository* the implementation of a mount needs to implement the whole JCR API.
This is a lot of work compared to a *ResourceProvider*, therefore a *RepositoryMount* should only be used if legacy
code using JCR API needs to be supported.
