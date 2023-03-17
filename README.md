# Provenance Manager application

## Table of Contents

1. [Introduction](#intro)
2. [How to Deploy](#deployment)

## Introduction <a name="intro"></a>

This is a demo application which offers graphical user interface (GUI) that allows authenticated users to embed provenance information to JPEG images. In addition both authenticated and non-authenticated users have the ability to consume provenance information by uploading a digital asset that they wish to inspect.

It uses the latest Exit Tool application (https://www.exiftool.org/).

This application uses a library to extend and handle data in custom JUMBF formats. The library is called MIPAMS FAKE MEDIA, it extends the MIPAMS JPEG Systems library and it can be found here: https://github.com/dmag-upc/mipams-fake-media

## Deploying <a name="deployment"></a>

First, you need to build the server jar package. To do that, navigate to the server/ subdirectory and run 

```
mvn clean package
```

In order to develop/deploy the application you simply need to download the following tools:

  * Docker
  * docker-compose

Follow the instructions for your operation system. 

In the docker subdirectory you may find the docker-compose.yml file that specifies the two services, namely provenance-server and provenance-client. 

To launch the application you need to execute the following command in the docker subdirectory:

```
docker-compose up
```

This will download the necessary docker images, build the images for demo-server and demo-client and launch the instances. Once all the services are up and running you can access the GUI application by opening a browser and visiting the following URL:

```
http://localhost:3001
```