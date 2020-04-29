package io.pivotal.cfapp.books.springbooks;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.r2dbc.core.DatabaseClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Provides HTTP endpoints for manipulating the BOOK table.
 * <ul>
 *   <li>{@code /list} Returns all books in the table (GET).</li>
 *   <li>{@code /add} Adds a new book with a given title and a generated UUID as {@code id} (POST).</li>
 *   <li>{@code /search/\{id\}} Finds a single book by its ID.</li>
 * </ul>
 */
@RestController
public class BookController {

  private DatabaseClient r2dbcClient;
  private BookRepository r2dbcRepository;

  @Autowired
  public BookController(DatabaseClient r2dbcClient, BookRepository r2dbcRepository) {
    this.r2dbcClient = r2dbcClient;
    this.r2dbcRepository = r2dbcRepository;
  }


  @GetMapping("/list")
  public Flux<Book> listBooks() {
    return r2dbcClient.execute("SELECT id, title FROM BOOK")
        .as(Book.class)
        .fetch().all();
  }

  @PostMapping("/add")
  public Mono<Void> addBook(@RequestBody String bookTitle) {
    return r2dbcClient.insert()
        .into("book")
        .value("id", UUID.randomUUID().toString())
        .value("title", bookTitle)
        .then();
  }


  @GetMapping("/search/{id}")
  public Mono<Book> searchBooks(@PathVariable String id) {
    return r2dbcRepository.findById(id);
  }

}