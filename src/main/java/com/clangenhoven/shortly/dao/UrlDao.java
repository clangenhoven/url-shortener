package com.clangenhoven.shortly.dao;

import com.clangenhoven.shortly.model.Url;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.Optional;

public interface UrlDao {

    @SqlUpdate("insert into url (url, short_url, owner_id) values (:url, :shortUrl, :ownerId)")
    void insert(@Bind("url") String url,
                @Bind("shortUrl") String shortUrl,
                @Bind("ownerId") long ownerId);

    @SqlQuery("select * from url where short_url = :shortUrl")
    Optional<Url> getByShortUrl(@Bind("shortUrl") String shortUrl);
}
