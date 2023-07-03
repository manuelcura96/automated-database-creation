FROM mysql:latest

COPY pipelines/include/create_developer.sql /scripts/

ENTRYPOINT ["docker-entrypoint.sh"]

EXPOSE 3306 33060

CMD ["mysqld"]
