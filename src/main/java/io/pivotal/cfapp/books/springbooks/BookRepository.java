package io.pivotal.cfapp.books.springbooks;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

/**
 * Spring Data repository for books.
 * <p>Query derivation is not supported yet.</p>
 */
interface BookRepository extends ReactiveCrudRepository<Book, String> {

}