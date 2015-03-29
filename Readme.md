#onyx-scheduler

Is a simple scheduler built in as a RESTful micro service based on [quartz-scheduler](http://quartz-scheduler.org/).

This work started as a port of [qzui](https://github.com/xhanin/qzui) to [spring-boot](http://projects.spring.io/spring-boot/) and follows some of the same ideas and motivations.

The main motivation is to have a simple (simpler than quartz) service for scheduling jobs in a persistent fashion (we are targeting for not overflowing memory and be resilient to nodes going down), with a rest endpoint which could allow any number of other services which provide callback URLs to use onyx scheduler to schedule invocations to such URLs, and in that way execute scheduled business logic abstracting complexity of providing and managing persistence storage and quartz for each of those services. Additionally I created it to have a playground for some technologies and practices.

Obviously by using onyx instead of quartz you add additional point of failures, network overhead, lose nice integration with frameworks, etc; but it basically has the advantages of a microservice based ecosystem adding a lot more flexibility allowing to 
 scale the scheduler part, isolating it from the rest of the business logic with more control over it, and flexibility to improve/extend its code (for example changing the storage when needed with change of traffic).
  
A point to take into consideration when submitting jobs is that to avoid putting load on onyx the callback URLs should respond fast or work asynchronously: if the request invocation could take seconds consider making the URL just queue the job and then have a worker consuming from the queue. Another option is having dedicated instances of onyx scheduler just for those slow processes, and other instances for the fast running ones.

##Building

###Build Preconditions
- JDK 1.8
- [Maven 3](http://maven.apache.org/) 

This is a Maven project, so just running `mvn package` you would get 
an executable jar in `target` folder.

If you want to run integration tests run `mvn verify`.

##Running

You can just run the application with `java -jar target/onyx-scheduler.jar` which will run onyx with no persistent job store.
If you want to run it with a provided mysql database which already contains quartz tables for persistent job store, then you can fire it with something like `java -jar target/onyx-scheduler.jar --spring.profiles.active=mysql-jobstore --spring.datasource.url=jdbc:mysql://localhost/onyxdb --spring.datasource.user=onyxuser --spring.datasource.password=onyxpass` (in this example the quartz database is `onyxdb` and user is `onyxuser` and password `onyxpass`

###Docker

[Docker](https://www.docker.com/) allows to easily deploy an application to any environment properly isolating the process.

####Docker preconditions
- [Docker installed](https://docs.docker.com/installation/)

A `Dockerfile` is provided so if you want to build it (after running `mvn package`) you can do something like `docker build -t onyx-scheduler .` and then `docker run -p 8080:8080 onyx-scheduler` to run the container. If you use [boot2docker](http://boot2docker.io/) remember to use the docker ip (`boot2docker ip` will display it) instead of localhost from the host machine to access the API.

###docker-compose

Docker compose allows to easily run a set of containers, scale them, etc. In just use it for local environment startup and experimentation.
 
####Docker-compose preconditions
- [docker-compose installed](https://docs.docker.com/compose/install/)

A `docker-compose.yml` file is provided which allows to run `docker-compose up` and have scheduler running with no persistent job storage.
Additionally a `docker-compose-mysql.yml` file is provided which allows through running `docker-compose -f docker-compose-mysql.yml up` to start the scheduler container and a mysql container linking them and using the mysql container for persistent job storage.

##API
The api is almost the same as qzui. Some examples for scheduling jobs:

Sending a `GET` to `http://www.google.com/humans.txt` on `2014-11-05T13:15:30Z`:

```
POST http://localhost:8080/onyx/groups/examples/jobs
Content-Type: application/json
Authorization: Basic YWRtaW46YWRtaW4=

{
  "type": "http",
  "name": "fixedTime",
  "method": "GET",
  "url": "http://httpbin.org/get",
  "triggers": [
    {"when": "2014-11-05T13:15:30Z"}
  ]
}
```

Sending a `POST` with body `test` to same url every 2 seconds:
```
POST http://localhost:8080/onyx/groups/examples/jobs
Content-Type: application/json
Authorization: Basic YWRtaW46YWRtaW4=

{
  "type": "http",
  "name": "cronExpression",
  "method": "POST",
  "headers": {"Content-Type": "application/json"},
  "body": "{\"field\":\"value\"}",
  "url": "http://httpbin.org/post",
  "triggers": [
    {"cron": "0/2 * * * * ?"}
  ]
}
```

##Configuration

You can check `application.yml` and spring-boot documentation. Later on I will add [spring cloud](http://projects.spring.io/spring-cloud/) configuration service.

##Contributing

Please feel free to send pull requests or fork, or send questions and proposals as issues.

##Clustering

When used in a cluster, keep the number of instances low per database due to the limitations of the clustering of quartz with JdbcStore. Use it mainly as a way of achieving high availability and not scalability while we don't implement a more efficient way of clustering (use a different job store). If you need to scale more the you can follow the advise of quartz documentation about sharding loads to different instances of quartz scheduler.
Additionally make sure that servers running the instances have synchronized clocks  (the clocks must be within a second of each other).

###Extending

The main point of extension is creating new `Job` types. For this just extend the `Job` class, and add the type in `Job` class in `JsonSubTypes` annotation to let
[jackson](https://github.com/FasterXML/jackson) know how to distinguish the 
JSON from the new job from the others. If you need some singletons (bean), 
could be for performance issues or just due to the logic required, 
then add them to `OnyxSchedulerApplication` or create a `@Configuration` class. 
