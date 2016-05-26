#!/bin/bash

FILES_DIR=/semiot-platform

VENDOR_DOCKER_IMAGES=(
    "fuseki"
    "mysql"
    "hbase"
    "opentsdb"
)
CUSTOM_DOCKER_IMAGES=(
    "api-gateway"
    "wamp-router"
    "device-proxy-service"
    "data-archiving-service"
    "frontend"
)
MAVEN_PROJECTS=(
    "commons-namespaces"
    "commons-rdf"
    "commons-restapi"
    "device-proxy-service"
    "data-archiving-service"
    "api-gateway"
)

function mvn_build() {
    echo "building maven project $1"

    pushd $1
        mvn clean install -DskipTests=true
    popd
}

function docker_build() {
    echo "building docker image $1 as $2"

    sudo docker build -t docker.semiot.ru/$1 $2
}

function stop_and_clean() {
    echo "stopping and cleaning target directory $FILES_DIR"

    sudo docker-compose kill && \
        sudo docker-compose rm -f && \
        sudo rm -rf $FILES_DIR/fuseki/ $FILES_DIR/felix-cache/ $FILES_DIR/hbase/ $FILES_DIR/tsdb/

    exit $?
}

function start_and_logs() {
    echo "starting docker-compose with logs"
    sudo docker-compose up -d && \
        sudo docker-compose logs

    exit $?
}

function restart_image() {
    echo "restarting image $1"
    sudo docker-compose restart $1
}

function build_webui() {
    echo "buildung webui files"

    pushd api-gateway/src/main/websrc
        npm run build
    popd
}

function mvn_build_all() {
    echo "building all maven projects"

    build_webui

    for prj in "${MAVEN_PROJECTS[@]}"
    do
        mvn_build $prj
    done
}

function docker_build_all() {
    echo "building all docker images"

    echo "building vendor images.."
    for image in "${VENDOR_DOCKER_IMAGES[@]}"
    do
        echo "building $image"
        docker_build $image docker/$image
    done

    echo "building custom images.."
    for image in "${CUSTOM_DOCKER_IMAGES[@]}"
    do
        echo "building $image"
        docker_build $image $image
    done
}


case "$1" in

    "stop-and-clean")
        stop_and_clean
        ;;

    "start-and-logs")
        start_and_logs
        ;;

    "build-mvn")
        if [ -z "$2" ];
            then
                mvn_build_all
            else
                mvn_build $2
        fi
        ;;

    "build-docker")
        if [ -z "$2" ];
            then
                docker_build_all
            else
                docker_build $2 $2
        fi
        ;;

    "build-all")
        mvn_build_all
        docker_build_all
        ;;

    "update-singe-image")
        if [ -z "$2" ];
            then
                echo "update-single-image requires a second argument; do nothing"
                exit 1
            else
                mvn_build $2
                docker_build $2 $2
                echo "done; now you should execute 'docker-compose restart IMAGE_NAME'" # echo maybe something else; did not test properly
        fi
        ;;

    "clean-deploy")
        stop_and_clean
        mvn_build_all
        docker_build_all
        start_and_logs
        ;;

    *)
        echo $1
        echo $"Usage: $0 {stop-and-clean|start-and-logs|build-mvn (NAME)|build-docker (NAME)|build-all|update-single-image NAME|clean-deploy}"
        exit 1

esac