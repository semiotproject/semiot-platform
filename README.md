# SemIoT Platform

## How to deploy

First of all, you need to install Docker and Docker Compose, if didn't do it yet. Read [instructions](https://docs.docker.com/compose/install/).

Then you're ready to pull the images from Docker Hub (it takes several minutes):
```
sudo docker-compose pull
```
and now you can start the containers:
```
sudo docker-compose up -d
```
If you want to see the logs of the containers then run the `up` command without `-d` flag.
