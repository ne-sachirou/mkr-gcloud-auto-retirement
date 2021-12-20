FROM clojure:openjdk-18

WORKDIR /mnt

RUN apt-get update \
 && apt-get install -y --no-install-recommends \
    curl \
 && curl -fsLO https://github.com/mackerelio/mkr/releases/download/v0.46.0/mkr_0.46.0-1_all.deb \
 && dpkg -i mkr_*_all.deb \
 && apt-get purge -y \
    curl \
 && rm -v mkr_*_all.deb \
 && apt-get autoremove -y \
 && apt-get clean \
 && rm -rf /var/lib/apt/lists/*

COPY deps.edn ./
RUN clojure -M

COPY entrypoint.sh \
     main.clj \
     ./
COPY src/ src/
ENTRYPOINT ["./entrypoint.sh"]
