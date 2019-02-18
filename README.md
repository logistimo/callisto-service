Callisto API
============

[![License: AGPL v3](https://img.shields.io/badge/License-AGPL%20v3-blue.svg)](http://www.gnu.org/licenses/agpl-3.0)

Callisto is a data visualization and exploration tool. This service provides a REST interface to register queries, and databases.
It contains a powerful query engine that allows  

* to specify filters on the registered queries dynamically
* to mix queries of multiple datasources
* to apply custom functions on the response
* pagination of the data

Please visit [www.logistimo.com](www.logistimo.com) for further details 

Latest Release
------------------

The most recent release is Logistimo [0.2](https://github.com/logistimo/callisto-service/tree/v0.2.0), released on 2017 May 13th.

How to use Logistimo
-------------------------

[Knowledge-base](https://logistimo.freshdesk.com)

Tech Stack
------------------

* JDK 1.8
* MongoDB 
* Spring boot
* Cassandra
* Maria DB

Modules
-------
1. Query engine
2. Plugins core
3. MariaDB plugin
4. Cassandra plugin
5. REST API

Build Instructions
------------------

To build the artifact and create a docker image of the callisto service, run the following commands.

1. Set environment

```
export MAVEN_OPTS=-Xmx718m
export MAVEN_HOME=/opt/apache-maven-3.5.3/
export JAVA_HOME=/opt/java-home
export PATH=$JAVA_HOME/bin:$PATH:$MAVEN_HOME/bin
```

2. Build the artifact

```
mvn clean compile package assembly:single -U
````

3. Build the docker image

```
docker build --rm=true --build-arg APP_NAME=callisto-application --build-arg APP_VERSION=<APP VERSION> -t callisto-service:latest .
```

Mailing Lists
-------------

For broad, opinion based, ask for external resources, debug issues, bugs, contributing to the project, and scenarios, it is recommended you use the community@logistimo.com mailing list.

community@logistimo.com  is for usage questions, help, and announcements.
[subscribe](https://groups.google.com/a/logistimo.com/d/forum/community/join) [unsubscribe](mailto:unsubscribe+community@logistimo.com)

developers@logistimo.com  is for people who want to contribute code to Logistimo.
[subscribe](https://groups.google.com/a/logistimo.com/d/forum/developers/join) [unsubscribe](mailto:unsubscribe+community@logistimo.com)

License Terms
---------------------------

This program is part of Logistimo SCM. Copyright © 2017 Logistimo.

Logistimo software is a mobile & web platform for supply chain management and remote temperature monitoring in low-resource settings, made available under the terms of the GNU Affero General Public License (AGPL). 

This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.

You can be released from the requirements of the license by purchasing a commercial license. To know more about the commercial license, please contact us at opensource@logistimo.com

Trademarks
----------

Logistimo, Logistimo.com, and the Logistimo logo are trademarks and/or service marks. Users may not make use of the trademarks or service marks without prior written permission. Other trademarks or trade names displayed on this website are the property of their respective trademark owners and subject to the respective owners’ terms of use.
