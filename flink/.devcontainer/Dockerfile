#-------------------------------------------------------------------------------------------------------------
# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License. See https://go.microsoft.com/fwlink/?linkid=2090316 for license information.
#-------------------------------------------------------------------------------------------------------------

FROM maven:3-jdk-8

# Avoid warnings by switching to noninteractive
ENV DEBIAN_FRONTEND=noninteractive

# This Dockerfile adds a non-root user with sudo access. Use the "remoteUser"
# property in devcontainer.json to use it. On Linux, the container user's GID/UIDs
# will be updated to match your local UID/GID (when using the dockerFile property).
# See https://aka.ms/vscode-remote/containers/non-root-user for details.
ARG USERNAME=vscode
ARG USER_UID=1000
ARG USER_GID=$USER_UID

# Maven settings
ARG MAVEN_VERSION=3.6.3
ARG MAVEN_SHA=c35a1803a6e70a126e80b2b3ae33eed961f83ed74d18fcd16909b2d44d7dada3203f1ffe726c17ef8dcca2dcaa9fca676987befeadc9b9f759967a8cb77181c0
ENV MAVEN_HOME=/usr/share/maven
COPY maven-settings.xml ${MAVEN_HOME}/ref/
ARG FLINK_VERSION=1.9.1

# Configure apt and install packages
RUN apt-get update \
    && apt-get -y install --no-install-recommends apt-utils dialog 2>&1 \
    #
    # Verify git and needed tools are installed
    && apt-get -y install \
    bsdtar \
    git \
    iproute2 \
    procps \
    curl \
    apt-transport-https \
    gnupg2 \
    lsb-release \
    systemd \
    ca-certificates \
    systemd \
    #
    # Create a non-root user to use if preferred - see https://aka.ms/vscode-remote/containers/non-root-user.
    && groupadd --gid $USER_GID $USERNAME \
    && useradd -s /bin/bash --uid ${USER_UID} --gid ${USER_GID} -m $USERNAME \
    # [Optional] Add sudo support for the non-root user
    && apt-get install -y sudo \
    && echo $USERNAME ALL=\(root\) NOPASSWD:ALL > /etc/sudoers.d/$USERNAME\
    && chmod 0440 /etc/sudoers.d/$USERNAME \
    # 
    # Install Maven
    && mkdir -p ${MAVEN_HOME} ${MAVEN_HOME}/ref \
    && curl -fsSL -o /tmp/apache-maven.tar.gz https://archive.apache.org/dist/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz \
    && echo "${MAVEN_SHA} /tmp/apache-maven.tar.gz" | sha512sum -c - \
    && tar -xzf /tmp/apache-maven.tar.gz -C ${MAVEN_HOME} --strip-components=1 \
    && rm -f /tmp/apache-maven.tar.gz \
    && ln -s ${MAVEN_HOME}/bin/mvn /usr/local/bin/mvn \
    #
    # Clean up
    && apt-get autoremove -y \
    && apt-get clean -y \
    && rm -rf /var/lib/apt/lists/*


# COPY ./pom.xml ./pom.xml
# RUN mvn -B -s ${MAVEN_HOME}/ref/maven-settings.xml dependency:go-offline && \
#     mvn -B -s ${MAVEN_HOME}/ref/maven-settings.xml dependency:resolve-plugins && \
#     mvn -B -s ${MAVEN_HOME}/ref/maven-settings.xml package -Dmaven.test.skip=true && \
#     (mvn -B -s ${MAVEN_HOME}/ref/maven-settings.xml --fail-never dependency:resolve || true)

# gives access to the .m2 folder to the 'vscode' user
RUN chown -R $USERNAME:$USERNAME /home/vscode/

ENV TZ=Australia/Sydney
ENV ACTIVE_PROFILE=dev
ENV DEBIAN_FRONTEND=dialog