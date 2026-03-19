FROM fluent/fluentd-kubernetes-daemonset:v1.4.2-debian-elasticsearch-1.1

# La imagen base ya tiene el plugin de Elasticsearch preinstalado.
USER root

RUN gem install fluent-plugin-detect-exceptions -v 0.0.12 --no-document && \
    gem sources --clear-all && \
    rm -rf /var/lib/apt/lists/* \
           /home/fluent/.gem/ruby/2.3.0/cache/*.gem

RUN mkdir -p /tmp/fluentd-buffers && chown -R fluent:fluent /tmp/fluentd-buffers

USER fluent

