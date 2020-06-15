# Pull base image.
FROM ubuntu:latest

RUN \
# Update
apt-get update -y && \
# Install Java
apt-get install default-jre -y

RUN mkdir -p ~/app
WORKDIR ~/app
RUN mkdir -p /img

ADD ./out/artifacts/chainML_jar/*.jar /app/

EXPOSE 50051

CMD java -jar /app/chainML.jar
