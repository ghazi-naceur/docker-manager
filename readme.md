
### Prerequisites
- JDK 21
- SBT 1.10.7

### Run locally

1- Run backend:

You can run the main class `Main.scala` under `backend/src/main/scala-3/in/oss/docker/manager`, or you use the sbt command for that:
```shell
cd docker-manager
sbt backend/clean cleanFiles compile
sbt backend/run
```

1- Run frontend:

a- Build:
```shell
cd docker-manager
sbt fastOptJS
```
or 
```shell
cd docker-manager
sbt
project frontend
~fastOptJS
```

b- Run:
```shell
cd docker-manager/frontend
npm install
npm run start
```