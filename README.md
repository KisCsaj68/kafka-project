## Kafka project to learn Kafka basics

### Techstack:
- Java / Springboot
- PSQL
- Kafka
- Docker

### To run the application:
- You need to execute `make run` at the project root.

### Architecture:
Based on a microservices.<br>
Two main stateless service:
- `api` - provides the REST interface for  the end users
- `agent` - provides the command executor service

There is also the `common` maven project which defines the common DTOs, Enums (for the statuses).

Each service run in their own docker container.
The docker images are built with a multi-stage Dockerfile, to ensure a small image size. It also utilizes a build cache volume (mounted to `/root/.m2` folder) at docker build time to avoid the dependency downloading on every rebuild. This results in a very optimal build time.
All microservices (api, agent, Kafka, PSQL) run in a docker based environment. On my machine I am using Orbstack tas a container runtime environment.<br>
The main components (`api`, `agent`) communicate with each other via Kafka topics to be asynchronous. The data models(DB entity) are saved/updated into a postgre relational database.<br>
Over the topics only the essential data travels, I use DataTransferObjects aka DTO instead on DB Entity. I configured 3 topics to ensure low latency for the updates. <br>

__Topics__:
- api --> agent `KafkaTaskRequest` - Sends the execution recipie to the agent for further process.
- agent --> api `KafkaTaskStatus` - Sends the updated status when status changed.
- agent --> api `KafkaTaskResult` - Sends the execution result(stderr, stdout, exitcode).

The database contains 3 tables. One for the tasks, status and result.

#### Api:
Built as an MVC pattern (without the view). I separated the api endpoints from the business logic.<br>
There is a controller `TaskController` which provides the REST api. I created separate DTOs for the REST api (`TaskRequest`, `TaskResponse`) to ensure only the public data leave the application (-> eg. changes to the db schema does not affect the schema for the rest endpoint). This service is exposed on port 3500 and have the predefined endpoints:
- GET /tasks/{task_id}
- GET /tasks
- POST /tasks

There is also the `TaskService` which holds the business logic, communicates with the agent and communicates with the DB.

#### Agent:
Simple solution to execute command. It send teh status changes, execution result via queues.

