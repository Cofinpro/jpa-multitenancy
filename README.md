# Multi-Tenancy with JPA

[![Build Status](https://travis-ci.org/Cofinpro/jpa-multitenancy.svg?branch=master)](https://travis-ci.org/Cofinpro/jpa-multitenancy)

This shows how to do multi-tenancy with JPA and Hibernate. You can find my blog post about it here:
 
https://medium.com/@Gregor_70338/multi-tenancy-with-jpa-c47416751c8e


## Build

Use gradle to assemble a wildfly swarm application:
```sh
./gradlew clean wildfly-swarm-package
```

## Run

You can run this on the shell with

```sh
java -jar ./build/libs/jpa-multitenant-swarm.jar`

```

Check out the result of these URLs:

http://localhost:8080/user?tenant=tenant1
http://localhost:8080/user?tenant=tenant2

