package io.pivotal.cfapp.books.springbooks;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

/**
 * Book entity.
 */
@Table
public class Book {

  @Id
  private String id;

  private String title;

  public Book(String id, String title) {
    this.id = id;
    this.title = title;
  }

  public String getId() {
    return id;
  }

  public String getTitle() {
    return this.title;
  }
}