FROM postgres:latest

COPY pipelines/include/create_developer.template /scripts/

ENTRYPOINT ["docker-entrypoint.sh"]

EXPOSE 5432

CMD ["postgres"]
