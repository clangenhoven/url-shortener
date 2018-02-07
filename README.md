# Prerequisites

* Vagrant
* VirtualBox

# Instructions

* Clone the repo
* In the repo directory, run `vagrant up`
    * Wait for the VM to become ready...
* Once ready, connect to the VM with `vagrant ssh`, then `cd /vagrant`
* Run `./gradlew update` to run liquibase database migrations
* Run `./gradlew run` to run the application

# Interaction

* Before creating a short url, first create a user. Use `curl` to post a JSON object to the `/createUser` endpoint:

  `curl -H 'Content-Type: application/json' -X POST -d '{"username": "username", "password": "password"}' http://localhost:8080/createUser -v`

* To create a shortened URL, again use `curl` to post a JSON object to the `/createUrl` endpoint, this time include the HTTP basic credentials for the created user:

  `curl -u username:password -H 'Content-Type: application/json' -X POST -d '{"url": "https://www.example.com", "shortUrl": "example"}' http://localhost:8080/createUrl -v`

  You can also leave the `shortUrl` property out, in which case a random short url will be generated and returned.

* Now navigate to `http://localhost:8080/u/example`. You should be redirected to the url you specified in the previous step, e.g. `https://www.example.com`.

* You can see info in JSON format about the url by navigating to `http://localhost:8080/i/example` and providing your username and password, provided that the url was created by your user.

* You can see info about all shortened urls your user has created by navigating to `http://localhost:8080/i`.

# Implementation details

The server is written in Java 8 using the Ratpack asynchronous web application framework. The build and dependency management tool is Gradle. Postgres is used as the relational database, and Redis is used as a cache for the database. Liquibase is used for database migrations. Other libraries used include Guice for dependency injection, Jdbi and c3p0 for database connectivity and thread pooling, the lettuce Redis client and the Pac4J security library.

# Scale

The application is designed to allow for easy scaling, both vertically and horizontally:

* It uses an asynchronous web application framework, Ratpack, that allows better utilization of the host hardware compared to a traditional synchronous framework. A synchronous web app is limited in how many requests it can process simultaneously by how many threads are available in its thread pool, whereas an asynchronous one needs far fewer threads as threads never sleep, instead they are constantly being scheduled to run non-blocking tasks. Fewer threads means less overhead, and higher throughput.
* The application itself is stateless, which means as many instances as necessary can safely be run side by side without running into race conditions or the like. Naturally, when running multiple instances, a load balancer should be used in order to distribute the load across the available instances.
* The data store used is PostgreSQL. While a RDBMS like Postgres can be a single point of failure since it is not distributed, it can also quite easily be configured with a failover host to provide high availability, and with read replicas to distribute the load away from the master.
* To further reduce load on the database, a Redis instance is used as a cache. This instance can be expanded into a cluster of Redis masters, which can be made highly available by setting up replicas of each master node.

# Known issues

* The authentication framework being used, Pac4J, doesn't allow disabling sessions and session cookies entirely, so a session cookie is being sent back by the application even though nothing is being stored in the server side session and authentication is per request using HTTP basic.
* The postgres user credentials are duplicated in the config, provisioning files, and liquibase activity in the gradle file. This should be centralized in one place.
