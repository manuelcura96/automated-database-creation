FROM postgres:latest

COPY pipelines/include/postgresql/create_developer.sql /scripts/

ENTRYPOINT ["docker-entrypoint.sh"]

EXPOSE 5432

CMD ["postgres"]
