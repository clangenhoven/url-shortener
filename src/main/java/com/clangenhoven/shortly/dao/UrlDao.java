package com.clangenhoven.shortly.dao;

import com.clangenhoven.shortly.model.Url;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.time.LocalDateTime;
import java.util.Optional;

public interface UrlDao {

    @SqlUpdate("update url set access_count = access_count + 1 where id = :id")
    void incrementUsage(@Bind("id") Long id);

    @SqlUpdate("insert into url (url, short_url, created, access_count, owner_id) " +
            "values (:url, :shortUrl, :created, 0, :ownerId)")
    void insert(@Bind("url") String url,
                @Bind("shortUrl") String shortUrl,
                @Bind("created") LocalDateTime dateTime,
                @Bind("ownerId") long ownerId);

    @SqlQuery("select * from url where short_url = :shortUrl")
    Optional<Url> getByShortUrl(@Bind("shortUrl") String shortUrl);
}
