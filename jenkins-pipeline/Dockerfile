# Start from official Jenkins LTS image
FROM jenkins/jenkins:lts

# Switch to root to install packages
USER root

# Install dependencies
RUN apt-get update && apt-get install -y \
    curl \
    gnupg \
    lsb-release \
    && rm -rf /var/lib/apt/lists/*

# Download and install Docker CLI (only CLI, no daemon)
RUN curl -fsSL https://download.docker.com/linux/static/stable/x86_64/docker-24.0.6.tgz -o docker.tgz \
    && tar xzvf docker.tgz --strip 1 -C /usr/local/bin docker/docker \
    && rm docker.tgz

# Give Jenkins user access to Docker
RUN usermod -aG root jenkins

# Switch back to Jenkins user
USER jenkins
