# sbom-service
A component of SBOMer NextGen that picks the generators and enhancers for the generation requests it receives, and orchestrates these SBOM generations and enhancements.

## Getting Started (Development) (WIP)

We can run the component locally through podman-compose, which will run the component with an ephemeral Kafka and Apicurio instance:

```shell script
bash ./hack/run-compose.sh
```

TODO: As the new components are implemented, they can be stood up in this podman-compose file using latest tagged images from Quay. This is so local deployment of the wider system can be possible for local development and testing purposes.

We can then invoke a generation manually with the request below:

```shell script
curl -i -X POST -H "Content-Type: application/json" -d '{"advisoryId": "1234"}' http://localhost:8080/v1/errata-tool/generate
```